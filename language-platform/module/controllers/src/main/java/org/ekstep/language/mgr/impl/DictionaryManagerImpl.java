package org.ekstep.language.mgr.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IDictionaryManager;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.util.IWordnetConstants;
import org.ekstep.language.util.WordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.common.JSONUtils;
import com.ilimi.graph.dac.enums.AuditProperties;
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
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.util.AWSUploader;

import akka.actor.ActorRef;

@Component
public class DictionaryManagerImpl extends BaseManager implements IDictionaryManager, IWordnetConstants {

    private static Logger LOGGER = LogManager.getLogger(IDictionaryManager.class.getName());
    private static final String LEMMA_PROPERTY = "lemma";
    private static final List<String> DEFAULT_STATUS = new ArrayList<String>();
    
    @Autowired
    private WordUtil wordUtil;

    static {
        DEFAULT_STATUS.add("Live");
    }

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public Response upload(File uploadedFile) {
        String bucketName = "ekstep-public";
        String folder = "language_assets";
        if (null == uploadedFile) {
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_UPLOAD_FILE.name(), "Upload file is blank.");
        }
        String[] urlArray = new String[] {};
        try {
            urlArray = AWSUploader.uploadFile(bucketName, folder, uploadedFile);
        } catch (Exception e) {
            throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(),
                    "Error wihile uploading the File.", e);
        }
        String url = urlArray[1];
        Response response = OK("url", url);
        return response;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
	public Response create(String languageId, String objectType, Request request) {
		if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "ObjectType is blank");
		try {
			List<Map> items = (List<Map>) request.get(LanguageParams.words.name());
			List<Node> nodeList = new ArrayList<Node>();
			if (null == items || items.size() <= 0)
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT.name(),
						objectType + " Object is blank");
			try {
				if (null != items && !items.isEmpty()) {
					Request requestDefinition = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER,
							"getNodeDefinition", GraphDACParams.object_type.name(), objectType);
					Response responseDefiniton = getResponse(requestDefinition, LOGGER);
					if (checkError(responseDefiniton)) {
						return responseDefiniton;
					}
					DefinitionDTO definition = (DefinitionDTO) responseDefiniton
							.get(GraphDACParams.definition_node.name());
					for (Map item : items) {
						String lemma = (String) item.get(LanguageParams.lemma.name());
						String language = LanguageMap.getLanguage(languageId).toUpperCase();
						boolean isValid = isValidWord(lemma, language);
						if (!isValid) {
							return ERROR(LanguageErrorCodes.ERR_CREATE_WORD.name(),
									"Lemma cannot be in a different language than " + language,
									ResponseCode.CLIENT_ERROR);
						}
						Node node = convertToGraphNode(languageId, objectType, item, definition);
						nodeList.add(node);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ServerException(LanguageErrorCodes.ERR_CREATE_WORD.name(), e.getMessage());
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
						Map<String, Object> result = res.getResult();
						if (result != null) {
							String nodeId = (String) result.get("node_id");
							if (nodeId != null) {
								lstNodeId.add(nodeId);
							}
						}
					}
					createRes = res;
				}
			}
			if (null == errResponse) {
				createRes.getResult().remove(GraphDACParams.node_id.name());
				createRes.put(GraphDACParams.node_id.name(), lstNodeId);
				return createRes;
			} else {
				errResponse.getResult().remove(GraphDACParams.node_id.name());
				errResponse.put(GraphDACParams.node_id.name(), lstNodeId);
				return errResponse;
			}
		} catch (ClassCastException e) {
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_CONTENT.name(), "Request format incorrect");
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public Response update(String languageId, String id, String objectType, Request request) {
		if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(id))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT_ID.name(), "Object Id is blank");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "ObjectType is blank");
		try {
			Map<String, Object> item = (Map<String, Object>) request.get(objectType.toLowerCase().trim());
			if (null == item)
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT.name(),
						objectType + " Object is blank");
			Request requestDefinition = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
					GraphDACParams.object_type.name(), objectType);
			Response responseDefiniton = getResponse(requestDefinition, LOGGER);
			Node node = null;
			if (checkError(responseDefiniton)) {
				return responseDefiniton;
			} else {
				String lemma = (String) item.get(LanguageParams.lemma.name());
				String language = LanguageMap.getLanguage(languageId).toUpperCase();
				boolean isValid = isValidWord(lemma, language);
				if (!isValid) {
					return ERROR(LanguageErrorCodes.ERR_CREATE_WORD.name(),
							"Lemma cannot be in a different language than " + language,
							ResponseCode.CLIENT_ERROR);
				}
				DefinitionDTO definition = (DefinitionDTO) responseDefiniton.get(GraphDACParams.definition_node.name());
				node = convertToGraphNode(languageId, objectType, item, definition);
				node.setIdentifier(id);
				node.setObjectType(objectType);
			}
			Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
			Response updateRes = getResponse(updateReq, LOGGER);
			return updateRes;
		} catch (ClassCastException e) {
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_CONTENT.name(), "Request format incorrect");
		}
	}

	public boolean isValidWord(String word, String language){
        boolean result = false;
        switch(language){
        case "HINDI":{
        	language = Character.UnicodeBlock.DEVANAGARI.toString();
        	break;
        }
        case "ENGLISH":{
        	language = Character.UnicodeBlock.BASIC_LATIN.toString();
        	break;
        }
        }
	    UnicodeBlock wordBlock = UnicodeBlock.forName(language);
	    for(int i=0; i<word.length(); i++){
	        UnicodeBlock charBlock = UnicodeBlock.of(word.charAt(i));
	        if(wordBlock.equals(charBlock) || (language.equalsIgnoreCase("Hindi") && charBlock.equals(Character.UnicodeBlock.DEVANAGARI_EXTENDED))){
	            result = true;
	            break;
	        }
	    }
	    return result;
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
            Map<String, Object> map = convertGraphNode(node, languageId, "Word", fields, true);
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
                    Map<String, Object> map = convertGraphNode(node, languageId, objectType, fields, false);
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
    
    private CSVFormat csvFileFormat = CSVFormat.DEFAULT;
    private static final String NEW_LINE_SEPARATOR = "\n";
    
    public void findWordsCSV(String languageId, String objectType, InputStream is, OutputStream out) {
        try {
            List<String[]> rows = getCSVRecords(is);
            List<String> words = new ArrayList<String>();
            List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
            if (null != rows && !rows.isEmpty()) {
                for (String[] row : rows) {
                    if (null != row && row.length > 0) {
                        String word = row[0];
                        if (StringUtils.isNotBlank(word) && !words.contains(word.trim())) {
                            word = word.trim();
                            words.add(word);
                            Node node = searchWord(languageId, objectType, word);
                            Map<String, Object> rowMap = new HashMap<String, Object>();
                            if (null == node) {
                                String nodeId = createWord(languageId, word, objectType);
                                rowMap.put("identifier", nodeId);
                                rowMap.put("lemma", word);
                                nodes.add(rowMap);
                            } else {
                                rowMap.put("identifier", node.getIdentifier());
                                rowMap.put("lemma", word);
                                if (null != node.getTags() && !node.getTags().isEmpty())
                                    rowMap.put("tags", node.getTags());
                                getSynsets(node, rowMap, nodes);
                            }
                        }
                    }
                }
            }
            getCSV(nodes, out);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MiddlewareException(LanguageErrorCodes.ERR_SEARCH_ERROR.name(), e.getMessage(), e);
        }
    }
    
    private void getSynsets(Node node, Map<String, Object> rowMap, List<Map<String, Object>> nodes) {
        List<Relation> inRels = node.getInRelations();
        boolean first = true;
        if (null != inRels && !inRels.isEmpty()) {
            for (Relation inRel : inRels) {
                if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(), inRel.getRelationType())
                        && StringUtils.equalsIgnoreCase("Synset", inRel.getStartNodeObjectType())) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.putAll(rowMap);
                    String synsetId = inRel.getStartNodeId();
                    map.put("synsetId", synsetId);
                    Map<String, Object> metadata = inRel.getStartNodeMetadata();
                    if (null != metadata) {
                        if (null != metadata.get("gloss"))
                            map.put("gloss", metadata.get("gloss"));
                        if (null != metadata.get("pos"))
                            map.put("pos", metadata.get("pos"));
                        if (first) {
                            first = false;
                            if (null != node.getMetadata().get("variants"))
                                map.put("variants", node.getMetadata().get("variants"));
                        }
                        if (null != metadata.get("translations")) {
                            String translations = (String) metadata.get("translations");
                            Object obj = JSONUtils.convertJSONString(translations);
                            if (null != obj)
                                map.put("translations", obj);
                            else
                                map.put("translations", translations);
                        }
                    }
                    nodes.add(map);
                }
            }
        }
        if (first)
            nodes.add(rowMap);
    }
    
    private void getCSV(List<Map<String, Object>> nodes, OutputStream out) throws Exception {
        List<String[]> allRows = new ArrayList<String[]>();
        List<String> headers = new ArrayList<String>();
        headers.add("identifier");
        headers.add("lemma");
        for (Map<String, Object> map : nodes) {
            Set<String> keys = map.keySet();
            for (String key : keys) {
                if (!headers.contains(key)) {
                    headers.add(key);
                }
            }
        }
        allRows.add(headers.toArray(new String[headers.size()]));
        for (Map<String, Object> map : nodes) {
            String[] row = new String[headers.size()];
            for (int i=0; i<headers.size(); i++) {
                String header = headers.get(i);
                Object val = map.get(header);
                addToDataRow(val, row, i);
            }
            allRows.add(row);
        }
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(out));
        CSVPrinter writer = new CSVPrinter(bWriter, csvFileFormat);
        writer.printRecords(allRows);
        bWriter.close();
        writer.close();
    }
    
    @SuppressWarnings("rawtypes")
    private void addToDataRow(Object val, String[] row, int i) {
        try {
            if (null != val) {
                if (val instanceof List) {
                    List list = (List) val;
                    String str = "";
                    for (int j=0; j<list.size(); j++) {
                        str += StringEscapeUtils.escapeCsv(list.get(j).toString());
                        if (j < list.size() - 1)
                            str += ",";
                    }
                    row[i] = str;
                } else if (val instanceof Object[]) {
                    Object[] arr = (Object[]) val;
                    String str = "";
                    for (int j=0; j<arr.length; j++) {
                        str += StringEscapeUtils.escapeCsv(arr[j].toString());
                        if (j < arr.length - 1)
                            str += ",";
                    }
                    row[i] = str;
                } else {
                    Object strObject = mapper.convertValue(val, Object.class);
                    row[i] = strObject.toString();
                }
            } else {
                row[i] = "";
            }
        } catch(Exception e) {
            row[i] = "";
        }
    }
    
    @SuppressWarnings("unchecked")
    private Node searchWord(String languageId, String objectType, String lemma) {
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new Filter("lemma", SearchConditions.OP_EQUAL, lemma));
        MetadataCriterion mc = MetadataCriterion.create(filters);
        sc.addMetadata(mc);
        sc.setResultSize(1);
        Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
        request.put(GraphDACParams.get_tags.name(), true);
        Response findRes = getResponse(request, LOGGER);
        if (checkError(findRes))
            return null;
        else {
            List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
            if (null != nodes && nodes.size() > 0)
                return nodes.get(0);
        }
        return null;
    }
    
    private List<String[]> getCSVRecords(InputStream is) throws Exception {
        InputStreamReader isReader = new InputStreamReader(is);
        CSVParser csvReader = new CSVParser(isReader, csvFileFormat);
        List<String[]> rows = new ArrayList<String[]>();
        List<CSVRecord> recordsList = csvReader.getRecords();
        if (null != recordsList && !recordsList.isEmpty()) {
            for (int i = 0; i < recordsList.size(); i++) {
                CSVRecord record = recordsList.get(i);
                String[] arr = new String[record.size()];
                for (int j = 0; j < record.size(); j++) {
                    String val = record.get(j);
                    arr[j] = val;
                }
                rows.add(arr);
            }
        }
        isReader.close();
        csvReader.close();
        return rows;
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
    public Response relatedObjects(String languageId, String objectType, String objectId, String relation,
            String[] fields, String[] relations) {
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
            Map<String, Object> map = convertGraphNode(node, languageId, objectType, fields, false);
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
                            } catch (Exception e) {
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

    @Override
    public Response translation(String languageId, String[] words, String[] languages) {
        if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
        if (null == words || words.length <= 0)
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "Word list is empty");
        if (null == languages || languages.length <= 0)
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT_ID.name(), "language list is empty");
        Map<String, Object> map = new HashMap<String, Object>();
        Response response = null;
        for (String word : words) {
            Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
                    GraphDACParams.node_id.name(), word);
            request.put(GraphDACParams.get_tags.name(), true);
            Response getNodeRes = getResponse(request, LOGGER);
            if (checkError(getNodeRes)) {
                return getNodeRes;
            } else {
            	response = copyResponse(getNodeRes);
                Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
                map.put(word, node.getMetadata().get("translations"));
                response.put("translations", map);
            }
        }
        return response;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
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
            statusList = getList(mapper, statusParam, true);
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
                    Object val = entry.getValue();
                    List list = getList(mapper, val, false);
                    if (null != list && !list.isEmpty()) {
                        if (list.size() > 1)
                            filters.add(new Filter(entry.getKey(), SearchConditions.OP_IN, list));
                        else {
                            Object value = list.get(0);
                            if (value instanceof String)
                                filters.add(new Filter(entry.getKey(), SearchConditions.OP_LIKE, value.toString()));
                            else
                                filters.add(new Filter(entry.getKey(), SearchConditions.OP_EQUAL, value));
                        }
                    } else if (null != val && StringUtils.isNotBlank(val.toString())) {
                        if (val instanceof String)
                            filters.add(new Filter(entry.getKey(), SearchConditions.OP_LIKE, val.toString()));
                        else
                            filters.add(new Filter(entry.getKey(), SearchConditions.OP_EQUAL, val));
                    }
                } else if (StringUtils.equalsIgnoreCase(PARAM_TAGS, entry.getKey())) {
                    Object val = entry.getValue();
                    List<String> tags = getList(mapper, val, true);
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
                    Map<String, Object> map = convertGraphNode(node, languageId, objectType, fields, false);
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
        List<String> fields = getList(mapper, objFields, true);
        if (null != fields && !fields.isEmpty()) {
            String[] arr = new String[fields.size()];
            for (int i = 0; i < fields.size(); i++)
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
    private List getList(ObjectMapper mapper, Object object, boolean returnList) {
        if (null != object && StringUtils.isNotBlank(object.toString())) {
            try {
                String strObject = mapper.writeValueAsString(object);
                List list = mapper.readValue(strObject.toString(), List.class);
                return list;
            } catch (Exception e) {
                if (returnList) {
                    List<String> list = new ArrayList<String>();
                    list.add(object.toString());
                    return list;
                }
            }
        }
        return null;
    }

    private Map<String, Object> convertGraphNode(Node node, String languageId, String objectType, String[] fields,
            boolean synsetMembers) {
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
            addRelationsData(languageId, objectType, node, map, synsetMembers);
            if (null != node.getTags() && !node.getTags().isEmpty()) {
                map.put("tags", node.getTags());
            }
            map.put("identifier", node.getIdentifier());
            map.put("language", LanguageMap.getLanguage(languageId));
        }
        return map;
    }

    private void addRelationsData(String languageId, String objectType, Node node, Map<String, Object> map,
            boolean synsetMembers) {
        List<String> synsetIds = new ArrayList<String>();
        List<Map<String, Object>> synonyms = new ArrayList<Map<String, Object>>();
        List<NodeDTO> antonyms = new ArrayList<NodeDTO>();
        List<NodeDTO> hypernyms = new ArrayList<NodeDTO>();
        List<NodeDTO> hyponyms = new ArrayList<NodeDTO>();
        List<NodeDTO> homonyms = new ArrayList<NodeDTO>();
        List<NodeDTO> meronyms = new ArrayList<NodeDTO>();
        getInRelationsData(node, synonyms, antonyms, hypernyms, hyponyms, homonyms, meronyms, synsetIds);
        getOutRelationsData(node, synonyms, antonyms, hypernyms, hyponyms, homonyms, meronyms, synsetIds);
        if (!synonyms.isEmpty()) {
            if (synsetMembers) {
                Map<String, List<String>> synMap = getSynonymMap(languageId, objectType, synsetIds);
                for (Map<String, Object> synonym : synonyms) {
                    String id = (String) synonym.get("identifier");
                    if (StringUtils.isNotBlank(id)) {
                        List<String> words = synMap.get(id);
                        if (null != words && !words.isEmpty())
                            synonym.put("words", words);
                    }
                }
            }
            map.put("synonyms", synonyms);
        }
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

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> getSynonymMap(String languageId, String objectType, List<String> nodeIds) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        if (null != nodeIds && !nodeIds.isEmpty()) {
            Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                    GraphDACParams.node_ids.name(), nodeIds);
            Response res = getResponse(req, LOGGER);
            if (!checkError(res)) {
                List<Node> nodes = (List<Node>) res.get(GraphDACParams.node_list.name());
                if (null != nodes && !nodes.isEmpty()) {
                    for (Node synset : nodes) {
                        List<Relation> outRels = synset.getOutRelations();
                        if (null != outRels && !outRels.isEmpty()) {
                            List<String> words = new ArrayList<String>();
                            for (Relation rel : outRels) {
                                if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(),
                                        rel.getRelationType())
                                        && StringUtils.equalsIgnoreCase(objectType, rel.getEndNodeObjectType())) {
                                    String word = getOutRelationWord(rel);
                                    words.add(word);
                                }
                            }
                            if (!words.isEmpty())
                                map.put(synset.getIdentifier(), words);
                        }
                    }
                }
            }
        }
        return map;
    }

    private void getInRelationsData(Node node, List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
            List<NodeDTO> hypernyms, List<NodeDTO> hyponyms, List<NodeDTO> homonyms, List<NodeDTO> meronyms,
            List<String> synsetIds) {
        if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
            for (Relation inRel : node.getInRelations()) {
                if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(), inRel.getRelationType())) {
                    if (null != inRel.getStartNodeMetadata() && !inRel.getStartNodeMetadata().isEmpty()) {
                        String identifier = (String) inRel.getStartNodeMetadata()
                                .get(SystemProperties.IL_UNIQUE_ID.name());
                        if (!synsetIds.contains(identifier))
                            synsetIds.add(identifier);
                        inRel.getStartNodeMetadata().remove(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
                        inRel.getStartNodeMetadata().remove(SystemProperties.IL_SYS_NODE_TYPE.name());
                        inRel.getStartNodeMetadata().remove(SystemProperties.IL_UNIQUE_ID.name());
                        inRel.getStartNodeMetadata().remove(AuditProperties.createdOn.name());
                        inRel.getStartNodeMetadata().remove(AuditProperties.lastUpdatedOn.name());
                        inRel.getStartNodeMetadata().put("identifier", identifier);
                        synonyms.add(inRel.getStartNodeMetadata());
                    }
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.ANTONYM.relationName(),
                        inRel.getRelationType())) {
                    antonyms.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.HYPERNYM.relationName(),
                        inRel.getRelationType())) {
                    hypernyms.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.HYPONYM.relationName(),
                        inRel.getRelationType())) {
                    hyponyms.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.HOLONYM.relationName(),
                        inRel.getRelationType())) {
                    homonyms.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.MERONYM.relationName(),
                        inRel.getRelationType())) {
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
            List<NodeDTO> hypernyms, List<NodeDTO> hyponyms, List<NodeDTO> homonyms, List<NodeDTO> meronyms,
            List<String> synsetIds) {
        if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
            for (Relation outRel : node.getOutRelations()) {
                if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(), outRel.getRelationType())) {
                    if (null != outRel.getEndNodeMetadata() && !outRel.getEndNodeMetadata().isEmpty()) {
                        String identifier = (String) outRel.getEndNodeMetadata()
                                .get(SystemProperties.IL_UNIQUE_ID.name());
                        if (!synsetIds.contains(identifier))
                            synsetIds.add(identifier);
                        outRel.getEndNodeMetadata().remove(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
                        outRel.getEndNodeMetadata().remove(SystemProperties.IL_SYS_NODE_TYPE.name());
                        outRel.getEndNodeMetadata().remove(SystemProperties.IL_UNIQUE_ID.name());
                        outRel.getEndNodeMetadata().remove(AuditProperties.createdOn.name());
                        outRel.getEndNodeMetadata().remove(AuditProperties.lastUpdatedOn.name());
                        outRel.getEndNodeMetadata().put("identifier", identifier);
                        synonyms.add(outRel.getEndNodeMetadata());
                    }
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.ANTONYM.relationName(),
                        outRel.getRelationType())) {
                    antonyms.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.HYPERNYM.relationName(),
                        outRel.getRelationType())) {
                    hypernyms.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.HYPONYM.relationName(),
                        outRel.getRelationType())) {
                    hyponyms.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.HOLONYM.relationName(),
                        outRel.getRelationType())) {
                    homonyms.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.MERONYM.relationName(),
                        outRel.getRelationType())) {
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
    private Node convertToGraphNode(String languageId, String objectType, Map<String, Object> map,
            DefinitionDTO definition) {
        Node node = new Node();
        if (null != map && !map.isEmpty()) {
            Map<String, String> lemmaIdMap = new HashMap<String, String>();
            Map<String, String> inRelDefMap = new HashMap<String, String>();
            Map<String, String> outRelDefMap = new HashMap<String, String>();
            getRelDefMaps(definition, inRelDefMap, outRelDefMap);
            List<Relation> inRelations = null;
            List<Relation> outRelations = null;
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
                } else if (StringUtils.equalsIgnoreCase("synonyms", entry.getKey())) {
                    try {
                        String objectStr = mapper.writeValueAsString(entry.getValue());
                        List<Map> list = mapper.readValue(objectStr, List.class);
                        if (null != list && !list.isEmpty()) {
                            Set<String> words = new HashSet<String>();
                            Map<String, List<String>> synsetWordMap = new HashMap<String, List<String>>();
                            for (Map<String, Object> obj : list) {
                                String synsetId = (String) obj.get("identifier");
                                List<String> wordList = (List<String>) obj.get("words");
                                Node synset = new Node(synsetId, SystemNodeTypes.DATA_NODE.name(), "Synset");
                                obj.remove("identifier");
                                obj.remove("words");
                                synset.setMetadata(obj);
                                Response res = null;
                                if (StringUtils.isBlank(synsetId)) {
                                    Request req = getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
                                            "createDataNode");
                                    req.put(GraphDACParams.node.name(), synset);
                                    res = getResponse(req, LOGGER);
                                } else {
                                    Request req = getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
                                            "updateDataNode");
                                    req.put(GraphDACParams.node_id.name(), synsetId);
                                    req.put(GraphDACParams.node.name(), synset);
                                    res = getResponse(req, LOGGER);
                                }
                                if (checkError(res)) {
                                    throw new ServerException(LanguageErrorCodes.ERR_CREATE_SYNONYM.name(),
                                            getErrorMessage(res));
                                } else {
                                    synsetId = (String) res.get(GraphDACParams.node_id.name());
                                }
                                if (null != wordList && !wordList.isEmpty()) {
                                    words.addAll(wordList);
                                }
                                if (StringUtils.isNotBlank(synsetId))
                                    synsetWordMap.put(synsetId, wordList);
                            }
                            getWordIdMap(lemmaIdMap, languageId, objectType, words);
                            for (Entry<String, List<String>> synset : synsetWordMap.entrySet()) {
                                List<String> wordList = synset.getValue();
                                String synsetId = synset.getKey();
                                if (null != wordList && !wordList.isEmpty()) {
                                    List<Relation> outRels = new ArrayList<Relation>();
                                    for (String word : wordList) {
                                        if (lemmaIdMap.containsKey(word)) {
                                            String wordId = lemmaIdMap.get(word);
                                            outRels.add(new Relation(null, RelationTypes.SYNONYM.relationName(),
                                                    wordId));
                                        } else {
                                            String wordId = createWord(lemmaIdMap, languageId, word, objectType);
                                            outRels.add(new Relation(null, RelationTypes.SYNONYM.relationName(),
                                                    wordId));
                                        }
                                    }
                                    Node synsetNode = new Node(synsetId, SystemNodeTypes.DATA_NODE.name(), "Synset");
                                    synsetNode.setOutRelations(outRels);
                                    Request req = getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
                                            "updateDataNode");
                                    req.put(GraphDACParams.node_id.name(), synsetId);
                                    req.put(GraphDACParams.node.name(), synsetNode);
                                    Response res = getResponse(req, LOGGER);
                                    if (checkError(res))
                                        throw new ServerException(LanguageErrorCodes.ERR_CREATE_SYNONYM.name(),
                                                getErrorMessage(res));
                                    else {
                                        if (null == inRelations)
                                            inRelations = new ArrayList<Relation>();
                                        inRelations.add(
                                                new Relation(synsetId, RelationTypes.SYNONYM.relationName(), null));
                                    }
                                } else {
                                    if (null == inRelations)
                                        inRelations = new ArrayList<Relation>();
                                    inRelations.add(new Relation(synsetId, RelationTypes.SYNONYM.relationName(), null));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new ServerException(LanguageErrorCodes.ERR_CREATE_SYNONYM.name(), e.getMessage(), e);
                    }
                } else if (inRelDefMap.containsKey(entry.getKey())) {
                    try {
                        String objectStr = mapper.writeValueAsString(entry.getValue());
                        List<Map> list = mapper.readValue(objectStr, List.class);
                        if (null != list && !list.isEmpty()) {
                            Set<String> words = new HashSet<String>();
                            for (Map obj : list) {
                                String wordId = (String) obj.get("identifier");
                                if (StringUtils.isBlank(wordId)) {
                                    String word = (String) obj.get("name");
                                    if (lemmaIdMap.containsKey(word)) {
                                        wordId = lemmaIdMap.get(word);
                                        if (null == inRelations)
                                            inRelations = new ArrayList<Relation>();
                                        inRelations.add(new Relation(wordId, inRelDefMap.get(entry.getKey()), null));
                                    } else {
                                        words.add(word);
                                    }
                                } else {
                                    if (null == inRelations)
                                        inRelations = new ArrayList<Relation>();
                                    inRelations.add(new Relation(wordId, inRelDefMap.get(entry.getKey()), null));
                                }
                            }
                            if (null != words && !words.isEmpty()) {
                                getWordIdMap(lemmaIdMap, languageId, objectType, words);
                                for (String word : words) {
                                    if (lemmaIdMap.containsKey(word)) {
                                        String wordId = lemmaIdMap.get(word);
                                        if (null == inRelations)
                                            inRelations = new ArrayList<Relation>();
                                        inRelations.add(new Relation(wordId, inRelDefMap.get(entry.getKey()), null));
                                    } else {
                                        String wordId = createWord(lemmaIdMap, languageId, word, objectType);
                                        if (null == inRelations)
                                            inRelations = new ArrayList<Relation>();
                                        inRelations.add(new Relation(wordId, inRelDefMap.get(entry.getKey()), null));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new ServerException(LanguageErrorCodes.ERR_CREATE_WORD.name(), e.getMessage(), e);
                    }
                } else if (outRelDefMap.containsKey(entry.getKey())) {
                    try {
                        String objectStr = mapper.writeValueAsString(entry.getValue());
                        List<Map> list = mapper.readValue(objectStr, List.class);
                        if (null != list && !list.isEmpty()) {
                            Set<String> words = new HashSet<String>();
                            for (Map obj : list) {
                                String wordId = (String) obj.get("identifier");
                                if (StringUtils.isBlank(wordId)) {
                                    String word = (String) obj.get("name");
                                    if (lemmaIdMap.containsKey(word)) {
                                        wordId = lemmaIdMap.get(word);
                                        if (null == outRelations)
                                            outRelations = new ArrayList<Relation>();
                                        outRelations.add(new Relation(null, outRelDefMap.get(entry.getKey()), wordId));
                                    } else {
                                        words.add(word);
                                    }
                                } else {
                                    if (null == outRelations)
                                        outRelations = new ArrayList<Relation>();
                                    outRelations.add(new Relation(null, outRelDefMap.get(entry.getKey()), wordId));
                                }
                            }
                            if (null != words && !words.isEmpty()) {
                                getWordIdMap(lemmaIdMap, languageId, objectType, words);
                                for (String word : words) {
                                    if (lemmaIdMap.containsKey(word)) {
                                        String wordId = lemmaIdMap.get(word);
                                        if (null == outRelations)
                                            outRelations = new ArrayList<Relation>();
                                        outRelations.add(new Relation(null, outRelDefMap.get(entry.getKey()), wordId));
                                    } else {
                                        String wordId = createWord(lemmaIdMap, languageId, word, objectType);
                                        if (null == outRelations)
                                            outRelations = new ArrayList<Relation>();
                                        outRelations.add(new Relation(null, outRelDefMap.get(entry.getKey()), wordId));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new ServerException(LanguageErrorCodes.ERR_CREATE_WORD.name(), e.getMessage(), e);
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

    private String createWord(Map<String, String> lemmaIdMap, String languageId, String word, String objectType) {
        String nodeId = createWord(languageId, word, objectType);
        lemmaIdMap.put(word, nodeId);
        return nodeId;
    }
    
    private String createWord(String languageId, String word, String objectType) {
        Node node = new Node(null, SystemNodeTypes.DATA_NODE.name(), objectType);
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(LEMMA_PROPERTY, word);
        node.setMetadata(metadata);
        Request req = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
        req.put(GraphDACParams.node.name(), node);
        Response res = getResponse(req, LOGGER);
        if (checkError(res)) {
            throw new ServerException(LanguageErrorCodes.ERR_CREATE_WORD.name(), getErrorMessage(res));
        }
        String nodeId = (String) res.get(GraphDACParams.node_id.name());
        return nodeId;
    }

    @SuppressWarnings("unchecked")
    private void getWordIdMap(Map<String, String> lemmaIdMap, String languageId, String objectType, Set<String> words) {
        if (null != words && !words.isEmpty()) {
            List<String> wordList = new ArrayList<String>();
            for (String word : words) {
                if (!lemmaIdMap.containsKey(word))
                    wordList.add(word);
            }
            if (null != wordList && !wordList.isEmpty()) {
                SearchCriteria sc = new SearchCriteria();
                sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
                sc.setObjectType(objectType);
                List<Filter> filters = new ArrayList<Filter>();
                filters.add(new Filter(LEMMA_PROPERTY, SearchConditions.OP_IN, words));
                MetadataCriterion mc = MetadataCriterion.create(filters);
                sc.addMetadata(mc);

                Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                        GraphDACParams.search_criteria.name(), sc);
                Response listRes = getResponse(req, LOGGER);
                if (checkError(listRes))
                    throw new ServerException(LanguageErrorCodes.ERR_SEARCH_ERROR.name(), getErrorMessage(listRes));
                else {
                    List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
                    if (null != nodes && !nodes.isEmpty()) {
                        for (Node node : nodes) {
                            if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
                                String lemma = (String) node.getMetadata().get(LEMMA_PROPERTY);
                                if (StringUtils.isNotBlank(lemma))
                                    lemmaIdMap.put(lemma, node.getIdentifier());
                            }
                        }
                    }
                }
            }
        }
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
    
    private boolean getBooleanValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            if (StringUtils.equalsIgnoreCase("yes", value.trim().toLowerCase())
                    || StringUtils.equalsIgnoreCase("true", value.trim().toLowerCase()))
                return true;
        }
        return false;
    }

	@Override
	public Response importWordSynset(String languageId, InputStream inputStream) throws Exception {
		Response importResponse = OK();
		String delimiter = "::";
		List<Map<String, String>> records = readFromCSV(inputStream);
		Map<Integer, String> errorMessageMap = new HashMap<Integer, String>();
		Map<Integer, String> messageMap = new HashMap<Integer, String>();
		int rowNo = 0;
		Map<String, String> wordIdMap = new HashMap<String, String>();
		Map<String, String> synsetIdMap = new HashMap<String, String>();
		List<String> wordNodeIds = new ArrayList<String>();
		DefinitionDTO wordDefintion = getDefintiion(languageId, LanguageParams.Word.name());
		DefinitionDTO defintion = getDefintiion(languageId, LanguageParams.Synset.name());
		for (Map<String, String> csvRecord : records) {
			rowNo++;
			Map<String, Object> synsetMap = new HashMap<String, Object>();
			Map<String, Object> wordMap = new HashMap<String, Object>();
			Map<String, Boolean> booleanProps = new HashMap<String, Boolean>();
			String wordId = null;
			String lemma = null;
			List<String> exampleSentences = null;
			String synsetId = null;
			List<String> pronunciations = null;
			List<String> tagList = null;
			boolean primaryMeaning = false;
			for (Map.Entry<String, String> entry : csvRecord.entrySet()) {
				switch (entry.getKey()) {
				case "identifier": {
					wordId = entry.getValue();
					break;
				}
				case ATTRIB_LEMMA: {
					lemma = entry.getValue();
					break;
				}
				case "tags": {
				    tagList = new ArrayList<String>();
                    if (StringUtils.isNotBlank(entry.getValue())) {
                        String[] arr = entry.getValue().split(",");
                        tagList = Arrays.asList(arr);
                    }
					break;
				}
				case ATTRIB_PICTURES: {
		            if (StringUtils.isNotBlank(entry.getValue())) {
		                String[] arr = entry.getValue().split(",");
		                synsetMap.put(ATTRIB_PICTURES, Arrays.asList(arr));
		            }
				}
				case ATTRIB_PRONUNCIATIONS: {
				    pronunciations = new ArrayList<String>();
				    if (StringUtils.isNotBlank(entry.getValue())) {
                        String[] arr = entry.getValue().split(",");
                        pronunciations = Arrays.asList(arr);
                    }
				}
				case ATTRIB_EXAMPLE_SENTENCES: {
				    exampleSentences = new ArrayList<String>();
                    if (StringUtils.isNotBlank(entry.getValue())) {
                        String[] arr = entry.getValue().split(delimiter);
                        exampleSentences = Arrays.asList(arr);
                    }
                    break;
                }
				case ATTRIB_HAS_CONNOTATIVE: 
                case ATTRIB_HAS_CONNOTATIVE_MEANING:{
                    booleanProps.put(ATTRIB_HAS_CONNOTATIVE, getBooleanValue(entry.getValue()));
                    break;
                }
				case ATTRIB_IS_PHRASE: 
				case ATTRIB_IS_LOAN_WORD: {
				    booleanProps.put(entry.getKey(), getBooleanValue(entry.getValue()));
                    break;
                }
				case "synsetId": {
					synsetId = entry.getValue();
					break;
				}
				case "primaryMeaning": {
					primaryMeaning = getBooleanValue(entry.getValue());
					break;
				}
				case ATTRIB_POS: {
				    if (StringUtils.isNotBlank(entry.getValue()))
                        synsetMap.put(entry.getKey(), entry.getValue().trim().toLowerCase());
                    break;
                }
				default: {
					if (StringUtils.isNotBlank(entry.getValue()))
						synsetMap.put(entry.getKey(), entry.getValue());
					break;
				}
				}
			}
			if (lemma == null || lemma.isEmpty()) {
				addMessage(rowNo, "lemma is mandatory", errorMessageMap);
				continue;
			}
			String language = LanguageMap.getLanguage(languageId).toUpperCase();
			boolean isValid = isValidWord(lemma, language);
			if (!isValid) {
				addMessage(rowNo, "Lemma cannot be in a different language than " + language, errorMessageMap);
				continue;
			}
			String gloss = (String) synsetMap.get(LanguageParams.gloss.name());
			if (gloss == null || gloss.isEmpty()) {
				addMessage(rowNo, "Gloss is mandatory", errorMessageMap);
				continue;
			}
			// create or update Synset
			if (synsetId == null || synsetId.isEmpty())
				synsetId = synsetIdMap.get(gloss);
			if (synsetId != null && !synsetId.isEmpty())
				synsetMap.put(LanguageParams.identifier.name(), synsetId);
			Node synsetNode = convertToGraphNode(languageId, LanguageParams.Synset.name(), synsetMap, defintion);
			synsetNode.setObjectType(LanguageParams.Synset.name());
			if (null != tagList && !tagList.isEmpty())
			    synsetNode.setTags(tagList);
			Response synsetResponse = createOrUpdateNode(languageId, synsetNode);
			if (checkError(synsetResponse)) {
				addMessage(rowNo, wordUtil.getErrorMessage(synsetResponse), errorMessageMap);
				continue;
			} else {
				synsetId = (String) synsetResponse.get(GraphDACParams.node_id.name());
				addMessage(rowNo, "Synset Id: " + synsetId, messageMap);
			}
			synsetIdMap.put(gloss, synsetId);
			// create or update Word
			if (wordId != null && !wordId.isEmpty()) {
				wordMap.put(LanguageParams.identifier.name(), wordId);
			}
			wordMap.put(LanguageParams.lemma.name(), lemma);
			String existingWordId = wordIdMap.get(lemma);
			if (existingWordId == null) {
				Node tempWordNode = wordUtil.searchWord(languageId, lemma);
				if (tempWordNode != null) {
					existingWordId = tempWordNode.getIdentifier();
					wordIdMap.put(lemma, existingWordId);
				}
			}
			if (wordId != null && !wordId.isEmpty()) {
				if (existingWordId != null && !existingWordId.equalsIgnoreCase(wordId)) {
					addMessage(rowNo, "Lemma already exists with node Id: " + existingWordId, errorMessageMap);
					continue;
				}
			} else {
				// wordId not present in csv
				wordId = existingWordId;
			}
			//wordId not present in map and DB
			if (wordId == null || wordId.isEmpty()) {
				// create Word
				Node wordNode = convertToGraphNode(languageId, LanguageParams.Word.name(), wordMap, wordDefintion);
				wordNode.setObjectType(LanguageParams.Word.name());
				Response wordResponse = createOrUpdateNode(languageId, wordNode);
				if (checkError(wordResponse)) {
					addMessage(rowNo, wordUtil.getErrorMessage(wordResponse), errorMessageMap);
					continue;
				}
				wordId = (String) wordResponse.get(GraphDACParams.node_id.name());
				addMessage(rowNo, "Word Id: " + wordId, messageMap);
				if (!wordNodeIds.contains(wordId))
					wordNodeIds.add(wordId);
				wordIdMap.put(lemma, wordId);
			}
			if (primaryMeaning) {
				wordMap.put(LanguageParams.primaryMeaningId.name(), synsetId);
				wordMap.putAll(booleanProps);
				if (null != pronunciations && !pronunciations.isEmpty())
				    wordMap.put(ATTRIB_PRONUNCIATIONS, pronunciations);
				if (null != exampleSentences && !exampleSentences.isEmpty())
				    wordMap.put(ATTRIB_EXAMPLE_SENTENCES, exampleSentences);
				Node wordNode = convertToGraphNode(languageId, LanguageParams.Word.name(), wordMap, wordDefintion);
				wordNode.setObjectType(LanguageParams.Word.name());
				wordNode.setTags(tagList);
				if (wordId != null && !wordId.isEmpty())
					wordNode.setIdentifier(wordId);
				Response wordResponse = createOrUpdateNode(languageId, wordNode);
				if (checkError(wordResponse)) {
					addMessage(rowNo, wordUtil.getErrorMessage(wordResponse), errorMessageMap);
					continue;
				}
				wordId = (String) wordResponse.get(GraphDACParams.node_id.name());
				addMessage(rowNo, "Word Id: " + wordId, messageMap);
				wordIdMap.put(lemma, wordId);
				if (!wordNodeIds.contains(wordId))
					wordNodeIds.add(wordId);
			}
			if (wordId != null && synsetId != null) {
				Response relationResponse = createRelation(wordId, RelationTypes.SYNONYM.relationName(), languageId,
						synsetId);
				if (checkError(relationResponse)) {
					addMessage(rowNo, wordUtil.getErrorMessage(relationResponse), errorMessageMap);
				} else {
					if (relationResponse.get("messages") != null)
						addMessage(rowNo, wordUtil.getErrorMessage(relationResponse), errorMessageMap);
				}
			}
		}
		if (!wordNodeIds.isEmpty())
			asyncUpdate(wordNodeIds, languageId);
		if (!errorMessageMap.isEmpty())
			importResponse.put(LanguageParams.errorMessages.name(), errorMessageMap);
		if (!messageMap.isEmpty()) 
			importResponse.put(LanguageParams.messages.name(), messageMap);
		return importResponse;
	}
	
	private Response createOrUpdateNode(String languageId, Node node) {
		Response reponse;
		if(node.getIdentifier() == null){
			reponse = createDataNode(languageId, node);
		}
		else{
			reponse = updateDataNode(languageId, node);
		}
		return reponse;
	}

	private Response createDataNode(String languageId, Node synsetNode) {
		Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		updateReq.put(GraphDACParams.node.name(), synsetNode);
		Response updateRes = getResponse(updateReq, LOGGER);
		return updateRes;
	}

	private void addMessage(int rowNo, String error, Map<Integer, String> errorMessageMap) {
		String errorMessage = errorMessageMap.get(rowNo);
		if(errorMessage == null){
			errorMessage = error;
		}
		else{
			errorMessage = errorMessage + ". " + error;
		}
		errorMessageMap.put(rowNo, errorMessage);
	}

	private Response createRelation(String endNodeId, String relationType, String languageId, String startNodeId) {
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
		request.put(GraphDACParams.start_node_id.name(), startNodeId);
		request.put(GraphDACParams.relation_type.name(), relationType);
		request.put(GraphDACParams.end_node_id.name(), endNodeId);
		Response response = getResponse(request, LOGGER);
		return response;
	}

	private Response updateDataNode(String languageId, Node synsetNode) {
		Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		updateReq.put(GraphDACParams.node.name(), synsetNode);
		updateReq.put(GraphDACParams.node_id.name(), synsetNode.getIdentifier());
		Response updateRes = getResponse(updateReq, LOGGER);
		return updateRes;
		
	}

	private DefinitionDTO getDefintiion(String languageId, String objectType) {
		Request requestDefinition = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response responseDefiniton = getResponse(requestDefinition, LOGGER);
		if (checkError(responseDefiniton)) {
			throw new ServerException(LanguageErrorCodes.ERR_SEARCH_ERROR.name(), getErrorMessage(responseDefiniton));
		}
		DefinitionDTO definition = (DefinitionDTO) responseDefiniton.get(GraphDACParams.definition_node.name());
		return definition;

	}

	private List<Map<String,String>> readFromCSV(InputStream inputStream) throws JsonProcessingException, IOException {
		List<Map<String,String>> records =  new ArrayList<Map<String,String>>();
		 InputStreamReader isReader = new InputStreamReader(inputStream, "UTF8");
	        CSVParser csvReader = new CSVParser(isReader, csvFileFormat);
	        List<String> headers = new ArrayList<String>();
	        List<CSVRecord> recordsList = csvReader.getRecords();
	        if (null != recordsList && !recordsList.isEmpty()) {
	            for (int i = 0; i < recordsList.size(); i++) {
	            	if(i == 0){
	            		CSVRecord headerecord = recordsList.get(i);
	            		 for (int j = 0; j < headerecord.size(); j++) {
	 	                    String val = headerecord.get(j);
	 	                   headers.add(val);
	 	                }
	            	}
	            	else{
	            		Map<String,String> csvRecord = new HashMap<String,String>();
		                CSVRecord record = recordsList.get(i);
		                for (int j = 0; j < record.size(); j++) {
		                    String val = record.get(j);
		                    csvRecord.put(headers.get(j), val);
		                }
		                records.add(csvRecord);
	            	}
	            }
	        }
	        isReader.close();
	        csvReader.close();
	        return records;
	}
	
	private void asyncUpdate(List<String> nodeIds, String languageId) {
	    Map<String, Object> map = new HashMap<String, Object>();
        map = new HashMap<String, Object>();
        map.put(LanguageParams.node_ids.name(), nodeIds);
        Request request = new Request();
        request.setRequest(map);
        request.setManagerName(LanguageActorNames.ENRICH_ACTOR.name());
        request.setOperation(LanguageOperations.enrichWords.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        makeLanguageAsyncRequest(request, LOGGER);
	}
	
	public void makeLanguageAsyncRequest(Request request, Logger logger) {
        ActorRef router = LanguageRequestRouterPool.getRequestRouter();
        try {
            router.tell(request, router);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }
	
}
