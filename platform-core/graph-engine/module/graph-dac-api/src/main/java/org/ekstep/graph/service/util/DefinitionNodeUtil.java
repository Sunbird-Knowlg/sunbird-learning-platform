package org.ekstep.graph.service.util;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.graph.dac.enums.GraphDACParams;
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
	 * @param graphId the graph id
	 * @param objectType the object type
	 * @param key the key
	 * @param request the request
	 * @return the metadata value
	 */
	public static String getMetadataValue(String graphId, String objectType, String key, Request request) {
		LOGGER.debug(
				"Reading Metadata Value for Graph Id: " + graphId + "Object Type: " + objectType + "For Key: " + key);
		
		// Initializing the value
		String value = "";
		
		LOGGER.info("Fetching the Definition Node to Read Metadata Value. | [Graph Id: " + graphId + "]");
		Node definitionNode = getDefinitionNode(GraphUtil.getGraphId(), objectType, request);
		if (null == definitionNode)
			throw new ResourceNotFoundException(DACErrorCodeConstants.MISSING_DEFINITION.name(),
					DACErrorMessageConstants.ERROR_MISSING_DEFINTION + " | [Object Type: " + objectType + " and Graph Id "
							+ graphId + "]");
		
		Map<String, Object> metadataMap = definitionNode.getMetadata();
		if (null != metadataMap && null != metadataMap.get(key))
			value = (String) metadataMap.get(key);
		
		LOGGER.info("Returning the Metadata Value - Key: " + key + " | value: " + value);
		return value;
	}

	/**
	 * Gets the definition node.
	 *
	 * @param graphId the graph id
	 * @param objectType the object type
	 * @param request the request
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
							((String) neo4jNode.getProperty(GraphDACParams.object_type.name())), objectType)) {
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

}
