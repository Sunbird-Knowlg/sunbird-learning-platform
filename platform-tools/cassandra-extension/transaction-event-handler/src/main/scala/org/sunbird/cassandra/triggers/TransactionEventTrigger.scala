package org.sunbird.cassandra.triggers

import java.util

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.apache.cassandra.db.{Clustering, Mutation}
import org.apache.cassandra.db.marshal.{AbstractType, CompositeType, ListType, MapType, SetType}
import org.apache.cassandra.db.partitions.Partition
import org.apache.cassandra.db.rows.{Cell, Unfiltered}
import org.apache.cassandra.triggers.ITrigger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
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
            logger.info(mapper.writeValueAsString(Map("operationType" -> "UPSERT", "partitionKeys" -> partitionData, "objectType" -> objectType) ++ processEvent(partition, partitionData)))
        }

        return null;
    }

    protected def processEvent(partition: Partition, partitionKeyData: Map[String, Any]): Map[String,Any] = {
        partition.unfilteredIterator().map(next => {
            val clusterKeyData: Map[String, Any] = getClusterKeyData(partition, next)
            val row = partition.getRow(next.clustering().asInstanceOf[Clustering])

            val columnDefinitions = row.columns().iterator()
            val metadata = columnDefinitions.map(columnDefinition => {
                val columnType = columnDefinition.`type`
                if (columnType.isInstanceOf[ListType[Any]]) {
                    val buffer = ListBuffer[Any]()
                    val cells = row.cells()
                    processListDataType(cells, buffer, columnDefinition.toString)

                } else if (columnType.isInstanceOf[SetType[Any]]) {
                    val buffer = ListBuffer[Any]()
                    val cells = row.cells()
                    processSetDataType(cells, buffer, columnDefinition.toString)

                } else if (columnType.isInstanceOf[MapType[Any, Any]]) {
                    val cells = row.cells()
                    processMapDataType(cells, columnDefinition.toString)

                } else {
                    val cell = row.getCell(columnDefinition)
                    Map(getColumnName(cell) -> Map("nv" -> processDefaultDataType(cell)))
                }
            }).toList.flatten.toMap
            Map("clusteringKeys" -> clusterKeyData, "metadata" -> metadata)
        }).reduce((a,b) => {
            a ++ b
        }).toList.toMap
    }

    private def processMapDataType(cells: Iterable[Cell], columnName: String): Map[String, Any] = {
        val mapdata = cells.toList.filter(cell => getColumnName(cell).toString.equalsIgnoreCase(columnName)).map(cell => {
            val mapColumnType = getColumnType(cell).asInstanceOf[MapType[AnyRef, AnyRef]]
            val cellValue = getCellValue(cell)
            val key = mapColumnType.getKeysType.compose(cell.path.get(0))

            if(cell.isLive(0))
                Map(key.toString -> cellValue)
            else
                Map(key.toString -> null)
        }).flatten.toMap
        Map(columnName -> mapdata)
    }


    private def processListDataType(cells: Iterable[Cell] ,buffer: ListBuffer[Any], columnName: String): Map[String, Any] = {

        cells.toList.filter(cell => getColumnName(cell).toString.equalsIgnoreCase(columnName)).map(cell => {
            if (cell.isLive(0) && !buffer.contains(getCellValue(cell))) buffer.add(getCellValue(cell))
        })
        Map(columnName -> buffer)
    }

    private def processSetDataType(cells: Iterable[Cell], buffer:ListBuffer[Any], columnName: String): Map[String, Any] = {

        cells.toList.filter(cell => getColumnName(cell).toString.equalsIgnoreCase(columnName)).map(cell => {
            val keyTypes = getColumnType(cell).asInstanceOf[SetType[Any]].getElementsType
            val cellValue = keyTypes.compose(cell.path.get(0))
            if (!buffer.contains(cellValue)) buffer.add(keyTypes.compose(cell.path.get(0)))
        })
        Map(columnName -> buffer)

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

    protected def getClusterKeyData(partition: Partition, next: Unfiltered): Map[String, Any] = {
        val clusterKeyList = partition.metadata().clusteringColumns()
        val clustering = next.clustering()
        if (clustering.size() > 0) {
            (for (i <- 0 to (clustering.size() - 1) ) yield {
                val columnName = clusterKeyList.get(i).name.toString
                val value = clusterKeyList.get(i).`type`.compose(clustering.get(i))
                Map(columnName -> value)
            }).flatten.toMap
        } else {
            Map()
        }
    }


    private def getColumnName(cell: Cell): String = cell.column().name.toString

    private def getCellValue(cell: Cell): AnyRef = cell.column.cellValueType.asInstanceOf[AbstractType[AnyRef]].compose(cell.value)

    private def getColumnType(cell: Cell) = cell.column.`type`
}
