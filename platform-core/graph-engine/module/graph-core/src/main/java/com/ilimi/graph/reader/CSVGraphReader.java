package com.ilimi.graph.reader;

import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.graph.model.node.RelationDefinition;

public class CSVGraphReader implements GraphReader {
    private List<Node> definitionNodes;
    private List<Node> dataNodes;
    private Map<String, List<StringValue>> tagMembersMap;
    private List<Relation> relations;
    private List<String> validations;
    private BaseGraphManager manager;
    private ObjectMapper mapper;
    Map<String, Map<String, String>> propertyDataMap;

    private static final String PROPERTY_ID = "identifier";
    private static final String PROPERTY_NODE_TYPE = "nodeType";
    private static final String PROPERTY_OBJECT_TYPE = "objectType";
    private static final String PROPERTY_TAGS = "tags";
    private static final String REL_HEADER_START_WITH = "rel:";
    private static final String LIST_STR_DELIMITER = "::";

    CSVFormat csvFileFormat = CSVFormat.DEFAULT;

    @SuppressWarnings("resource")
    public CSVGraphReader(BaseGraphManager manager, ObjectMapper mapper, String graphId, InputStream inputStream,
            Map<String, Map<String, String>> propertyDataMap) throws Exception {
        this.manager = manager;
        this.mapper = mapper;
        this.propertyDataMap = propertyDataMap;
        definitionNodes = new ArrayList<Node>();
        dataNodes = new ArrayList<Node>();
        tagMembersMap = new HashMap<String, List<StringValue>>(); 
        relations = new ArrayList<Relation>();
        validations = new ArrayList<String>();
        InputStreamReader isReader = new InputStreamReader(inputStream);
        CSVParser csvReader = new CSVParser(isReader, csvFileFormat);
        List<CSVRecord> recordsList = csvReader.getRecords();
        CSVRecord headerRecord = recordsList.get(0);
        List<String> allHeaders = new ArrayList<String>();
        List<String> listValueHeaders = Arrays.asList("internalValidation", "externalValidation", "assessmentMethod");
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
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_GRAPH_ERROR.name(), "Required columns are missing.");
        }

        for (int i = 1; i < recordsList.size(); i++) {
            CSVRecord record = recordsList.get(i);
            String uniqueId = record.get(uniqueIdIndex);
            String nodeType = SystemNodeTypes.DATA_NODE.name();
            String objectType = record.get(objectTypeIndex);
            if(StringUtils.isBlank(uniqueId) || StringUtils.isBlank(objectType)) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_GRAPH_ERROR.name(), "Required data(uniqueId, objectType) is missing for the row["+(i+1)+"]: " +record);
            }            
            Map<String, Object> metadata = new HashMap<String, Object>();
            for (int j = 0; j < allHeaders.size(); j++) {
                if (!skipIndexes.contains(j) && !relHeaders.values().contains(j)) {
                    if (StringUtils.isNotBlank(record.get(j))) {
                        String metadataKey = getMetadataKey(objectType, allHeaders.get(j));
                        if(listValueHeaders.contains(metadataKey)) {
                            
                            String[] valList = getListFromString(record.get(j));
                            metadata.put(metadataKey, valList);
                        } else {
                            metadata.put(metadataKey, record.get(j));
                        }
                        
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
            node.setOutRelations(relations);
            dataNodes.add(node);
            if(tagsIndex != -1) {
                String tagsData = record.get(tagsIndex);
                if(StringUtils.isNotBlank(tagsData)) {
                    String[] recordTags = tagsData.split(LIST_STR_DELIMITER);
                    for(String tagName : recordTags) {
                        tagName = tagName.trim();
                        if(tagMembersMap.containsKey(tagName)) {
                            tagMembersMap.get(tagName).add(new StringValue(uniqueId));
                        } else {
                            List<StringValue> members = new ArrayList<StringValue>();
                            members.add(new StringValue(uniqueId));
                            tagMembersMap.put(tagName, members);
                        }
                    }
                }
            }
        }
        isReader.close();
        csvReader.close();
    }

    private String[] getListFromString(String valStr) {
        String[] valList = new String[]{};
        if(StringUtils.isNotBlank(valStr)) {
            String[] vals = valStr.split(LIST_STR_DELIMITER);
            return vals;
        }
        return valList;
    }

    private String getMetadataKey(String objectType, String title) {
        if (propertyDataMap != null) {
            Map<String, String> objectPropMap = propertyDataMap.get(objectType);
            if (objectPropMap != null) {
                String propertyName = objectPropMap.get(title);
                if (StringUtils.isNotBlank(propertyName))
                    return propertyName;
            }
            return title;
        } else {
            return title;
        }
    }

//    private void validateProperty(CSVRecord record, int index, String propertyName) {
//        if (StringUtils.isBlank(record.get(index)))
//            validations.add(propertyName + " is invalid or empty on record:" + record);
//    }

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

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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
    public Map<String, List<StringValue>> getTagMembersMap() {
        return tagMembersMap;
    }

    /**
     * @param tagMembersMap the tagMembersMap to set
     */
    public void setTagMembersMap(Map<String, List<StringValue>> tagMembersMap) {
        this.tagMembersMap = tagMembersMap;
    }
    
}
