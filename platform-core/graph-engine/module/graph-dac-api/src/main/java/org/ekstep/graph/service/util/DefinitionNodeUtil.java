package org.ekstep.graph.service.util;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;

/**
 * The Class DefinitionNodeUtil.
 * 
 * @author Mohammad Azharuddin
 */
public class DefinitionNodeUtil {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(DefinitionNodeUtil.class.getName());

	/**
	 * Gets the metadata value.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @param key
	 *            the key
	 * @param request
	 *            the request
	 * @return the metadata value
	 */
	public static String getMetadataValue(String graphId, String objectType, String key, Request request) {
		LOGGER.debug(
				"Reading Metadata Value for Graph Id: " + graphId + "Object Type: " + objectType + "For Key: " + key);

		// Initializing the value
		String value = "";

		LOGGER.info("Fetching the Definition Node to Read Metadata Value. | [Graph Id: " + graphId + "]");
		Node definitionNode = getDefinitionNode(graphId, objectType, request);
		if (null == definitionNode)
			throw new ResourceNotFoundException(DACErrorCodeConstants.MISSING_DEFINITION.name(),
					DACErrorMessageConstants.MISSING_DEFINTION_ERROR + " | [Object Type: " + objectType
							+ " and Graph Id " + graphId + "]");

		Map<String, Object> metadataMap = definitionNode.getMetadata();
		if (null != metadataMap && null != metadataMap.get(key))
			value = (String) metadataMap.get(key);

		LOGGER.info("Returning the Metadata Value - Key: " + key + " | value: " + value);
		return value;
	}

	/**
	 * Gets the metadata value.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @param key
	 *            the key
	 * @return the metadata value
	 */
	public static String getMetadataValue(String graphId, String objectType, String key) {
		LOGGER.debug(
				"Reading Metadata Value for Graph Id: " + graphId + "Object Type: " + objectType + "For Key: " + key);

		// Initializing the value
		String value = "";

		LOGGER.info("Fetching the Definition Node to Read Metadata Value. | [Graph Id: " + graphId + "]");
		Map<String, Object> metadataMap = getDefinitionNodeMetadata(graphId, objectType);
		if (null == metadataMap)
			throw new ResourceNotFoundException(DACErrorCodeConstants.MISSING_DEFINITION.name(),
					DACErrorMessageConstants.MISSING_DEFINTION_ERROR + " | [Object Type: " + objectType
							+ " and Graph Id " + graphId + "]");

		if (null != metadataMap && null != metadataMap.get(key))
			value = (String) metadataMap.get(key);

		LOGGER.info("Returning the Metadata Value - Key: " + key + " | value: " + value);
		return value;
	}

	/**
	 * Gets the definition node.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @param request
	 *            the request
	 * @return the definition node
	 */
	private static Node getDefinitionNode(String graphId, String objectType, Request request) {
		LOGGER.debug("Fetching Definition Node for Object Id: " + objectType + "of Graph Id: " + graphId + ".");

		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		Node node = new Node();
		try (Transaction tx = graphDb.beginTx()) {
			ResourceIterator<org.neo4j.graphdb.Node> nodes = graphDb.findNodes(NODE_LABEL,
					SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DEFINITION_NODE.name());

			if (null != nodes) {
				LOGGER.info("Iterating over all the Definition Nodes for Graph Id: " + graphId);
				while (nodes.hasNext()) {
					org.neo4j.graphdb.Node neo4jNode = nodes.next();
					if (StringUtils.equalsIgnoreCase(
							((String) neo4jNode.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name())),
							objectType)) {
						node = new Node(graphId, neo4jNode);
						LOGGER.info("Found the Definition Node for Object type " + objectType + " | Graph Id: "
								+ graphId + " | Node Id:  " + node.getIdentifier());
						nodes.close();
						break;
					}
				}
				nodes.close();
			}
			tx.success();

			LOGGER.debug("Returning the Definition Node for Object " + node.getObjectType() + "of Graph Id: "
					+ node.getGraphId());
			return node;
		}
	}

	/**
	 * Gets the definition node.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @return the definition node
	 */
	private static Map<String, Object> getDefinitionNodeMetadata(String graphId, String objectType) {
		LOGGER.debug("Fetching Definition Node for Object Id: " + objectType + "of Graph Id: " + graphId + ".");
		Map metadataMap = null;
		try (Driver driver = DriverUtil.getDriver(graphId)) {
			LOGGER.info("Driver Initialised. | [Graph Id: " + graphId + "]");
			try (Session session = driver.session()) {
				try (org.neo4j.driver.v1.Transaction tx = session.beginTransaction()) {
					String query = "match (n:" + graphId + "{" + SystemProperties.IL_SYS_NODE_TYPE.name() + ":'"
							+ SystemNodeTypes.DEFINITION_NODE.name() + "',"
							+ SystemProperties.IL_FUNC_OBJECT_TYPE.name() + ":'"+objectType+"'}) return (n) as result";
					StatementResult result = tx.run(query);
					if (result.hasNext()) {
						Record record = result.next();
						InternalNode node = (InternalNode) record.values().get(0).asObject();
						metadataMap = node.asMap();
					}
					tx.success();
					tx.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					session.close();
				}
			}
		}
		return metadataMap;
	}
}
