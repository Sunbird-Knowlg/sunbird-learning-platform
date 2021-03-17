package org.sunbird.graph.reader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.model.node.MetadataDefinition;
import org.sunbird.graph.model.node.RelationDefinition;

public class CSVGraphReader implements GraphReader {
	private List<Node> definitionNodes;
	private List<Node> dataNodes;
	private Map<String, List<String>> tagMembersMap;
	private List<Relation> relations;
	private List<String> validations;
	private BaseGraphManager manager;
	private ObjectMapper mapper;
	Map<String, Map<String, MetadataDefinition>> propertyDataMap;

	public static final String PROPERTY_ID = "identifier";
	public static final String PROPERTY_NODE_TYPE = "nodeType";
	public static final String PROPERTY_OBJECT_TYPE = "objectType";
	public static final String PROPERTY_TAGS = "tags";
	public static final String REL_HEADER_START_WITH = "rel:";
	public static final String LIST_STR_DELIMITER = "::";

	CSVFormat csvFileFormat = CSVFormat.DEFAULT;

	@SuppressWarnings("resource")
	public CSVGraphReader(BaseGraphManager manager, ObjectMapper mapper, String graphId, InputStream inputStream,
			Map<String, Map<String, MetadataDefinition>> propertyDataMap) throws Exception {
		this.manager = manager;
		this.mapper = mapper;
		this.propertyDataMap = propertyDataMap;
		definitionNodes = new ArrayList<Node>();
		dataNodes = new ArrayList<Node>();
		tagMembersMap = new HashMap<String, List<String>>();
		relations = new ArrayList<Relation>();
		validations = new ArrayList<String>();
		try (InputStreamReader isReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
				CSVParser csvReader = new CSVParser(isReader, csvFileFormat)) {
			List<CSVRecord> recordsList = csvReader.getRecords();
			CSVRecord headerRecord = recordsList.get(0);
			List<String> allHeaders = new ArrayList<String>();
			Map<String, Integer> relHeaders = new HashMap<String, Integer>();
			for (int i = 0; i < headerRecord.size(); i++) {
				allHeaders.add(headerRecord.get(i));
				if (headerRecord.get(i).startsWith(REL_HEADER_START_WITH)) {
					relHeaders.put(headerRecord.get(i), i);
				}
			}
			int uniqueIdIndex = allHeaders.indexOf(PROPERTY_ID);
			int nodeTypeIndex = allHeaders.indexOf(PROPERTY_NODE_TYPE);
			int objectTypeIndex = allHeaders.indexOf(PROPERTY_OBJECT_TYPE);
			int tagsIndex = allHeaders.indexOf(PROPERTY_TAGS);
			List<Integer> skipIndexes = Arrays.asList(uniqueIdIndex, nodeTypeIndex, objectTypeIndex, tagsIndex);
			if (!hasValidIndexes(uniqueIdIndex, objectTypeIndex)) {
				throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_MISSING_REQ_COLUMNS.name(),
						"Required columns are missing.");
			}

			for (int i = 1; i < recordsList.size(); i++) {
				CSVRecord record = recordsList.get(i);
				String uniqueId = record.get(uniqueIdIndex);
				String nodeType = SystemNodeTypes.DATA_NODE.name();
				String objectType = record.get(objectTypeIndex);
				if (StringUtils.isBlank(uniqueId) || StringUtils.isBlank(objectType)) {
					throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_MISSING_REQ_COLUMN_DATA.name(),
							"Required data(uniqueId, objectType) is missing for the row[" + (i + 1) + "]: " + record);
				}
				Map<String, Object> metadata = new HashMap<String, Object>();
				for (int j = 0; j < allHeaders.size(); j++) {
					if (!skipIndexes.contains(j) && !relHeaders.values().contains(j)) {
						String metadataKey = getMetadataKey(objectType, allHeaders.get(j));
						String val = record.get(j);
						if (isListProperty(objectType, allHeaders.get(j))) {
							String[] valList = getListFromString(val);
							metadata.put(metadataKey, valList);
						} else {
							if (StringUtils.isNotBlank(val))
								val = val.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
							else
								val = null;
							Object value = getMetadataValue(objectType, metadataKey, val);
							metadata.put(metadataKey, value);
						}
					}
				}
				Node node = new Node(graphId, metadata);
				node.setIdentifier(uniqueId);
				node.setNodeType(nodeType);
				node.setObjectType(objectType);
				List<Relation> relations = new ArrayList<Relation>();
				for (String relHeader : relHeaders.keySet()) {
					String relName = relHeader.replaceAll(REL_HEADER_START_WITH, "");
					String[] endNodeIds = record.get(relHeaders.get(relHeader)).toString().split(",");
					for (String endNodeId : endNodeIds) {
						endNodeId = endNodeId.trim();
						if (StringUtils.isNotBlank(endNodeId)) {
							Relation relation = new Relation(uniqueId, relName, endNodeId);
							relations.add(relation);
						}
					}
				}
				if(null != relHeaders.keySet()&& !relHeaders.keySet().isEmpty()){
					node.setOutRelations(relations);
				}
				dataNodes.add(node);
				if (tagsIndex != -1) {
					String tagsData = record.get(tagsIndex);
					if (StringUtils.isNotBlank(tagsData)) {
						String[] recordTags = tagsData.split(LIST_STR_DELIMITER);
						for (String tagName : recordTags) {
							tagName = tagName.trim();
							if (tagMembersMap.containsKey(tagName)) {
								tagMembersMap.get(tagName).add(uniqueId);
							} else {
								List<String> members = new ArrayList<String>();
								members.add(uniqueId);
								tagMembersMap.put(tagName, members);
							}
						}
					}
				}
			}
		}
	}

	private String[] getListFromString(String valStr) {
		if (StringUtils.isNotBlank(valStr)) {
			valStr = valStr.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
			String[] vals = valStr.trim().split("\\s*" + LIST_STR_DELIMITER + "\\s*");
			if (null != vals && vals.length > 0)
				return vals;
		}
		return null;
	}

	private String getMetadataKey(String objectType, String title) {
		if (propertyDataMap != null) {
			Map<String, MetadataDefinition> objectPropMap = propertyDataMap.get(objectType);
			if (objectPropMap != null) {
				MetadataDefinition def = objectPropMap.get(title);
				if (null != def && StringUtils.isNotBlank(def.getPropertyName())) {
					return def.getPropertyName();
				}
			}
			return title;
		} else {
			return title;
		}
	}

	@SuppressWarnings("rawtypes")
	private Object getMetadataValue(String objectType, String title, String val) {
		if (propertyDataMap != null) {
			Map<String, MetadataDefinition> objectPropMap = propertyDataMap.get(objectType);
			if (objectPropMap != null) {
				MetadataDefinition def = objectPropMap.get(title);
				if (null != def) {
					Object value = val;
					if (StringUtils.isBlank(val) && null != def.getDefaultValue()
							&& StringUtils.isNotBlank(def.getDefaultValue().toString()))
						value = def.getDefaultValue();
					if (null != value) {
						String datatype = def.getDataType();
						if (StringUtils.equalsIgnoreCase("list", datatype)
								|| StringUtils.equalsIgnoreCase("multi-select", datatype)) {
							if (value instanceof List) {
								value = ((List) value).toArray();
							} else if (!(value instanceof Object[])) {
								value = new String[] { value.toString() };
							}
						} else if (StringUtils.equalsIgnoreCase("number", datatype)) {
							try {
								BigDecimal bd = new BigDecimal(val.toString());
								value = bd.doubleValue();
							} catch (Exception e) {
							}
						} else if (StringUtils.equalsIgnoreCase("boolean", datatype)) {
							try {
								Boolean b = new Boolean(val.toString());
								value = b;
							} catch (Exception e) {
							}
						}
					}
					return value;
				}
			}
		}
		return val;
	}

	private boolean isListProperty(String objectType, String title) {
		if (propertyDataMap != null) {
			Map<String, MetadataDefinition> objectPropMap = propertyDataMap.get(objectType);
			if (objectPropMap != null) {
				MetadataDefinition def = objectPropMap.get(title);
				if (null != def && StringUtils.isNotBlank(def.getDataType())) {
					if (StringUtils.equalsIgnoreCase(def.getDataType(), "list")
							|| StringUtils.equalsIgnoreCase(def.getDataType(), "multi-select"))
						return true;
				}
			}
		}
		return false;
	}

	// private void validateProperty(CSVRecord record, int index, String
	// propertyName) {
	// if (StringUtils.isBlank(record.get(index)))
	// validations.add(propertyName + " is invalid or empty on record:" +
	// record);
	// }

	private boolean hasValidIndexes(int... indexes) {
		boolean isValid = true;
		for (int index : indexes) {
			if (index == -1) {
				isValid = false;
				break;
			}
		}
		return isValid;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private List<RelationDefinition> getRelationDefinitions(String metadataStr) throws Exception {
		List<RelationDefinition> metadata = new ArrayList<RelationDefinition>();
		if (StringUtils.isNotBlank(metadataStr)) {
			List<Map<String, Object>> metaStrList = mapper.readValue(metadataStr, List.class);
			for (Map<String, Object> defMeta : metaStrList) {
				RelationDefinition relDef = mapper.convertValue(defMeta, RelationDefinition.class);
				metadata.add(relDef);
			}
		}
		return metadata;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private List<MetadataDefinition> getDefinitionNodeMetadata(String metadataStr) throws Exception {
		List<MetadataDefinition> metadata = new ArrayList<MetadataDefinition>();
		if (StringUtils.isNotBlank(metadataStr)) {
			List<Map<String, Object>> metaStrList = mapper.readValue(metadataStr, List.class);
			for (Map<String, Object> defMeta : metaStrList) {
				MetadataDefinition metaDef = mapper.convertValue(defMeta, MetadataDefinition.class);
				metadata.add(metaDef);
			}
		}
		return metadata;
	}

	@Override
	public List<Node> getDefinitionNodes() {
		return definitionNodes;
	}

	@Override
	public List<Node> getDataNodes() {
		return dataNodes;
	}

	@Override
	public List<Relation> getRelations() {
		return relations;
	}

	/**
	 * @param definitionNodes
	 *            the definitionNodes to set
	 */
	@Override
	public void setDefinitionNodes(List<Node> definitionNodes) {
		this.definitionNodes = definitionNodes;
	}

	/**
	 * @param dataNodes
	 *            the dataNodes to set
	 */
	@Override
	public void setDataNodes(List<Node> dataNodes) {
		this.dataNodes = dataNodes;
	}

	/**
	 * @param relations
	 *            the relations to set
	 */
	@Override
	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}

	@Override
	public List<String> getValidations() {
		return validations;
	}

	/**
	 * @return the tagMembersMap
	 */
	public Map<String, List<String>> getTagMembersMap() {
		return tagMembersMap;
	}

	/**
	 * @param tagMembersMap
	 *            the tagMembersMap to set
	 */
	public void setTagMembersMap(Map<String, List<String>> tagMembersMap) {
		this.tagMembersMap = tagMembersMap;
	}

}
