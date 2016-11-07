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
import java.util.Iterator;
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
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IDictionaryManager;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.util.BaseLanguageManager;
import org.ekstep.language.util.IWordnetConstants;
import org.ekstep.language.util.LogWordEventUtil;
import org.ekstep.language.util.WordCacheUtil;
import org.ekstep.language.util.WordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.CoverageIgnore;
import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.ConvertGraphNode;
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

import akka.actor.ActorRef;

/**
 * Provides implementation for word, synsets and relations manipulations
 * @author Amarnath, Rayulu, Azhar 
 */
@Component
public class DictionaryManagerImpl extends BaseLanguageManager implements IDictionaryManager, IWordnetConstants {

	/** The logger. */
    private static Logger LOGGER = LogManager.getLogger(IDictionaryManager.class.getName());
    
    /** The Constant LEMMA_PROPERTY. */
    private static final String LEMMA_PROPERTY = "lemma";
    
    /** The Constant DEFAULT_STATUS. */
    private static final List<String> DEFAULT_STATUS = new ArrayList<String>();
    
    private static final String s3Media = "s3.media.folder";
	
    /** The word util. */
    @Autowired
    private WordUtil wordUtil;
    
    static {
        DEFAULT_STATUS.add("Live");
    }

    /** The mapper. */
    private ObjectMapper mapper = new ObjectMapper();

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IDictionaryManager#upload(java.io.File)
	 */
    @Override
    public Response upload(File uploadedFile) {
        if (null == uploadedFile) {
            throw new ClientException(LanguageErrorCodes.ERR_INVALID_UPLOAD_FILE.name(), "Upload file is blank.");
        }
        String[] urlArray = new String[] {};
        try {
        	String folder = S3PropertyReader.getProperty(s3Media);
            urlArray = AWSUploader.uploadFile(folder, uploadedFile);
        } catch (Exception e) {
            throw new ServerException(LanguageErrorCodes.ERR_MEDIA_UPLOAD_FILE.name(),
                    "Error wihile uploading the File.", e);
        }
        String url = urlArray[1];
        Response response = OK("url", url);
        return response;
    }

	
	/**
	 * Checks if is valid word.
	 *
	 * @param word
	 *            the word
	 * @param language
	 *            the language
	 * @return true, if is valid word
	 */
	public boolean isValidWord(String word, String language){
		boolean result = false;
		try {
	        if (StringUtils.isBlank(language))
	        	return true;
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
		} catch (Exception e) {
			// return true... if UnicodeBlock is not identified...
			result = true;
		}
        return result;
    }
	
    /*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IDictionaryManager#findAll(java.lang.String,
	 * java.lang.String, java.lang.String[], java.lang.Integer, java.lang.String)
	 */
    @SuppressWarnings("unchecked")
    @Override
    public Response findAll(String languageId, String objectType, String[] fields, Integer limit, String version) {
        if (StringUtils.isBlank(languageId))
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
                	Map<String, Object> map = null;
                	if (StringUtils.equalsIgnoreCase(BaseController.API_VERSION, version))
                		map = convertGraphNode(node, languageId, objectType, fields, false);
                	else
                		map = addPrimaryAndOtherMeaningsToWord(node, languageId);
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
    
    /** The csv file format. */
	private CSVFormat csvFileFormat = CSVFormat.DEFAULT;

	/** The Constant NEW_LINE_SEPARATOR. */
	private static final String NEW_LINE_SEPARATOR = "\n";
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#findWordsCSV(java.lang.String,
	 * java.lang.String, java.io.InputStream, java.io.OutputStream)
	 */
	@CoverageIgnore
	@Override
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
                                Map<String, Object> metadata = node.getMetadata();
                                rowMap.put("grade", metadata.get("grade"));
                                rowMap.put("status", metadata.get("status"));
                                rowMap.put("pronunciations", metadata.get("pronunciations"));
                                rowMap.put("hasConnotative", metadata.get("hasConnotative"));
                                rowMap.put("isPhrase", metadata.get("isPhrase"));
                                rowMap.put("isLoanWord", metadata.get("isLoanWord"));
                                rowMap.put("orthographic_complexity", metadata.get("orthographic_complexity"));
                                rowMap.put("phonologic_complexity", metadata.get("phonologic_complexity"));
                                if (null != node.getTags() && !node.getTags().isEmpty())
                                    rowMap.put("tags", node.getTags());
                                getSynsets(languageId, node, rowMap, nodes);
                            }
                        }
                    }
                }
            }
            getCSV(nodes, out);
        } catch (Exception e) {
        	LOGGER.error(e.getMessage(), e);
            throw new MiddlewareException(LanguageErrorCodes.ERR_SEARCH_ERROR.name(), e.getMessage(), e);
        }
    }
    
	/**
	 * Gets the synsets.
	 *
	 * @param languageId
	 *            the language id
	 * @param node
	 *            the node
	 * @param rowMap
	 *            the row map
	 * @param nodes
	 *            the nodes
	 * @return the synsets
	 */
	@CoverageIgnore
    private void getSynsets(String languageId, Node node, Map<String, Object> rowMap, List<Map<String, Object>> nodes) {
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
                        map.put("exampleSentences", metadata.get("exampleSentences"));
                        map.put("pictures", metadata.get("pictures"));
                        if (first) {
                            first = false;
                            if (null != node.getMetadata().get("variants"))
                                map.put("variants", node.getMetadata().get("variants"));
                        }
                    }
                    try {
						Node synset = getDataNode(languageId, synsetId, "Synset");
						if (null != synset && null != synset.getOutRelations() && !synset.getOutRelations().isEmpty()) {
							Map<String, List<String>> rels = new HashMap<String, List<String>>(); 
							for (Relation rel : synset.getOutRelations()) {
								if (StringUtils.equalsIgnoreCase("Word", rel.getEndNodeObjectType())) {
									String type = rel.getRelationType();
									Map<String, Object> word = rel.getEndNodeMetadata();
									String lemma = (String) word.get("lemma");
									List<String> words = rels.get(type);
									if (null == words) {
										words = new ArrayList<String>();
										rels.put(type, words);
									}
									if (StringUtils.isNotBlank(lemma) && !words.contains(lemma))
										words.add(lemma);
								}
							}
							map.putAll(rels);
						}
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
					}
                    nodes.add(map);
                }
            }
        }
        if (first)
            nodes.add(rowMap);
    }
    
    /**
	 * Gets the csv.
	 *
	 * @param nodes
	 *            the nodes
	 * @param out
	 *            the out
	 * @return the csv
	 * @throws Exception
	 *             the exception
	 */
	@CoverageIgnore
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
        try (BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(out));
				CSVPrinter writer = new CSVPrinter(bWriter, csvFileFormat)) {
			writer.printRecords(allRows);
		}
    }
    
    /**
	 * Adds the to data row.
	 *
	 * @param val
	 *            the val
	 * @param row
	 *            the row
	 * @param i
	 *            the i
	 */
	@CoverageIgnore
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
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    /**
	 * Search word.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param lemma
	 *            the lemma
	 * @return the node
	 */
	@CoverageIgnore
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
    
    /**
	 * Gets the CSV records from an input stream.
	 *
	 * @param is
	 *            the is
	 * @return the CSV records
	 * @throws Exception
	 *             the exception
	 */
	@CoverageIgnore
    private List<String[]> getCSVRecords(InputStream is) throws Exception {
        try (InputStreamReader isReader = new InputStreamReader(is);
				CSVParser csvReader = new CSVParser(isReader, csvFileFormat)) {
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
            return rows;
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IDictionaryManager#deleteRelation(java.lang.
	 * String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
    @Override
    public Response deleteRelation(String languageId, String objectType, String objectId1, String relation,
            String objectId2) {
        if (StringUtils.isBlank(languageId))
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

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#addRelation(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
    @Override
    public Response addRelation(String languageId, String objectType, String objectId1, String relation,
            String objectId2) {
        if (StringUtils.isBlank(languageId))
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

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IDictionaryManager#list(java.lang.String,
	 * java.lang.String, com.ilimi.common.dto.Request, java.lang.String)
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Response list(String languageId, String objectType, Request request, String version) {
        if (StringUtils.isBlank(languageId))
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
                            filters.add(new Filter(entry.getKey(), SearchConditions.OP_EQUAL, list.get(0)));
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
                	Map<String, Object> map = null;
                	if (StringUtils.equalsIgnoreCase(BaseController.API_VERSION, version))
                		map = convertGraphNode(node, languageId, objectType, fields, false);
                	else
                		map = addPrimaryAndOtherMeaningsToWord(node, languageId);
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

    /**
	 * Gets the fields.
	 *
	 * @param request
	 *            the request
	 * @return the fields
	 */
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

    /**
	 * Sets the limit.
	 *
	 * @param request
	 *            the request
	 * @param sc
	 *            the sc
	 */
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

    /**
	 * Gets the list.
	 *
	 * @param mapper
	 *            the mapper
	 * @param object
	 *            the object
	 * @param returnList
	 *            the return list
	 * @return the list
	 */
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

    /**
	 * Convert graph node.
	 *
	 * @param node
	 *            the node
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param fields
	 *            the fields
	 * @param synsetMembers
	 *            the synset members
	 * @return the map
	 */
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

    /**
	 * Adds the relations data.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param node
	 *            the node
	 * @param map
	 *            the map
	 * @param synsetMembers
	 *            the synset members
	 */
    private void addRelationsData(String languageId, String objectType, Node node, Map<String, Object> map,
            boolean synsetMembers) {
        List<String> synsetIds = new ArrayList<String>();
        List<Map<String, Object>> synonyms = new ArrayList<Map<String, Object>>();
        List<NodeDTO> antonyms = new ArrayList<NodeDTO>();
        List<NodeDTO> hypernyms = new ArrayList<NodeDTO>();
        List<NodeDTO> hyponyms = new ArrayList<NodeDTO>();
        List<NodeDTO> homonyms = new ArrayList<NodeDTO>();
        List<NodeDTO> meronyms = new ArrayList<NodeDTO>();
        List<NodeDTO> tools = new ArrayList<NodeDTO>();
        List<NodeDTO> workers = new ArrayList<NodeDTO>();
        List<NodeDTO> actions = new ArrayList<NodeDTO>();
        List<NodeDTO> objects = new ArrayList<NodeDTO>();
        List<NodeDTO> converse = new ArrayList<NodeDTO>();
        getInRelationsData(node, synonyms, antonyms, hypernyms, hyponyms, homonyms, meronyms, tools, workers, actions, objects, converse, synsetIds);
        getOutRelationsData(node, synonyms, antonyms, hypernyms, hyponyms, homonyms, meronyms, tools, workers, actions, objects, converse, synsetIds);
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
        if (!tools.isEmpty())
            map.put("tools", tools);
        if (!workers.isEmpty())
            map.put("workers", workers);
        if (!actions.isEmpty())
            map.put("actions", actions);
        if (!objects.isEmpty())
            map.put("objects", objects);
        if (!converse.isEmpty())
            map.put("converse", converse);
        
    }

    /**
	 * Gets the synonym map.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param nodeIds
	 *            the node ids
	 * @return the synonym map
	 */
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

    /**
	 * Gets the in relations of a word.
	 *
	 * @param node
	 *            the node
	 * @param synonyms
	 *            the synonyms
	 * @param antonyms
	 *            the antonyms
	 * @param hypernyms
	 *            the hypernyms
	 * @param hyponyms
	 *            the hyponyms
	 * @param homonyms
	 *            the homonyms
	 * @param meronyms
	 *            the meronyms
	 * @param tools
	 *            the tools
	 * @param workers
	 *            the workers
	 * @param actions
	 *            the actions
	 * @param objects
	 *            the objects
	 * @param converse
	 *            the converse
	 * @param synsetIds
	 *            the synset ids
	 * @return the in relations data
	 */
    private void getInRelationsData(Node node, List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
            List<NodeDTO> hypernyms, List<NodeDTO> hyponyms, List<NodeDTO> homonyms, List<NodeDTO> meronyms,
            List<NodeDTO> tools, List<NodeDTO> workers, List<NodeDTO> actions, List<NodeDTO> objects, List<NodeDTO> converse, List<String> synsetIds) {
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
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.TOOL.relationName(),
                        inRel.getRelationType())) {
                    tools.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.WORKER.relationName(),
                        inRel.getRelationType())) {
                    workers.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.ACTION.relationName(),
                        inRel.getRelationType())) {
                    actions.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.OBJECT.relationName(),
                        inRel.getRelationType())) {
                    objects.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.CONVERSE.relationName(),
                        inRel.getRelationType())) {
                    converse.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                }
            }
        }
    }

    /**
	 * Gets the in relation of a word.
	 *
	 * @param inRel
	 *            the in rel
	 * @return the in relation word
	 */
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

    /**
	 * Gets the out relations data of a word.
	 *
	 * @param node
	 *            the node
	 * @param synonyms
	 *            the synonyms
	 * @param antonyms
	 *            the antonyms
	 * @param hypernyms
	 *            the hypernyms
	 * @param hyponyms
	 *            the hyponyms
	 * @param homonyms
	 *            the homonyms
	 * @param meronyms
	 *            the meronyms
	 * @param tools
	 *            the tools
	 * @param workers
	 *            the workers
	 * @param actions
	 *            the actions
	 * @param objects
	 *            the objects
	 * @param converse
	 *            the converse
	 * @param synsetIds
	 *            the synset ids
	 * @return the out relations data
	 */
    @CoverageIgnore
    private void getOutRelationsData(Node node, List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
            List<NodeDTO> hypernyms, List<NodeDTO> hyponyms, List<NodeDTO> homonyms, List<NodeDTO> meronyms,
            List<NodeDTO> tools, List<NodeDTO> workers, List<NodeDTO> actions, List<NodeDTO> objects, List<NodeDTO> converse, List<String> synsetIds) {
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
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.TOOL.relationName(),
                        outRel.getRelationType())) {
                    tools.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.WORKER.relationName(),
                        outRel.getRelationType())) {
                    workers.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.ACTION.relationName(),
                        outRel.getRelationType())) {
                    actions.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.OBJECT.relationName(),
                        outRel.getRelationType())) {
                    objects.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.CONVERSE.relationName(),
                        outRel.getRelationType())) {
                    converse.add(new NodeDTO(outRel.getEndNodeId(), getOutRelationWord(outRel),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
                }
            }
        }
    }

    /**
	 * Gets the out relation of a word.
	 *
	 * @param outRel
	 *            the out rel
	 * @return the out relation word
	 */
    @CoverageIgnore
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

    /**
	 * Convert Object Map to graph node.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param map
	 *            the object map
	 * @param definition
	 *            the definition
	 * @return the node
	 */
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
                    	LOGGER.error(e.getMessage(), e);
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
                    	LOGGER.error(e.getMessage(), e);
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
                    	LOGGER.error(e.getMessage(), e);
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
                    	LOGGER.error(e.getMessage(), e);
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

    /**
	 * Creates the word.
	 *
	 * @param lemmaIdMap
	 *            the lemma id map
	 * @param languageId
	 *            the language id
	 * @param word
	 *            the word
	 * @param objectType
	 *            the object type
	 * @return the string
	 */
    @CoverageIgnore
    private String createWord(Map<String, String> lemmaIdMap, String languageId, String word, String objectType) {
        String nodeId = createWord(languageId, word, objectType);
        lemmaIdMap.put(word, nodeId);
        return nodeId;
    }
    
    /**
	 * Creates the word.
	 *
	 * @param languageId
	 *            the language id
	 * @param word
	 *            the word
	 * @param objectType
	 *            the object type
	 * @return the string
	 */
    @CoverageIgnore
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

    /**
	 * Gets the word id map.
	 *
	 * @param lemmaIdMap
	 *            the lemma id map
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param words
	 *            the words
	 * @return the word id map
	 */
    @CoverageIgnore
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

    /**
	 * Gets the rel def maps.
	 *
	 * @param definition
	 *            the definition
	 * @param inRelDefMap
	 *            the in rel def map
	 * @param outRelDefMap
	 *            the out rel def map
	 * @return the rel def maps
	 */
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
    
    /**
	 * Gets the boolean value.
	 *
	 * @param value
	 *            the value
	 * @return the boolean value
	 */
    @CoverageIgnore
    private boolean getBooleanValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            if (StringUtils.equalsIgnoreCase("yes", value.trim().toLowerCase())
                    || StringUtils.equalsIgnoreCase("true", value.trim().toLowerCase()))
                return true;
        }
        return false;
    }

    /**
	 * Convert to graph node.
	 *
	 * @param map
	 *            the map
	 * @param definition
	 *            the definition
	 * @return the node
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node convertToGraphNode(Map<String, Object> map, DefinitionDTO definition) throws Exception {
		Node node = new Node();
		if (null != map && !map.isEmpty()) {
			Map<String, String> inRelDefMap = new HashMap<String, String>();
			Map<String, String> outRelDefMap = new HashMap<String, String>();
			getRelDefMaps(definition, inRelDefMap, outRelDefMap);
			List<Relation> inRelations = null;
			List<Relation> outRelations = null;
			Map<String, Object> metadata = new HashMap<String, Object>();
			for (Entry<String, Object> entry : map.entrySet()) {
				if (StringUtils.equalsIgnoreCase("identifier", entry.getKey())) {
				    if (StringUtils.isNotBlank((String) entry.getValue()))
				        node.setIdentifier((String) entry.getValue()); 
				} else if (StringUtils.equalsIgnoreCase("objectType", entry.getKey())) {
					node.setObjectType((String) entry.getValue());
				} else if (StringUtils.equalsIgnoreCase("tags", entry.getKey())) {
					try {
						String objectStr = mapper.writeValueAsString(entry.getValue());
						List<String> tags = mapper.readValue(objectStr, List.class);
						node.setTags(tags);
					} catch (Exception e) {
						e.printStackTrace();
						throw e;
					}
				} else if (inRelDefMap.containsKey(entry.getKey())) {
					try {
						String objectStr = mapper.writeValueAsString(entry.getValue());
						List<Map> list = mapper.readValue(objectStr, List.class);
						if (null != list && !list.isEmpty()) {
							for (Map obj : list) {
								NodeDTO dto = (NodeDTO) mapper.convertValue(obj, NodeDTO.class);
								if (null == inRelations)
									inRelations = new ArrayList<Relation>();
								inRelations
										.add(new Relation(dto.getIdentifier(), inRelDefMap.get(entry.getKey()), null));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw e;
					}
				} else if (outRelDefMap.containsKey(entry.getKey())) {
					try {
						String objectStr = mapper.writeValueAsString(entry.getValue());
						List<Map> list = mapper.readValue(objectStr, List.class);
						if (null != list && !list.isEmpty()) {
							for (Map obj : list) {
								NodeDTO dto = (NodeDTO) mapper.convertValue(obj, NodeDTO.class);
								Relation relation = new Relation(null, outRelDefMap.get(entry.getKey()),
										dto.getIdentifier());
								if (null != dto.getIndex() && dto.getIndex().intValue() >= 0) {
									Map<String, Object> relMetadata = new HashMap<String, Object>();
									relMetadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), dto.getIndex());
									relation.setMetadata(relMetadata);
								}
								if (null == outRelations)
									outRelations = new ArrayList<Relation>();
								outRelations.add(relation);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw e;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#createWordV2(java.lang.String,
	 * java.lang.String, com.ilimi.common.dto.Request, boolean)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Response createWordV2(String languageId, String objectType, Request request, boolean forceUpdate) {
		if (StringUtils.isBlank(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "ObjectType is blank");
		try {
			Response createRes = OK();
			List<String> lstNodeId = new ArrayList<String>();
			List<String> errorMessages = new ArrayList<String>();
			List<Map> items = (List<Map>) request.get(LanguageParams.words.name());
			if (null == items || items.size() <= 0)
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT.name(),
						objectType + " Object is blank");
			try {
				if (null != items && !items.isEmpty()) {
					Request requestDefinition = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER,
							"getNodeDefinition", GraphDACParams.object_type.name(), objectType);
					Response responseDefiniton = getResponse(requestDefinition, LOGGER);
					if (!checkError(responseDefiniton)) {
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
							Response wordResponse = createOrUpdateWord(item, definition, languageId, true, lstNodeId, forceUpdate);
							if(checkError(wordResponse)){
								errorMessages.add(wordUtil.getErrorMessage(wordResponse));
							}
							List<String> errorMessage = (List<String>) wordResponse.get(LanguageParams.error_messages.name());
							if (errorMessage != null && !errorMessage.isEmpty()) {
								errorMessages.addAll(errorMessage);
							}
							String nodeId = (String) wordResponse.get(GraphDACParams.node_id.name());
							if (nodeId != null) {
							    createRes.put(GraphDACParams.node_id.name(), nodeId);
								lstNodeId.add(nodeId);
							}
						}
					}
				}
			} catch (Exception e) {
				errorMessages.add(e.getMessage());
			}
			createRes.put(GraphDACParams.node_ids.name(), lstNodeId);
			if(!errorMessages.isEmpty()){
				createRes.put(LanguageParams.error_messages.name(), errorMessages);
			}
			return createRes;
		} catch (ClassCastException e) {
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_CONTENT.name(), "Request format incorrect");
		}
	}
	
	/**
	 * Validate list property.
	 *
	 * @param map
	 *            the map
	 * @param propertyName
	 *            the property name
	 */
	@SuppressWarnings("rawtypes")
    private void validateListProperty(Map<String, Object> map, String propertyName) {
	    if (null != map && !map.isEmpty() && StringUtils.isNotBlank(propertyName)) {
	        Object value = map.get(propertyName);
	        if (null != value && value instanceof List) {
	            List list = (List) value;
	            if (null != list && !list.isEmpty()) {
	                Iterator iter = list.iterator();
	                while (iter.hasNext()) {
	                    Object obj = iter.next();
	                    if (null == obj || StringUtils.isBlank(obj.toString()))
	                        iter.remove();
	                }
	            }
	        }
	    }
	}

	/**
	 * Creates the or update word.
	 *
	 * @param item
	 *            the item
	 * @param definition
	 *            the definition
	 * @param languageId
	 *            the language id
	 * @param createFlag
	 *            the create flag
	 * @param nodeIdList
	 *            the node id list
	 * @param forceUpdate
	 *            the force update
	 * @return the response
	 */
	@SuppressWarnings({ "unchecked" })
	private Response createOrUpdateWord(Map<String, Object> item, DefinitionDTO definition, String languageId,
			boolean createFlag, List<String> nodeIdList, boolean forceUpdate) {
		Response createRes = new Response();
		List<String> errorMessages = new ArrayList<String>();
		try {
			Map<String, Object> primaryMeaning = (Map<String, Object>) item.get(LanguageParams.primaryMeaning.name());
			if (primaryMeaning == null) {
				return ERROR(LanguageErrorCodes.ERROR_PRIMARY_MEANING_EMPTY.name(),
						"Primary meaning field is mandatory", ResponseCode.SERVER_ERROR);
			}
			String wordPrimaryMeaningId = (String) item.get(LanguageParams.primaryMeaningId.name());
			String primarySynsetId = (String) primaryMeaning.get(LanguageParams.identifier.name());
			if (wordPrimaryMeaningId != null && primarySynsetId != null
					&& !wordPrimaryMeaningId.equalsIgnoreCase(primarySynsetId)) {
				return ERROR(LanguageErrorCodes.ERROR_PRIMARY_MEANING_MISMATCH.name(),
						"primaryMeaningId cannot be different from primary meaning synset Id",
						ResponseCode.SERVER_ERROR);
			}
			validateListProperty(item, ATTRIB_PRONUNCIATIONS);
			validateListProperty(primaryMeaning, ATTRIB_PICTURES);
			validateListProperty(primaryMeaning, ATTRIB_EXAMPLE_SENTENCES);
			validateListProperty(primaryMeaning, LanguageParams.tags.name());
			String gloss = (String) primaryMeaning.get(ATTRIB_GLOSS);

			// create or update Primary meaning Synset
			List<Map<String, Object>> synonyms = (List<Map<String, Object>>) primaryMeaning
					.get(LanguageParams.synonyms.name());
			if (null != synonyms && !synonyms.isEmpty())
			    item.put(ATTRIB_HAS_SYNONYMS, true);
			else
			    item.put(ATTRIB_HAS_SYNONYMS, null);
			List<String> synonymWordIds = processRelationWords(synonyms, languageId, errorMessages, definition, nodeIdList);

			List<Map<String, Object>> hypernyms = (List<Map<String, Object>>) primaryMeaning
					.get(LanguageParams.hypernyms.name());
			List<String> hypernymWordIds = processRelationWords(hypernyms, languageId, errorMessages, definition, nodeIdList);

			List<Map<String, Object>> holonyms = (List<Map<String, Object>>) primaryMeaning
					.get(LanguageParams.holonyms.name());
			List<String> holonymWordIds = processRelationWords(holonyms, languageId, errorMessages, definition, nodeIdList);

			List<Map<String, Object>> antonyms = (List<Map<String, Object>>) primaryMeaning
					.get(LanguageParams.antonyms.name());
			if (null != antonyms && !antonyms.isEmpty())
                item.put(ATTRIB_HAS_ANTONYMS, true);
            else
                item.put(ATTRIB_HAS_ANTONYMS, null);
			List<String> antonymWordIds = processRelationWords(antonyms, languageId, errorMessages, definition, nodeIdList);

			List<Map<String, Object>> hyponyms = (List<Map<String, Object>>) primaryMeaning
					.get(LanguageParams.hyponyms.name());
			List<String> hyponymWordIds = processRelationWords(hyponyms, languageId, errorMessages, definition, nodeIdList);

			List<Map<String, Object>> meronyms = (List<Map<String, Object>>) primaryMeaning
					.get(LanguageParams.meronyms.name());
			List<String> meronymWordIds = processRelationWords(meronyms, languageId, errorMessages, definition, nodeIdList);

			List<Map<String, Object>> tools = (List<Map<String, Object>>) primaryMeaning
					.get(LanguageParams.tools.name());
			List<String> toolWordIds = processRelationWords(tools, languageId, errorMessages, definition, nodeIdList);
			
			List<Map<String, Object>> workers = (List<Map<String, Object>>) primaryMeaning
					.get(LanguageParams.workers.name());
			List<String> workerWordIds = processRelationWords(workers, languageId, errorMessages, definition, nodeIdList);
			
			List<Map<String, Object>> actions = (List<Map<String, Object>>) primaryMeaning
					.get(LanguageParams.actions.name());
			List<String> actionWordIds = processRelationWords(actions, languageId, errorMessages, definition, nodeIdList);
			
			List<Map<String, Object>> objects = (List<Map<String, Object>>) primaryMeaning
					.get(LanguageParams.objects.name());
			List<String> objectWordIds = processRelationWords(objects, languageId, errorMessages, definition, nodeIdList);
			
			List<Map<String, Object>> converse = (List<Map<String, Object>>) primaryMeaning
					.get(LanguageParams.converse.name());
			List<String> converseWordIds = processRelationWords(converse, languageId, errorMessages, definition, nodeIdList);
			
			primaryMeaning.remove(LanguageParams.synonyms.name());
			primaryMeaning.remove(LanguageParams.hypernyms.name());
			primaryMeaning.remove(LanguageParams.hyponyms.name());
			primaryMeaning.remove(LanguageParams.holonyms.name());
			primaryMeaning.remove(LanguageParams.antonyms.name());
			primaryMeaning.remove(LanguageParams.meronyms.name());
			primaryMeaning.remove(LanguageParams.tools.name());
			primaryMeaning.remove(LanguageParams.objects.name());
			primaryMeaning.remove(LanguageParams.actions.name());
			primaryMeaning.remove(LanguageParams.workers.name());
			primaryMeaning.remove(LanguageParams.converse.name());
			
			if(primaryMeaning.containsKey(ATTRIB_EXAMPLE_SENTENCES)){
				Object exampleSentences = primaryMeaning.get(ATTRIB_EXAMPLE_SENTENCES);
				item.put(ATTRIB_EXAMPLE_SENTENCES, exampleSentences);
				primaryMeaning.remove(ATTRIB_EXAMPLE_SENTENCES);
			}
			
			Response synsetResponse = createSynset(languageId, primaryMeaning);
			if (checkError(synsetResponse)) {
				return synsetResponse;
			}
			
			String primaryMeaningId = (String) synsetResponse.get(GraphDACParams.node_id.name());
			Node synsetNode = wordUtil.getDataNode(languageId, primaryMeaningId);
			addSynsetRelation(synonymWordIds, RelationTypes.SYNONYM.relationName(), languageId, synsetNode,
					errorMessages, forceUpdate);
			addSynsetRelation(hypernymWordIds, RelationTypes.HYPERNYM.relationName(), languageId, synsetNode,
					errorMessages, forceUpdate);
			addSynsetRelation(holonymWordIds, RelationTypes.HOLONYM.relationName(), languageId, synsetNode,
					errorMessages, forceUpdate);
			addSynsetRelation(antonymWordIds, RelationTypes.ANTONYM.relationName(), languageId, synsetNode,
					errorMessages, forceUpdate);
			addSynsetRelation(hyponymWordIds, RelationTypes.HYPONYM.relationName(), languageId, synsetNode,
					errorMessages, forceUpdate);
			addSynsetRelation(meronymWordIds, RelationTypes.MERONYM.relationName(), languageId, synsetNode,
					errorMessages, forceUpdate);
			addSynsetRelation(toolWordIds, RelationTypes.TOOL.relationName(), languageId, synsetNode,
					errorMessages, forceUpdate);
			addSynsetRelation(workerWordIds, RelationTypes.WORKER.relationName(), languageId, synsetNode,
					errorMessages, forceUpdate);
			addSynsetRelation(actionWordIds, RelationTypes.ACTION.relationName(), languageId, synsetNode,
					errorMessages, forceUpdate);
			addSynsetRelation(objectWordIds, RelationTypes.OBJECT.relationName(), languageId, synsetNode,
					errorMessages, forceUpdate);
			addSynsetRelation(converseWordIds, RelationTypes.CONVERSE.relationName(), languageId, synsetNode,
					errorMessages, forceUpdate);
			
			// update wordMap with primary synset data
			item.put(LanguageParams.primaryMeaningId.name(), primaryMeaningId);
			
			//get Synset data and set to word
			if(synsetNode != null){
				Map<String, Object> synsetMetadata = synsetNode.getMetadata();
				String category = (String) synsetMetadata.get(LanguageParams.category.name());
				if (StringUtils.isNotBlank(category))
					item.put(LanguageParams.category.name(), category);
				List<String> tags = synsetNode.getTags();
				item.put(LanguageParams.tags.name(), tags);
				if (primaryMeaning.containsKey(ATTRIB_PICTURES)){
					Object pictures = primaryMeaning.get(ATTRIB_PICTURES);
					item.put(ATTRIB_PICTURES, pictures);
				}
			}
			
			// create or update Other meaning Synsets
			List<String> otherMeaningIds = new ArrayList<String>();
			List<Map<String, Object>> otherMeanings = (List<Map<String, Object>>) item
					.get(LanguageParams.otherMeanings.name());
			if (otherMeanings != null && !otherMeanings.isEmpty()) {
				for (Map<String, Object> otherMeaning : otherMeanings) {
					Response otherSynsetResponse = createSynset(languageId, otherMeaning);
					if (checkError(otherSynsetResponse)) {
						return otherSynsetResponse;
					}
					String otherMeaningId = (String) otherSynsetResponse.get(GraphDACParams.node_id.name());
					otherMeaningIds.add(otherMeaningId);
				}
			}

			// create Word
			item.remove(LanguageParams.primaryMeaning.name());
			item.remove(LanguageParams.otherMeanings.name());
			Integer synsetCount = otherMeaningIds.size() + 1;
			item.put(ATTRIB_SYNSET_COUNT, synsetCount);
			String lemma = (String) item.get(ATTRIB_LEMMA);
			lemma = lemma.trim().toLowerCase();
			item.put(ATTRIB_LEMMA, lemma);
			item.put(ATTRIB_MEANING, gloss);
			if (StringUtils.isNotBlank(lemma) && lemma.trim().contains(" ")) {
                Object isPhrase = item.get(ATTRIB_IS_PHRASE);
                if (null == isPhrase)
                    item.put(ATTRIB_IS_PHRASE, true);
            }
			
			Node node = convertToGraphNode(languageId, LanguageParams.Word.name(), item, definition);
			node.setObjectType(LanguageParams.Word.name());
			String wordIdentifier = (String) item.get(LanguageParams.identifier.name());
			String prevState = LanguageParams.Draft.name();
			if(wordIdentifier == null && createFlag){
				Node existingWordNode = wordUtil.searchWord(languageId, (String) item.get(LanguageParams.lemma.name()));
				if(existingWordNode != null){
					wordIdentifier = existingWordNode.getIdentifier();
					item.put(LanguageParams.identifier.name(), wordIdentifier);
					Object lastUpdatedBy = existingWordNode.getMetadata().get(ATTRIB_LAST_UPDATED_BY);
					String stringLastUpdatedBy = null;
					if(lastUpdatedBy != null && lastUpdatedBy instanceof String[]){
						if(((String[])lastUpdatedBy).length > 0) {
							stringLastUpdatedBy = ((String[])lastUpdatedBy)[0];
						}
						node.getMetadata().put(ATTRIB_LAST_UPDATED_BY, stringLastUpdatedBy);
					}
					prevState = (String)existingWordNode.getMetadata().get(LanguageParams.status.name());
					createFlag = false;
				}
			}
			if (createFlag) {
				createRes = createWord(node, languageId);
			} else {
				createRes = updateWord(node, languageId, wordIdentifier);
			}
			if (!checkError(createRes)) {
				String wordId = (String) createRes.get("node_id");
				Node word = getWord(wordId, languageId, errorMessages);
				Object status = word.getMetadata().get(LanguageParams.status.name());
				if(null!=status && !StringUtils.equalsIgnoreCase((String)status,prevState))
				{
					word.getMetadata().put("prevState", prevState);
					LogWordEventUtil.logWordLifecycleEvent(word.getIdentifier(), word.getMetadata());
				}
				if (null != word && null != word.getInRelations() && !word.getInRelations().isEmpty()) {
				    for (Relation rel : word.getInRelations()) {
				        if (StringUtils.equalsIgnoreCase(rel.getRelationType(), RelationTypes.SYNONYM.relationName())) {
				            String synsetId = rel.getStartNodeId();
				            if (!StringUtils.equals(synsetId, primaryMeaningId) && !otherMeaningIds.contains(synsetId))
				                removeSynonymRelation(languageId, wordId, synsetId);
				        }
				    }
				}
				// add Primary Synonym Relation
				addSynonymRelation(languageId, wordId, primaryMeaningId);
				// add other meaning Synonym Relation
				for (String otherMeaningId : otherMeaningIds) {
					addSynonymRelation(languageId, wordId, otherMeaningId);
				}
			} else {
				errorMessages.add(wordUtil.getErrorMessage(createRes));
			}
		} catch (Exception e) {
			errorMessages.add(e.getMessage());
		}
		if(!errorMessages.isEmpty()){
			createRes.put(LanguageParams.error_messages.name(), errorMessages);
		}
		return createRes;
	}

	/**
	 * Adds the synset relation of the word.
	 *
	 * @param wordIds
	 *            the word ids
	 * @param relationType
	 *            the relation type
	 * @param languageId
	 *            the language id
	 * @param synsetNode
	 *            the synset node
	 * @param errorMessages
	 *            the error messages
	 * @param forceUpdate
	 *            the force update
	 */
	private void addSynsetRelation(List<String> wordIds, String relationType, String languageId, Node synsetNode,
	        List<String> errorMessages, boolean forceUpdate) {
	    String synsetId = synsetNode.getIdentifier();
	    if (null != wordIds) {
	        if (null != synsetNode.getOutRelations() && !synsetNode.getOutRelations().isEmpty()) {
                for (Relation rel : synsetNode.getOutRelations()) {
                    if (StringUtils.equalsIgnoreCase(rel.getRelationType(), relationType)) {
                        String wordId = rel.getEndNodeId();
                        if (!wordIds.contains(wordId))
                            removeSynsetRelation(wordId, relationType, languageId, synsetId);
                    }
                }
            }
	        for (String wordId : wordIds) {
	            if (relationType.equalsIgnoreCase(RelationTypes.SYNONYM.relationName())) {
	                Node wordNode = getWord(wordId, languageId, errorMessages);
	                Map<String, Object> metadata = wordNode.getMetadata();
	                if (metadata != null) {
	                    String primaryMeaningId = (String) metadata.get(ATTRIB_PRIMARY_MEANING_ID);
	                    String wordLemma = (String) metadata.get(ATTRIB_LEMMA);
	                    if (StringUtils.isNotBlank(primaryMeaningId) && !primaryMeaningId.equalsIgnoreCase(synsetId) && !forceUpdate) {
	                        errorMessages.add("Word '" + wordLemma + "' has a different primary meaning");
	                        continue;
	                    } else {
	                        metadata.put(ATTRIB_PRIMARY_MEANING_ID, synsetId);
	                        if (null != synsetNode.getMetadata()) {
	                        	String gloss = (String) synsetNode.getMetadata().get(ATTRIB_GLOSS);
		                        metadata.put(ATTRIB_MEANING, gloss);
	                        }
	                        wordNode.setMetadata(metadata);
	                        wordNode.setObjectType(LanguageParams.Word.name());
	                        updateWord(wordNode, languageId, wordId);
	                        if (forceUpdate && StringUtils.isNotBlank(primaryMeaningId) && !primaryMeaningId.equalsIgnoreCase(synsetId)) {
                                removeSynsetRelation(wordId, relationType, languageId, primaryMeaningId);
                            }
	                    }
	                }
	            }
	            addSynsetRelation(wordId, relationType, languageId, synsetId, errorMessages);
	        }
	    }
	}

	/**
	 * Creates the synset relation.
	 *
	 * @param wordId
	 *            the word id
	 * @param relationType
	 *            the relation type
	 * @param languageId
	 *            the language id
	 * @param synsetId
	 *            the synset id
	 * @param errorMessages
	 *            the error messages
	 */
	private void addSynsetRelation(String wordId, String relationType, String languageId, String synsetId,
	        List<String> errorMessages) {
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
		request.put(GraphDACParams.start_node_id.name(), synsetId);
		request.put(GraphDACParams.relation_type.name(), relationType);
		request.put(GraphDACParams.end_node_id.name(), wordId);
		Response response = getResponse(request, LOGGER);
		if (checkError(response)) {
			errorMessages.add(response.getParams().getErrmsg());
		}
	}
	
	/**
	 * Removes the synset relation from the word.
	 *
	 * @param wordId
	 *            the word id
	 * @param relationType
	 *            the relation type
	 * @param languageId
	 *            the language id
	 * @param synsetId
	 *            the synset id
	 */
	private void removeSynsetRelation(String wordId, String relationType, String languageId, String synsetId) {
        Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "removeRelation");
        request.put(GraphDACParams.start_node_id.name(), synsetId);
        request.put(GraphDACParams.relation_type.name(), relationType);
        request.put(GraphDACParams.end_node_id.name(), wordId);
        Response response = getResponse(request, LOGGER);
        if (checkError(response)) {
            throw new ServerException(response.getParams().getErr(), response.getParams().getErrmsg());
        }
    }
	
	/**
	 * Removes the synonym relation from the word.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordId
	 *            the word id
	 * @param synsetId
	 *            the synset id
	 */
	@CoverageIgnore
	private void removeSynonymRelation(String languageId, String wordId, String synsetId) {
	    removeSynsetRelation(wordId, RelationTypes.SYNONYM.relationName(), languageId, synsetId);
    }

	/**
	 * Gets the word based on the word Id and returns error messages.
	 *
	 * @param wordId
	 *            the word id
	 * @param languageId
	 *            the language id
	 * @param errorMessages
	 *            the error messages
	 * @return the word
	 */
	private Node getWord(String wordId, String languageId, List<String> errorMessages) {
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), wordId);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request, LOGGER);
		if (checkError(getNodeRes)) {
			errorMessages.add(getNodeRes.getParams().getErrmsg());
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		return node;
	}

	/**
	 * Process relation words of a given word.
	 *
	 * @param synsetRelations
	 *            the synset relations
	 * @param languageId
	 *            the language id
	 * @param errorMessages
	 *            the error messages
	 * @param wordDefintion
	 *            the word defintion
	 * @param nodeIdList
	 *            the node id list
	 * @return the list
	 */
	private List<String> processRelationWords(List<Map<String, Object>> synsetRelations, String languageId,
	        List<String> errorMessages, DefinitionDTO wordDefintion, List<String> nodeIdList) {
		List<String> wordIds = null;
		if (synsetRelations != null) {
		    wordIds = new ArrayList<String>();
			for (Map<String, Object> word : synsetRelations) {
				String wordId = createOrUpdateWordsWithoutPrimaryMeaning(word, languageId, errorMessages,
						wordDefintion);
				if (wordId != null) {
					wordIds.add(wordId);
				}
			}
			if (!wordIds.isEmpty())
			    nodeIdList.addAll(wordIds);
		}
		return wordIds;
	}
	
	/**
	 * Creates or update words without processing primary meaning.
	 *
	 * @param word
	 *            the word
	 * @param languageId
	 *            the language id
	 * @param errorMessages
	 *            the error messages
	 * @param definition
	 *            the definition
	 * @return the string
	 */
	private String createOrUpdateWordsWithoutPrimaryMeaning(Map<String, Object> word, String languageId,
	        List<String> errorMessages, DefinitionDTO definition) {
		String lemma = (String) word.get(LanguageParams.name.name());
		if (StringUtils.isBlank(lemma)) 
		    lemma = (String) word.get(LanguageParams.lemma.name());
		if (StringUtils.isBlank(lemma)) {
			errorMessages.add("Lemma is mandatory");
			return null;
		} else {
			lemma = lemma.trim().toLowerCase();
		    word.put(LanguageParams.lemma.name(), lemma);
		    word.remove(LanguageParams.name.name());
			String language = LanguageMap.getLanguage(languageId).toUpperCase();
			boolean isValid = isValidWord(lemma, language);
			if (!isValid) {
				errorMessages.add("Lemma cannot be in a different language than " + language);
				return null;
			}
			if (StringUtils.isNotBlank(lemma) && lemma.trim().contains(" ")) {
			    Object isPhrase = word.get(ATTRIB_IS_PHRASE);
			    if (null == isPhrase)
			        word.put(ATTRIB_IS_PHRASE, true);
			}
			String identifier = (String) word.get(LanguageParams.identifier.name());
			if(StringUtils.isBlank(identifier)){
				Node existingWordNode = wordUtil.searchWord(languageId, lemma);
				if(existingWordNode != null){
					identifier = existingWordNode.getIdentifier();
					word.put(LanguageParams.identifier.name(), identifier);
					Object lastUpdatedBy = existingWordNode.getMetadata().get(ATTRIB_LAST_UPDATED_BY);
					String stringLastUpdatedBy = null;
					if(lastUpdatedBy != null && lastUpdatedBy instanceof String[]){
						if(((String[])lastUpdatedBy).length > 0) {
							stringLastUpdatedBy = ((String[])lastUpdatedBy)[0];
						}
						word.put(ATTRIB_LAST_UPDATED_BY, stringLastUpdatedBy);
					}
				}
			}
			Response wordResponse;
			Node wordNode = convertToGraphNode(languageId, LanguageParams.Word.name(), word, definition);
			wordNode.setObjectType(LanguageParams.Word.name());
			if (identifier == null) {
				wordResponse = createWord(wordNode, languageId);
			} else {
				wordResponse = updateWord(wordNode, languageId, identifier);
			}
			if (checkError(wordResponse)) {
				errorMessages.add(wordUtil.getErrorMessage(wordResponse));
				return null;
			}
			String nodeId = (String) wordResponse.get(GraphDACParams.node_id.name());
			return nodeId;
		}
	}

	/**
	 * Creates the synset.
	 *
	 * @param languageId
	 *            the language id
	 * @param synsetObj
	 *            the synset obj
	 * @return the response
	 * @throws Exception
	 *             the exception
	 */
	private Response createSynset(String languageId, Map<String, Object> synsetObj) throws Exception {
		String operation = "updateDataNode";
		String identifier = (String) synsetObj.get(LanguageParams.identifier.name());
		if (StringUtils.isBlank(identifier)) {
			operation = "createDataNode";
		}

		DefinitionDTO synsetDefinition = getDefinitionDTO(LanguageParams.Synset.name(), languageId);
		// synsetObj.put(GraphDACParams.object_type.name(),
		// LanguageParams.Synset.name());
		Node synsetNode = convertToGraphNode(synsetObj, synsetDefinition);
		synsetNode.setObjectType(LanguageParams.Synset.name());
		Request synsetReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, operation);
		synsetReq.put(GraphDACParams.node.name(), synsetNode);
		if (operation.equalsIgnoreCase("updateDataNode")) {
			synsetReq.put(GraphDACParams.node_id.name(), synsetNode.getIdentifier());
		}
		Response res = getResponse(synsetReq, LOGGER);
		return res;
	}

	/**
	 * Adds the synonym relation.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordId
	 *            the word id
	 * @param synsetId
	 *            the synset id
	 */
	private void addSynonymRelation(String languageId, String wordId, String synsetId) {
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
		request.put(GraphDACParams.start_node_id.name(), synsetId);
		request.put(GraphDACParams.relation_type.name(), RelationTypes.SYNONYM.relationName());
		request.put(GraphDACParams.end_node_id.name(), wordId);
		Response response = getResponse(request, LOGGER);
		if (checkError(response)) {
			throw new ServerException(response.getParams().getErr(), response.getParams().getErrmsg());
		}
	}
	
	/**
	 * Creates the word using the Node object.
	 *
	 * @param node
	 *            the node
	 * @param languageId
	 *            the language id
	 * @return the response
	 */
	private Response createWord(Node node, String languageId) {
		Request validateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "validateNode");
		validateReq.put(GraphDACParams.node.name(), node);
		Response validateRes = getResponse(validateReq, LOGGER);
		if (checkError(validateRes)) {
			return validateRes;
		} else {
			Request createReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
			createReq.put(GraphDACParams.node.name(), node);
			Response res = getResponse(createReq, LOGGER);
			return res;
		}
	}

	/**
	 * Updates word using the Node object.
	 *
	 * @param node
	 *            the node
	 * @param languageId
	 *            the language id
	 * @param wordId
	 *            the word id
	 * @return the response
	 */
	private Response updateWord(Node node, String languageId, String wordId) {
		Request validateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "validateNode");
		validateReq.put(GraphDACParams.node.name(), node);
		validateReq.put(GraphDACParams.node_id.name(), wordId);
		Response validateRes = getResponse(validateReq, LOGGER);
		if (checkError(validateRes)) {
			return validateRes;
		} else {
			Request createReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			createReq.put(GraphDACParams.node.name(), node);
			createReq.put(GraphDACParams.node_id.name(), wordId);
			Response res = getResponse(createReq, LOGGER);
			return res;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#updateWordV2(java.lang.String,
	 * java.lang.String, java.lang.String, com.ilimi.common.dto.Request,
	 * boolean)
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public Response updateWordV2(String languageId, String id, String objectType, Request request, boolean forceUpdate) {
		if (StringUtils.isBlank(languageId))
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
			item.put(LanguageParams.identifier.name(), id);
			String lemma = (String) item.get(LanguageParams.lemma.name());
			String language = LanguageMap.getLanguage(languageId).toUpperCase();
			boolean isValid = isValidWord(lemma, language);
			if (!isValid) {
				return ERROR(LanguageErrorCodes.ERR_CREATE_WORD.name(),
						"Lemma cannot be in a different language than " + language,
						ResponseCode.CLIENT_ERROR);
			}
			Request requestDefinition = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
					GraphDACParams.object_type.name(), objectType);
			Response responseDefiniton = getResponse(requestDefinition, LOGGER);
			if (checkError(responseDefiniton)) {
				return responseDefiniton;
			}
			DefinitionDTO definition = (DefinitionDTO) responseDefiniton.get(GraphDACParams.definition_node.name());
			List<String> lstNodeId = new ArrayList<String>();
			Response updateResponse = createOrUpdateWord(item, definition, languageId, false, lstNodeId, forceUpdate);
			String wordId = (String) updateResponse.get(GraphDACParams.node_id.name());
			if (StringUtils.isNotBlank(wordId))
			    lstNodeId.add(wordId);
			if (!lstNodeId.isEmpty()) {
				lstNodeId.add(wordId);
				updateResponse.put(GraphDACParams.node_ids.name(), lstNodeId);
			}
			return updateResponse;

		} catch (ClassCastException e) {
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_CONTENT.name(), "Request format incorrect");
		}
	}

	/**
	 * Updates word using the word map object.
	 *
	 * @param languageId
	 *            the language id
	 * @param id
	 *            the id
	 * @param wordMap
	 *            the word map
	 * @return the response
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@CoverageIgnore
	public Response updateWord(String languageId, String id, Map wordMap) throws Exception {
		Node node = null;
		DefinitionDTO definition = getDefinitionDTO(LanguageParams.Word.name(), languageId);
		node = convertToGraphNode(wordMap, definition);
		node.setIdentifier(id);
		node.setObjectType(LanguageParams.Word.name());
		Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		updateReq.put(GraphDACParams.node.name(), node);
		updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
		Response updateRes = getResponse(updateReq, LOGGER);
		return updateRes;
	}

	/**
	 * Gets the data node from Graph.
	 *
	 * @param languageId
	 *            the language id
	 * @param nodeId
	 *            the node id
	 * @param objectType
	 *            the object type
	 * @return the data node
	 * @throws Exception
	 *             the exception
	 */
	@CoverageIgnore
	public Node getDataNode(String languageId, String nodeId, String objectType) throws Exception {
		Request getNodeReq = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode");
		getNodeReq.put(GraphDACParams.node_id.name(), nodeId);
		getNodeReq.put(GraphDACParams.graph_id.name(), languageId);
		Response getNodeRes = getResponse(getNodeReq, LOGGER);
		if (checkError(getNodeRes)) {
			throw new ServerException(LanguageErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(getNodeRes));
		}
		return (Node) getNodeRes.get(GraphDACParams.node.name());
	}

	/**
	 * Gets the definition DTO from the Graph.
	 *
	 * @param definitionName
	 *            the definition name
	 * @param graphId
	 *            the graph id
	 * @return the definition DTO
	 */
	public DefinitionDTO getDefinitionDTO(String definitionName, String graphId) {
		Request requestDefinition = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), definitionName);
		Response responseDefiniton = getResponse(requestDefinition, LOGGER);
		if (checkError(responseDefiniton)) {
			throw new ServerException(LanguageErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(responseDefiniton));
		} else {
			DefinitionDTO definition = (DefinitionDTO) responseDefiniton.get(GraphDACParams.definition_node.name());
			return definition;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IDictionaryManager#findV2(java.lang.String,
	 * java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Response find(String languageId, String id, String[] fields, String version) {
		if (StringUtils.isBlank(languageId))
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
			try {
				Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
				Map<String, Object> map = null;
				if (StringUtils.equalsIgnoreCase(BaseController.API_VERSION, version))
					map = convertGraphNode(node, languageId, "Word", fields, true);
				else
					map = addPrimaryAndOtherMeaningsToWord(node, languageId);
				Response response = copyResponse(getNodeRes);
				response.put(LanguageObjectTypes.Word.name(), map);
				return response;
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				e.printStackTrace();
				return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), ResponseCode.CLIENT_ERROR);
			}

		}
	}
	
	/**
	 * Gets the synsets of a word.
	 *
	 * @param word
	 *            the word
	 * @return the synsets
	 */
	@CoverageIgnore
	private List<Node> getSynsets(Node word) {
	    List<Node> synsets = new ArrayList<Node>();
	    if (null != word && null != word.getInRelations() && !word.getInRelations().isEmpty()) {
	        List<Relation> relations = word.getInRelations();
	        for (Relation rel : relations) {
	            if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(), rel.getRelationType()) 
	                    && StringUtils.equalsIgnoreCase(LanguageParams.Synset.name(), rel.getStartNodeObjectType())) {
	                String synsetId = rel.getStartNodeId();
	                Map<String, Object> metadata = rel.getStartNodeMetadata();
	                Node synset = new Node(synsetId, rel.getStartNodeType(), rel.getStartNodeObjectType());
	                synset.setMetadata(metadata);
	                synsets.add(synset);
	            }
	        }
	    }
	    return synsets;
	}
	
	/**
	 * Gets the synsets as a map from a given synset.
	 *
	 * @param synset
	 *            the synset
	 * @param def
	 *            the def
	 * @return the synset map
	 */
	@CoverageIgnore
	private Map<String, Object> getSynsetMap(Node synset, DefinitionDTO def) {
	    Map<String, Object> map = new HashMap<String, Object>();
	    if (null != synset) {
	        if (null != synset.getMetadata() && !synset.getMetadata().isEmpty())
	            map.putAll(synset.getMetadata());
	        if (null != def.getOutRelations() && !def.getOutRelations().isEmpty()) {
	            Map<String, List<NodeDTO>> relMap = new HashMap<String, List<NodeDTO>>();
	            Map<String, String> relTitleMap = new HashMap<String, String>();
	            for (RelationDefinition rDef : def.getOutRelations()) {
	                relTitleMap.put(rDef.getRelationName(), rDef.getTitle());
	                relMap.put(rDef.getTitle(), new ArrayList<NodeDTO>());
	            }
	            if (null != synset.getOutRelations() && !synset.getOutRelations().isEmpty()) {
	                List<Relation> relations = synset.getOutRelations();
	                for (Relation rel : relations) {
	                    if (StringUtils.equalsIgnoreCase(LanguageParams.Word.name(), rel.getEndNodeObjectType())) {
	                        String title = relTitleMap.get(rel.getRelationType());
	                        if (StringUtils.isNotBlank(title)) {
	                            List<NodeDTO> list = relMap.get(title);
	                            String lemma = (String) rel.getEndNodeMetadata().get(LanguageParams.lemma.name());
	                            list.add(new NodeDTO(rel.getEndNodeId(), lemma, rel.getEndNodeObjectType()));
	                        }
	                    }
	                }
	            }
	            map.putAll(relMap);
	        }
	        if (null != synset.getTags() && !synset.getTags().isEmpty())
	            map.put("tags", synset.getTags());
	        map.put("identifier", synset.getIdentifier());
	    }
	    return map;
	}
	
	/**
	 * Gets the other meaning map from the synset metadata.
	 *
	 * @param synset
	 *            the synset
	 * @return the other meaning map
	 */
	@CoverageIgnore
	private Map<String, Object> getOtherMeaningMap(Node synset) {
	    Map<String, Object> map = new HashMap<String, Object>();
	    if (null != synset) {
	        if (null != synset.getMetadata() && !synset.getMetadata().isEmpty()) {
	            for (Entry<String, Object> entry : synset.getMetadata().entrySet()) {
	                if (!SystemProperties.isSystemProperty(entry.getKey())) {
	                    map.put(entry.getKey(), entry.getValue());
	                }
	            }
	        }
	        map.put("identifier", synset.getIdentifier());
	    }
	    return map;
	}

	/**
	 * Adds the primary and other meanings to word.
	 *
	 * @param node
	 *            the node
	 * @param languageId
	 *            the language id
	 * @return the map
	 */
	@SuppressWarnings("unchecked")
	@CoverageIgnore
	private Map<String, Object> addPrimaryAndOtherMeaningsToWord(Node node, String languageId) {
		try {
			DefinitionDTO defintion = getDefinitionDTO(LanguageParams.Word.name(), languageId);
			Map<String, Object> map = ConvertGraphNode.convertGraphNode(node, languageId, defintion, null);
			String primaryMeaningId = (String) map.get(LanguageParams.primaryMeaningId.name());
			List<Node> synsets = getSynsets(node);
			if (primaryMeaningId != null) {
				if (!isValidSynset(synsets, primaryMeaningId)) {
					primaryMeaningId = null;
					map.put(LanguageParams.primaryMeaningId.name(), null);
				}
			}
			if (primaryMeaningId == null || primaryMeaningId.isEmpty()) {
				if (synsets != null && !synsets.isEmpty()) {
					Node primarySynonym = synsets.get(0);
					primaryMeaningId = primarySynonym.getIdentifier();
					Map<String, Object> updateWordMap = new HashMap<String, Object>();
					updateWordMap.put("identifier", map.get(LanguageParams.identifier.name()));
					updateWordMap.put("lemma", map.get(LanguageParams.lemma.name()));
					updateWordMap.put(LanguageParams.primaryMeaningId.name(), primaryMeaningId);
					map.put(LanguageParams.primaryMeaningId.name(), primaryMeaningId);
					Response updateResponse;
					try {
						updateResponse = updateWord(languageId, (String) map.get(LanguageParams.identifier.name()),
								updateWordMap);
						if (checkError(updateResponse)) {
							throw new ServerException(LanguageErrorCodes.SYSTEM_ERROR.name(),
									wordUtil.getErrorMessage(updateResponse));
						}
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						e.printStackTrace();
					}

				}
			}
			if (primaryMeaningId != null) {
				Node synsetNode = getDataNode(languageId, primaryMeaningId, "Synset");
				DefinitionDTO synsetDefintion = getDefinitionDTO(LanguageParams.Synset.name(), languageId);
				Map<String, Object> synsetMap = getSynsetMap(synsetNode, synsetDefintion);
				String category = (String) synsetMap.get(LanguageParams.category.name());
				if (StringUtils.isNotBlank(category))
					map.put(LanguageParams.category.name(), category);
				
				synsetMap.remove(ATTRIB_EXAMPLE_SENTENCES);
				Object exampleSentences = map.get(ATTRIB_EXAMPLE_SENTENCES);
				if(exampleSentences != null){
					synsetMap.put(ATTRIB_EXAMPLE_SENTENCES, exampleSentences);
				}
				map.remove(ATTRIB_EXAMPLE_SENTENCES);
				
				//remove same word from synset
				
				String wordIdentifier = (String) map.get(LanguageParams.identifier.name());
				List<NodeDTO> synonymsNewList = new ArrayList<NodeDTO>();
				List<NodeDTO> synonymsList = (List<NodeDTO>) synsetMap.get(LanguageParams.synonyms.name());
				if(synonymsList != null){
					for(NodeDTO synonym: synonymsList){
						String synsetIdentifier = synonym.getIdentifier();
						if(synsetIdentifier != null && wordIdentifier != null && !synsetIdentifier.equalsIgnoreCase(wordIdentifier)){
							synonymsNewList.add(synonym);
						}
					}
					synsetMap.put(LanguageParams.synonyms.name(), synonymsNewList);
				}
				
				map.put(LanguageParams.primaryMeaning.name(), synsetMap);
			}
			if (synsets != null && !synsets.isEmpty()) {
				List<Map<String, Object>> otherMeaningsList = new ArrayList<Map<String, Object>>();
				for (int i = 0; i < synsets.size(); i++) {
					Node otherSynonym = synsets.get(i);
					String otherMeaningId = otherSynonym.getIdentifier();
					if (primaryMeaningId != null && !otherMeaningId.equalsIgnoreCase(primaryMeaningId)) {
						Map<String, Object> synsetMap = getOtherMeaningMap(otherSynonym);
						otherMeaningsList.add(synsetMap);
					}
				}
				if (!otherMeaningsList.isEmpty()) {
					map.put(LanguageParams.otherMeanings.name(), otherMeaningsList);
				}
			}
			map.remove(LanguageParams.synonyms.name());
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(LanguageErrorCodes.SYSTEM_ERROR.name(), e.getMessage());
		}
	}

	/**
	 * Checks if is valid synset.
	 *
	 * @param synsets
	 *            the synsets
	 * @param synsetId
	 *            the synset id
	 * @return true, if is valid synset
	 */
	@CoverageIgnore
	private boolean isValidSynset(List<Node> synsets, String synsetId) {
		if (synsets != null) {
			for (Node synsetDTO : synsets) {
				String id = synsetDTO.getIdentifier();
				if (synsetId.equalsIgnoreCase(id)) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#importWordSynset(java.lang.
	 * String, java.io.InputStream)
	 */
	@Override
	@CoverageIgnore
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
		            break;
				}
				case ATTRIB_PRONUNCIATIONS: {
				    pronunciations = new ArrayList<String>();
				    if (StringUtils.isNotBlank(entry.getValue())) {
                        String[] arr = entry.getValue().split(",");
                        pronunciations = Arrays.asList(arr);
                    }
				    break;
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
	
	/**
	 * Creates or update node.
	 *
	 * @param languageId
	 *            the language id
	 * @param node
	 *            the node
	 * @return the response
	 */
	@CoverageIgnore
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

	/**
	 * Creates the data node in the graph.
	 *
	 * @param languageId
	 *            the language id
	 * @param synsetNode
	 *            the synset node
	 * @return the response
	 */
	@CoverageIgnore
	private Response createDataNode(String languageId, Node synsetNode) {
		Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		updateReq.put(GraphDACParams.node.name(), synsetNode);
		Response updateRes = getResponse(updateReq, LOGGER);
		return updateRes;
	}

	/**
	 * Adds the error message per row.
	 *
	 * @param rowNo
	 *            the row no
	 * @param error
	 *            the error
	 * @param errorMessageMap
	 *            the error message map
	 */
	@CoverageIgnore
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

	/**
	 * Creates the relation between the nodes.
	 *
	 * @param endNodeId
	 *            the end node id
	 * @param relationType
	 *            the relation type
	 * @param languageId
	 *            the language id
	 * @param startNodeId
	 *            the start node id
	 * @return the response
	 */
	@CoverageIgnore
	private Response createRelation(String endNodeId, String relationType, String languageId, String startNodeId) {
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
		request.put(GraphDACParams.start_node_id.name(), startNodeId);
		request.put(GraphDACParams.relation_type.name(), relationType);
		request.put(GraphDACParams.end_node_id.name(), endNodeId);
		Response response = getResponse(request, LOGGER);
		return response;
	}

	/**
	 * Updates the data node.
	 *
	 * @param languageId
	 *            the language id
	 * @param synsetNode
	 *            the synset node
	 * @return the response
	 */
	@CoverageIgnore
	private Response updateDataNode(String languageId, Node synsetNode) {
		Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		updateReq.put(GraphDACParams.node.name(), synsetNode);
		updateReq.put(GraphDACParams.node_id.name(), synsetNode.getIdentifier());
		Response updateRes = getResponse(updateReq, LOGGER);
		return updateRes;
		
	}

	/**
	 * Gets the defintiion DTO from the graph.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @return the defintiion
	 */
	@CoverageIgnore
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

	/**
	 * Read from CSV and returns as a Map.
	 *
	 * @param inputStream
	 *            the input stream
	 * @return the list
	 * @throws JsonProcessingException
	 *             the json processing exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@CoverageIgnore
	private List<Map<String,String>> readFromCSV(InputStream inputStream) throws JsonProcessingException, IOException {
		List<Map<String,String>> records =  new ArrayList<Map<String,String>>();
		try (InputStreamReader isReader = new InputStreamReader(inputStream, "UTF8");
				CSVParser csvReader = new CSVParser(isReader, csvFileFormat)) {
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
	        return records;
		}
	}
	
	/**
	 * Asynchronously enriches the words.
	 *
	 * @param nodeIds
	 *            the node ids
	 * @param languageId
	 *            the language id
	 */
	@CoverageIgnore
	private void asyncUpdate(List<String> nodeIds, String languageId) {
	    Map<String, Object> map = new HashMap<String, Object>();
        map = new HashMap<String, Object>();
        map.put(LanguageParams.node_ids.name(), nodeIds);
        Request request = new Request();
        request.setRequest(map);
        request.setManagerName(LanguageActorNames.ENRICH_ACTOR.name());
        request.setOperation(LanguageOperations.enrichWords.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);
        makeAsyncLanguageRequest(request, LOGGER);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#loadEnglishWordsArpabetsMap(
	 * java.io.InputStream)
	 */
	@Override
	@CoverageIgnore
	public Response loadEnglishWordsArpabetsMap(InputStream in){

		Map<String, Object> map = new HashMap<String, Object>();
        map = new HashMap<String, Object>();
        map.put(LanguageParams.input_stream.name(), in);
        Request request = new Request();
        request.setRequest(map);
        request.setManagerName(LanguageActorNames.TRANSLITERATOR_ACTOR.name());
        request.setOperation(LanguageOperations.loadWordsArpabetsMap.name());
        request.getContext().put(LanguageParams.language_id.name(), "en");

        return getLanguageResponse(request, LOGGER);
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#getSyllables(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	@CoverageIgnore
	public Response getSyllables(String languageId, String word){

		Map<String, Object> map = new HashMap<String, Object>();
        map = new HashMap<String, Object>();
        map.put(LanguageParams.word.name(), word);
        Request request = new Request();
        request.setRequest(map);
        request.setManagerName(LanguageActorNames.TRANSLITERATOR_ACTOR.name());
        request.setOperation(LanguageOperations.getSyllables.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);

        return getLanguageResponse(request, LOGGER);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#getArpabets(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	@CoverageIgnore
	public Response getArpabets(String languageId, String word){

		Map<String, Object> map = new HashMap<String, Object>();
        map = new HashMap<String, Object>();
        map.put(LanguageParams.word.name(), word);
        Request request = new Request();
        request.setRequest(map);
        request.setManagerName(LanguageActorNames.TRANSLITERATOR_ACTOR.name());
        request.setOperation(LanguageOperations.getArpabets.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);

        return getLanguageResponse(request, LOGGER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#getPhoneticSpellingByLanguage(
	 * java.lang.String, java.lang.String, boolean)
	 */
	@Override
	@CoverageIgnore
	public Response getPhoneticSpellingByLanguage(String languageId, String word, boolean addEndVirama){

		Map<String, Object> map = new HashMap<String, Object>();
        map = new HashMap<String, Object>();
        map.put(LanguageParams.word.name(), word);
        map.put(LanguageParams.addClosingVirama.name(), addEndVirama);
        Request request = new Request();
        request.setRequest(map);
        request.setManagerName(LanguageActorNames.TRANSLITERATOR_ACTOR.name());
        request.setOperation(LanguageOperations.getPhoneticSpellingByLanguage.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);

        return getLanguageResponse(request, LOGGER);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#getSimilarSoundWords(java.lang
	 * .String, java.lang.String)
	 */
	@Override
	@CoverageIgnore
	public Response getSimilarSoundWords(String languageId, String word){

		Map<String, Object> map = new HashMap<String, Object>();
        map = new HashMap<String, Object>();
        map.put(LanguageParams.word.name(), word);
        Request request = new Request();
        request.setRequest(map);
        request.setManagerName(LanguageActorNames.TRANSLITERATOR_ACTOR.name());
        request.setOperation(LanguageOperations.getSimilarSoundWords.name());
        request.getContext().put(LanguageParams.language_id.name(), languageId);

        return getLanguageResponse(request, LOGGER);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#transliterate(java.lang
	 * .String, com.ilimi.common.dto.Request, boolean)
	 */
	@Override
	@CoverageIgnore
	public Response transliterate(String languageId, Request request, boolean addEndVirama) {
		String text = (String) request.get("text");
		
	    Map<String, Object> map = new HashMap<String, Object>();
        map = new HashMap<String, Object>();
        map.put(LanguageParams.text.name(), text);
        map.put(LanguageParams.addClosingVirama.name(), addEndVirama);
        Request transliterateRequest = new Request();
        transliterateRequest.setRequest(map);
        transliterateRequest.setManagerName(LanguageActorNames.TRANSLITERATOR_ACTOR.name());
        transliterateRequest.setOperation(LanguageOperations.transliterate.name());
        transliterateRequest.getContext().put(LanguageParams.language_id.name(), languageId);

        return getLanguageResponse(transliterateRequest, LOGGER);
	}
	
}
