package org.ekstep.language.mgr.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.CoverageIgnore;
import org.ekstep.common.dto.NodeDTO;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.MiddlewareException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.ConvertGraphNode;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.graph.common.JSONUtils;
import org.ekstep.graph.dac.enums.AuditProperties;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.enums.SystemProperties;
import org.ekstep.graph.dac.model.Filter;
import org.ekstep.graph.dac.model.MetadataCriterion;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.dac.model.SearchConditions;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.dac.model.Sort;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.MetadataDefinition;
import org.ekstep.graph.model.node.RelationDefinition;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IDictionaryManager;
import org.ekstep.language.util.BaseLanguageManager;
import org.ekstep.language.util.IWordnetConstants;
import org.ekstep.language.util.WordUtil;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

// TODO: Auto-generated Javadoc
/**
 * Provides implementation for word, synsets and relations manipulations.
 *
 * @author Amarnath, Rayulu, Azhar and karthik
 */
@Component
public class DictionaryManagerImpl extends BaseLanguageManager implements IDictionaryManager, IWordnetConstants {

	/** The logger. */
	

	/** The Constant LEMMA_PROPERTY. */
	private static final String LEMMA_PROPERTY = "lemma";

	/** The Constant DEFAULT_STATUS. */
	private static final List<String> DEFAULT_STATUS = new ArrayList<String>();

	/** The Constant s3Media. */
	private static final String s3Media = "s3.media.folder";

	/** The Constant special. */
	// inside regex all special character mentioned!, by default inside string
	// black slash and double quote should be escaped using black slash, in
	// addition to that here open bracket([) and close bracket(]) has been
	// escaped and dash(-) were mentioned at last special character for regex
	// constraints to work
	private static final Pattern special = Pattern.compile(".*[`~!@#$%^&*()_=+\\[\\]{}|\\;:'\",<.>/?-].*");

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
	 * @param languageId
	 *            the languageId
	 * @return true, if is valid word
	 */
	public boolean isValidWord(String word, String languageId) {
		try {

			Matcher hasSpecial = special.matcher(word);
			if (hasSpecial.matches()) {
				return false;
			}

			char firstLetter = word.charAt(0);
			int i = firstLetter;
			String uc = String.format("%04x", i);
			int hexVal = Integer.parseInt(uc, 16);
			Node languageNode = getDataNode("domain", "lang_" + languageId, "Language");
			String startUnicode = (String) languageNode.getMetadata().get("startUnicode");
			String endUnicode = (String) languageNode.getMetadata().get("endUnicode");

			if (startUnicode != null && endUnicode != null) {
				int min = Integer.parseInt(startUnicode, 16);
				int max = Integer.parseInt(endUnicode, 16);
				if (hexVal >= min && hexVal <= max) {
				} else {
					return false;
				}
			}

		} catch (Exception e) {
			// return true... if language object is not defined...
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IDictionaryManager#findAll(java.lang.String,
	 * java.lang.String, java.lang.String[], java.lang.Integer,
	 * java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response findAll(String languageId, String objectType, String[] fields, Integer limit, String version) {
		if (StringUtils.isBlank(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "Object Type is blank");
		TelemetryManager.log("Find All Content : " + languageId + ", ObjectType: " + objectType);
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
		Response findRes = getResponse(request);
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
			TelemetryManager.error("Exception: "+ e.getMessage(), e);
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
						TelemetryManager.error("Exception: "+ e.getMessage(), e);
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
			for (int i = 0; i < headers.size(); i++) {
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
					for (int j = 0; j < list.size(); j++) {
						str += StringEscapeUtils.escapeCsv(list.get(j).toString());
						if (j < list.size() - 1)
							str += ",";
					}
					row[i] = str;
				} else if (val instanceof Object[]) {
					Object[] arr = (Object[]) val;
					String str = "";
					for (int j = 0; j < arr.length; j++) {
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
		} catch (Exception e) {
			row[i] = "";
			TelemetryManager.error("Exception: "+ e.getMessage(), e);
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
		Response findRes = getResponse(request);
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
		return getResponse(request);
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
		return getResponse(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IDictionaryManager#list(java.lang.String,
	 * java.lang.String, org.ekstep.common.dto.Request, java.lang.String)
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
				}
				// PARAM_TAGS - specific condition removed.
			}
		}
		if (null != filters && !filters.isEmpty()) {
			MetadataCriterion mc = MetadataCriterion.create(filters);
			sc.addMetadata(mc);
		}
		Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		req.put(GraphDACParams.get_tags.name(), true);
		Response listRes = getResponse(req);
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
				map.put("keywords", node.getTags());
			}
			map.put("identifier", node.getIdentifier());
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
		getInRelationsData(node, synonyms, antonyms, hypernyms, hyponyms, homonyms, meronyms, tools, workers, actions,
				objects, converse, synsetIds);
		getOutRelationsData(node, synonyms, antonyms, hypernyms, hyponyms, homonyms, meronyms, tools, workers, actions,
				objects, converse, synsetIds);
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
			Response res = getResponse(req);
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
			List<NodeDTO> tools, List<NodeDTO> workers, List<NodeDTO> actions, List<NodeDTO> objects,
			List<NodeDTO> converse, List<String> synsetIds) {
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
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.TOOL.relationName(), inRel.getRelationType())) {
					tools.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.WORKER.relationName(), inRel.getRelationType())) {
					workers.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.ACTION.relationName(), inRel.getRelationType())) {
					actions.add(new NodeDTO(inRel.getStartNodeId(), getInRelationWord(inRel),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.OBJECT.relationName(), inRel.getRelationType())) {
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
			List<NodeDTO> tools, List<NodeDTO> workers, List<NodeDTO> actions, List<NodeDTO> objects,
			List<NodeDTO> converse, List<String> synsetIds) {
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
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.TOOL.relationName(), outRel.getRelationType())) {
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
		Response res = getResponse(req);
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
				Response listRes = getResponse(req);
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
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
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
	 * java.lang.String, org.ekstep.common.dto.Request, boolean)
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
			Response errorRes = null;
			List<String> lstNodeId = new ArrayList<String>();
			List<String> errorMessages = new ArrayList<String>();
			List<Map> items = (List<Map>) request.get(LanguageParams.words.name());
			if (null == items || items.size() <= 0)
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT.name(),
						objectType + " Object is blank");
			try {
				if (null != items && !items.isEmpty()) {
					for (Map item : items) {
						String lemma = (String) item.get(LanguageParams.lemma.name());
						if (null == lemma)
							throw new ClientException(LanguageErrorCodes.ERR_CREATE_WORD.name(),
									"Word - lemma can not be empty");
						lemma = lemma.trim();
						if(languageId.equalsIgnoreCase("en"))
							lemma = lemma.toLowerCase();
						item.put(LanguageParams.lemma.name(), lemma);
						boolean isValid = isValidWord(lemma, languageId);
						if (!isValid) {
							Matcher hasSpecial = special.matcher(lemma);
							if (hasSpecial.matches()) {
								return ERROR(LanguageErrorCodes.ERR_CREATE_WORD.name(),
										"Word should not contain any special characters ", ResponseCode.CLIENT_ERROR);
							} else {
								return ERROR(LanguageErrorCodes.ERR_CREATE_WORD.name(),
										"Word cannot be in a different language", ResponseCode.CLIENT_ERROR);
							}
						}

						if (item.get(LanguageParams.primaryMeaning.name()) == null) {
							return ERROR(LanguageErrorCodes.ERROR_PRIMARY_MEANING_EMPTY.name(),
									"Primary meaning field is mandatory for word", ResponseCode.SERVER_ERROR);
						}

						Response wordResponse = createOrUpdateWord(languageId, item, lstNodeId, forceUpdate, true);
						if (checkError(wordResponse)) {
							errorMessages.add(wordUtil.getErrorMessage(wordResponse));
							errorRes = wordResponse;
						}
						List<String> errorMessage = (List<String>) wordResponse
								.get(LanguageParams.error_messages.name());
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
			} catch (Exception e) {
				errorMessages.add(e.getMessage());
			}
			if(items.size()==1&&errorRes!=null) {
				return errorRes;
			}
				
			createRes.put(GraphDACParams.node_ids.name(), lstNodeId);
			if (!errorMessages.isEmpty()) {
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
	@SuppressWarnings({ "rawtypes", "unused" })
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
	@SuppressWarnings("unused")
	private void removeSynsetRelation(String wordId, String relationType, String languageId, String synsetId) {
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "removeRelation");
		request.put(GraphDACParams.start_node_id.name(), synsetId);
		request.put(GraphDACParams.relation_type.name(), relationType);
		request.put(GraphDACParams.end_node_id.name(), wordId);
		Response response = getResponse(request);
		if (checkError(response)) {
			throw new ServerException(response.getParams().getErr(), response.getParams().getErrmsg());
		}
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
		Response getNodeRes = getResponse(getNodeReq);
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
		Response responseDefiniton = getResponse(requestDefinition);
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
		Response getNodeRes = getResponse(request);
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
				TelemetryManager.error("Exception: "+ e.getMessage(), e);
				e.printStackTrace();
				return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), ResponseCode.CLIENT_ERROR);
			}

		}
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
						if (StringUtils.equalsIgnoreCase(LanguageParams.Word.name(), rel.getEndNodeObjectType())
								&& StringUtils.equalsIgnoreCase(rel.getRelationType(), RelationTypes.SYNONYM.name())) {
							String title = relTitleMap.get(rel.getRelationType());
							if (StringUtils.isNotBlank(title)) {
								List<NodeDTO> list = relMap.get(title);
								String lemma = (String) rel.getEndNodeMetadata().get(LanguageParams.lemma.name());
								String status = (String) rel.getEndNodeMetadata().get(LanguageParams.status.name());
								if (status != LanguageParams.Retired.name())
									list.add(new NodeDTO(rel.getEndNodeId(), lemma, rel.getEndNodeObjectType()));
							}
						} else if (StringUtils.equalsIgnoreCase(LanguageParams.Synset.name(),
								rel.getEndNodeObjectType())) {
							String title = relTitleMap.get(rel.getRelationType());
							if (StringUtils.isNotBlank(title)) {
								List<NodeDTO> list = relMap.get(title);
								String gloss = (String) rel.getEndNodeMetadata().get(LanguageParams.gloss.name());
								list.add(new NodeDTO(rel.getEndNodeId(), gloss, rel.getEndNodeObjectType()));
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
	 * correct the synset relation synset's relation other than synonym should
	 * be another synset, if it is word, word's primary meaning will be its
	 * relation
	 *
	 * @param synset
	 *            the synset
	 * @param def
	 *            the def
	 * @return the synset map
	 */
	@CoverageIgnore
	private Node correctSynset(String languageId, Node synset) throws Exception {

		boolean update = false;
		if (null != synset.getOutRelations() && !synset.getOutRelations().isEmpty()) {
			List<Relation> relations = synset.getOutRelations();
			for (Relation rel : relations) {
				if (StringUtils.equalsIgnoreCase(LanguageParams.Word.name(), rel.getEndNodeObjectType())
						&& !StringUtils.equalsIgnoreCase(rel.getRelationType(), RelationTypes.SYNONYM.name())) {
					TelemetryManager
							.log("correcting synset for synser id -" + synset.getIdentifier() + " and relation type - "
									+ rel.getRelationType() + "related word Id -" + rel.getEndNodeId());
					update = true;
					String wordId = rel.getEndNodeId();
					try {
						Node wordNode = getDataNode(languageId, wordId, LanguageParams.Word.name());
						DefinitionDTO defintion = getDefinitionDTO(LanguageParams.Word.name(), languageId);
						Map<String, Object> map = ConvertGraphNode.convertGraphNode(wordNode, languageId, defintion, null);
						List<Node> synsets = wordUtil.getSynsets(wordNode);
						String primaryMeaningId = wordUtil.updatePrimaryMeaning(languageId, map, synsets);

//						String primaryMeaningId = (String) wordNode.getMetadata()
//								.get(LanguageParams.primaryMeaningId.name());
						String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
						if (primaryMeaningId == null) {
							primaryMeaningId = createSynset(languageId, lemma, wordNode.getIdentifier(), new ArrayList<>(), null);
							//set primary meaning id into words metadata
							setPrimaryMeaningId(languageId, wordId, primaryMeaningId);
							//add relation between word and newly created primary meaning/synset
							addRelation(languageId, LanguageParams.Synset.name(), primaryMeaningId,
									RelationTypes.SYNONYM.relationName(), wordId);
						}

						deleteRelation(languageId, LanguageParams.Synset.name(), synset.getIdentifier(),
								rel.getRelationType(), wordId);

						addRelation(languageId, LanguageParams.Synset.name(), synset.getIdentifier(),
								rel.getRelationType(), primaryMeaningId);

					} catch (Exception e) {
						TelemetryManager.error("error while correcting synset for synset id-" + synset.getIdentifier() + " ,"
								+ e.getMessage(), e);
						throw e;
					}
				}
			}

			if (update)
				return getDataNode(languageId, synset.getIdentifier(), LanguageParams.Synset.name());
		}

		return synset;
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
	@CoverageIgnore
	private Map<String, Object> addPrimaryAndOtherMeaningsToWord(Node node, String languageId) {
		try {
			DefinitionDTO defintion = getDefinitionDTO(LanguageParams.Word.name(), languageId);
			Map<String, Object> map = ConvertGraphNode.convertGraphNode(node, languageId, defintion, null);
			String wordIdentifier = (String) map.get(LanguageParams.identifier.name());
			List<Node> synsets = wordUtil.getSynsets(node);

			String primaryMeaningId = wordUtil.updatePrimaryMeaning(languageId, map, synsets);

			if (primaryMeaningId != null) {
				Map<String, Object> primaryMeaningMap = getMeaningObjectMap(languageId, primaryMeaningId,
						wordIdentifier);
				String category = (String) primaryMeaningMap.get(LanguageParams.category.name());
				if (StringUtils.isNotBlank(category))
					map.put(LanguageParams.category.name(), category);
				primaryMeaningMap.remove(ATTRIB_EXAMPLE_SENTENCES);
				Object exampleSentences = map.get(ATTRIB_EXAMPLE_SENTENCES);
				if (exampleSentences != null) {
					primaryMeaningMap.put(ATTRIB_EXAMPLE_SENTENCES, exampleSentences);
				}
				map.remove(ATTRIB_EXAMPLE_SENTENCES);
				map.put(LanguageParams.primaryMeaning.name(), primaryMeaningMap);
			}
			if (synsets != null && !synsets.isEmpty()) {
				List<Map<String, Object>> otherMeaningsList = new ArrayList<Map<String, Object>>();
				for (int i = 0; i < synsets.size(); i++) {
					Node otherSynonym = synsets.get(i);
					String otherMeaningId = otherSynonym.getIdentifier();
					if (primaryMeaningId != null && !otherMeaningId.equalsIgnoreCase(primaryMeaningId)) {
						Map<String, Object> otherMeaning = getMeaningObjectMap(languageId, otherMeaningId,
								wordIdentifier);
						otherMeaningsList.add(otherMeaning);
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
		if (node.getIdentifier() == null) {
			reponse = createDataNode(languageId, node);
		} else {
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
		Response updateRes = getResponse(updateReq);
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
		if (errorMessage == null) {
			errorMessage = error;
		} else {
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
		Response response = getResponse(request);
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
		Response updateRes = getResponse(updateReq);
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
		Response responseDefiniton = getResponse(requestDefinition);
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
	private List<Map<String, String>> readFromCSV(InputStream inputStream) throws JsonProcessingException, IOException {
		List<Map<String, String>> records = new ArrayList<Map<String, String>>();
		try (InputStreamReader isReader = new InputStreamReader(inputStream, "UTF8");
				CSVParser csvReader = new CSVParser(isReader, csvFileFormat)) {
			List<String> headers = new ArrayList<String>();
			List<CSVRecord> recordsList = csvReader.getRecords();
			if (null != recordsList && !recordsList.isEmpty()) {
				for (int i = 0; i < recordsList.size(); i++) {
					if (i == 0) {
						CSVRecord headerecord = recordsList.get(i);
						for (int j = 0; j < headerecord.size(); j++) {
							String val = headerecord.get(j);
							headers.add(val);
						}
					} else {
						Map<String, String> csvRecord = new HashMap<String, String>();
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
		makeAsyncLanguageRequest(request);
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
	public Response loadEnglishWordsArpabetsMap(InputStream in) {

		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put(LanguageParams.input_stream.name(), in);
		Request request = new Request();
		request.setRequest(map);
		request.setManagerName(LanguageActorNames.TRANSLITERATOR_ACTOR.name());
		request.setOperation(LanguageOperations.loadWordsArpabetsMap.name());
		request.getContext().put(LanguageParams.language_id.name(), "en");

		return getLanguageResponse(request);

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
	public Response getSyllables(String languageId, String word) {

		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put(LanguageParams.word.name(), word);
		Request request = new Request();
		request.setRequest(map);
		request.setManagerName(LanguageActorNames.TRANSLITERATOR_ACTOR.name());
		request.setOperation(LanguageOperations.getSyllables.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);

		return getLanguageResponse(request);
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
	public Response getArpabets(String languageId, String word) {

		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put(LanguageParams.word.name(), word);
		Request request = new Request();
		request.setRequest(map);
		request.setManagerName(LanguageActorNames.TRANSLITERATOR_ACTOR.name());
		request.setOperation(LanguageOperations.getArpabets.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);

		return getLanguageResponse(request);
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
	public Response getPhoneticSpellingByLanguage(String languageId, String word, boolean addEndVirama) {

		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put(LanguageParams.word.name(), word);
		map.put(LanguageParams.addClosingVirama.name(), addEndVirama);
		Request request = new Request();
		request.setRequest(map);
		request.setManagerName(LanguageActorNames.TRANSLITERATOR_ACTOR.name());
		request.setOperation(LanguageOperations.getPhoneticSpellingByLanguage.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);

		return getLanguageResponse(request);
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
	public Response getSimilarSoundWords(String languageId, String word) {

		Map<String, Object> map = new HashMap<String, Object>();
		map = new HashMap<String, Object>();
		map.put(LanguageParams.word.name(), word);
		Request request = new Request();
		request.setRequest(map);
		request.setManagerName(LanguageActorNames.TRANSLITERATOR_ACTOR.name());
		request.setOperation(LanguageOperations.getSimilarSoundWords.name());
		request.getContext().put(LanguageParams.language_id.name(), languageId);

		return getLanguageResponse(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.mgr.IDictionaryManager#transliterate(java.lang
	 * .String, org.ekstep.common.dto.Request, boolean)
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

		return getLanguageResponse(transliterateRequest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IDictionaryManager#partialUpdateWordV2(java.lang.
	 * String, java.lang.String, java.lang.String, org.ekstep.common.dto.Request,
	 * boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response updateWordV2(String languageId, String id, String objectType, Request request,
			boolean forceUpdate) {
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
			if (lemma != null) {
				lemma = lemma.trim();
				if(languageId.equalsIgnoreCase("en"))
					lemma = lemma.toLowerCase();
				item.put(LanguageParams.lemma.name(), lemma);
				boolean isValid = isValidWord(lemma, languageId);
				if (!isValid) {
					Matcher hasSpecial = special.matcher(lemma);
					if (hasSpecial.matches()) {
						return ERROR(LanguageErrorCodes.ERR_CREATE_WORD.name(),
								"Word should not contain any special characters ", ResponseCode.CLIENT_ERROR);
					} else {
						return ERROR(LanguageErrorCodes.ERR_CREATE_WORD.name(),
								"Word cannot be in a different language", ResponseCode.CLIENT_ERROR);
					}
				}
			}
			List<String> lstNodeId = new ArrayList<String>();

			String wordStatus = (String) item.get(LanguageParams.status.name());
			if (StringUtils.isEmpty(wordStatus)) {
				item.put(LanguageParams.status.name(), LanguageParams.Live.name());
			}

			Response updateResponse = createOrUpdateWord(languageId, item, lstNodeId, forceUpdate, false);
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
	 * Gets the meaning object map.
	 *
	 * @param languageId
	 *            the language id
	 * @param meaningId
	 *            the meaning id
	 * @param wordId
	 *            the word id
	 * @return the meaning object map
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	private Map<String, Object> getMeaningObjectMap(String languageId, String meaningId, String wordId) {
		try {
			Node synsetNode = getDataNode(languageId, meaningId, "Synset");
			// Added for correcting synset relationship between from synset and
			// words to synset and synset
			synsetNode = correctSynset(languageId, synsetNode);
			DefinitionDTO synsetDefintion = getDefinitionDTO(LanguageParams.Synset.name(), languageId);
			Map<String, Object> synsetMap = getSynsetMap(synsetNode, synsetDefintion);

			// remove same word from synset
			List<NodeDTO> synonymsNewList = new ArrayList<NodeDTO>();
			List<NodeDTO> synonymsList = (List<NodeDTO>) synsetMap.get(LanguageParams.synonyms.name());
			if (synonymsList != null) {
				for (NodeDTO synonym : synonymsList) {
					String synsetIdentifier = synonym.getIdentifier();
					if (synsetIdentifier != null && wordId != null && !synsetIdentifier.equalsIgnoreCase(wordId)) {
						synonymsNewList.add(synonym);
					}
				}
				synsetMap.put(LanguageParams.synonyms.name(), synonymsNewList);
			}

			for (Entry<String, Object> entry : synsetMap.entrySet()) {
				if (wordUtil.getRelations().contains(entry.getKey())
						&& !entry.getKey().equalsIgnoreCase(LanguageParams.synonyms.name())) {

					List<NodeDTO> wordList = new ArrayList<NodeDTO>();
					List<NodeDTO> relatedSynsets = (List<NodeDTO>) entry.getValue();

					if (relatedSynsets != null) {
						for (NodeDTO relatedSynset : relatedSynsets) {
							String synsetIdentifier = relatedSynset.getIdentifier();
							if (synsetIdentifier != null) {
								Node relatedSynsetNode = getDataNode(languageId, synsetIdentifier, "Synset");
								List<String> relatedWordIds = getRelatedWordIds(relatedSynsetNode, meaningId,
										wordUtil.getRelationName(entry.getKey()));
								Map<String, Object> relatedSynsetMap = getSynsetMap(relatedSynsetNode, synsetDefintion);
								List<NodeDTO> relatedSynsetWordList = (List<NodeDTO>) relatedSynsetMap
										.get(LanguageParams.synonyms.name());
								if (relatedSynsetWordList != null) {
									// if relatedWordIds is available in
									// relation, then only put those word nodes
									// alone otherwise put all valid word nodes
									List<NodeDTO> filteredWordNodes;
									if (relatedWordIds != null)
										filteredWordNodes = relatedSynsetWordList.stream()
												.filter(relatedSynsetWord -> relatedWordIds
														.contains(relatedSynsetWord.getIdentifier()))
												.collect(Collectors.toList());
									else
										filteredWordNodes = relatedSynsetWordList.stream()
												.filter(relatedSynsetWord -> isValidWord(relatedSynsetWord.getName(),
														languageId))
												.collect(Collectors.toList());
									wordList.addAll(filteredWordNodes);
								}
							}
						}
					}

					entry.setValue(wordList);
				}
			}

			return synsetMap;

		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(LanguageErrorCodes.SYSTEM_ERROR.name(), e.getMessage());
		}
	}

	/**
	 * Creates or updates the word.
	 *
	 *
	 *1) search words {all related words in all meanings} and store it in LemmaWordMap
	 *2) get Node {main word} if it is already exist
	 *3) update main word metadata into main word object
	 *4) create Synset Node Object for primary meaning
	 *5) update/create Synset Object(from step 3) into graph
	 *6) update PrimaryMeaningId in main word object and check/update primary meaning of all synonym words in primary meaning
	 *7) do step 4 and 5 each other meaning
	 *8) if word is already exist, then remove old meaning relations(PrimaryMeaning/OtherMeaning) for main word
	 *
	 *
	 * @param languageId
	 *            the language id
	 * @param wordRequestMap
	 *            the word request map
	 * @param wordIds
	 *            the word ids
	 * @param forceUpdate
	 *            the force update
	 * @param createReq TODO
	 * @return the response
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private Response createOrUpdateWord(String languageId, Map<String, Object> wordRequestMap, List<String> wordIds,
			boolean forceUpdate, boolean createReq) {

		List<String> errorMessages = new ArrayList<String>();
		Response wordResponse = new Response();
		try {
			//search words{ all related words in all meaning } and store it in map for future reference 
			Map<String, Object> lemmaWordMap = getLemmaWordMap(languageId, wordRequestMap);
			String wordIdentifier = (String) wordRequestMap.get(LanguageParams.identifier.name());
			List<String> wordSynsetIds = new ArrayList<>();

			String lemma = (String) wordRequestMap.get(LanguageParams.lemma.name());
			if (lemma != null) {
				lemma = lemma.trim();
				wordRequestMap.put(LanguageParams.lemma.name(), lemma);
			}

			//get Word node if it is already exist
			Node existingWordNode = null;
			if (wordIdentifier == null) {
				existingWordNode = wordUtil.searchWord(languageId, lemma);
				if (existingWordNode != null) {
					/*wordIdentifier = existingWordNode.getIdentifier();
					wordRequestMap.put(LanguageParams.identifier.name(), wordIdentifier);*/
					return ERROR(LanguageErrorCodes.ERR_DUPLICATE_WORD.name(), "Word is already exist", ResponseCode.CLIENT_ERROR);
				}
			} else {
				existingWordNode = getDataNode(languageId, wordIdentifier, LanguageParams.Word.name());
				lemma = existingWordNode.getMetadata().get(LanguageParams.lemma.name()).toString();
			}

			//remove primary/othermeaning and tags from main word request object
			Map<String, Object> primaryMeaning = (Map<String, Object>) wordRequestMap
					.get(LanguageParams.primaryMeaning.name());
			wordRequestMap.remove(LanguageParams.primaryMeaning.name());

			List<Map<String, Object>> otherMeanings = (List<Map<String, Object>>) wordRequestMap
					.get(LanguageParams.otherMeanings.name());
			
			wordRequestMap.remove(LanguageParams.otherMeanings.name());
			
			List<String> keywords = (List<String>) wordRequestMap.get(LanguageParams.tags.name());
			wordRequestMap.remove(LanguageParams.tags.name());

			Node word = new Node(wordIdentifier, SystemNodeTypes.DATA_NODE.name(), "Word");
			if (keywords != null) {
				word.setTags(keywords);
			}

			word.setMetadata(wordRequestMap);

			//create/update word object node into graph
			wordResponse = createOrUpdateNode(languageId, word);

			if (checkError(wordResponse)) {
				errorMessages.add(wordUtil.getErrorMessage(wordResponse));
				return wordResponse;
			} else {
				wordIdentifier = (String) wordResponse.get(GraphDACParams.node_id.name());
			}
			
			Map<String, Object> primaryMeaningSynonym = null;
			String primaryMeaningId = null;

			if (primaryMeaning != null) {

				//copy primary meaning's synonym words into map(primaryMeaningSynonym)
				if (primaryMeaning.get(LanguageParams.synonyms.name()) != null) {
					List<Map<String, Object>> synonyms = (List<Map<String, Object>>) primaryMeaning
							.get(LanguageParams.synonyms.name());
					if (!synonyms.isEmpty()) {
						List<Map<String, Object>> synonymCopy = new ArrayList<>(synonyms);
						primaryMeaningSynonym = new HashMap<>();
						primaryMeaningSynonym.put(LanguageParams.synonyms.name(), synonymCopy);
					}
				}
				
				//get synset node object for primary meaning
				Node primaryMeaningSynset = createNodeObjectForSynset(languageId, primaryMeaning, wordIdentifier, lemma, lemmaWordMap, wordIds,
						errorMessages, true, false);
				//create/update node for primary meaning
				Response synsetResponse = createOrUpdateNode(languageId, primaryMeaningSynset);
				if (checkError(synsetResponse)) {
					return synsetResponse;
				}
				primaryMeaningId = (String) synsetResponse.get(GraphDACParams.node_id.name());
				wordRequestMap.put(LanguageParams.primaryMeaningId.name(), primaryMeaningId);

				//add  primary meaning id into wordSynsetIds
				wordSynsetIds.add(primaryMeaningId);

				//check/update primary meaning of all synonym words in primary meaning
				if (primaryMeaningSynonym != null) {
					List<String> synonyms = getRelatedWordLemmaFrom(languageId, primaryMeaningSynonym);
					//List<Node> synonmNodes = searchWords(languageId, synonms);
					for (String synonym : synonyms) {
						Node pmSynonymWord = (Node) lemmaWordMap.get(synonym);
						if (pmSynonymWord != null) {
							String existingPmId = (String) pmSynonymWord.getMetadata()
									.get(LanguageParams.primaryMeaningId.name());
							String synWordLemma = (String) pmSynonymWord.getMetadata().get(LanguageParams.lemma.name());
							if (existingPmId != null && primaryMeaningId.equalsIgnoreCase(existingPmId)) {

							} else {
								if (forceUpdate || existingPmId == null) {
									setPrimaryMeaningId(languageId, pmSynonymWord.getIdentifier(), primaryMeaningId);
								} else {
									errorMessages.add("Word '" + synWordLemma + "' has a different primary meaning");
								}
							}
						}
					}
				}
				
				if (existingWordNode != null) {
					String existingPrimaryMeaningId = (String) existingWordNode.getMetadata()
							.get(LanguageParams.primaryMeaningId.name());
					if (!StringUtils.equalsIgnoreCase(primaryMeaningId, existingPrimaryMeaningId)) {
						setPrimaryMeaningId(languageId, wordIdentifier, primaryMeaningId);
					}
				} else {
					setPrimaryMeaningId(languageId, wordIdentifier, primaryMeaningId);
				}

			}

			//create/update synset object for all other meanings and add synonym relation between other meaning and main word 
			if (otherMeanings != null) {
				for (Map<String, Object> otherMeaning : otherMeanings) {
					Node otherMeaningSynset = createNodeObjectForSynset(languageId, otherMeaning, wordIdentifier, lemma, lemmaWordMap, wordIds,
							errorMessages, false, false);
					Response synsetResponse = createOrUpdateNode(languageId, otherMeaningSynset);
					if (checkError(synsetResponse)) {
						return synsetResponse;
					}
					String otherMeaningId = (String) synsetResponse.get(GraphDACParams.node_id.name());
					//add  other meaning id into wordSynsetIds
					wordSynsetIds.add(otherMeaningId);
				}
			}

			//if word is already exist, then remove old meaning relations(PrimaryMeaning/OtherMeaning) for main word and update metada of meaning(words)
			if (existingWordNode != null) {
				String existingPrimaryMeaningId = (String) existingWordNode.getMetadata()
						.get(LanguageParams.primaryMeaningId.name());


				if (primaryMeaning != null&&StringUtils.isNotEmpty(existingPrimaryMeaningId)&&!StringUtils.equalsIgnoreCase(primaryMeaningId, existingPrimaryMeaningId)) {
					//remove old primary meaning's synoym relation with main word
					Map<String, Object> meaning = new HashMap<>();
					meaning.put(LanguageParams.identifier.name(), existingPrimaryMeaningId);
					Node otherMeaningSynset = createNodeObjectForSynset(languageId, meaning, wordIdentifier, lemma, lemmaWordMap, wordIds,
							errorMessages, false, true);
					Response synsetResponse = createOrUpdateNode(languageId, otherMeaningSynset);
				}
				
				if(otherMeanings != null){
					List<Relation> existingSynonymRelation = wordUtil.getSynonymRelations(existingWordNode.getInRelations());
					List<String> existingSynsets = getSynsetId(existingSynonymRelation);
					existingSynsets.remove(existingPrimaryMeaningId);
					existingSynsets.removeAll(wordSynsetIds);
					//remove old existing invalid other meaning's synoym relation with main word
					for(String synsetId:existingSynsets){
						Map<String, Object> meaning = new HashMap<>();
						meaning.put(LanguageParams.identifier.name(), synsetId);
						Node otherMeaningSynset = createNodeObjectForSynset(languageId, meaning, wordIdentifier, lemma, lemmaWordMap, wordIds,
								errorMessages, false, true);
						Response synsetResponse = createOrUpdateNode(languageId, otherMeaningSynset);
					}
				}

			}

		} catch (Exception e) {
			errorMessages.add(e.getMessage());
		}
		if (!errorMessages.isEmpty()) {
			wordResponse.put(LanguageParams.error_messages.name(), errorMessages);
		}
		return wordResponse;

	}

	/**
	 * Gets the related word lemma from.
	 * @param languageId TODO
	 * @param meaningObj
	 *            the meaning obj
	 *
	 * @return the related word lemma from
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	private List<String> getRelatedWordLemmaFrom(String languageId, Map<String, Object> meaningObj) {

		List<String> lemmas = new ArrayList<>();

		for (Entry<String, Object> entry : meaningObj.entrySet()) {
			if (wordUtil.getRelations().contains(entry.getKey())) {
				List<Map<String, Object>> words = (List<Map<String, Object>>) entry.getValue();
				for (Map<String, Object> word : words) {
					String lemma = (String) word.get(LanguageParams.lemma.name());
					if (lemma == null) {
						lemma = (String) word.get(LanguageParams.name.name());
					}
					if (lemma != null) {
						lemma = lemma.trim();
						if(languageId.equalsIgnoreCase("en"))
							lemma = lemma.toLowerCase();
						word.put(LanguageParams.lemma.name(), lemma);
						lemmas.add(lemma);
					}

				}
			}
		}

		return lemmas;
	}

	/**
	 * Gets the all lemma.
	 * @param languageId TODO
	 * @param wordRequestMap
	 *            the word request map
	 *
	 * @return the all lemma
	 */
	@SuppressWarnings("unchecked")
	private List<String> getAllLemma(String languageId, Map<String, Object> wordRequestMap) {
		List<String> lemmas = new ArrayList<>();

		Map<String, Object> primaryMeaning = (Map<String, Object>) wordRequestMap
				.get(LanguageParams.primaryMeaning.name());
		if (null != primaryMeaning)
			lemmas.addAll(getRelatedWordLemmaFrom(languageId, primaryMeaning));

		List<Map<String, Object>> otherMeanings = (List<Map<String, Object>>) wordRequestMap
				.get(LanguageParams.otherMeanings.name());
		if (null != otherMeanings && !otherMeanings.isEmpty())
			for (Map<String, Object> otherMeaning : otherMeanings)
				lemmas.addAll(getRelatedWordLemmaFrom(languageId, otherMeaning));

		return lemmas;
	}

	/**
	 * Search words.
	 *
	 * @param languageId
	 *            the language id
	 * @param words
	 *            the words
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	private List<Node> searchWords(String languageId, List<String> words) {
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		sc.setObjectType(LanguageParams.Word.name());
		List<Filter> filters = new ArrayList<Filter>();
		if (null != words && !words.isEmpty()) {
			for (String word : words)
				filters.add(new Filter("lemma", SearchConditions.OP_EQUAL, word));
		}
		MetadataCriterion mc = MetadataCriterion.create(filters);
		mc.setOp(SearchConditions.LOGICAL_OR);
		sc.addMetadata(mc);
		Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		req.put(GraphDACParams.get_tags.name(), true);
		Response listRes = getResponse(req);
		if (checkError(listRes))
			throw new ServerException(LanguageErrorCodes.ERR_PARSER_ERROR.name(), "Search failed");
		else {
			List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
			return nodes;
		}
	}

	/**
	 * Gets the lemma word map.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordRequestMap
	 *            the word request map
	 * @return the lemma word map
	 */
	private Map<String, Object> getLemmaWordMap(String languageId, Map<String, Object> wordRequestMap) {

		Map<String, Object> lemmaWordMap = new HashMap<>();
		List<String> lemmas = getAllLemma(languageId, wordRequestMap);
		List<Node> words = new ArrayList<>();
		if (lemmas != null && lemmas.size() > 0)
			words = searchWords(languageId, lemmas);

		for (Node word : words) {
			lemmaWordMap.put(word.getMetadata().get(LanguageParams.lemma.name()).toString(), word);
		}

		return lemmaWordMap;
	}

	/**
	 * Creates the node object for synset.
	 *
	 *creation of main word's synset/meaning node object as follows
		1. for each relation of synset other than synonyms
			1.a for each word entry in relation 
				a. get primary meaning(PM) of related word (create a word with primary meaning if it is not exist)
				b. add corresponding relation between related Word's PM and main word's meaning/synset along with relatedWordIds metadata
		2. for each word in synonym of given meaning/synset map
			a. create synonym word if it not exist
			b. add synonym relation between word and main word's meaning/synset
		3. get synset node and Merge relations if synset node is already exist
		4. check and do update for updating/removing synonym relation of mainWord
		4. update tags into synset object
		5. update metadata into synset object
	 *
	 * @param languageId
	 *            the language id
	 * @param meaningMap
	 *            the meaning map
	 * @param mainWordId
	 *            the main WordId
	 * @param mainWordlemma
	 *            the main word lemma
	 * @param lemmaWordMap
	 *            the lemma word map
	 * @param wordIds
	 *            the word ids
	 * @param errorMessages
	 *            the error messages
	 * @param isPrimary 
	 *            boolean to indicate whether meaning/sysnet is primary or not
	 * @param deleteWordAsscociation
	 *            boolean to indicate to remove main word de-association from meaning object and metatadata update of meaning(words)
	 * @return the node
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	private Node createNodeObjectForSynset(String languageId, Map<String, Object> meaningMap,
			String mainWordId, String mainWordlemma, Map<String, Object> lemmaWordMap, List<String> wordIds, List<String> errorMessages, boolean isPrimary, boolean deleteWordAsscociation) {

		String synsetId = (String) meaningMap.get("identifier");
		Node synset = new Node(synsetId, SystemNodeTypes.DATA_NODE.name(), "Synset");

		List<Relation> outRelations = new ArrayList<Relation>();
		List<String> emptyRelations = new ArrayList<String>();
		List<Map<String, Object>> words = null;
		Map<String, Relation> relationsMap = new HashMap<>();
		for (Entry<String, Object> entry : meaningMap.entrySet()) {
			if (wordUtil.getRelations().contains(entry.getKey())
					&& !entry.getKey().equalsIgnoreCase(LanguageParams.synonyms.name())) {

				List<Map<String, Object>> relationWordMap = (List<Map<String, Object>>) entry.getValue();

				if (relationWordMap != null) {

					if (relationWordMap.size() == 0) {
						emptyRelations.add(wordUtil.getRelationName(entry.getKey()));
					} else {
						for (Map<String, Object> word : relationWordMap) {
							String lemma = (String) word.get(LanguageParams.lemma.name());
							String primaryMeaningId = getPrimaryMeaningId(languageId, lemma, lemmaWordMap, wordIds,
									errorMessages, entry.getKey());
							if (primaryMeaningId != null) {
								// create relation between related word's
								// primary meaning and main
								// word's given synset/meaning along with related wordIds as metadata
								// property
								Node wordNode = (Node) lemmaWordMap.get(lemma);
								String wordId = wordNode.getIdentifier();
								String relationKey = synsetId + ":" + wordUtil.getRelationName(entry.getKey()) + ":"
										+ primaryMeaningId;
								if (relationsMap.get(relationKey) != null) {
									Relation relation = relationsMap.get(relationKey);
									List<String> relatedWordIds = (List<String>) relation.getMetadata()
											.get(LanguageParams.relatedWordIds.name());
									if (!relatedWordIds.contains(wordId))
										relatedWordIds.add(wordId);
								} else {
									Relation relation = new Relation(synsetId, wordUtil.getRelationName(entry.getKey()),
											primaryMeaningId);
									Map<String, Object> relationMeta = new HashMap<>();
									List<String> relatedWordIds = new ArrayList<>();
									relatedWordIds.add(wordId);
									relationMeta.put(LanguageParams.relatedWordIds.name(), relatedWordIds);
									relation.setMetadata(relationMeta);

									outRelations.add(relation);
									relationsMap.put(relationKey, relation);
								}
							}
						}
					}

				}

			}
		}

		List<Map<String, Object>> synonyms = (List<Map<String, Object>>) meaningMap.get(LanguageParams.synonyms.name());

		if (synonyms != null) {
			
			words = new ArrayList<>();
			
			if (synonyms.size() == 0) {
				//emptyRelations.add(wordUtil.getRelationName(LanguageParams.synonyms.name()));
				words.add(getWordMap(mainWordId, mainWordlemma));
				Relation relation = new Relation(synsetId, wordUtil.getRelationName(LanguageParams.synonyms.name()),
						mainWordId);
				outRelations.add(relation);

			} else {
				for (Map<String, Object> word : synonyms) {
					String lemma = (String) word.get(LanguageParams.lemma.name());
					Node wordNode = (Node) lemmaWordMap.get(lemma);
					String wordId;
					if (wordNode == null) {
						wordId = createWord(languageId, lemma, errorMessages, LanguageParams.synonyms.name());
						if (wordId == null)
							continue;

						try {
							Node wordTocache = getDataNode(languageId, wordId, LanguageParams.Word.name());
							lemmaWordMap.put(lemma, wordTocache);
						} catch (Exception e) {
							errorMessages.add("eror while getting recently created word in order to cache it");
						}

					} else {
						wordId = wordNode.getIdentifier();
					}
					wordIds.add(wordId);
					//add word to synset words(metadata)
					words.add(getWordMap(wordId, lemma));
					
					Relation relation = new Relation(synsetId, wordUtil.getRelationName(LanguageParams.synonyms.name()),
							wordId);
/*					if(isPrimary)
						relation.setMetadata(getPrimaryMeaningMetadata());*/
					outRelations.add(relation);
				}
				//add main word to words and relations
				words.add(getWordMap(mainWordId, mainWordlemma));
				Relation relation = new Relation(synsetId, wordUtil.getRelationName(LanguageParams.synonyms.name()),
						mainWordId);
				outRelations.add(relation);
			}
		}

		meaningMap.remove(LanguageParams.synonyms.name());
		meaningMap.remove(LanguageParams.hypernyms.name());
		meaningMap.remove(LanguageParams.hyponyms.name());
		meaningMap.remove(LanguageParams.holonyms.name());
		meaningMap.remove(LanguageParams.antonyms.name());
		meaningMap.remove(LanguageParams.meronyms.name());
		meaningMap.remove(LanguageParams.tools.name());
		meaningMap.remove(LanguageParams.objects.name());
		meaningMap.remove(LanguageParams.actions.name());
		meaningMap.remove(LanguageParams.workers.name());
		meaningMap.remove(LanguageParams.converse.name());
		
		Node existingSynset = null;
		if (outRelations.size() > 0 || emptyRelations.size() > 0) {

			//merge relations with exist one if it is already exist
			if (synsetId != null) {
				try {
					existingSynset = getDataNode(languageId, synsetId, "Synset");
					outRelations = wordUtil.getMergedRelations(outRelations, existingSynset.getOutRelations(),
							emptyRelations);
				} catch (Exception e) {
				}
			}	
			synset.setOutRelations(outRelations);
		}

		//add relation between mainword and synset when synonym is not mentioned to support partial update
		// remove relation between mainword and synset when deleteWordAsscociation is true
		if (synsetId != null) {
			try{
				if(existingSynset==null){
					existingSynset=getDataNode(languageId, synsetId, "Synset");
					outRelations = existingSynset.getOutRelations();
				}
				List<Relation> synonymRelations = wordUtil.getSynonymRelations(outRelations);
				if(!deleteWordAsscociation){
					//copy synonym relation to support partial update
					if (synonyms == null) {
						if (synonymRelations == null || synonymRelations.size() == 0) {
							words = new ArrayList<>();
							words.add(getWordMap(mainWordId, mainWordlemma));
							Relation relation = new Relation(synsetId,
									wordUtil.getRelationName(LanguageParams.synonyms.name()), mainWordId);
							if(outRelations==null)
								outRelations = new ArrayList<>();
							outRelations.add(relation);
							synset.setOutRelations(outRelations);
						} else {
							if (!isWordSynonymAlready(mainWordId, synonymRelations)) {
								String wordsStr = (String) existingSynset.getMetadata()
										.get(LanguageParams.words.name());
								if (wordsStr != null) {
									Object val = JSONUtils.convertJSONString(wordsStr);
									words = (List<Map<String, Object>>) val;
									words.add(getWordMap(mainWordId, mainWordlemma));
								}
								Relation relation = new Relation(synsetId,
										wordUtil.getRelationName(LanguageParams.synonyms.name()), mainWordId);
								outRelations.add(relation);
								synset.setOutRelations(outRelations);
							}
						}
					}
				} else {
					if(synonymRelations!=null&& synonymRelations.size()>0){
						String wordsStr = (String)existingSynset.getMetadata().get(LanguageParams.words.name());
						if (wordsStr != null) {
							Object val = JSONUtils.convertJSONString(wordsStr);
							words = (List<Map<String, Object>>) val;
							removeWordFromWords(mainWordId, words);
						}
						outRelations = outRelations.stream()
								.filter(rel -> !mainWordId
										.equalsIgnoreCase(rel.getEndNodeId()))
								.collect(Collectors.toList());
						synset.setOutRelations(outRelations);
					}
				}
				
			} catch (Exception e) {
			}
		} else {
			if(synonyms == null){
				words = new ArrayList<>();
				words.add(getWordMap(mainWordId, mainWordlemma));
				Relation relation = new Relation(synsetId, wordUtil.getRelationName(LanguageParams.synonyms.name()),
						mainWordId);
				outRelations.add(relation);
				synset.setOutRelations(outRelations);
			}
			
		}
		

		if(words!=null)
			meaningMap.put(LanguageParams.words.name(), words);

		if(synset.getOutRelations()!=null) {
			Relation mainWordSynRel = getSynonymRelationOf(mainWordId,
					wordUtil.getSynonymRelations(synset.getOutRelations()));
			//set primary Meaning flag in synonym relation
			if (isPrimary && mainWordSynRel != null)
				mainWordSynRel.setMetadata(getPrimaryMeaningMetadata());
			
			// set exampleSentence
			if (meaningMap.get(ATTRIB_EXAMPLE_SENTENCES)!=null && mainWordSynRel != null) {
				Map<String, Object> relMetadata = mainWordSynRel.getMetadata();
				if(relMetadata==null){
					relMetadata = new HashMap<>();
					mainWordSynRel.setMetadata(relMetadata);
				}

				relMetadata.put(ATTRIB_EXAMPLE_SENTENCES, meaningMap.get(ATTRIB_EXAMPLE_SENTENCES));
				meaningMap.remove(ATTRIB_EXAMPLE_SENTENCES);
			}
		}

		
		// commented on 30-Nov for keyword/themes model change
		//List<String> tags = (List<String>) meaningMap.get(LanguageParams.tags.name());
		meaningMap.remove(LanguageParams.tags.name());
		meaningMap.remove(LanguageParams.keywords.name());
		
		// set synset metadata
		synset.setMetadata(meaningMap);
		

		// commented on 30-Nov for keyword/themes model change
		// set synset tags
		/*if (tags != null) {
			synset.setTags(tags);
		}*/

		return synset;

	}

	
	private Map<String, Object> getPrimaryMeaningMetadata() {
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put(LanguageParams.isPrimary.name(), true);
		return metadata;
	}

	private void removeWordFromWords(String id, List<Map<String, Object>> words){
		
		Iterator<Map<String, Object>> wordItr = words.iterator();
		while(wordItr.hasNext()){
			Map<String, Object> wordMap = (Map<String, Object>)wordItr.next();
			String wordId = (String)wordMap.get(LanguageParams.identifier.name());
			if(wordId != null && wordId.equalsIgnoreCase(id))
				wordItr.remove();
		}
		
	}
	
	private Map<String, Object> getWordMap(String id, String lemma){
		Map<String, Object> wordMap = new HashMap<>();
		wordMap.put(LanguageParams.identifier.name(), id);
		wordMap.put(LanguageParams.name.name(), lemma);
		return wordMap;
	}
	
	private boolean isWordSynonymAlready(String wordId, List<Relation> synonyms){
		
		if(synonyms!=null)
			for(Relation synonym: synonyms)
				if(synonym.getEndNodeId().equalsIgnoreCase(wordId))
					return true;
		
		return false;
	}
	
	private Relation getSynonymRelationOf(String wordId, List<Relation> synonyms){
		
		if(synonyms!=null)
			for(Relation synonym: synonyms)
				if(synonym.getEndNodeId().equalsIgnoreCase(wordId))
					return synonym;
		
		return null;
	}
	
	private List<String> getSynsetId(List<Relation> synonyms){

		List<String> synsetIds =new ArrayList<String>();
		
		if(synonyms!=null)
			for(Relation synonym: synonyms)
				synsetIds.add(synonym.getStartNodeId());
		
		return synsetIds;
	}
	/**
	 * Gets the synonym id.
	 *
	 * @param word
	 *            the word
	 * @return the synonym id
	 */
	private String getSynonymId(Node word) {

		List<Relation> inRelation = word.getInRelations();
		if (inRelation != null && !inRelation.isEmpty()) {
			for (Relation rel : inRelation) {
				String relType = rel.getRelationType();
				Map<String, Object> startNodeMetadata = rel.getStartNodeMetadata();
				String startNodeObjType = (String) startNodeMetadata.get(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
				if (relType.equalsIgnoreCase(RelationTypes.SYNONYM.relationName())
						&& startNodeObjType.equalsIgnoreCase(LanguageObjectTypes.Synset.name())) {
					return rel.getStartNodeId();
				}
			}
		}
		return null;
	}

	/**
	 * Gets the primary meaning id.
	 *
	 * @param languageId
	 *            the language id
	 * @param lemma
	 *            the lemma
	 * @param lemmaWordMap
	 *            the lemma word map
	 * @param wordIds
	 *            the word ids
	 * @param errorMessages
	 *            the error messages
	 * @param relationName
	 *            the relation name
	 * @return the primary meaning id
	 */
	private String getPrimaryMeaningId(String languageId, String lemma, Map<String, Object> lemmaWordMap,
			List<String> wordIds, List<String> errorMessages, String relationName) {

		String primaryMeaningId = null;
		Node word = (Node) lemmaWordMap.get(lemma);

		if (word != null) {
			primaryMeaningId = (String) word.getMetadata().get(LanguageParams.primaryMeaningId.name());
			if (primaryMeaningId == null) {
				primaryMeaningId = getSynonymId(word);
				if (primaryMeaningId == null) {
					primaryMeaningId = createSynset(languageId, lemma, word.getIdentifier(), errorMessages, relationName);
				}
				setPrimaryMeaningId(languageId, word.getIdentifier(), primaryMeaningId);
			}
			wordIds.add(word.getIdentifier());
		} else {
			String wordId = createWord(languageId, lemma, errorMessages, relationName);
			if (wordId != null) {
				primaryMeaningId = createSynset(languageId, lemma, wordId, errorMessages, relationName);
				if (primaryMeaningId != null) {
					setPrimaryMeaningId(languageId, wordId, primaryMeaningId);
				}
				wordIds.add(wordId);

				try {
					Node wordTocache = getDataNode(languageId, wordId, LanguageParams.Word.name());
					lemmaWordMap.put(lemma, wordTocache);
				} catch (Exception e) {
					errorMessages.add("eror while getting recently created word in order to cache it");
				}

			}
		}

		return primaryMeaningId;
	}

	/**
	 * Sets the primary meaning id.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordId
	 *            the word id
	 * @param primaryMeaningId
	 *            the primary meaning id
	 */
	private void setPrimaryMeaningId(String languageId, String wordId, String primaryMeaningId) {
		Node node = new Node(wordId, SystemNodeTypes.DATA_NODE.name(), LanguageParams.Word.name());
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put(LanguageParams.primaryMeaningId.name(), primaryMeaningId);
		node.setMetadata(metadata);
		Request req = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		req.put(GraphDACParams.node.name(), node);
		req.put(GraphDACParams.node_id.name(), wordId);
		Response res = getResponse(req);
		if (checkError(res)) {
			throw new ServerException(LanguageErrorCodes.ERR_UPDATE_WORD.name(), getErrorMessage(res));
		}
	}

	/**
	 * Creates the synset.
	 *
	 * @param languageId
	 *            the language id
	 * @param lemma
	 *            the lemma
	 * @param errorMessages
	 *            the error messages
	 * @param relationName
	 *            the relation name
	 * @return the string
	 */
	private String createSynset(String languageId, String lemma, List<String> errorMessages, String relationName) {

		Node node = new Node(null, SystemNodeTypes.DATA_NODE.name(), LanguageParams.Synset.name());
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put(LanguageParams.gloss.name(), lemma);
		node.setMetadata(metadata);
		Request req = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		req.put(GraphDACParams.node.name(), node);
		Response res = getResponse(req);
		if (checkError(res)) {
			errorMessages.add(wordUtil.getErrorMessage(res) + "- synset creation error in " + relationName);
			return null;

		}
		String nodeId = (String) res.get(GraphDACParams.node_id.name());
		return nodeId;
	}

	/**
	 * Creates the synset.
	 *
	 * @param languageId
	 *            the language id
	 * @param lemma
	 *            the lemma
	 * @param synonymId
	 *            the synonymId
	 * @param errorMessages
	 *            the error messages
	 * @param relationName
	 *            the relation name
	 * @return the string
	 */
	private String createSynset(String languageId, String lemma, String synonymWordId, List<String> errorMessages, String relationName) {

		Node node = new Node(null, SystemNodeTypes.DATA_NODE.name(), LanguageParams.Synset.name());
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put(LanguageParams.gloss.name(), lemma);

		List<Map<String, Object>> words = new ArrayList<>();
		Map<String, Object> word = new HashMap<String, Object>();
		word.put(LanguageParams.identifier.name(), synonymWordId);
		word.put(LanguageParams.name.name(), lemma);
		words.add(word);
		metadata.put(LanguageParams.words.name(), words);
		
		node.setMetadata(metadata);
		//add synonym relation
		List<Relation> outRelations = new ArrayList<>();
		Relation synonym = new Relation(null, wordUtil.getRelationName(LanguageParams.synonyms.name()),
				synonymWordId);
		synonym.setMetadata(getPrimaryMeaningMetadata());
		outRelations.add(synonym);
		node.setOutRelations(outRelations);
		Request req = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		req.put(GraphDACParams.node.name(), node);
		Response res = getResponse(req);
		if (checkError(res)) {
			errorMessages.add(wordUtil.getErrorMessage(res) + "- synset creation error in " + relationName);
			return null;

		}
		String nodeId = (String) res.get(GraphDACParams.node_id.name());
		return nodeId;
	}
	/**
	 * Creates the word.
	 *
	 * @param languageId
	 *            the language id
	 * @param lemma
	 *            the lemma
	 * @param errorMessages
	 *            the error messages
	 * @param relationName
	 *            the relation name
	 * @return the string
	 */
	private String createWord(String languageId, String lemma, List<String> errorMessages, String relationName) {

		if (!isValidWord(lemma, languageId, errorMessages, relationName))
			return null;
		Node node = new Node(null, SystemNodeTypes.DATA_NODE.name(), LanguageParams.Word.name());
		Map<String, Object> metadata = new HashMap<String, Object>();
		if(languageId.equalsIgnoreCase("en"))
			lemma = lemma.toLowerCase();
		metadata.put(LEMMA_PROPERTY, lemma);
		metadata.put(ATTRIB_STATUS, "Draft");
		node.setMetadata(metadata);
		Request req = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		req.put(GraphDACParams.node.name(), node);
		Response res = getResponse(req);
		if (checkError(res)) {
			errorMessages.add(wordUtil.getErrorMessage(res) + "- word creation error in " + relationName);
			return null;
		}
		String nodeId = (String) res.get(GraphDACParams.node_id.name());
		return nodeId;
	}

	/**
	 * Checks if is valid word.
	 *
	 * @param word
	 *            the word
	 * @param languageId
	 *            the language id
	 * @param errorMessages
	 *            the error messages
	 * @param relationName
	 *            the relation name
	 * @return true, if is valid word
	 */
	public boolean isValidWord(String word, String languageId, List<String> errorMessages, String relationName) {
		try {

			Matcher hasSpecial = special.matcher(word);
			if (hasSpecial.matches()) {
				errorMessages
						.add("Word should not contain any special characters for word in " + relationName + " List");
				return false;
			}

			char firstLetter = word.charAt(0);
			int i = firstLetter;
			String uc = String.format("%04x", i);
			int hexVal = Integer.parseInt(uc, 16);
			Node languageNode = getDataNode("domain", "lang_" + languageId, "Language");
			String startUnicode = (String) languageNode.getMetadata().get("startUnicode");
			String endUnicode = (String) languageNode.getMetadata().get("endUnicode");

			if (startUnicode != null && endUnicode != null) {
				int min = Integer.parseInt(startUnicode, 16);
				int max = Integer.parseInt(endUnicode, 16);
				if (hexVal >= min && hexVal <= max) {
				} else {
					errorMessages.add("Word cannot be in a different language for word in " + relationName + " List");
					return false;
				}
			}

		} catch (Exception e) {
			// return true... if language object is not defined...
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private List<String> getRelatedWordIds(Node synsetNode, String meaningId, String relationName) {

		List<String> relatedWordIds = null;

		List<Relation> synsetInRelation = synsetNode.getInRelations();

		if (!CollectionUtils.isEmpty(synsetInRelation)) {
			for (Relation rel : synsetInRelation)
				if (rel.getStartNodeId().equalsIgnoreCase(meaningId)
						&& rel.getRelationType().equalsIgnoreCase(relationName)) {
					Object wordIds = rel.getMetadata().get(LanguageParams.relatedWordIds.name());
					if (wordIds != null) {
						if (wordIds instanceof String[])
							relatedWordIds = Arrays.asList((String[]) wordIds);
						else
							relatedWordIds = (List<String>) wordIds;
					}
					break;
				}
		}

		return relatedWordIds;
	}

    @SuppressWarnings("unchecked")
	private List<String> getListProperty(Object property){
		List<String> listProperty = null;
		
		if(property!=null)				
	    	if (property instanceof String[])
	    		listProperty = Arrays.asList((String[]) property);
			else
				listProperty = (List<String>) property;
    	
    	return listProperty;
    }
    
	private List<String> getJSONProperties(DefinitionDTO definition) {
        List<String> props = new ArrayList<String>();
        if (null != definition && null != definition.getProperties()) {
            for (MetadataDefinition mDef : definition.getProperties()) {
                if (StringUtils.equalsIgnoreCase("json", mDef.getDataType()) && StringUtils.isNotBlank(mDef.getPropertyName())) {
                    props.add(mDef.getPropertyName().toLowerCase());
                }
            }
        }
        TelemetryManager.log("JSON properties: " + props);
        return props;
    }
    
	private Map<String, Object> getMetadata(Map<String, Object> metadata, DefinitionDTO definition) {

		Map<String, Object> map = new HashMap<String, Object>();

		if (null != metadata && !metadata.isEmpty()) {
			List<String> jsonProps = getJSONProperties(definition);
			for (Entry<String, Object> entry : metadata.entrySet()) {
				String key = entry.getKey();
				if (StringUtils.isNotBlank(key)) {
					if (jsonProps.contains(key.toLowerCase())) {
						Object val = JSONUtils.convertJSONString((String) entry.getValue());
						TelemetryManager.log("JSON Property " + key + " converted value is " + val);
						if (null != val)
							map.put(key, val);
					} else
						map.put(key, entry.getValue());
				}
			}
		}
		return map;
	}

	private Map<String, Object> getWordMap(String languageId, Node node) {

		DefinitionDTO wordDefinition = getDefinitionDTO(LanguageParams.Word.name(), languageId);
		DefinitionDTO synsetDefinition = getDefinitionDTO(LanguageParams.Synset.name(), languageId);
		Map<String, Object> wordMap = getMetadata(node.getMetadata(), wordDefinition);
		
		if(!wordMap.containsKey(LanguageParams.identifier.name()))
			wordMap.put(LanguageParams.identifier.name(), node.getIdentifier());
		
		List<Map<String, Object>> synsets = new ArrayList<>();
		wordMap.put("synsets", synsets);
		wordUtil.updatePrimaryMeaning(languageId, wordMap, wordUtil.getSynsets(node));
		
		if (null != node.getInRelations() && !node.getInRelations().isEmpty())
			for (Relation inRelation : node.getInRelations()) {
				if (StringUtils.equalsIgnoreCase(inRelation.getRelationType(), RelationTypes.SYNONYM.relationName())
						&& StringUtils.equalsIgnoreCase(inRelation.getStartNodeObjectType(),
								LanguageParams.Synset.name())) {
					Map<String, Object> synsetMap = getMetadata(inRelation.getStartNodeMetadata(), synsetDefinition);
					if (!synsetMap.containsKey(LanguageParams.identifier.name()))
						synsetMap.put(LanguageParams.identifier.name(), inRelation.getStartNodeId());
					// copy word-synset relation metadata like exampleSentence into synset map
					Map<String, Object> synonymRelMetadata = inRelation.getMetadata();
					if (synonymRelMetadata != null && synonymRelMetadata.size() > 0)
						synsetMap.putAll(synonymRelMetadata);
					synsets.add(synsetMap);
				}
			}

		if(node.getTags()!=null)
			wordMap.put("tags",node.getTags());

		return wordMap;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getSynsetMap(String languageId, Node synset) {

		DefinitionDTO synsetDefinition = getDefinitionDTO(LanguageParams.Synset.name(), languageId);
		Map<String, Object> synsetMap = getMetadata(synset.getMetadata(), synsetDefinition);

		if(!synsetMap.containsKey(LanguageParams.identifier.name()))
			synsetMap.put(LanguageParams.identifier.name(), synset.getIdentifier());

		if (null != synsetDefinition.getOutRelations() && !synsetDefinition.getOutRelations().isEmpty()) {
			Map<String, List<NodeDTO>> relMap = new HashMap<String, List<NodeDTO>>();
			Map<String, String> relTitleMap = new HashMap<String, String>();
			for (RelationDefinition rDef : synsetDefinition.getOutRelations()) {
				relTitleMap.put(rDef.getRelationName(), rDef.getTitle());
				relMap.put(rDef.getTitle(), new ArrayList<NodeDTO>());
			}
			if (null != synset.getOutRelations() && !synset.getOutRelations().isEmpty()) {
				List<Relation> relations = synset.getOutRelations();
				for (Relation rel : relations) {
						if (StringUtils.equalsIgnoreCase(LanguageParams.Word.name(), rel.getEndNodeObjectType())
								&& StringUtils.equalsIgnoreCase(rel.getRelationType(), RelationTypes.SYNONYM.name())) {
							String title = relTitleMap.get(rel.getRelationType());
							if (StringUtils.isNotBlank(title)) {
								List<NodeDTO> list = relMap.get(title);
								String lemma = (String) rel.getEndNodeMetadata().get(LanguageParams.lemma.name());
								String status = (String) rel.getEndNodeMetadata().get(LanguageParams.status.name());
								if (!StringUtils.equalsIgnoreCase(status, LanguageParams.Retired.name()))
									list.add(new NodeDTO(rel.getEndNodeId(), lemma, rel.getEndNodeObjectType()));
							}
						} else{
							 if (StringUtils.equalsIgnoreCase(LanguageParams.Synset.name(),
										rel.getEndNodeObjectType())) {
									String title = relTitleMap.get(rel.getRelationType());
									if (StringUtils.isNotBlank(title)) {
										List<NodeDTO> list = relMap.get(title);
										String wordsStr = (String) rel.getEndNodeMetadata().get(LanguageParams.words.name());
										Object val = JSONUtils.convertJSONString(wordsStr);
										List<Map<String, Object>> words = (List<Map<String, Object>>) val;
										List<String> relatedWordIds = getListProperty(rel.getMetadata().get(LanguageParams.relatedWordIds.name()));
										
										if(relatedWordIds!=null && words!=null)
											words = words.stream()
														        .filter(map -> relatedWordIds.contains(map.get(LanguageParams.identifier.name())))
														        .collect(Collectors.toList());
										if(words!=null)
											for(Map<String, Object> word:words)
												list.add(new NodeDTO(word.get(LanguageParams.identifier.name()).toString(), (String)word.get(LanguageParams.name.name()).toString(), LanguageParams.word.name()));
									}
							 }
						}
					}
				}
			synsetMap.putAll(relMap);
			}

		if(synset.getTags()!=null)
			synsetMap.put("tags",synset.getTags());
		return synsetMap;
	}
	
	@Override
	public Response getWordV3(String languageId, String id) {

		if (StringUtils.isBlank(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(id))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT_ID.name(), "Object Id is blank");
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), id);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request);
		if (checkError(getNodeRes)) {
			return getNodeRes;
		} else {
			try {
				Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
				Map<String, Object> map = getWordMap(languageId, node);
				Response response = copyResponse(getNodeRes);
				response.put(LanguageObjectTypes.Word.name(), map);
				return response;
			} catch (Exception e) {
				TelemetryManager.error("Exception: "+ e.getMessage(), e);
				e.printStackTrace();
				return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), ResponseCode.CLIENT_ERROR);
			}

		}
	}

	@Override
	public Response getSynsetV3(String languageId, String id) {
		
		if (StringUtils.isBlank(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(id))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT_ID.name(), "Object Id is blank");
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), id);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request);
		if (checkError(getNodeRes)) {
			return getNodeRes;
		} else {
			try {
				Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
				// Added for correcting synset relationship between from synset and
				// words to synset and synset
				node = correctSynset(languageId, node);
				Map<String, Object> map = getSynsetMap(languageId, node);
				Response response = copyResponse(getNodeRes);
				response.put(LanguageObjectTypes.Synset.name(), map);
				return response;
			} catch (Exception e) {
				TelemetryManager.error("Exception: "+ e.getMessage(), e);
				e.printStackTrace();
				return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), ResponseCode.CLIENT_ERROR);
			}

		}	
		
	}
	
	@Override
	public Response bulkUpdateWordsCSV(String languageId, InputStream wordStream) {

		if (StringUtils.isBlank(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (null == wordStream)
			throw new ClientException(LanguageErrorCodes.ERR_EMPTY_INPUT_STREAM.name(), "Word object is emtpy");

		DefinitionDTO synsetDefinition = getDefinitionDTO(LanguageParams.Synset.name(), languageId);
		DefinitionDTO wordDefinition = getDefinitionDTO(LanguageParams.Word.name(), languageId);
		
		try {
			List<Map<String, Object>> wordRecords = readWordsFromCSV(languageId, wordStream, wordDefinition,
					synsetDefinition);
			List<String> errorMessages = new ArrayList<>();
			TelemetryManager.log("Bulk word Update | word count :" + wordRecords.size());
			for (Map<String, Object> word : wordRecords) {
				TelemetryManager.log("Bulk word Update | word :" + word.toString());
				List<String> lstNodeId = new ArrayList<>();
				Response wordResponse = createOrUpdateWord(languageId, word, lstNodeId, true, false);
				if (checkError(wordResponse)) {
					errorMessages.add(wordUtil.getErrorMessage(wordResponse));
					TelemetryManager.error("ClientException during bulk word update for word:" + word.toString() + " message: " +
							wordUtil.getErrorMessage(wordResponse));
				}
				String nodeId = (String) wordResponse.get(GraphDACParams.node_id.name());
				if (nodeId != null) {
					//lstNodeId.add(nodeId);
					TelemetryManager.log("Bulk word Update | successfull for  word: " + nodeId);
				}
			}
			return OK("errors", errorMessages);
		} catch (ClientException e) {
			TelemetryManager.error("ClientException: "+ e.getMessage(), e);
			return ERROR(LanguageErrorCodes.ERR_INVALID_UPLOAD_FILE.name(), e.getMessage(), ResponseCode.CLIENT_ERROR);
		} catch (Exception e) {
			TelemetryManager.error("Exception: "+ e.getMessage(), e);
			return ERROR(LanguageErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), ResponseCode.SERVER_ERROR);
		}

	}

	private List<Map<String, Object>> readWordsFromCSV(String languageId, InputStream inputStream,
			DefinitionDTO wordDefinition, DefinitionDTO synsetDefinition)
			throws JsonProcessingException, IOException, ClientException {

		Map<String, MetadataDefinition> wordPropMap = getDefProperties(wordDefinition);
		Map<String, MetadataDefinition> synsetPropMap = getDefProperties(synsetDefinition);

		List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();
		try (InputStreamReader isReader = new InputStreamReader(inputStream, "UTF8");
				CSVParser csvReader = new CSVParser(isReader, csvFileFormat)) {
			List<String> headers = new ArrayList<String>();
			List<CSVRecord> recordsList = csvReader.getRecords();
			boolean meaningUpdate = false;
			List<String> wordList = new ArrayList<>();
			Map<String, Map<String, Object>> wordRecordMap = new HashMap<>();
			if (null != recordsList && !recordsList.isEmpty()) {
				for (int i = 0; i < recordsList.size(); i++) {
					if (i == 0) {
						CSVRecord headerecord = recordsList.get(i);
						for (int j = 0; j < headerecord.size(); j++) {
							String val = headerecord.get(j);
							if (!val.startsWith("word:") && !val.startsWith("synset:"))
								throw new ClientException(LanguageErrorCodes.ERR_INVALID_UPLOAD_FILE.name(),
										"invalid format, header fields should start with either word:<property_name> or synset:<property_name>");
							headers.add(val);
						}
						// if(headers.contains("word:lemma"));
						if (!headers.stream().anyMatch("word:lemma"::equalsIgnoreCase))
							throw new ClientException(LanguageErrorCodes.ERR_INVALID_UPLOAD_FILE.name(),
									"lemma is mandatory");
					} else {
						CSVRecord record = recordsList.get(i);
						Map<String, Object> word = new HashMap<>();
						Map<String, Object> synset = new HashMap<>();
						String wordLemma = "";
						if(headers.size()!=record.size())
							throw new ClientException(LanguageErrorCodes.ERR_INVALID_UPLOAD_FILE.name(),
									"records not matched with header provided");
						
						for (int j = 0; j < record.size(); j++) {
							String property = headers.get(j);
							String val = record.get(j);
							if (StringUtils.equalsIgnoreCase(property, "word:lemma"))
								wordLemma = val.trim();

							if (property.startsWith("word:")) {
								property = property.substring(property.indexOf(":") + 1);
								populateWordMap(word, wordPropMap, property, val);
							}
							if (property.startsWith("synset:")) {
								property = property.substring(property.indexOf(":") + 1);
								populateSynsetMap(synset, synsetPropMap, property, val);
							}

						}
						if (synset.size() > 0) {
							word.put(LanguageParams.primaryMeaning.name(), synset);
							meaningUpdate = true;
						}
						wordList.add(wordLemma);
						wordRecordMap.put(wordLemma, word);
						records.add(word);
					}
				}
				if (records.size() > 0 && meaningUpdate) {
					List<Map<String, Object>> words = wordUtil.indexSearch(languageId, wordList);

					if(words!=null)
						for (Map<String, Object> word : words) {
							String wordLemma = word.get(LanguageParams.lemma.name()).toString();
							Map<String, Object> wordRecord = wordRecordMap.get(wordLemma);
							Map<String, Object> synsetRecord = (Map<String, Object>) wordRecord
									.get(LanguageParams.primaryMeaning.name());
							String primaryMeaningId = (String)word.get(LanguageParams.primaryMeaningId.name());
							if (StringUtils.isNotBlank(primaryMeaningId))
								synsetRecord.put(LanguageParams.identifier.name(), primaryMeaningId);
							else
								synsetRecord.put(LanguageParams.gloss.name(), wordLemma);
							wordRecordMap.remove(wordLemma);
						}

					for (Entry<String, Map<String, Object>> entry : wordRecordMap.entrySet()) {
						Map<String, Object> wordRecord = entry.getValue();
						Map<String, Object> synsetRecord = (Map<String, Object>) wordRecord
								.get(LanguageParams.primaryMeaning.name());
						synsetRecord.put(LanguageParams.gloss.name(), entry.getKey());
					}

				}
			}
			return records;
		}
	}

	private Map<String, MetadataDefinition> getDefProperties(DefinitionDTO wordDefinition) {
		Map<String, MetadataDefinition> propMap = new HashMap<>();
		List<MetadataDefinition> wordProperties = wordDefinition.getProperties();
		if (wordProperties != null) {
			for (MetadataDefinition propDef : wordProperties) {
				// propMap.put(propDef.getTitle(), propDef);
				propMap.put(propDef.getPropertyName(), propDef);
			}
		}
		return propMap;
	}

	private void populateWordMap(Map<String, Object> word, Map<String, MetadataDefinition> wordPropMap, String property,
			String val) {

		if (wordPropMap.containsKey(property)) {
			if (StringUtils.isNotBlank(val))
				val = val.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
			else
				val = null;
			Object value = getMetadataValue(wordPropMap, property, val);
			word.put(property, value);
		}

	}

	private void populateSynsetMap(Map<String, Object> synset, Map<String, MetadataDefinition> synsetPropMap,
			String property, String val) {

		if (wordUtil.getRelations().contains(property)) {
			if (StringUtils.isNotBlank(val)) {
				List<String> relatedWords = Arrays.asList(val.trim().split("\\s*,\\s*"));
				synset.put(property, getRelatedWordMap(relatedWords));
			}
		} else if (synsetPropMap.containsKey(property)) {
			if (StringUtils.isNotBlank(val))
				val = val.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
			else
				val = null;
			Object value = getMetadataValue(synsetPropMap, property, val);
			synset.put(property, value);
		} else if (StringUtils.equalsIgnoreCase(property, "tags")) {
			List<String> tags;
			if (StringUtils.isNotBlank(val))
				tags = Arrays.asList(val.trim().split("\\s*,\\s*"));
			else
				tags = new ArrayList<>();
			synset.put("tags", tags);
		}

	}

	@SuppressWarnings("rawtypes")
	private Object getMetadataValue(Map<String, MetadataDefinition> objectPropMap, String property, String val) {
		if (objectPropMap != null) {
			MetadataDefinition def = objectPropMap.get(property);
			if (null != def) {
				Object value = val;
				if (StringUtils.isBlank(val) && null != def.getDefaultValue()
						&& StringUtils.isNotBlank(def.getDefaultValue().toString()))
					value = def.getDefaultValue();
				if (null != value) {
					String datatype = def.getDataType();
					if (StringUtils.equalsIgnoreCase("list", datatype)
							|| StringUtils.equalsIgnoreCase("multi-select", datatype)) {
						/*
						 * if (value instanceof List) { value = ((List)
						 * value).toArray(); } else if (!(value instanceof
						 * Object[])) { value = new String[] { value.toString()
						 * }; }
						 */
						value = Arrays.asList(val.trim().split("\\s*,\\s*"));
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
		return val;
	}

	private List<Map<String, Object>> getRelatedWordMap(List<String> words) {

		// if(words.isEmpty())
		// return null;
		List<Map<String, Object>> wordList = new ArrayList<>();
		for (String word : words) {
			Map<String, Object> wordMap = new HashMap<>();
			wordMap.put(LanguageParams.lemma.name(), word);
			wordList.add(wordMap);
		}
		return wordList;
	}
}