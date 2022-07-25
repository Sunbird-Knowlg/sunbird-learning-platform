/**
 * 
 */
package org.sunbird.learning.framework;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.mgr.BaseManager;
import org.sunbird.common.mgr.ConvertGraphNode;
import org.sunbird.common.util.FrameworkCache;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.model.cache.CategoryCache;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.learning.hierarchy.store.HierarchyStore;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pradyumna
 *
 */
public class FrameworkHierarchy extends BaseManager {

	protected static final String GRAPH_ID = (Platform.config.hasPath("graphId")) ? Platform.config.getString("graphId")
			: "domain";

	private static final String keyspace = Platform.config.hasPath("hierarchy.keyspace.name")
			? Platform.config.getString("hierarchy.keyspace.name")
			: "hierarchy_store";
	private static final String table = Platform.config.hasPath("framework.hierarchy.table")
			? Platform.config.getString("framework.hierarchy.table")
			: "framework_hierarchy";
	private static final String objectType = "Framework";
	private HierarchyStore hierarchyStore = new HierarchyStore(keyspace, table, objectType, false);

	/**
	 * @param id
	 * @throws Exception
	 */
	public void generateFrameworkHierarchy(String id) throws Exception {
		Response responseNode = getDataNode(GRAPH_ID, id);
		TelemetryManager.log("FrameworkHierarchy: generateFrameworkHierarchy method");
		if (checkError(responseNode))
			throw new ResourceNotFoundException("ERR_DATA_NOT_FOUND", "Data not found with id : " + id);
		Node node = (Node) responseNode.get(GraphDACParams.node.name());
		if (StringUtils.equalsIgnoreCase(node.getObjectType(), "Framework")) {
			FrameworkCache.delete(id);
			TelemetryManager.log("FrameworkHierarchy: generateFrameworkHierarchy method:: After Deleting Framework Cache");
			Map<String, Object> frameworkDocument = new HashMap<>();
			Map<String, Object> frameworkHierarchy = getHierarchy(node.getIdentifier(), 0, false, true);
			CategoryCache.setFramework(node.getIdentifier(), frameworkHierarchy);
			TelemetryManager.log("FrameworkHierarchy: generateFrameworkHierarchy method:: After generating Hierarchy: frameworkHierarchy: " + frameworkHierarchy);
			frameworkDocument.putAll(frameworkHierarchy);
			frameworkDocument.put("identifier", node.getIdentifier());
			frameworkDocument.put("objectType", node.getObjectType());
			DefinitionDTO definition = getDefinition(GRAPH_ID, node.getObjectType());
			String[] fields = getFields(definition);
			for (String field : fields) {
				if(null!=node.getMetadata().get(field))
					frameworkDocument.put(field, node.getMetadata().get(field));
			}
			TelemetryManager.log("FrameworkHierarchy: generateFrameworkHierarchy method::Before saving Hierarchy to cassandra " );
			hierarchyStore.saveOrUpdateHierarchy(node.getIdentifier(),frameworkDocument);
		} else {
			throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "The object with given identifier is not a framework: " + id);
		}
	}

	/**
	 *
	 * @param frameworkId
	 * @return frameworkHierarchy
	 * @throws Exception
	 */
	public Map<String, Object> getFrameworkHierarchy(String frameworkId) throws Exception{
		Map<String, Object> hierarchy = hierarchyStore.getHierarchy(frameworkId);
		if(MapUtils.isEmpty(hierarchy)) {
			throw new ResourceNotFoundException(ResponseCode.RESOURCE_NOT_FOUND.name(), "Framework not found : " +
					frameworkId);
		}
		return  hierarchy;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getHierarchy(String id, int index, boolean includeMetadata, boolean includeRelations) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();
		Response responseNode = getDataNode(GRAPH_ID, id);
		if (checkError(responseNode))
			throw new ResourceNotFoundException("ERR_DATA_NOT_FOUND", "Data not found with id : " + id,
					ResponseCode.RESOURCE_NOT_FOUND);
		Node node = (Node) responseNode.get(GraphDACParams.node.name());

		Map<String, Object> metadata = node.getMetadata();
		String status = (String) metadata.get("status");
		if (StringUtils.equalsIgnoreCase("Live", status)) {
			String objectType = node.getObjectType();
			DefinitionDTO definition = getDefinition(GRAPH_ID, objectType);
			if (includeMetadata) {
				String[] fields = getFields(definition);
				if (fields != null) {
					for (String field : fields) {
						data.put(field, metadata.get(field));
					}
				} else {
					data.putAll(node.getMetadata());
				}
				data.put("identifier", node.getIdentifier());
				data.put("index", index);

			}
			if (includeRelations) {
				Map<String, String> inRelDefMap = new HashMap<>();
				Map<String, String> outRelDefMap = new HashMap<>();
				List<String> sortKeys = new ArrayList<String>();
				ConvertGraphNode.getRelationDefinitionMaps(definition, inRelDefMap, outRelDefMap);
				List<Relation> outRelations = node.getOutRelations();
				if (null != outRelations && !outRelations.isEmpty()) {
					for (Relation relation : outRelations) {
						String type = relation.getRelationType();
						String key = type + relation.getEndNodeObjectType();
						String title = outRelDefMap.get(key);
						List<Map<String, Object>> relData = (List<Map<String, Object>>) data.get(title);
						if (relData == null) {
							relData = new ArrayList<Map<String, Object>>();
							data.put(title, relData);
							if ("hasSequenceMember".equalsIgnoreCase(type))
								sortKeys.add(title);
						}
						Map<String, Object> relMeta = relation.getMetadata();
						int seqIndex = 0;
						if (relMeta != null) {
							Object indexObj = relMeta.get("IL_SEQUENCE_INDEX");
							if (indexObj != null)
								seqIndex = ((Long) indexObj).intValue();
						}
						boolean getChildren = true;
						// TODO: This condition value should get from definition node.
						if ("associations".equalsIgnoreCase(title)) {
							getChildren = false;
						}
						Map<String, Object> childData = getHierarchy(relation.getEndNodeId(), seqIndex, true, getChildren);
						if (!childData.isEmpty())
							relData.add(childData);
					}
				}
				for (String key : sortKeys) {
					List<Map<String, Object>> prop = (List<Map<String, Object>>) data.get(key);
					getSorted(prop);
				}
			}
		}

		return data;
	}

	private DefinitionDTO getDefinition(String graphId, String objectType) {
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = getResponse(request);
		if (!checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}

	private String[] getFields(DefinitionDTO definition) {
		Map<String, Object> meta = definition.getMetadata();
		return (String[]) meta.get("fields");
	}

	private void getSorted(List<Map<String, Object>> relObjects) {
		Collections.sort(relObjects, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				int o1Index = (int) o1.get("index");
				int o2Index = (int) o2.get("index");
				return o1Index - o2Index;
			}
		});
	}
}
