package org.sunbird.cassandra.triggers

import java.util

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.cassandra.db.{Clustering, Mutation}
import org.apache.cassandra.db.marshal.{CompositeType, ListType, MapType}
import org.apache.cassandra.db.partitions.Partition
import org.apache.cassandra.db.rows.{Cell, CellPath, Unfiltered}
import org.apache.cassandra.triggers.ITrigger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


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
            logger.info(mapper.writeValueAsString(Map("operationType" -> "UPSERT", "partitionKeys" -> partitionData, "objectType" -> objectType, "metadata" -> processEvent(partition,partitionData))))
        }

        return null;
    }

    protected def processEvent(partition: Partition, partitionKeyData: Map[String, Any]): mutable.Map[String, Any] = {
        try {
            val unfilteredIterator = partition.unfilteredIterator()
            val event = mutable.Map[String, Any]()
            while (unfilteredIterator.hasNext) {
                val next = unfilteredIterator.next()
                val clusterKeyData: Map[String, Any] = getClusterKeyData(partition, next)
                val row = partition.getRow(next.clustering().asInstanceOf[Clustering])
                val cells = row.cells().iterator()
                var eventMap: Map[String, Any] = Map()
                while (cells.hasNext) {
                    val cell = cells.next()
                    val columnType = getColumnType(cell)
                    if (columnType.isInstanceOf[MapType[Any, Any]])
                        eventMap += (getColumnName(cell) -> processMapDataType(eventMap, cell))
                    else if (columnType.isInstanceOf[ListType[String]])
                        eventMap += (getColumnName(cell) -> processListDataType(cell, eventMap))
                    else
                        eventMap += (getColumnName(cell) -> Map("nv" -> processDefaultDataType(cell)))
                    logger.info(mapper.writeValueAsString(eventMap))
                }
                eventMap += (OPERATION_TYPE -> UPDATE_ROW)
                eventMap += (OBJECT_TYPE -> partition.metadata.cfName)
                event.putAll(eventMap)
                event.putAll(clusterKeyData)
            }
            event.putAll(partitionKeyData)
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

    private def processMapDataType(eventMap: Map[String, Any], cell: Cell): Map[String, Any] = {
        var returnMap = Map[String, Any]()
        val columnName = getColumnName(cell)
        val columnType = getColumnType(cell)
        val cellValue = getCellValue(cell)
        val keysType = columnType.asInstanceOf[MapType[AnyRef, AnyRef]].getKeysType
        val path: CellPath = cell.path()
        for (i <- 0 until path.size()) {
            val byteBuffer = path.get(i)
            val cellKey = keysType.compose(byteBuffer)
            if (eventMap.containsKey(columnName)) {
                returnMap = eventMap(columnName).asInstanceOf[Map[String, Any]]
                returnMap += (cellKey.toString -> cellValue)
            }
            returnMap += (cellKey.toString -> cellValue)
        }
        returnMap
    }

    private def processListDataType(cell: Cell, eventMap: Map[String, Any]): List[AnyRef] = {
        val columnName = getColumnName(cell)
        val returnList = ListBuffer[AnyRef]()
        if (cell.isLive(0))
            if (eventMap.containsKey(columnName)) {
                returnList.addAll(eventMap(columnName).asInstanceOf[List[AnyRef]])
                returnList.add(getCellValue(cell))
            } else {
                returnList.add(getCellValue(cell))
            }
        returnList.toList
    }

    private def processDefaultDataType(cell: Cell): String = {
        if (cell.isLive(0))
            getCellValue(cell).toString
        else null
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
