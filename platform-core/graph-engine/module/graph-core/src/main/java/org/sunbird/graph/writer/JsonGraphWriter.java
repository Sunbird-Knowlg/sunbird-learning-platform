package org.sunbird.graph.writer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.model.node.DefinitionNode;
import org.sunbird.graph.model.node.MetadataDefinition;
import org.sunbird.graph.model.node.RelationDefinition;

public class JsonGraphWriter implements GraphWriter {

	String exportedData = "";
	private List<Node> nodes;
	private List<Relation> relations;
	private ObjectMapper mapper;

	public JsonGraphWriter(List<Node> nodes, List<Relation> relations) {
		mapper = new ObjectMapper();
		this.nodes = nodes;
		this.relations = null == relations ? new ArrayList<Relation>() : relations;
	}

	@SuppressWarnings("unchecked")
	@Override
	public OutputStream getData() throws Exception {
		StringBuilder sb = new StringBuilder();
		List<Map<String, Object>> definitionNodesList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> dataNodesList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> relationsList = new ArrayList<Map<String, Object>>();
		if (nodes != null) {
			for (Node node : nodes) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("uniqueId", node.getIdentifier());
				map.put("nodeType", node.getNodeType());
				map.put("objectType", node.getObjectType());
				if (StringUtils.isNotBlank(node.getNodeType())
						&& node.getNodeType().equals(SystemNodeTypes.DEFINITION_NODE.name())) {
					Map<String, Object> metadata = mapper.convertValue(node.getMetadata(), Map.class);
					List<MetadataDefinition> indexedMetadata = new ArrayList<MetadataDefinition>();
					if (StringUtils.isNotBlank((String) metadata.get(DefinitionNode.INDEXABLE_METADATA_KEY))) {
						String metaList = (String) metadata.get(DefinitionNode.INDEXABLE_METADATA_KEY);
						try {
							List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(metaList,
									List.class);
							for (Map<String, Object> metaMap : listMap) {
								indexedMetadata.add(
										(MetadataDefinition) mapper.convertValue(metaMap, MetadataDefinition.class));
							}
						} catch (Exception e) {
						}
					}
					map.put("indexedMetadata", indexedMetadata);

					List<MetadataDefinition> nonIndexedMetadata = new ArrayList<MetadataDefinition>();
					if (StringUtils.isNotBlank((String) metadata.get(DefinitionNode.NON_INDEXABLE_METADATA_KEY))) {
						String metaList = (String) metadata.get(DefinitionNode.NON_INDEXABLE_METADATA_KEY);
						try {
							List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(metaList,
									List.class);
							for (Map<String, Object> metaMap : listMap) {
								nonIndexedMetadata.add(
										(MetadataDefinition) mapper.convertValue(metaMap, MetadataDefinition.class));
							}
						} catch (Exception e) {
						}
					}
					map.put("nonIndexedMetadata", nonIndexedMetadata);

					List<RelationDefinition> inRelationMetadata = new ArrayList<RelationDefinition>();
					if (StringUtils.isNotBlank((String) metadata.get(DefinitionNode.IN_RELATIONS_KEY))) {
						String inRelList = (String) metadata.get(DefinitionNode.IN_RELATIONS_KEY);
						try {
							List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(inRelList,
									List.class);
							for (Map<String, Object> metaMap : listMap) {
								inRelationMetadata.add(
										(RelationDefinition) mapper.convertValue(metaMap, RelationDefinition.class));
							}
						} catch (Exception e) {
						}
					}
					map.put("inRelations", inRelationMetadata);

					List<RelationDefinition> outRelationMetadata = new ArrayList<RelationDefinition>();
					if (StringUtils.isNotBlank((String) metadata.get(DefinitionNode.OUT_RELATIONS_KEY))) {
						String outRelList = (String) metadata.get(DefinitionNode.OUT_RELATIONS_KEY);
						try {
							List<Map<String, Object>> listMap = (List<Map<String, Object>>) mapper.readValue(outRelList,
									List.class);
							for (Map<String, Object> metaMap : listMap) {
								outRelationMetadata.add(
										(RelationDefinition) mapper.convertValue(metaMap, RelationDefinition.class));
							}
						} catch (Exception e) {
						}
					}
					map.put("outRelations", outRelationMetadata);
					definitionNodesList.add(map);
				} else if (StringUtils.isNotBlank(node.getNodeType())
						&& (node.getNodeType().equals(SystemNodeTypes.DATA_NODE.name())
								|| node.getNodeType().equals(SystemNodeTypes.SEQUENCE.name()))) {
					map.put("metadata", node.getMetadata());
					dataNodesList.add(map);
				}
			}
		}
		if (relations != null) {
			for (Relation relation : relations) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("startNode", relation.getStartNodeId());
				map.put("endNode", relation.getEndNodeId());
				map.put("type", relation.getRelationType());
				map.put("metadata", relation.getMetadata());
				relationsList.add(map);
			}
		}
		Map<String, Object> exportMap = new HashMap<String, Object>();
		exportMap.put("definitionNodes", definitionNodesList);
		exportMap.put("numberOfNodes", dataNodesList.size());
		exportMap.put("numberOfRelations", relationsList.size());
		exportMap.put("nodes", dataNodesList);
		exportMap.put("relations", relationsList);
		sb.append(mapper.writeValueAsString(exportMap));
		exportedData = sb.toString();
		try (OutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(exportedData.getBytes());
			return outputStream;
		}
	}

}
