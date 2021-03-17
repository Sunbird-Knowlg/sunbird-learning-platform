package org.sunbird.graph.service.util;

import java.util.Map;

import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.service.common.DACErrorCodeConstants;
import org.sunbird.graph.service.common.DACErrorMessageConstants;
import org.sunbird.graph.service.common.GraphOperation;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

/**
 * The Class DefinitionNodeUtil.
 * 
 * @author Mohammad Azharuddin
 */
public class DefinitionNodeUtil {

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
		TelemetryManager.log(
				"Reading Metadata Value for Graph Id: " + graphId + "Object Type: " + objectType + "For Key: " + key);

		// Initializing the value
		String value = "";

		TelemetryManager.log("Fetching the Definition Node to Read Metadata Value. | [Graph Id: " + graphId + "]");
		Map<String, Object> metadataMap = getDefinitionNodeMetadata(graphId, objectType);
		if (null == metadataMap)
			throw new ResourceNotFoundException(DACErrorCodeConstants.MISSING_DEFINITION.name(),
					DACErrorMessageConstants.MISSING_DEFINTION_ERROR + " | [Object Type: " + objectType
							+ " and Graph Id " + graphId + "]");

		if (null != metadataMap && null != metadataMap.get(key))
			value = (String) metadataMap.get(key);

		TelemetryManager.log("Returning the Metadata Value - Key: " + key + " | value: " + value);
		return value;
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
		TelemetryManager.log("Fetching Definition Node for Object Id: " + objectType + "of Graph Id: " + graphId + ".");
		Map<String, Object> metadataMap = null;
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
		TelemetryManager.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			try {
				String query = "match (n:" + graphId + "{" + SystemProperties.IL_SYS_NODE_TYPE.name() + ":'"
						+ SystemNodeTypes.DEFINITION_NODE.name() + "'," + SystemProperties.IL_FUNC_OBJECT_TYPE.name()
						+ ":'" + objectType + "'}) return (n) as result";
				StatementResult result = session.run(query);
				if (result.hasNext()) {
					Record record = result.next();
					InternalNode node = (InternalNode) record.values().get(0).asObject();
					metadataMap = node.asMap();
				}
			} catch (Exception e) {
				throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
						DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
			}
		}
		return metadataMap;
	}
}
