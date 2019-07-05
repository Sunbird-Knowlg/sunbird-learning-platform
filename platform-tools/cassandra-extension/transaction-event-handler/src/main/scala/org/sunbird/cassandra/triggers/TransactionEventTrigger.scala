package org.sunbird.cassandra.triggers

import java.util

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.cassandra.db.{Clustering, Mutation}
import org.apache.cassandra.db.marshal.{CompositeType, MapType}
import org.apache.cassandra.db.partitions.Partition
import org.apache.cassandra.db.rows.{Cell, CellPath, Unfiltered}
import org.apache.cassandra.triggers.ITrigger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable


class TransactionEventTrigger extends ITrigger {
    private val OBJECT_TYPE = "objectType"
    private val OPERATION_TYPE = "operationType"
    private val UPDATE_ROW = "UPSERT"
    private val DELETE_ROW = "DELETE"

    val logger = LoggerFactory.getLogger("org.sunbird.cassandra.triggers")
    @transient val mapper = new ObjectMapper();
    mapper.registerModule(DefaultScalaModule);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.setSerializationInclusion(Include.NON_NULL);


    override def augment(partition: Partition): util.Collection[Mutation] = {
        val partitionData = getPartitionKeyData(partition)
        val levelDeletion = partition.partitionLevelDeletion
        val objectType = partition.metadata().ksName + "." + partition.metadata().cfName
        if (!levelDeletion.isLive) {
            logger.info(mapper.writeValueAsString(Map("operationType" -> "DELETE" ,"partitionKeys" -> partitionData, "objectType" -> objectType)))
        } else {
            logger.info(mapper.writeValueAsString(processEvent(partition,partitionData)))
            logger.info(mapper.writeValueAsString(Map("operationType" -> "UPSERT", "partitionKeys" -> partitionData, "objectType" -> objectType)))
        }

        return null;
    }

    protected def processEvent(partition: Partition, partitionKeyData: Map[String, Any]): Map[String,Any] = {
        try {
            val unfilteredIterator = partition.unfilteredIterator()
            var event = Map[String,Any]()
            while (unfilteredIterator.hasNext) {
                val next = unfilteredIterator.next()
                val clusterKeyData: Map[String, Any] = getClusterKeyData(partition, next)
                val clustering = next.clustering()
                val row = partition.getRow(clustering.asInstanceOf[Clustering])
                val cells = row.cells()
                var eventMap: Map[String,Any] = Map()
                val updatedDataMap = Map[String, AnyRef]()
                while (cells.iterator().hasNext) {
                    val cell = cells.iterator().next()
                    val columnType = getColumnType(cell)
//                    if (columnType.isInstanceOf[Map[Any, Any]])
//                        processMapDataType(eventMap, updatedDataMap, cell)
//                    else if (columnType.isInstanceOf[List[Any]])
//                        processListDataType(eventMap, updatedDataMap, cell)
//                    else
                    eventMap = processDefaultDataType(cell)
                    logger.info(mapper.writeValueAsString(eventMap))
                }
                eventMap += (OPERATION_TYPE -> UPDATE_ROW)
                eventMap += (OBJECT_TYPE -> partition.metadata.cfName)
//                eventMap.putAll(partitionKeyData)
//                eventMap.putAll(clusterKeyData)
                event = eventMap
            }
            logger.info(mapper.writeValueAsString(event))
            event
        } catch {
            case e: Exception => {
                e.printStackTrace
                logger.error("This is an error", e)
                logger.error(e.printStackTrace.toString)
            }
                null
        }
    }

    private def processMapDataType(dataMap: Map[String, Any], updateColumnCollectionInfo: Map[String, AnyRef], cell: Cell): Unit = {
        val columnName = getColumnName(cell)
        val columnType = getColumnType(cell)
        val cellValue = getCellValue(cell)
        val mapType = columnType.asInstanceOf[MapType[AnyRef, AnyRef]]
        val keysType = mapType.getKeysType
        val path: CellPath = cell.path()
        val size: Int = path.size()
        for (i <- 0 to size) {
            val byteBuffer = path.get(i)
            val cellKey = keysType.compose(byteBuffer)
            var map: Map[String,AnyRef] = Map()
            if (!dataMap.containsKey(columnName))
                dataMap.put(columnName, map)
            else
                map = dataMap(columnName).asInstanceOf[Map[String, AnyRef]]
            if (cell.isLive(0)) map.put(cellKey.toString, cellValue)
            else {
                if (!updateColumnCollectionInfo.containsKey(columnName)) updateColumnCollectionInfo.put(columnName, columnType.getClass.getName)
                map += (cellKey.toString -> null)
            }
        }
    }


    private def processListDataType(dataMap: Map[String,Any], updateColumnCollectionInfo: Map[String, AnyRef], cell: Cell): Unit = {
        val columnName = getColumnName(cell)
        val cellValue = getCellValue(cell)
        val columnType = getColumnType(cell)
        updateColumnCollectionInfo.put(columnName, columnType.getClass.getName)
        if (cell.isLive(0)) if (!dataMap.containsKey(columnName)) {
            val arrayList = List[AnyRef]()
            arrayList.add(cellValue)
            dataMap.put(columnName, arrayList)
        } else {
            val arrayList =  dataMap(columnName).asInstanceOf[List[AnyRef]]
            if (!arrayList.contains(cellValue))
                arrayList.add(cellValue)
        }
    }

    private def processDefaultDataType(cell: Cell): Map[String, Any] = {
        val columnName = getColumnName(cell)
        if (cell.isLive(0))
            Map(columnName -> getCellValue(cell))
        else Map(columnName -> null)
    }

    protected def getPartitionKeyData(partition: Partition): Map[String, Any] = {
        val partitionColumns = partition.metadata().partitionKeyColumns;
        val keyBuffer = partition.partitionKey.getKey;

        if (partitionColumns.size() == 1) {
            val value = partitionColumns.get(0).`type`.compose(keyBuffer)
            return Map(partitionColumns.get(0).name.toString -> value)
        } else {
            partitionColumns.zipWithIndex.map { case (c, i) => {
                val colName = c.name.toString;
                val colType = c.`type`;
                val valueBuffer = CompositeType.extractComponent(keyBuffer, i)
                val value = colType.compose(valueBuffer)
                Map(colName -> value)
            }}.flatten.toMap
        }
    }
//
//    protected def getClusterKeyData(partition: Partition, next: Unfiltered): Map[String, Any] = {
//        val clusterKeyList = partition.metadata().clusteringColumns()
//        val clustering = next.clustering()
//        (for (i <- 0 to clustering.size()) yield {
//            val columnName = clusterKeyList.get(i).name.toString
//            val value = clusterKeyList.get(i).`type`.compose(clustering.get(i))
//            Map(columnName -> value)
//        }).flatten.toMap
//    }

    protected def getClusterKeyData(partition: Partition, next: Unfiltered): Map[String, Any] = {
        val clusterKeyList = partition.metadata().clusteringColumns()
        val clustering = next.clustering()
        var keyDataMap = Map[String, Any]()
        if (clustering.size() != 0)
            for (i <- 0 to clustering.size()) {
                val columnName = clusterKeyList.get(i).name.toString
                val value = clusterKeyList.get(i).`type`.compose(clustering.get(i))
                keyDataMap += (columnName -> value)
            }
        keyDataMap
    }

    import org.apache.cassandra.db.marshal.AbstractType

    private def getColumnName(cell: Cell): String = cell.column().name.toString

    private def getCellValue(cell: Cell): AnyRef = cell.column.cellValueType.asInstanceOf[AbstractType[AnyRef]].compose(cell.value)

    private def getColumnType(cell: Cell) = cell.column.`type`
}
