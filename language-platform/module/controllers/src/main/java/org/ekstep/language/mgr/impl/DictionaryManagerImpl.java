package org.ekstep.language.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IDictionaryManager;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.dac.model.TagCriterion;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.RelationDefinition;

@Component
public class DictionaryManagerImpl extends BaseManager implements IDictionaryManager {

    private static Logger LOGGER = LogManager.getLogger(IDictionaryManager.class.getName());
    private static final String LEMMA_PROPERTY = "lemma";
    private static final List<String> DEFAULT_STATUS = new ArrayList<String>();
    static {
        DEFAULT_STATUS.add("Live");
    }
    
    private ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public Response create(String languageId, String objectType, Request request) {
        if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "ObjectType is blank");
        List<Map> items = (List<Map>) request.get("words");
        List<Node> nodeList = new ArrayList<Node>();
        if (null == items || items.size() <= 0)
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT.name(), objectType + " Object is blank");
        try {
            if (null != items && !items.isEmpty()) {
            	Request requestDefinition = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
                        GraphDACParams.object_type.name(), objectType);
                Response responseDefiniton = getResponse(requestDefinition, LOGGER);
            	 if (!checkError(responseDefiniton)) {
                     DefinitionDTO definition = (DefinitionDTO) responseDefiniton.get(GraphDACParams.definition_node.name());
                     for (Map item : items) {
                         Node node = convertToGraphNode(item, definition);
                         nodeList.add(node);
                     }
                 }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Response createRes = new Response();
        Response errResponse = null;
        List<String> lstNodeId = new ArrayList<String>();
        for (Node node : nodeList) {
        	node.setObjectType(objectType);
	        Request validateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "validateNode");
	        validateReq.put(GraphDACParams.node.name(), node);
	        Response validateRes = getResponse(validateReq, LOGGER);
	        if (checkError(validateRes)) {
	            return validateRes;
	        } else {
	            Request createReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
	            createReq.put(GraphDACParams.node.name(), node);
	            Response res = getResponse(createReq, LOGGER);
	            if (checkError(res)) {
	            	errResponse = res;
	            } else {
	            	lstNodeId.add(node.getIdentifier());
	            }
	            createRes = res;
	            System.out.println("Response: | ");
	        }
        }
        if (null == errResponse) {
        	createRes.getResult().remove("node_id");
        	createRes.put("node_id", lstNodeId);
        	return createRes;
        } else {
        	errResponse.getResult().remove("node_id");
        	errResponse.put("node_id", lstNodeId);
        	return errResponse;
        }
    }

    @Override
    public Response update(String languageId, String id, String objectType, Request request) {
        if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
        if (StringUtils.isBlank(id))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT_ID.name(), "Object Id is blank");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "ObjectType is blank");
        Node item = (Node) request.get(objectType.toLowerCase().trim());
        if (null == item)
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT.name(), objectType + " Object is blank");
        item.setIdentifier(id);
        item.setObjectType(objectType);
        Request validateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "validateNode");
        validateReq.put(GraphDACParams.node.name(), item);
        Response validateRes = getResponse(validateReq, LOGGER);
        if (checkError(validateRes)) {
            return validateRes;
        } else {
            Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
            updateReq.put(GraphDACParams.node.name(), item);
            updateReq.put(GraphDACParams.node_id.name(), item.getIdentifier());
            Response updateRes = getResponse(updateReq, LOGGER);
            return updateRes;
        }
    }

    @Override
    public Response find(String languageId, String id, String[] fields) {
        if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
        if (StringUtils.isBlank(id))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT_ID.name(), "Object Id is blank");
        Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
                GraphDACParams.node_id.name(), id);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request, LOGGER);
        if (checkError(getNodeRes)) {
            return getNodeRes;
        } else {
            Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
            Map<String, Object> map = convertGraphNode(node, languageId, fields);
            Response response = copyResponse(getNodeRes);
            response.put(LanguageObjectTypes.Word.name(), map);
            return response;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response findAll(String languageId, String objectType, String[] fields, Integer limit) {
        if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "Object Type is blank");
        LOGGER.info("Find All Content : " + languageId + ", ObjectType: " + objectType);
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
        sc.sort(new Sort(PARAM_STATUS, Sort.SORT_ASC));
        if (null != limit && limit.intValue() > 0)
            sc.setResultSize(limit);
        else
            sc.setResultSize(DEFAULT_LIMIT);
        Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
        request.put(GraphDACParams.get_tags.name(), true);
        Response findRes = getResponse(request, LOGGER);
        if (checkError(findRes))
            return findRes;
        else {
            List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            if (null != nodes && !nodes.isEmpty()) {
                for (Node node : nodes) {
                    Map<String, Object> map = convertGraphNode(node, languageId, fields);
                    if (null != map && !map.isEmpty()) {
                        list.add(map);
                    }
                }
            }
            Response response = copyResponse(findRes);
            response.put("words", list);
            return response;
        }
    }

    @Override
    public Response deleteRelation(String languageId, String objectType, String objectId1, String relation,
            String objectId2) {
        if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "ObjectType is blank");
        if (StringUtils.isBlank(objectId1) || StringUtils.isBlank(objectId2))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT_ID.name(), "Object Id is blank");
        if (StringUtils.isBlank(relation))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_RELATION_NAME.name(), "Relation name is blank");
        Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "removeRelation");
        request.put(GraphDACParams.start_node_id.name(), objectId1);
        request.put(GraphDACParams.relation_type.name(), relation);
        request.put(GraphDACParams.end_node_id.name(), objectId2);
        return getResponse(request, LOGGER);
    }
    
    @Override
    public Response addRelation(String languageId, String objectType, String objectId1, String relation,
            String objectId2) {
        if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "ObjectType is blank");
        if (StringUtils.isBlank(objectId1) || StringUtils.isBlank(objectId2))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT_ID.name(), "Object Id is blank");
        if (StringUtils.isBlank(relation))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_RELATION_NAME.name(), "Relation name is blank");
        Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
        request.put(GraphDACParams.start_node_id.name(), objectId1);
        request.put(GraphDACParams.relation_type.name(), relation);
        request.put(GraphDACParams.end_node_id.name(), objectId2);
        return getResponse(request, LOGGER);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Response relatedObjects(String languageId, String objectType, String objectId, String relation, String[] fields, String[] relations) {
    	if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
        if (StringUtils.isBlank(objectType))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "ObjectType is blank");
        if (StringUtils.isBlank(objectId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT_ID.name(), "Object Id is blank");
        if (StringUtils.isBlank(relation))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_RELATION_NAME.name(), "Relation name is blank");
        Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
                GraphDACParams.node_id.name(), objectId);
        request.put(GraphDACParams.get_tags.name(), true);
        Response getNodeRes = getResponse(request, LOGGER);
        if (checkError(getNodeRes)) {
            return getNodeRes;
        } else {
            Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
            Map<String, Object> map = convertGraphNode(node, languageId, fields);
            if (StringUtils.equalsIgnoreCase("synonym", relation)) {
                List<Map<String, Object>> synonyms = (List<Map<String, Object>>) map.get("synonyms");
                getSynsetMembers(languageId, synonyms);
                Response response = copyResponse(getNodeRes);
                response.put(LanguageParams.synonyms.name(), synonyms);
                return response;
            } else {
                Map<String, List<NodeDTO>> relationMap = new HashMap<String, List<NodeDTO>>();
                if (null != relations && relations.length > 0) {
                    for (String rel : relations) {
                        if (map.containsKey(rel)) {
                            try {
                                List<NodeDTO> list = (List<NodeDTO>) map.get(rel);
                                relationMap.put(rel, list);
                            } catch(Exception e) {
                            }
                        }
                    }
                }
                Response response = copyResponse(getNodeRes);
                response.put(LanguageParams.relations.name(), relationMap);
                return response;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void getSynsetMembers(String languageId, List<Map<String, Object>> synonyms) {
        if (null != synonyms && !synonyms.isEmpty()) {
            List<String> synsetIds = new ArrayList<String>();
            for (Map<String, Object> synonymObj : synonyms) {
                String synsetId = (String) synonymObj.get("identifier");
                if (StringUtils.isNotBlank(synsetId))
                    synsetIds.add(synsetId);
            }
            Request synsetReq = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNodes",
                    GraphDACParams.node_ids.name(), synsetIds);
            Response synsetRes = getResponse(synsetReq, LOGGER);
            List<Node> synsets = (List<Node>) synsetRes.get(GraphDACParams.node_list.name());
            Map<String, List<String>> membersMap = new HashMap<String, List<String>>();
            if (null != synsets && !synsets.isEmpty()) {
                for (Node synset : synsets) {
                    List<String> words = getSynsetMemberWords(synset);
                    membersMap.put(synset.getIdentifier(), words);
                }
            }
            for (Map<String, Object> synonymObj : synonyms) {
                String synsetId = (String) synonymObj.get("identifier");
                if (membersMap.containsKey(synsetId)) {
                    List<String> words = membersMap.get(synsetId);
                    synonymObj.put("words", words);
                }
            }
        }
    }
    
    private List<String> getSynsetMemberWords(Node synset) {
        List<String> words = new ArrayList<String>();
        List<Relation> outRels = synset.getOutRelations();
        if (null != outRels && !outRels.isEmpty()) {
            for (Relation outRel : outRels) {
                if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(), outRel.getRelationType())) {
                    String word = getOutRelationWord(outRel);
                    if (StringUtils.isNotBlank(word))
                        words.add(word);
                }
            }
        }
        return words;
    }
    
	@SuppressWarnings("null")
	@Override
    public Response translation(String languageId, String[] words, String[] languages) {
    	if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
        if (null == words || words.length <= 0)
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "Word list is empty");
        if (null == languages || languages.length <= 0)
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT_ID.name(), "language list is empty");
        Map<String, Object> map = new HashMap<String,Object>();
        Response response = null;
        for (String word: words) {
        	Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
                    GraphDACParams.node_id.name(), word);
            request.put(GraphDACParams.get_tags.name(), true);
            Response getNodeRes = getResponse(request, LOGGER);
            if (checkError(getNodeRes)) {
                return getNodeRes;
            } else {
                Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
                map.put(word, node.getMetadata().get("translations"));
                response.put("translations", map);
            }
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    public Response list(String languageId, String objectType, Request request) {
        if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
        sc.sort(new Sort(SystemProperties.IL_UNIQUE_ID.name(), Sort.SORT_ASC));
        setLimit(request, sc);

        List<Filter> filters = new ArrayList<Filter>();
        List<String> statusList = new ArrayList<String>();
        Object statusParam = request.get(PARAM_STATUS);
        if (null != statusParam) {
            statusList = getList(mapper, statusParam, PARAM_STATUS);
            if (null != statusList && !statusList.isEmpty())
                filters.add(new Filter(PARAM_STATUS, SearchConditions.OP_IN, statusList));
        }
        if (null != request.getRequest() && !request.getRequest().isEmpty()) {
            for (Entry<String, Object> entry : request.getRequest().entrySet()) {
                if (!StringUtils.equalsIgnoreCase(PARAM_FIELDS, entry.getKey())
                        && !StringUtils.equalsIgnoreCase(PARAM_LIMIT, entry.getKey())
                        && !StringUtils.equalsIgnoreCase(PARAM_STATUS, entry.getKey())
                        && !StringUtils.equalsIgnoreCase("word-lists", entry.getKey())
                        && !StringUtils.equalsIgnoreCase(PARAM_TAGS, entry.getKey())) {
                    List<String> list = getList(mapper, entry.getValue(), entry.getKey());
                    if (null != list && !list.isEmpty()) {
                        filters.add(new Filter(entry.getKey(), SearchConditions.OP_IN, list));
                    }
                } else if (StringUtils.equalsIgnoreCase(PARAM_TAGS, entry.getKey())) {
                    List<String> tags = getList(mapper, entry.getValue(), entry.getKey());
                    if (null != tags && !tags.isEmpty()) {
                        TagCriterion tc = new TagCriterion(tags);
                        sc.setTag(tc);
                    }
                }
            }
        }
        if (null != filters && !filters.isEmpty()) {
            MetadataCriterion mc = MetadataCriterion.create(filters);
            sc.addMetadata(mc);
        }
        Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
        req.put(GraphDACParams.get_tags.name(), true);
        Response listRes = getResponse(req, LOGGER);
        if (checkError(listRes))
            return listRes;
        else {
            List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            if (null != nodes && !nodes.isEmpty()) {
                String[] fields = getFields(request);
                for (Node node : nodes) {
                    Map<String, Object> map = convertGraphNode(node, languageId, fields);
                    if (null != map && !map.isEmpty()) {
                        list.add(map);
                    }
                }
            }
            Response response = copyResponse(listRes);
            response.put("words", list);
            return response;
        }
    }

    @SuppressWarnings("unchecked")
    private String[] getFields(Request request) {
        Object objFields = request.get(PARAM_FIELDS);
        List<String> fields = getList(mapper, objFields, PARAM_FIELDS);
        if (null != fields && !fields.isEmpty()) {
            String[] arr = new String[fields.size()];
            for (int i=0; i<fields.size(); i++)
                arr[i] = fields.get(i);
            return arr;
        }
        return null;
    }

    private void setLimit(Request request, SearchCriteria sc) {
        Integer limit = null;
        try {
            Object obj = request.get(PARAM_LIMIT);
            if (obj instanceof String)
                limit = Integer.parseInt((String) obj);
            else
                limit = (Integer) request.get(PARAM_LIMIT);
        } catch (Exception e) {
        }
        if (null == limit || limit.intValue() <= 0)
            limit = DEFAULT_LIMIT;
        sc.setResultSize(limit);
    }

    @SuppressWarnings("rawtypes")
    private List getList(ObjectMapper mapper, Object object, String propName) {
        if (null != object) {
            try {
                String strObject = mapper.writeValueAsString(object);
                List list = mapper.readValue(strObject.toString(), List.class);
                return list;
            } catch (Exception e) {
                List<String> list = new ArrayList<String>();
                list.add(object.toString());
                return list;
            }
        }
        return null;
    }

    private Map<String, Object> convertGraphNode(Node node, String languageId, String[] fields) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (null != node) {
            if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
                if (null == fields || fields.length <= 0)
                    map.putAll(node.getMetadata());
                else {
                    for (String field : fields) {
                        if (node.getMetadata().containsKey(field))
                            map.put(field, node.getMetadata().get(field));
                    }
                }
            }
            addRelationsData(node, map);
            if (null != node.getTags() && !node.getTags().isEmpty()) {
                map.put("tags", node.getTags());
            }
            map.put("identifier", node.getIdentifier());
            map.put("language", LanguageMap.getLanguage(languageId));
        }
        return map;
    }

    private void addRelationsData(Node node, Map<String, Object> map) {
        List<Map<String, Object>> synonyms = new ArrayList<Map<String, Object>>();
        List<NodeDTO> antonyms = new ArrayList<NodeDTO>();
        List<NodeDTO> hypernyms = new ArrayList<NodeDTO>();
        List<NodeDTO> hyponyms = new ArrayList<NodeDTO>();
        List<NodeDTO> homonyms = new ArrayList<NodeDTO>();
        List<NodeDTO> meronyms = new ArrayList<NodeDTO>();
        getInRelationsData(node, synonyms, antonyms, hypernyms, hyponyms, homonyms, meronyms);
        getOutRelationsData(node, synonyms, antonyms, hypernyms, hyponyms, homonyms, meronyms);
        if (!synonyms.isEmpty())
            map.put("synonyms", synonyms);
        if (!antonyms.isEmpty())
            map.put("antonyms", antonyms);
        if (!hypernyms.isEmpty())
            map.put("hypernyms", hypernyms);
        if (!hyponyms.isEmpty())
            map.put("hyponyms", hyponyms);
        if (!homonyms.isEmpty())
            map.put("homonyms", homonyms);
        if (!meronyms.isEmpty())
            map.put("meronyms", meronyms);
    }

    private void getInRelationsData(Node node, List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
            List<NodeDTO> hypernyms, List<NodeDTO> hyponyms, List<NodeDTO> homonyms, List<NodeDTO> meronyms) {
        if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
            for (Relation inRel : node.getInRelations()) {
                if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(), inRel.getRelationType())) {
                    if (null != inRel.getStartNodeMetadata() && !inRel.getStartNodeMetadata().isEmpty()) {
                        inRel.getStartNodeMetadata().remove(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
                        inRel.getStartNodeMetadata().remove(SystemProperties.IL_SYS_NODE_TYPE.name());
                        inRel.getStartNodeMetadata().remove(SystemProperties.IL_UNIQUE_ID.name());
                        synonyms.add(inRel.getStartNodeMetadata());
                    }
                } else
                    if (StringUtils.equalsIgnoreCase(RelationTypes.ANTONYM.relationName(), inRel.getRelationType())) {
                    antonyms.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.HYPERNYM.relationName(),
                        inRel.getRelationType())) {
                    hypernyms.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else
                    if (StringUtils.equalsIgnoreCase(RelationTypes.HYPONYM.relationName(), inRel.getRelationType())) {
                    hyponyms.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.HOMONYM.relationName(),
                        inRel.getRelationType())) {
                    homonyms.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else
                    if (StringUtils.equalsIgnoreCase(RelationTypes.MERONYM.relationName(), inRel.getRelationType())) {
                    meronyms.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                }
            }
        }
    }
    
    private String getInRelationWord(Relation inRel) {
        String name = null;
        if (null != inRel.getStartNodeMetadata() && !inRel.getStartNodeMetadata().isEmpty()) {
            if (inRel.getStartNodeMetadata().containsKey(LEMMA_PROPERTY)) {
                name = (String) inRel.getStartNodeMetadata().get(LEMMA_PROPERTY);
            }
        }
        if (StringUtils.isBlank(name))
            name = inRel.getStartNodeName();
        return name;
    }

    private void getOutRelationsData(Node node, List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
            List<NodeDTO> hypernyms, List<NodeDTO> hyponyms, List<NodeDTO> homonyms, List<NodeDTO> meronyms) {
        if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
            for (Relation outRel : node.getOutRelations()) {
                if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(), outRel.getRelationType())) {
                    if (null != outRel.getEndNodeMetadata() && !outRel.getEndNodeMetadata().isEmpty()) {
                        outRel.getEndNodeMetadata().remove(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
                        outRel.getEndNodeMetadata().remove(SystemProperties.IL_SYS_NODE_TYPE.name());
                        outRel.getEndNodeMetadata().remove(SystemProperties.IL_UNIQUE_ID.name());
                        synonyms.add(outRel.getEndNodeMetadata());
                    }
                } else
                    if (StringUtils.equalsIgnoreCase(RelationTypes.ANTONYM.relationName(), outRel.getRelationType())) {
                    antonyms.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.HYPERNYM.relationName(),
                        outRel.getRelationType())) {
                    hypernyms.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else
                    if (StringUtils.equalsIgnoreCase(RelationTypes.HYPONYM.relationName(), outRel.getRelationType())) {
                    hyponyms.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.HOMONYM.relationName(),
                        outRel.getRelationType())) {
                    homonyms.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else
                    if (StringUtils.equalsIgnoreCase(RelationTypes.MERONYM.relationName(), outRel.getRelationType())) {
                    meronyms.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                }
            }
        }
    }
    
    private String getOutRelationWord(Relation outRel) {
        String name = null;
        if (null != outRel.getEndNodeMetadata() && !outRel.getEndNodeMetadata().isEmpty()) {
            if (outRel.getEndNodeMetadata().containsKey(LEMMA_PROPERTY)) {
                name = (String) outRel.getEndNodeMetadata().get(LEMMA_PROPERTY);
            }
        }
        if (StringUtils.isBlank(name))
            name = outRel.getEndNodeName();
        return name;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Node convertToGraphNode(Map<String, Object> map, DefinitionDTO definition) {
        Node node = new Node();
        if (null != map && !map.isEmpty()) {
            Map<String, String> inRelDefMap = new HashMap<String, String>();
            Map<String, String> outRelDefMap = new HashMap<String, String>();
            getRelDefMaps(definition, inRelDefMap, outRelDefMap);
            List<Relation> inRelations = new ArrayList<Relation>();
            List<Relation> outRelations = new ArrayList<Relation>();
            Map<String, Object> metadata = new HashMap<String, Object>();
            for (Entry<String, Object> entry : map.entrySet()) {
                if (StringUtils.equalsIgnoreCase("identifier", entry.getKey())) {
                    node.setIdentifier((String) entry.getValue());
                } else if (StringUtils.equalsIgnoreCase("objectType", entry.getKey())) {
                    node.setObjectType((String) entry.getValue());
                } else if (StringUtils.equalsIgnoreCase("tags", entry.getKey())) {
                    try {
                        String objectStr = mapper.writeValueAsString(entry.getValue());
                        List<String> tags = mapper.readValue(objectStr, List.class);
                        if (null != tags && !tags.isEmpty())
                            node.setTags(tags);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (inRelDefMap.containsKey(entry.getKey())) {
                    try {
                        String objectStr = mapper.writeValueAsString(entry.getValue());
                        List<Map> list = mapper.readValue(objectStr, List.class);
                        if (null != list && !list.isEmpty()) {
                            for (Map obj : list) {
                                NodeDTO dto = (NodeDTO) mapper.convertValue(obj, NodeDTO.class);
                                inRelations
                                        .add(new Relation(dto.getIdentifier(), inRelDefMap.get(entry.getKey()), null));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (outRelDefMap.containsKey(entry.getKey())) {
                    try {
                        String objectStr = mapper.writeValueAsString(entry.getValue());
                        List<Map> list = mapper.readValue(objectStr, List.class);
                        if (null != list && !list.isEmpty()) {
                            for (Map obj : list) {
                                NodeDTO dto = (NodeDTO) mapper.convertValue(obj, NodeDTO.class);
                                outRelations
                                        .add(new Relation(null, outRelDefMap.get(entry.getKey()), dto.getIdentifier()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    metadata.put(entry.getKey(), entry.getValue());
                }
            }
            node.setInRelations(inRelations);
            node.setOutRelations(outRelations);
            node.setMetadata(metadata);
        }
        return node;
    }

    private void getRelDefMaps(DefinitionDTO definition, Map<String, String> inRelDefMap,
            Map<String, String> outRelDefMap) {
        if (null != definition) {
            if (null != definition.getInRelations() && !definition.getInRelations().isEmpty()) {
                for (RelationDefinition rDef : definition.getInRelations()) {
                    if (StringUtils.isNotBlank(rDef.getTitle()) && StringUtils.isNotBlank(rDef.getRelationName())) {
                        inRelDefMap.put(rDef.getTitle(), rDef.getRelationName());
                    }
                }
            }
            if (null != definition.getOutRelations() && !definition.getOutRelations().isEmpty()) {
                for (RelationDefinition rDef : definition.getOutRelations()) {
                    if (StringUtils.isNotBlank(rDef.getTitle()) && StringUtils.isNotBlank(rDef.getRelationName())) {
                        outRelDefMap.put(rDef.getTitle(), rDef.getRelationName());
                    }
                }
            }
        }
    }

}
