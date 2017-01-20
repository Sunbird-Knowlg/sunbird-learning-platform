
package org.ekstep.language.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.language.cache.VarnaCache;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.model.CitationBean;
import org.ekstep.language.model.WordIndexBean;
import org.ekstep.language.model.WordInfoBean;
import org.ekstep.language.translation.BaseTranslationSet;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.RelationCriterion;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.dac.model.TagCriterion;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.graph.model.node.RelationDefinition;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

/**
 * Provides utility methods required by the Dictionary controller to search,
 * create/update words and interact with Graph actors.
 * 
 * @author Amarnath, Karthik, Rayulu
 */

@Component
public class WordUtil extends BaseManager implements IWordnetConstants {

	private ObjectMapper mapper = new ObjectMapper();
	private static Logger LOGGER = LogManager.getLogger(WordUtil.class.getName());
	private static final String LEMMA_PROPERTY = "lemma";

	/**
	 * Returns akka request for a given request map
	 * 
	 * @param requestMap
	 *            request body
	 * 
	 * @return Request object
	 */
	@SuppressWarnings("unchecked")
	protected Request getRequest(Map<String, Object> requestMap)
			throws JsonParseException, JsonMappingException, IOException {
		Request request = new Request();
		if (null != requestMap && !requestMap.isEmpty()) {
			String id = (String) requestMap.get("id");
			String ver = (String) requestMap.get("ver");
			String ts = (String) requestMap.get("ts");
			request.setId(id);
			request.setVer(ver);
			request.setTs(ts);
			Object reqParams = requestMap.get("params");
			if (null != reqParams) {
				RequestParams params = (RequestParams) mapper.convertValue(reqParams, RequestParams.class);
				request.setParams(params);
			}
			Object requestObj = requestMap.get("request");
			if (null != requestObj) {
				String strRequest = mapper.writeValueAsString(requestObj);
				Map<String, Object> map = mapper.readValue(strRequest, Map.class);
				if (null != map && !map.isEmpty())
					request.setRequest(map);
			}
		}
		return request;
	}

	/**
	 * Sets the limit in a search criteria
	 * 
	 * @param Request
	 * 
	 * @param SearchCriteria
	 * 
	 * @return none
	 */
	private void setLimit(Request request, SearchCriteria sc) {
		Integer limit = null;
		Object obj = request.get(PARAM_LIMIT);
		if (obj instanceof String)
			limit = Integer.parseInt((String) obj);
		else
			limit = (Integer) request.get(PARAM_LIMIT);
		if (null == limit || limit.intValue() <= 0)
			limit = DEFAULT_LIMIT;
		sc.setResultSize(limit);
	}

	/**
	 * Given an object, returns the list of objects
	 * 
	 * @param mapper
	 *            to convert between json and map
	 * 
	 * @param object
	 *            object to be converted to list
	 * 
	 * @param propName
	 *            not used.
	 * 
	 * @return list of objects
	 */
	@SuppressWarnings("rawtypes")
	public List getList(ObjectMapper mapper, Object object, String propName) {
		if (null != object) {
			try {
				String strObject = mapper.writeValueAsString(object);
				List list = mapper.readValue(strObject.toString(), List.class);
				return list;
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				List<String> list = new ArrayList<String>();
				list.add(object.toString());
				return list;
			}
		}
		return null;
	}

	/**
	 * Retrieves the identifier of a word using the lemma from Graph DB
	 * 
	 * @param languageId
	 * 
	 * @param word
	 *            Lemma of the word
	 * 
	 * @return Word ID
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getWordIdentifierFromGraph(String languageId, String word) throws Exception {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, List> lemmaMap = new LinkedHashMap<String, List>();
		lemmaMap.put("lemma", getList(mapper, word, null));
		map.put("request", lemmaMap);
		Request request = getRequest(map);
		Response response = list(languageId, LanguageObjectTypes.Word.name(), request);
		LOGGER.info("Search | Response: " + response);
		List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("words");
		if (list != null && !list.isEmpty()) {
			Map<String, Object> wordMap = list.get(0);
			return (String) wordMap.get("identifier");
		}
		return null;
	}

	/**
	 * Retrieves the identifier of a word using the lemma, from Elasticsearch
	 * 
	 * @param languageId
	 * 
	 * @param word
	 *            Lemma of the word
	 * 
	 * @return Word ID
	 */
	@SuppressWarnings("unchecked")
	public String getWordIdentifierFromIndex(String languageId, String word) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil();
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, getList(mapper, word, null));

		// perform a text search on ES
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class, searchCriteria, indexName,
				Constants.WORD_INDEX_TYPE);
		Map<String, Object> wordIdsMap = new HashMap<String, Object>();

		// form a map of word Ids to word
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("wordId", wordIndex.getWordIdentifier());
			wordIdsMap.put(wordIndex.getWord(), wordMap);
		}

		// get word Id from the map
		if (wordIdsMap.get(word) != null) {
			return (String) ((Map<String, Object>) wordIdsMap.get(word)).get("wordId");
		}
		return null;
	}

	/**
	 * Returns the formatted string of a date time
	 * 
	 * @param dateTime
	 * 
	 * @return formatted date time
	 */
	public String getFormattedDateTime(long dateTime) {
		String dateTimeString = "";
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(dateTime);
			dateTimeString = formatter.format(cal.getTime());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			dateTimeString = "";
		}
		return dateTimeString;
	}

	/**
	 * Adds citation, word index and Word info indexes to Elasticsearch
	 * 
	 * @param indexes
	 *            Map of type of indexes to List of indexes
	 * 
	 * @param language
	 *            graph Id of the language
	 * 
	 * @return void
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addIndexesToElasticSearch(Map<String, List> indexes, String language) throws Exception {

		List<CitationBean> citationsList = indexes.get(Constants.CITATION_INDEX_COMMON_NAME);
		List<WordInfoBean> wordInfoList = indexes.get(Constants.WORD_INFO_INDEX_COMMON_NAME);

		String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME + "_" + language;
		String wordIndexName = Constants.WORD_INDEX_COMMON_NAME + "_" + language;
		String wordInfoIndexName = Constants.WORD_INFO_INDEX_COMMON_NAME + "_" + language;

		ObjectMapper mapper = new ObjectMapper();
		ArrayList<String> citiationIndexes = new ArrayList<String>();
		Map<String, String> wordIndexesWithId = new HashMap<String, String>();
		Map<String, String> wordIndexInfoWithId = new HashMap<String, String>();
		ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();

		createCitationIndex(citationIndexName, Constants.CITATION_INDEX_TYPE, elasticSearchUtil);
		createWordIndex(wordIndexName, Constants.WORD_INDEX_TYPE, elasticSearchUtil);
		createWordInfoIndex(wordInfoIndexName, Constants.WORD_INFO_INDEX_TYPE, elasticSearchUtil);
		if (citationsList != null) {
			for (CitationBean citation : citationsList) {
				if (citation.getDate() == null || citation.getDate().isEmpty()) {
					citation.setDate(getFormattedDateTime(System.currentTimeMillis()));
				}
				String wordIdentifier = getWordIdentifierFromIndex(language, citation.getWord());
				String wordIndexJson;
				if (wordIdentifier != null) {
					if (citation.getRootWord() == null || citation.getRootWord().isEmpty()) {
						String rootWord = getRootWordsFromIndex(citation.getWord(), language);
						citation.setRootWord(rootWord);
					}
				} else if (wordIdentifier == null) {
					if (citation.getRootWord() != null && !citation.getRootWord().isEmpty()) {
						wordIdentifier = getWordIdentifierFromIndex(language, citation.getRootWord());
					} else {
						citation.setRootWord(citation.getWord());
					}
					if (wordIdentifier != null) {
						wordIndexJson = getWordIndex(citation.getWord(), citation.getRootWord(), wordIdentifier,
								mapper);
						wordIndexesWithId.put(citation.getWord(), wordIndexJson);
					} else {
						Map<String, Object> wordMap = new HashMap<String, Object>();
						wordMap.put("lemma", citation.getRootWord());
						List<Map<String, Object>> wordList = new ArrayList<Map<String, Object>>();
						wordList.add(wordMap);
						Request request = new Request();
						request.put("words", wordList);
						Response response = create(language, LanguageObjectTypes.Word.name(), request);
						List<String> nodeIdList = (List<String>) response.get("node_id");
						if (nodeIdList != null && !nodeIdList.isEmpty()) {
							wordIdentifier = nodeIdList.get(0);
							wordIndexJson = null;
							if (wordIdentifier != null) {
								wordIndexJson = getWordIndex(citation.getRootWord(), citation.getRootWord(),
										wordIdentifier, mapper);
								wordIndexesWithId.put(citation.getRootWord(), wordIndexJson);

								if (!citation.getWord().equalsIgnoreCase(citation.getRootWord())) {
									wordIndexJson = getWordIndex(citation.getWord(), citation.getRootWord(),
											wordIdentifier, mapper);
									wordIndexesWithId.put(citation.getWord(), wordIndexJson);
								}
							}
						} else {
							LOGGER.info("Unable to add word to graph");
						}
					}
				}
				String citationJson = mapper.writeValueAsString(citation);
				citiationIndexes.add(citationJson);
			}
			elasticSearchUtil.bulkIndexWithAutoGenerateIndexId(citationIndexName, Constants.CITATION_INDEX_TYPE,
					citiationIndexes);
			elasticSearchUtil.bulkIndexWithIndexId(wordIndexName, Constants.WORD_INDEX_TYPE, wordIndexesWithId);
		}
		if (wordInfoList != null) {
			for (WordInfoBean wordInfo : wordInfoList) {
				String wordInfoJson = mapper.writeValueAsString(wordInfo);
				wordIndexInfoWithId.put(wordInfo.getWord(), wordInfoJson);
			}
			elasticSearchUtil.bulkIndexWithIndexId(wordInfoIndexName, Constants.WORD_INFO_INDEX_TYPE,
					wordIndexInfoWithId);
		}
	}

	/**
	 * Creates word info index with predefined settings and mappings
	 * 
	 * @param indexName
	 *            name of the index
	 * 
	 * @param indexType
	 *            index type name
	 * 
	 * @param elasticSearchUtil
	 *            Elastic search utility
	 * 
	 * @return void
	 */
	// TODO: Use ElasticSearchUtil's generic index creator
	private void createWordInfoIndex(String indexName, String indexType, ElasticSearchUtil elasticSearchUtil)
			throws IOException {
		JSONBuilder settingBuilder = new JSONStringer();
		settingBuilder.object().key("settings").object().key("analysis").object().key("filter").object()
		.key("nfkc_normalizer").object().key("type").value("icu_normalizer").key("name").value("nfkc")
		.endObject().endObject().key("analyzer").object().key("ind_normalizer").object().key("tokenizer")
		.value("icu_tokenizer").key("filter").array().value("nfkc_normalizer").endArray().endObject()
		.endObject().endObject().endObject().endObject();

		JSONBuilder mappingBuilder = new JSONStringer();
		mappingBuilder.object().key(indexType).object().key("properties").object().key("word").object().key("type")
		.value("string").key("analyzer").value("ind_normalizer").endObject().key("rootWord").object()
		.key("type").value("string").key("analyzer").value("ind_normalizer").endObject().key("inflection")
		.object().key("type").value("string").key("analyzer").value("ind_normalizer").endObject().key("pos")
		.object().key("type").value("string").key("index").value("not_analyzed").endObject().key("gender")
		.object().key("type").value("string").key("index").value("not_analyzed").endObject().key("number")
		.object().key("type").value("string").key("index").value("not_analyzed").endObject().key("pers")
		.object().key("type").value("string").key("index").value("not_analyzed").endObject().key("wordCase")
		.object().key("type").value("string").key("index").value("not_analyzed").endObject().key("rts").object()
		.key("type").value("string").key("index").value("not_analyzed").endObject().endObject().endObject()
		.endObject();

		elasticSearchUtil.addIndex(indexName, indexType, settingBuilder.toString(), mappingBuilder.toString());
	}

	/**
	 * Creates word index with predefined settings and mappings
	 * 
	 * @param indexName
	 *            name of the index
	 * 
	 * @param indexType
	 *            index type name
	 * 
	 * @param elasticSearchUtil
	 *            Elastic search utility
	 * 
	 * @return void
	 */
	// TODO: Use ElasticSearchUtil's generic index creator
	public void createWordIndex(String indexName, String indexType, ElasticSearchUtil elasticSearchUtil)
			throws IOException {
		JSONBuilder settingBuilder = new JSONStringer();
		settingBuilder.object().key("settings").object().key("analysis").object().key("filter").object()
		.key("nfkc_normalizer").object().key("type").value("icu_normalizer").key("name").value("nfkc")
		.endObject().endObject().key("analyzer").object().key("ind_normalizer").object().key("tokenizer")
		.value("icu_tokenizer").key("filter").array().value("nfkc_normalizer").endArray().endObject()
		.endObject().endObject().endObject().endObject();

		JSONBuilder mappingBuilder = new JSONStringer();
		mappingBuilder.object().key(indexType).object().key("properties").object().key("word").object().key("type")
		.value("string").key("analyzer").value("ind_normalizer").key("fields").object().key("hash").object()
		.key("type").value("murmur3").endObject().endObject().endObject().key("rootWord").object().key("type")
		.value("string").key("analyzer").value("ind_normalizer").key("fields").object().key("hash").object()
		.key("type").value("murmur3").endObject().endObject().endObject().key("date").object().key("type")
		.value("date").key("format").value("dd-MMM-yyyy HH:mm:ss").endObject().endObject().endObject()
		.endObject();

		elasticSearchUtil.addIndex(indexName, indexType, settingBuilder.toString(), mappingBuilder.toString());
	}

	/**
	 * Creates citation index with predefined settings and mappings
	 * 
	 * @param indexName
	 *            name of the index
	 * 
	 * @param indexType
	 *            index type name
	 * 
	 * @param elasticSearchUtil
	 *            Elastic search utility
	 * 
	 * @return void
	 */
	// TODO: Use ElasticSearchUtil's generic index creator
	public void createCitationIndex(String indexName, String indexType, ElasticSearchUtil elasticSearchUtil)
			throws IOException {
		JSONBuilder settingBuilder = new JSONStringer();
		settingBuilder.object().key("settings").object().key("analysis").object().key("filter").object()
		.key("nfkc_normalizer").object().key("type").value("icu_normalizer").key("name").value("nfkc")
		.endObject().endObject().key("analyzer").object().key("ind_normalizer").object().key("tokenizer")
		.value("icu_tokenizer").key("filter").array().value("nfkc_normalizer").endArray().endObject()
		.endObject().endObject().endObject().endObject();

		JSONBuilder mappingBuilder = new JSONStringer();
		mappingBuilder.object().key(indexType).object().key("properties").object().key("word").object().key("type")
		.value("string").key("analyzer").value("ind_normalizer").key("fields").object().key("hash").object()
		.key("type").value("murmur3").endObject().endObject().endObject().key("rootWord").object().key("type")
		.value("string").key("analyzer").value("ind_normalizer").key("fields").object().key("hash").object()
		.key("type").value("murmur3").endObject().endObject().endObject().key("date").object().key("type")
		.value("date").key("format").value("dd-MMM-yyyy HH:mm:ss").endObject().key("sourceType").object()
		.key("type").value("string").key("index").value("not_analyzed").endObject().key("source").object()
		.key("type").value("string").key("index").value("not_analyzed").endObject().key("fileName").object()
		.key("type").value("string").key("index").value("not_analyzed").endObject().key("pos").object()
		.key("type").value("string").key("index").value("not_analyzed").endObject().key("grade").object()
		.key("type").value("string").key("index").value("not_analyzed").endObject().endObject().endObject()
		.endObject();

		elasticSearchUtil.addIndex(indexName, indexType, settingBuilder.toString(), mappingBuilder.toString());
	}

	/**
	 * Gets root words from Elasticsearch for a given word and language Id
	 * 
	 * @param word
	 *            word for which the root word needs to be found
	 * 
	 * @param languageId
	 *            index type name
	 * 
	 * @param elasticSearchUtil
	 *            Elastic search utility
	 * 
	 * @return void
	 */
	@SuppressWarnings({ "unchecked" })
	public String getRootWordsFromIndex(String word, String languageId) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil();
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, getList(mapper, word, null));
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class, searchCriteria, indexName,
				Constants.WORD_INDEX_TYPE);
		Map<String, Object> rootWordsMap = new HashMap<String, Object>();
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("rootWord", wordIndex.getRootWord());
			rootWordsMap.put(wordIndex.getWord(), wordMap);
		}

		if (rootWordsMap.get(word) != null) {
			return (String) ((Map<String, Object>) rootWordsMap.get(word)).get("rootWord");
		}
		return null;
	}

	/**
	 * Returns the word index DTO for a given word
	 * 
	 * @param word
	 *            lemma of the word
	 * 
	 * @param rootWord
	 *            lemma of the root word
	 * 
	 * @param wordIdentifier
	 *            ID of the word in the graph
	 * 
	 * @param mapper
	 *            objectMapper for JSON operations
	 * 
	 * @return void
	 */
	public String getWordIndex(String word, String rootWord, String wordIdentifier, ObjectMapper mapper)
			throws JsonGenerationException, JsonMappingException, IOException {
		String wordIndexJson = null;
		Map<String, String> wordIndex = new HashMap<String, String>();
		wordIndex.put("word", word);
		wordIndex.put("rootWord", rootWord);
		wordIndex.put("id", wordIdentifier);
		wordIndexJson = mapper.writeValueAsString(wordIndex);
		return wordIndexJson;
	}

	/**
	 * Searches the graph for given object type, and request
	 * 
	 * @param languageId
	 *            graphId for the language
	 * @param objectType
	 *            object type of nodes to be searched
	 * @param request
	 *            Map of filters
	 * @return Response from Graph
	 */
	@SuppressWarnings("unchecked")
	public Response list(String languageId, String objectType, Request request) {
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

	/**
	 * Returns the fields property from the request
	 * 
	 * @param request
	 *            Map of filters
	 * @return Array of fields
	 */
	@SuppressWarnings("unchecked")
	private String[] getFields(Request request) {
		Object objFields = request.get(PARAM_FIELDS);
		List<String> fields = getList(mapper, objFields, PARAM_FIELDS);
		if (null != fields && !fields.isEmpty()) {
			String[] arr = new String[fields.size()];
			for (int i = 0; i < fields.size(); i++)
				arr[i] = fields.get(i);
			return arr;
		}
		return null;
	}

	/**
	 * Converts a Node DTO into a MAP based on the array of fields provided
	 * 
	 * @param node
	 *            Node object that needs to be converted
	 * @param languageId
	 *            Graph Id for the language
	 * @param fields
	 *            array of fields that should be returned from the node DTO
	 * @return
	 */
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

	/**
	 * Adds relations data to the Word map from the Word Node
	 * 
	 * @param node
	 *            Node object of the Word
	 * @param map
	 *            Word map
	 */
	private void addRelationsData(Node node, Map<String, Object> map) {
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
				objects, converse);
		getOutRelationsData(node, synonyms, antonyms, hypernyms, hyponyms, homonyms, meronyms, tools, workers, actions,
				objects, converse);
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

	/**
	 * Retrieves the IN relations from a word node and populates the respective
	 * relations list
	 * 
	 * @param node
	 *            Word node DTO
	 * @param synonyms
	 *            List of synonyms
	 * @param antonyms
	 *            List of antonyms
	 * @param hypernyms
	 *            List of hypernyms
	 * @param hyponyms
	 *            List of hyponyms
	 * @param homonyms
	 *            List of homonyms
	 * @param meronyms
	 *            List of meronyms
	 * @param tools
	 *            List of tools
	 * @param workers
	 *            List of workers
	 * @param actions
	 *            List of actions
	 * @param objects
	 *            List of objects
	 * @param converse
	 *            List of converse
	 */
	private void getInRelationsData(Node node, List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
			List<NodeDTO> hypernyms, List<NodeDTO> hyponyms, List<NodeDTO> homonyms, List<NodeDTO> meronyms,
			List<NodeDTO> tools, List<NodeDTO> workers, List<NodeDTO> actions, List<NodeDTO> objects,
			List<NodeDTO> converse) {
		if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
			for (Relation inRel : node.getInRelations()) {
				if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(), inRel.getRelationType())) {
					if (null != inRel.getStartNodeMetadata() && !inRel.getStartNodeMetadata().isEmpty()) {
						inRel.getStartNodeMetadata().remove(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
						inRel.getStartNodeMetadata().remove(SystemProperties.IL_SYS_NODE_TYPE.name());
						inRel.getStartNodeMetadata().remove(SystemProperties.IL_UNIQUE_ID.name());
						synonyms.add(inRel.getStartNodeMetadata());
					}
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.ANTONYM.relationName(),
						inRel.getRelationType())) {
					antonyms.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.HYPERNYM.relationName(),
						inRel.getRelationType())) {
					hypernyms.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.HYPONYM.relationName(),
						inRel.getRelationType())) {
					hyponyms.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.MERONYM.relationName(),
						inRel.getRelationType())) {
					meronyms.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.TOOL.relationName(), inRel.getRelationType())) {
					tools.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.WORKER.relationName(), inRel.getRelationType())) {
					workers.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.ACTION.relationName(), inRel.getRelationType())) {
					actions.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.OBJECT.relationName(), inRel.getRelationType())) {
					objects.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.CONVERSE.relationName(),
						inRel.getRelationType())) {
					converse.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel.getRelationType()));
				}
			}
		}
	}

	/**
	 * Retrieves the OUT relations from a word node and populates the respective
	 * relations list
	 * 
	 * @param node
	 *            Word node DTO
	 * @param synonyms
	 *            List of synonyms
	 * @param antonyms
	 *            List of antonyms
	 * @param hypernyms
	 *            List of hypernyms
	 * @param hyponyms
	 *            List of hyponyms
	 * @param homonyms
	 *            List of homonyms
	 * @param meronyms
	 *            List of meronyms
	 * @param tools
	 *            List of tools
	 * @param workers
	 *            List of workers
	 * @param actions
	 *            List of actions
	 * @param objects
	 *            List of objects
	 * @param converse
	 *            List of converse
	 */
	private void getOutRelationsData(Node node, List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
			List<NodeDTO> hypernyms, List<NodeDTO> hyponyms, List<NodeDTO> homonyms, List<NodeDTO> meronyms,
			List<NodeDTO> tools, List<NodeDTO> workers, List<NodeDTO> actions, List<NodeDTO> objects,
			List<NodeDTO> converse) {
		if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			for (Relation outRel : node.getOutRelations()) {
				if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(), outRel.getRelationType())) {
					if (null != outRel.getEndNodeMetadata() && !outRel.getEndNodeMetadata().isEmpty()) {
						outRel.getEndNodeMetadata().remove(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
						outRel.getEndNodeMetadata().remove(SystemProperties.IL_SYS_NODE_TYPE.name());
						outRel.getEndNodeMetadata().remove(SystemProperties.IL_UNIQUE_ID.name());
						synonyms.add(outRel.getEndNodeMetadata());
					}
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.ANTONYM.relationName(),
						outRel.getRelationType())) {
					antonyms.add(new NodeDTO(outRel.getEndNodeId(), outRel.getEndNodeName(),
							outRel.getEndNodeObjectType(), outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.HYPERNYM.relationName(),
						outRel.getRelationType())) {
					hypernyms.add(new NodeDTO(outRel.getEndNodeId(), outRel.getEndNodeName(),
							outRel.getEndNodeObjectType(), outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.HYPONYM.relationName(),
						outRel.getRelationType())) {
					hyponyms.add(new NodeDTO(outRel.getEndNodeId(), outRel.getEndNodeName(),
							outRel.getEndNodeObjectType(), outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.MERONYM.relationName(),
						outRel.getRelationType())) {
					meronyms.add(new NodeDTO(outRel.getEndNodeId(), outRel.getEndNodeName(),
							outRel.getEndNodeObjectType(), outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.TOOL.relationName(), outRel.getRelationType())) {
					tools.add(new NodeDTO(outRel.getEndNodeId(), outRel.getEndNodeName(), outRel.getEndNodeObjectType(),
							outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.WORKER.relationName(),
						outRel.getRelationType())) {
					workers.add(new NodeDTO(outRel.getEndNodeId(), outRel.getEndNodeName(),
							outRel.getEndNodeObjectType(), outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.ACTION.relationName(),
						outRel.getRelationType())) {
					actions.add(new NodeDTO(outRel.getEndNodeId(), outRel.getEndNodeName(),
							outRel.getEndNodeObjectType(), outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.OBJECT.relationName(),
						outRel.getRelationType())) {
					objects.add(new NodeDTO(outRel.getEndNodeId(), outRel.getEndNodeName(),
							outRel.getEndNodeObjectType(), outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.CONVERSE.relationName(),
						outRel.getRelationType())) {
					converse.add(new NodeDTO(outRel.getEndNodeId(), outRel.getEndNodeName(),
							outRel.getEndNodeObjectType(), outRel.getRelationType()));
				}
			}
		}
	}

	/**
	 * Creates a Node on the Graph
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param objectType
	 *            Type of the Object
	 * @param request
	 *            Map of request body, contains list of words
	 * @return Response object
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Response create(String languageId, String objectType, Request request)
			throws JsonGenerationException, JsonMappingException, IOException {
		if (StringUtils.isBlank(languageId))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid Language Id");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECTTYPE.name(), "ObjectType is blank");
		List<Map> items = (List<Map>) request.get("words");
		List<Node> nodeList = new ArrayList<Node>();
		if (null == items || items.size() <= 0)
			throw new ClientException(LanguageErrorCodes.ERR_INVALID_OBJECT.name(), objectType + " Object is blank");
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
			createRes.getResult().remove("node_id");
			createRes.put("node_id", lstNodeId);
			return createRes;
		} else {
			errResponse.getResult().remove("node_id");
			errResponse.put("node_id", lstNodeId);
			return errResponse;
		}
	}

	/**
	 * Converts the given map into a Node object
	 * 
	 * @param map
	 *            Map represnetation of object
	 * @param definition
	 *            DefintiionDTO of the object
	 * @return Node object
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Node convertToGraphNode(Map<String, Object> map, DefinitionDTO definition)
			throws JsonGenerationException, JsonMappingException, IOException {
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
					String objectStr = mapper.writeValueAsString(entry.getValue());
					List<String> tags = mapper.readValue(objectStr, List.class);
					if (null != tags && !tags.isEmpty())
						node.setTags(tags);
				} else if (inRelDefMap.containsKey(entry.getKey())) {
					String objectStr = mapper.writeValueAsString(entry.getValue());
					List<Map> list = mapper.readValue(objectStr, List.class);
					if (null != list && !list.isEmpty()) {
						for (Map obj : list) {
							NodeDTO dto = (NodeDTO) mapper.convertValue(obj, NodeDTO.class);
							inRelations.add(new Relation(dto.getIdentifier(), inRelDefMap.get(entry.getKey()), null));
						}
					}
				} else if (outRelDefMap.containsKey(entry.getKey())) {
					String objectStr = mapper.writeValueAsString(entry.getValue());
					List<Map> list = mapper.readValue(objectStr, List.class);
					if (null != list && !list.isEmpty()) {
						for (Map obj : list) {
							NodeDTO dto = (NodeDTO) mapper.convertValue(obj, NodeDTO.class);
							outRelations.add(new Relation(null, outRelDefMap.get(entry.getKey()), dto.getIdentifier()));
						}
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
	 * Returns both IN relations and OUT relation deinfitions from a given
	 * Definition DTO
	 * 
	 * @param definition
	 *            Defintion DTO
	 * @param inRelDefMap
	 *            Map of relation title to relation name for IN relations
	 * @param outRelDefMap
	 *            Map of relation title to relation name for OUT relations
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
	 * Validate word citations request for null or empty words
	 * 
	 * @param citationBeanList
	 *            List of Citation beans
	 * @return List of error messages
	 */
	public ArrayList<String> validateCitationsList(List<CitationBean> citationBeanList) {
		ArrayList<String> errorMessageList = new ArrayList<String>();
		for (CitationBean citation : citationBeanList) {
			if (citation.getWord() == null || citation.getWord().isEmpty()) {
				errorMessageList.add("Word cannot be null");
			}
		}
		return errorMessageList;
	}

	/**
	 * Search words based on list of lemmas
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param lemmas
	 *            list of lemmas
	 * @return Map of lemma to Word Node
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Node> searchWords(String languageId, List<String> lemmas) {
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		Set<String> words = new HashSet<String>();
		words.addAll(lemmas);
		Response findRes = getSearchWordsResponse(languageId, ATTRIB_LEMMA, lemmas);
		if (!checkError(findRes)) {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0) {
				for (Node node : nodes) {
					String wordLemma = (String) node.getMetadata().get(ATTRIB_LEMMA);
					nodeMap.put(wordLemma, node);
					words.remove(wordLemma);
				}
			}
		}
		if (null != words && !words.isEmpty()) {
			Response searchRes = getSearchWordsResponse(languageId, ATTRIB_VARIANTS, new ArrayList<String>(words));
			if (!checkError(searchRes)) {
				List<Node> nodes = (List<Node>) searchRes.get(GraphDACParams.node_list.name());
				if (null != nodes && nodes.size() > 0) {
					for (Node node : nodes) {
						String wordLemma = (String) node.getMetadata().get(ATTRIB_LEMMA);
						nodeMap.put(wordLemma, node);
					}
				}
			}
		}
		LOGGER.info("returning nodemap size: " + nodeMap.size());
		return nodeMap;
	}

	/**
	 * Search words based on either lemma or variants fields
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param lemmas
	 *            list of lemmas
	 * @return Map of lemma to Word Node
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Node> searchWordsForComplexity(String languageId, List<String> lemmas) {
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		Set<String> words = new HashSet<String>();
		words.addAll(lemmas);
		Response findRes = getSearchWordsResponse(languageId, ATTRIB_LEMMA, lemmas);
		if (!checkError(findRes)) {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0) {
				for (Node node : nodes) {
					String wordLemma = (String) node.getMetadata().get(ATTRIB_LEMMA);
					nodeMap.put(wordLemma, node);
					words.remove(wordLemma);
				}
			}
		}
		if (null != words && !words.isEmpty()) {
			Response searchRes = getSearchWordsResponse(languageId, ATTRIB_VARIANTS, new ArrayList<String>(words));
			if (!checkError(searchRes)) {
				List<Node> nodes = (List<Node>) searchRes.get(GraphDACParams.node_list.name());
				if (null != nodes && nodes.size() > 0) {
					for (Node node : nodes) {
						node.getMetadata().put(LanguageParams.morphology.name(), true);
						String wordLemma = (String) node.getMetadata().get(ATTRIB_LEMMA);
						if (node.getMetadata().get(ATTRIB_VARIANTS) != null) {
							String[] variants = (String[]) node.getMetadata().get(ATTRIB_VARIANTS);
							for (String variant : variants) {
								nodeMap.put(variant, node);
							}
						}
						nodeMap.put(wordLemma, node);
					}
				}
			}
		}
		LOGGER.info("returning nodemap size: " + nodeMap.size());
		return nodeMap;
	}

	/**
	 * Performs a search on the graph using a property and a list of property
	 * values
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param property
	 *            field that will be used for search
	 * @param words
	 *            List of property values
	 * @return
	 */
	private Response getSearchWordsResponse(String languageId, String property, List<String> words) {
		SearchCriteria sc = new SearchCriteria();
		sc.setObjectType("Word");
		sc.addMetadata(MetadataCriterion.create(Arrays.asList(new Filter(property, SearchConditions.OP_IN, words))));
		Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes");
		req.put(GraphDACParams.search_criteria.name(), sc);
		Response searchRes = getResponse(req, LOGGER);
		return searchRes;
	}

	/**
	 * Searches for a word in the graph using the lemma
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param lemma
	 *            lemma of thw word
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Node searchWord(String languageId, String lemma) {
		Node node = null;
		Property property = new Property(LanguageParams.lemma.name(), lemma);
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodesByProperty");
		request.put(GraphDACParams.metadata.name(), property);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		if (!checkError(findRes)) {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0)
				node = nodes.get(0);
		}
		if (null == node) {
			SearchCriteria sc = new SearchCriteria();
			sc.setObjectType("Word");
			sc.addMetadata(MetadataCriterion
					.create(Arrays.asList(new Filter("variants", SearchConditions.OP_IN, Arrays.asList(lemma)))));
			sc.setResultSize(1);
			Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes");
			req.put(GraphDACParams.search_criteria.name(), sc);
			Response searchRes = getResponse(req, LOGGER);
			if (!checkError(searchRes)) {
				List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
				if (null != nodes && nodes.size() > 0)
					node = nodes.get(0);
			}
		}
		return node;
	}

	/**
	 * Searches for a word in the graph using the lemma or variants property
	 * 
	 * @param languageId
	 * @param lemma
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Node searchWordForComplexity(String languageId, String lemma) {
		Node node = null;
		Property property = new Property(LanguageParams.lemma.name(), lemma);
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodesByProperty");
		request.put(GraphDACParams.metadata.name(), property);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		if (!checkError(findRes)) {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0)
				node = nodes.get(0);
		}
		if (null == node) {
			SearchCriteria sc = new SearchCriteria();
			sc.setObjectType("Word");
			sc.addMetadata(MetadataCriterion
					.create(Arrays.asList(new Filter("variants", SearchConditions.OP_IN, Arrays.asList(lemma)))));
			sc.setResultSize(1);
			Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes");
			req.put(GraphDACParams.search_criteria.name(), sc);
			Response searchRes = getResponse(req, LOGGER);
			if (!checkError(searchRes)) {
				List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
				if (null != nodes && nodes.size() > 0)
					node = nodes.get(0);
				node.getMetadata().put(LanguageParams.morphology.name(), true);
			}
		}
		return node;
	}

	/**
	 * Creates a cache of word Ids using the lemma or variant as the key
	 * 
	 * @param languageId
	 * @param wordLemmaMap
	 *            Map of Lemma/Variant to Word Id
	 * @param errorMessages
	 *            List of error messages during this operation
	 */
	public void cacheAllWords(String languageId, Map<String, String> wordLemmaMap, List<String> errorMessages) {
		try {
			long startTime = System.currentTimeMillis();
			List<Node> nodes = getAllObjects(languageId, LanguageParams.Word.name());
			long endTime = System.currentTimeMillis();
			System.out.println("Time taken to load all words: " + (endTime - startTime));
			if (nodes != null) {
				for (Node node : nodes) {
					Map<String, Object> metadata = node.getMetadata();
					String lemma = (String) metadata.get("lemma");
					wordLemmaMap.put(lemma, node.getIdentifier());

					String[] variants = (String[]) metadata.get("variants");
					if (variants != null) {
						for (String variant : variants) {
							wordLemmaMap.put(variant, node.getIdentifier());
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			errorMessages.add(e.getMessage());
		}
	}

	/**
	 * Gets all objects of a given object type
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param objectType
	 * @return List of nodes
	 */
	@SuppressWarnings("unchecked")
	public List<Node> getAllObjects(String languageId, String objectType) {
		Property property = new Property(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), objectType);
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodesByProperty");
		request.put(GraphDACParams.metadata.name(), property);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		if (!checkError(findRes)) {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			return nodes;
		}
		return null;
	}

	/**
	 * Get the data node from Graph based on the node Id
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param nodeId
	 * @return Node
	 */
	public Node getDataNode(String languageId, String nodeId) {
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode");
		request.put(GraphDACParams.node_id.name(), nodeId);
		request.put(GraphDACParams.get_tags.name(), true);

		Response findRes = getResponse(request, LOGGER);
		if (checkError(findRes))
			return null;
		else {
			Node node = (Node) findRes.get(GraphDACParams.node.name());
			if (null != node)
				return node;
		}
		return null;
	}

	/**
	 * Creates word node in the Graph
	 * 
	 * @param languageId
	 *            Graph Id
	 * @param word
	 *            lemma of the word
	 * @param objectType
	 *            object type of the node to be created
	 * @return
	 */
	public String createWord(String languageId, String word, String objectType) {
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
	 * Returns the error message from the response
	 * 
	 * @param response
	 *            Response from akka request
	 */
	@SuppressWarnings("unchecked")
	public String getErrorMessage(Response response) {
		String errorMessage = "";
		ResponseParams params = response.getParams();
		if (null != params) {
			errorMessage = errorMessage + ": " + params.getErrmsg();
		}
		List<String> messages = (List<String>) response.get("messages");
		if (messages != null) {
			for (String message : messages) {
				errorMessage = errorMessage + ": " + message;
			}
		}
		if (!errorMessage.isEmpty()) {
			return errorMessage.substring(2);
		}
		return response.getResponseCode().name();
	}

	/**
	 * Given a words with relations to other words, creates the related words in
	 * the graph
	 * 
	 * @param synsetRelations
	 *            Map of relation name to list of words
	 * @param languageId
	 * @param errorMessages
	 *            List of error messages
	 * @param wordDefintion
	 *            Definition DTO of the word
	 * @param wordLemmaMap
	 *            Cache of word lemma to word Id
	 * @param nodeIds
	 *            List of word Ids
	 * @return List of word Ids created
	 */
	private List<String> processRelationWords(List<Map<String, Object>> synsetRelations, String languageId,
			List<String> errorMessages, DefinitionDTO wordDefintion, Map<String, String> wordLemmaMap,
			ArrayList<String> nodeIds) {
		List<String> wordIds = new ArrayList<String>();
		if (synsetRelations != null) {
			for (Map<String, Object> word : synsetRelations) {
				String wordId = createOrUpdateWordsWithoutPrimaryMeaning(word, languageId, errorMessages, wordDefintion,
						wordLemmaMap, nodeIds);
				if (wordId != null) {
					wordIds.add(wordId);
				}
			}
		}
		return wordIds;
	}

	/**
	 * Creates or updates words without processing words in the relations
	 * 
	 * @param word
	 *            Word object as a map
	 * @param languageId
	 * @param errorMessages
	 *            List of error messages
	 * @param definition
	 *            Definition DTO of the word
	 * @param wordLemmaMap
	 *            Cache of word lemma to word Id
	 * @param nodeIds
	 *            List of word Ids
	 * @return
	 */
	private String createOrUpdateWordsWithoutPrimaryMeaning(Map<String, Object> word, String languageId,
			List<String> errorMessages, DefinitionDTO definition, Map<String, String> wordLemmaMap,
			ArrayList<String> nodeIds) {
		String lemma = (String) word.get(LanguageParams.lemma.name());
		if (lemma == null || lemma.trim().isEmpty()) {
			return null;
		} else {
			word.put(LanguageParams.lemma.name(), lemma.trim());
			String identifier = (String) word.get(LanguageParams.identifier.name());
			if (identifier == null) {
				identifier = wordLemmaMap.get(lemma);
				if (identifier != null) {
					return identifier;
				}
			}
			Response wordResponse;
			List<String> sources = new ArrayList<String>();
			sources.add(ATTRIB_SOURCE_IWN);
			word.put(ATTRIB_SOURCES, sources);
			Node wordNode = convertToGraphNode(languageId, LanguageParams.Word.name(), word, definition,false);
			wordNode.setObjectType(LanguageParams.Word.name());
			if (identifier == null) {
				wordResponse = createWord(wordNode, languageId);
			} else {
				wordResponse = updateWord(wordNode, languageId, identifier);
			}
			if (checkError(wordResponse)) {
				errorMessages.add(getErrorMessage(wordResponse));
				return null;
			}
			String nodeId = (String) wordResponse.get(GraphDACParams.node_id.name());
			wordLemmaMap.put(lemma, nodeId);
			nodeIds.add(nodeId);
			return nodeId;
		}
	}

	/**
	 * Creates word in the graph
	 * 
	 * @param node
	 *            Node object of the word
	 * @param languageId
	 * @return Response
	 */
	private Response createWord(Node node, String languageId) {
		Request createReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "createDataNode");
		createReq.put(GraphDACParams.node.name(), node);
		Response res = getResponse(createReq, LOGGER);
		return res;
	}

	/**
	 * Updates the word in the graph
	 * 
	 * @param node
	 *            Node object of the word
	 * @param languageId
	 * @param wordId
	 *            Id of the word that needs to be updated
	 * @return
	 */
	public Response updateWord(Node node, String languageId, String wordId) {
		Request createReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		createReq.put(GraphDACParams.node.name(), node);
		createReq.put(GraphDACParams.node_id.name(), wordId);
		Response res = getResponse(createReq, LOGGER);
		return res;
	}

	/**
	 * Updates the cache of Word ids using lemma as the key, with new words
	 * 
	 * @param lemmaIdMap
	 *            Existing cache
	 * @param languageId
	 *            Graph Id
	 * @param objectType
	 * @param words
	 *            Set of words
	 */
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
	 * Converts a map object into a node object
	 * 
	 * @param languageId
	 * @param objectType
	 * @param map
	 *            Map representation of graph node
	 * @param definition
	 *            Defintion DTO of the result node
	 * @return Node object
	 */
	// TODO: Must Refactor and/or remove
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node convertToGraphNode(String languageId, String objectType, Map<String, Object> map,
			DefinitionDTO definition,boolean updateRel) {
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
											outRels.add(
													new Relation(null, RelationTypes.SYNONYM.relationName(), wordId));
										} else {
											String wordId = createWord(lemmaIdMap, languageId, word, objectType);
											outRels.add(
													new Relation(null, RelationTypes.SYNONYM.relationName(), wordId));
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

	/**
	 * Crates the word in the Graph and adds to word ID cache
	 * 
	 * @param lemmaIdMap
	 *            Word Id cache
	 * @param languageId
	 * @param word
	 *            lemma of the word
	 * @param objectType
	 *            object type of the word
	 * @return Id of the word
	 */
	private String createWord(Map<String, String> lemmaIdMap, String languageId, String word, String objectType) {
		String nodeId = createWord(languageId, word, objectType);
		lemmaIdMap.put(word, nodeId);
		return nodeId;
	}

	/**
	 * Gets the Definition DTO for a given object type
	 * 
	 * @param definitionName
	 *            Object type
	 * @param graphId
	 * @return
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

	/**
	 * Creates the Synset node in the graph
	 * 
	 * @param languageId
	 * @param synsetObj
	 * @param synsetDefinition
	 * @return
	 * @throws Exception
	 */
	private Response createSynset(String languageId, Map<String, Object> synsetObj, DefinitionDTO synsetDefinition, int indoWordnetId, int englishTranslationId)
			throws Exception {
		String operation = "updateDataNode";
		String identifier = (String) synsetObj.get(LanguageParams.identifier.name());
		if (identifier == null || identifier.isEmpty()) {
			operation = "createDataNode";
		}

		Node synsetNode = convertToGraphNode(synsetObj, synsetDefinition);
		synsetNode.setObjectType(LanguageParams.Synset.name());
		Request synsetReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, operation);
		synsetReq.put(GraphDACParams.node.name(), synsetNode);
		if (operation.equalsIgnoreCase("updateDataNode")) {
			synsetReq.put(GraphDACParams.node_id.name(), synsetNode.getIdentifier());
		}
		Response res = getResponse(synsetReq, LOGGER);
		String primaryMeaningId = (String) res.get(GraphDACParams.node_id.name());
		//createProxyNode(primaryMeaningId,LanguageParams.translations.name(),indoWordnetId);
		createProxyNodeAndTranslationSet(primaryMeaningId, LanguageParams.translations.name(),indoWordnetId, englishTranslationId);
		return res;
	}

	/**
	 * Creates or updates a word with a primary meaning and processes all
	 * relation words
	 * 
	 * @param item
	 *            Word object from request
	 * @param languageId
	 * @param wordLemmaMap
	 *            Cahce of word lemma to word Id
	 * @param wordDefinition
	 * @param nodeIds
	 *            List of nodes created
	 * @param synsetDefinition
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> createOrUpdateWord(Map<String, Object> item, String languageId,
			Map<String, String> wordLemmaMap, DefinitionDTO wordDefinition, ArrayList<String> nodeIds,
			DefinitionDTO synsetDefinition, int englishTranslationId) {
		Response createRes = new Response();
		List<String> errorMessages = new ArrayList<String>();
		try {
			int indowordnetId = (int) item.get(LanguageParams.indowordnetId.name());

			Map<String, Object> primaryMeaning = (Map<String, Object>) item.get(LanguageParams.primaryMeaning.name());
			if (primaryMeaning == null) {
				errorMessages
				.add("Primary meaning field is missing: Id: " + indowordnetId + " Language: " + languageId);
			}
			
			// get gloss of the primary meaning
			String gloss = (String) primaryMeaning.get(LanguageParams.gloss.name());

			// create or update Primary meaning Synset
			List<String> synsetRelations = Arrays.asList(new String[] { LanguageParams.synonyms.name(),
					LanguageParams.hypernyms.name(), LanguageParams.holonyms.name(), LanguageParams.antonyms.name(),
					LanguageParams.hyponyms.name(), LanguageParams.meronyms.name(), LanguageParams.tools.name(),
					LanguageParams.workers.name(), LanguageParams.actions.name(), LanguageParams.objects.name(),
					LanguageParams.converse.name() });

			Map<String, List<String>> relationWordIdMap = new HashMap<String, List<String>>();

			for (String synsetRelationName : synsetRelations) {
				List<Map<String, Object>> relations = (List<Map<String, Object>>) primaryMeaning
						.get(synsetRelationName);
				List<String> relationWordIds = processRelationWords(relations, languageId, errorMessages,
						wordDefinition, wordLemmaMap, nodeIds);
				if (relationWordIds != null && !relationWordIds.isEmpty()) {
					relationWordIdMap.put(synsetRelationName, relationWordIds);
				}
				primaryMeaning.remove(synsetRelationName);
			}

			String synsetIdentifer = languageId + ":S:" + String.format("%08d", indowordnetId);
			primaryMeaning.put(LanguageParams.identifier.name(), synsetIdentifer);
			Response synsetResponse = createSynset(languageId, primaryMeaning, synsetDefinition, indowordnetId, englishTranslationId);
			if (checkError(synsetResponse)) {
				errorMessages
				.add(getErrorMessage(synsetResponse) + ": Id: " + indowordnetId + " Language: " + languageId);
			}
			Map<String, String> relationNameMap = new HashMap<String, String>();
			relationNameMap.put(LanguageParams.synonyms.name(), RelationTypes.SYNONYM.relationName());
			relationNameMap.put(LanguageParams.hypernyms.name(), RelationTypes.HYPERNYM.relationName());
			relationNameMap.put(LanguageParams.hyponyms.name(), RelationTypes.HYPONYM.relationName());
			relationNameMap.put(LanguageParams.holonyms.name(), RelationTypes.HOLONYM.relationName());
			relationNameMap.put(LanguageParams.antonyms.name(), RelationTypes.ANTONYM.relationName());
			relationNameMap.put(LanguageParams.meronyms.name(), RelationTypes.MERONYM.relationName());
			relationNameMap.put(LanguageParams.tools.name(), RelationTypes.TOOL.relationName());
			relationNameMap.put(LanguageParams.objects.name(), RelationTypes.OBJECT.relationName());
			relationNameMap.put(LanguageParams.actions.name(), RelationTypes.ACTION.relationName());
			relationNameMap.put(LanguageParams.workers.name(), RelationTypes.WORKER.relationName());
			relationNameMap.put(LanguageParams.converse.name(), RelationTypes.CONVERSE.relationName());

			String primaryMeaningId = (String) synsetResponse.get(GraphDACParams.node_id.name());
			for (String synsetRelationName : synsetRelations) {
				List<String> wordIds = relationWordIdMap.get(synsetRelationName);
				String relationName = relationNameMap.get(synsetRelationName);
				addSynsetRelation(wordIds, relationName, languageId, primaryMeaningId, errorMessages);
			}

			// create Word
			item.remove(LanguageParams.primaryMeaning.name());
			List<String> words = (List<String>) item.get(LanguageParams.words.name());
			for (String lemma : words) {
				boolean createFlag = true;
				String wordIdentifier = wordLemmaMap.get(lemma);
				if (wordIdentifier != null) {
					createFlag = false;
					addSynonymRelation(languageId, wordIdentifier, primaryMeaningId, errorMessages);
					continue;
				}
				Map<String, Object> wordMap = new HashMap<String, Object>();
				wordMap.put(LanguageParams.lemma.name(), lemma.trim());
				wordMap.put(LanguageParams.primaryMeaningId.name(), primaryMeaningId);
				wordMap.put(LanguageParams.meaning.name(), gloss);
				String pos = (String) primaryMeaning.get(LanguageParams.pos.name());
				if (StringUtils.isNotBlank(pos)) {
					List<String> posList = new ArrayList<String>();
					posList.add(pos);
					wordMap.put(LanguageParams.pos.name(), posList);
				}
				List<String> sources = new ArrayList<String>();
				sources.add(ATTRIB_SOURCE_IWN);
				wordMap.put(ATTRIB_SOURCES, sources);
				Node node = convertToGraphNode(languageId, LanguageParams.Word.name(), wordMap, wordDefinition,true);
				node.setObjectType(LanguageParams.Word.name());
				if (createFlag) {
					createRes = createWord(node, languageId);
				} else {
					String status = (String) node.getMetadata().get(LanguageParams.status.name());
					if(!StringUtils.equalsIgnoreCase(languageId, "en") || 
							(StringUtils.equalsIgnoreCase(languageId, "en") && StringUtils.equalsIgnoreCase(status,LanguageParams.Draft.name()))){
						createRes = updateWord(node, languageId, wordIdentifier);
					}/*else if(StringUtils.equalsIgnoreCase(languageId, "en") && StringUtils.equalsIgnoreCase(status,LanguageParams.live.name())){
						createRes = updateWord(node, languageId, wordIdentifier);
					}*/

				}
				if (!checkError(createRes)) {
					String wordId = (String) createRes.get("node_id");
					wordLemmaMap.put(lemma, wordId);
					nodeIds.add(wordId);
					// add Synonym Relation
					addSynonymRelation(languageId, wordId, primaryMeaningId, errorMessages);
				} else {
					errorMessages.add(getErrorMessage(createRes));
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			e.printStackTrace();
			errorMessages.add(e.getMessage());
		}
		return errorMessages;
	}

	/**
	 * Processes and creates a synonym relation for the word
	 * 
	 * @param languageId
	 * @param wordId
	 *            ID of the word
	 * @param synsetId
	 *            ID of the synset
	 * @param errorMessages
	 *            List of error messages
	 */
	private void addSynonymRelation(String languageId, String wordId, String synsetId, List<String> errorMessages) {
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
		request.put(GraphDACParams.start_node_id.name(), synsetId);
		request.put(GraphDACParams.relation_type.name(), RelationTypes.SYNONYM.relationName());
		request.put(GraphDACParams.end_node_id.name(), wordId);
		Response response = getResponse(request, LOGGER);
		if (checkError(response)) {
			errorMessages.add(getErrorMessage(response));
		}
	}

	/**
	 * Get word from graph
	 * 
	 * @param wordId
	 *            Id of the word
	 * @param languageId
	 *            Graph ID
	 * @param errorMessages
	 *            List of error messages
	 * @return
	 */
	private Node getWord(String wordId, String languageId, List<String> errorMessages) {
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), wordId);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request, LOGGER);
		if (checkError(getNodeRes)) {
			errorMessages.add(getErrorMessage(getNodeRes));
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		return node;
	}

	/**
	 * Creates relations between list of words and a synset
	 * 
	 * @param wordIds
	 *            List of word Ids
	 * @param relationType
	 *            Type of relation to be created
	 * @param languageId
	 * @param synsetId
	 *            Id of the synset
	 * @param errorMessages
	 *            List of error messages
	 */
	private void addSynsetRelation(List<String> wordIds, String relationType, String languageId, String synsetId,
			List<String> errorMessages) {
		if (wordIds != null) {
			for (String wordId : wordIds) {
				if (relationType.equalsIgnoreCase(RelationTypes.SYNONYM.relationName())) {
					Node wordNode = getWord(wordId, languageId, errorMessages);
					Map<String, Object> metadata = wordNode.getMetadata();
					if (metadata != null) {
						String primaryMeaningId = (String) metadata.get(ATTRIB_PRIMARY_MEANING_ID);
						if (primaryMeaningId != null && !primaryMeaningId.equalsIgnoreCase(synsetId)) {
							errorMessages.add("Word :" + wordId + " has an existing different primary meaning");
							continue;
						} else if (primaryMeaningId == null) {
							metadata.put(ATTRIB_PRIMARY_MEANING_ID, synsetId);
							wordNode.setMetadata(metadata);
							wordNode.setObjectType(LanguageParams.Word.name());
							updateWord(wordNode, languageId, wordId);
						}
					}
				}
				addSynsetRelation(wordId, relationType, languageId, synsetId, errorMessages);
			}
		}
	}

	/**
	 * Creates relation between word and synset
	 * 
	 * @param wordId
	 *            Id of the word
	 * @param relationType
	 *            Type of relation
	 * @param languageId
	 *            Graph Id
	 * @param synsetId
	 *            Id of the synset
	 * @param errorMessages
	 *            List of error messages
	 */
	private void addSynsetRelation(String wordId, String relationType, String languageId, String synsetId,
			List<String> errorMessages) {
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
		request.put(GraphDACParams.start_node_id.name(), synsetId);
		request.put(GraphDACParams.relation_type.name(), relationType);
		request.put(GraphDACParams.end_node_id.name(), wordId);
		Response response = getResponse(request, LOGGER);
		if (checkError(response)) {
			errorMessages.add(getErrorMessage(response));
		}
	}

	/**
	 * Transliterates an english text into the specified language
	 * 
	 * @param languageId
	 *            language into which the text needs to be transliterated
	 * @param text
	 *            the text which needs to be transliterated
	 * @param addEndVirama
	 *            if virama needs to be added at the end of words that end with
	 *            a consonant
	 * @return the transliterated text
	 */
	public String transliterateText(String languageId, String text, boolean addEndVirama) {
		if (StringUtils.isNotBlank(text)) {
			Map<String, String> tokenMap = new HashMap<String, String>();
			StringBuilder result = new StringBuilder();
			StringTokenizer st = new StringTokenizer(text);
			while (st.hasMoreTokens()) {
				String word = st.nextToken().trim();
				String token = LanguageUtil.replacePunctuations(word);
				if (tokenMap.containsKey(token)) {
					result.append(tokenMap.get(token)).append(" ");
				} else {
					String arpabets = WordCacheUtil.getArpabets(token);
					if (StringUtils.isNotBlank(arpabets)) {
						List<Node> varnas = new ArrayList<Node>();
						boolean found = getVarnasForWord(languageId, arpabets, varnas);
						if (found) {
							List<String> unicodes = getUnicodes(languageId, varnas, addEndVirama);
							String output = getTextFromUnicode(unicodes);
							tokenMap.put(token, output);
							result.append(output).append(" ");
						} else {
							result.append(word).append(" ");
						}
					} else {
						result.append(word).append(" ");
					}
				}
			}
			return result.toString();
		}
		return text;
	}

	/**
	 * Returns the list of unicodes for the given list of varna objects.
	 * 
	 * @param languageId
	 *            code of the language to which the varnas belong to
	 * @param varnas
	 *            list of varnas whose unicodes are returned
	 * @param addEndVirama
	 *            if virama needs to be added after the last varna (if it is a
	 *            consonant)
	 * @return the list of unicodes for the input list of varnas
	 */
	private List<String> getUnicodes(String languageId, List<Node> varnas, boolean addEndVirama) {
		VarnaCache cache = VarnaCache.getInstance();
		List<String> unicodes = new ArrayList<String>();
		boolean start_of_syllable = true;
		String lastVarna = null;
		String viramaUnicode = cache.getViramaUnicode(languageId);
		for (Node varna : varnas) {
			String varnaType = (String) varna.getMetadata().get(GraphDACParams.type.name());
			lastVarna = varnaType;
			String varnaUnicode = (String) varna.getMetadata().get(GraphDACParams.unicode.name());
			if (varnaType.equalsIgnoreCase("Consonant")) {
				if (start_of_syllable) {
					start_of_syllable = false;
				} else {
					if (StringUtils.isNotEmpty(viramaUnicode))
						unicodes.add(viramaUnicode);
					else
						throw new ServerException(LanguageErrorCodes.ERROR_VIRAMA_NOT_FOUND.name(),
								"Virama is not found in " + languageId + " graph");
				}
				unicodes.add(varnaUnicode);
			} else if (varnaType.equalsIgnoreCase("Vowel")) {
				if (start_of_syllable) {
					unicodes.add(varnaUnicode);
				} else {
					String vowelSignUnicode = cache.getVowelSign(languageId, varna.getIdentifier());
					if (StringUtils.isNotEmpty(vowelSignUnicode))
						unicodes.add(vowelSignUnicode);
					start_of_syllable = true;
				}
			} else {
				unicodes.add(varnaUnicode);
				start_of_syllable = true;
			}
		}
		if (addEndVirama && StringUtils.equalsIgnoreCase("Consonant", lastVarna)
				&& StringUtils.isNotBlank(viramaUnicode))
			unicodes.add(viramaUnicode);
		return unicodes;
	}

	/**
	 * Returns list of varnas in the specified language for a given english word
	 * (in arpabet notation)
	 * 
	 * @param languageId
	 *            language of the varnas
	 * @param arpabets
	 *            arpabet notation of the english word
	 * @param varnas
	 *            list of varnas for the input english word
	 * @return true if varnas are found for all arpabets of the word, else false
	 */
	private boolean getVarnasForWord(String languageId, String arpabets, List<Node> varnas) {
		VarnaCache cache = VarnaCache.getInstance();
		String arpabetArr[] = arpabets.split("\\s");
		boolean found = true;
		for (String arpabet : arpabetArr) {
			// get iso symbol of the arpabet
			String isoSymbol = cache.getISOSymbol("en", arpabet);

			// get matching varnas for the iso symbol of the arpabet
			found = getVarnas(isoSymbol, languageId, cache, varnas);

			// get varnas for alternate ISO symbol of the arpabet if varnas are
			// not found for the main iso symbol
			if (!found) {
				// get iso symbol of the arpabet
				String altISOSymbol = cache.getAltISOSymbol("en", arpabet);

				// get matching varnas for the alternate iso symbol of the
				// arpabet
				found = getVarnas(altISOSymbol, languageId, cache, varnas);
			}
		}
		return found;
	}

	/**
	 * Get the list of varnas for a give isoSymbol. This method also accepts
	 * input in the form of 'iso1+iso2+iso3' and returns a list of varnas
	 * matching for all the iso symbols in the input.
	 * 
	 * @param isoSymbol
	 *            one or more iso symbols (separated by +)
	 * @param languageId
	 *            language of the varnas
	 * @param cache
	 *            the VarnaCache object
	 * @param varnas
	 *            list of varnas for the input iso symbol(s)
	 * @return true if varnas are found for all iso symbols, else false
	 */
	private boolean getVarnas(String isoSymbol, String languageId, VarnaCache cache, List<Node> varnas) {
		boolean found = true;
		if (StringUtils.isNotBlank(isoSymbol)) {
			if (isoSymbol.contains("+")) {
				String isoSymbols[] = isoSymbol.split("\\+");
				for (String iso : isoSymbols) {
					Node node = cache.getVarnaNode(languageId, iso);
					if (null != node)
						varnas.add(node);
					else {
						found = false;
						break;
					}
				}
			} else {
				Node node = cache.getVarnaNode(languageId, isoSymbol);
				if (null != node)
					varnas.add(node);
				else {
					found = false;
				}
			}
		} else {
			found = false;
		}
		return found;
	}

	/**
	 * Returns the phonetic spelling (transliterated form) of the given english
	 * word into the specified language.
	 * 
	 * @param languageId
	 *            language into which the text needs to be transliterated
	 * @param word
	 *            the word that needs to be trans
	 * @param addEndVirama
	 *            if virama needs to be added at the end of words that end with
	 *            a consonant
	 * @return the transliterated text of the given english word
	 */
	public String getPhoneticSpellingByLanguage(String languageId, String word, boolean addEndVirama) {
		String arpabets = WordCacheUtil.getArpabets(word);
		if (StringUtils.isNotBlank(arpabets)) {
			List<Node> varnas = new ArrayList<Node>();
			boolean found = getVarnasForWord(languageId, arpabets, varnas);
			if (found) {
				List<String> unicodes = getUnicodes(languageId, varnas, addEndVirama);
				return getTextFromUnicode(unicodes);
			}
		}
		// return empty string if arpabets for the given word are not found
		return "";
	}

	/**
	 * Returns the text from a list of unicodes
	 * 
	 * @param unicodes
	 * @return Text
	 */
	private String getTextFromUnicode(List<String> unicodes) {

		String text = "";
		for (String unicode : unicodes) {
			if (StringUtils.isNotEmpty(unicode)) {
				int hexVal = Integer.parseInt(unicode, 16);
				text += (char) hexVal;
			}
		}
		return text;
	}

	/**
	 * Returns a list of syllables for a given word
	 * 
	 * @param languageId
	 * @param word
	 *            lemma of the word
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> buildSyllables(String languageId, String word) {
		String syllables = "";

		String arpabets = WordCacheUtil.getArpabets(word);
		if (StringUtils.isEmpty(arpabets))
			return ListUtils.EMPTY_LIST;

		String arpabetArr[] = arpabets.split("\\s");

		for (String arpabet : arpabetArr) {
			Property arpabetProp = new Property(GraphDACParams.identifier.name(), arpabet);
			Node varnaNode = getVarnaNodeByProperty(languageId, arpabetProp);
			Map<String, Object> metaData = varnaNode.getMetadata();
			String iso = (String) metaData.get(GraphDACParams.isoSymbol.name());
			String type = (String) metaData.get(GraphDACParams.type.name());
			syllables += iso;
			if (type.equalsIgnoreCase("Vowel")) {
				syllables += ",";
			}
			syllables += " ";
		}

		if (syllables.endsWith(", "))
			syllables = syllables.substring(0, syllables.length() - 2);
		else
			syllables = syllables.substring(0, syllables.length() - 1);

		return syllables.length() > 0 ? Arrays.asList(syllables.split(", ")) : ListUtils.EMPTY_LIST;
	}

	/**
	 * Gets node by given property
	 * 
	 * @param languageId
	 * @param property
	 *            Property filter
	 * @return Node object
	 */
	@SuppressWarnings("unchecked")
	private Node getVarnaNodeByProperty(String languageId, Property property) {
		Node node = null;
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodesByProperty");
		request.put(GraphDACParams.metadata.name(), property);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		if (!checkError(findRes)) {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0)
				node = nodes.get(0);
		}
		return node;
	}

	/**
	 * Gets the word complexity of the given word
	 * 
	 * @param lemma
	 *            lemma of the word
	 * @param languageId
	 * @return Word complexity value
	 * @throws Exception
	 */
	public Double getWordComplexity(String lemma, String languageId) throws Exception {
		Node word = searchWordForComplexity(languageId, lemma);
		if (word == null)
			throw new ResourceNotFoundException(LanguageErrorCodes.ERR_WORDS_NOT_FOUND.name(),
					"Word not found: " + lemma);
		return getWordComplexity(word, languageId);
	}

	/**
	 * Gets the word complexity for a given list of lemmas
	 * 
	 * @param lemmas
	 *            List of lemmas
	 * @param languageId
	 * @return Map of lemma to word complexity
	 */
	public Map<String, Double> getWordComplexity(List<String> lemmas, String languageId) {
		Map<String, Node> nodeMap = searchWordsForComplexity(languageId, lemmas);
		Map<String, Double> map = new HashMap<String, Double>();
		if (null != lemmas && !lemmas.isEmpty()) {
			for (String lemma : lemmas) {
				Node node = nodeMap.get(lemma);
				if (null != node) {
					try {
						Double complexity = getWordComplexity(node, languageId);
						map.put(lemma, complexity);
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						map.put(lemma, null);
					}
				} else {
					map.put(lemma, null);
				}
			}
		}
		return map;
	}

	/**
	 * Computes the word complexity of the given word node using the word
	 * complexity definition
	 * 
	 * @param word
	 *            Node object of the word
	 * @param languageId
	 *            Graph Id
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Double getWordComplexity(Node word, String languageId) throws Exception {
		Map<String, Object> wordMap = convertGraphNode(word, languageId, null);
		String languageGraphName = "language";
		DefinitionDTO wordComplexityDefinition = DefinitionDTOCache
				.getDefinitionDTO(LanguageObjectTypes.WordComplexity.name(), languageGraphName);
		if (wordComplexityDefinition == null)
			throw new ResourceNotFoundException(LanguageErrorCodes.ERR_DEFINITION_NOT_FOUND.name(),
					"Definition not found for " + LanguageObjectTypes.WordComplexity.name());
		List<MetadataDefinition> properties = wordComplexityDefinition.getProperties();
		Double complexity = 0.0;
		for (MetadataDefinition property : properties) {
			nextProperty: {
			String renderingHintsString = property.getRenderingHints();
			renderingHintsString = renderingHintsString.replaceAll("'", "\"");
			Double defaultValue = Double.valueOf((String) property.getDefaultValue());
			Map<String, Object> renderingHintsMap = mapper.readValue(renderingHintsString,
					new TypeReference<Map<String, Object>>() {
			});
			String field = (String) renderingHintsMap.get("metadata");
			String dataType = (String) renderingHintsMap.get("datatype");
			if (StringUtils.isBlank(dataType))
				dataType = "String";
			Map<String, Object> valueMap = (Map<String, Object>) renderingHintsMap.get("value");
			Object wordField = wordMap.get(field);
			List fieldValues = new ArrayList<>();
			if (wordField == null) {
				fieldValues.add(null);
			} else {
				fieldValues = getList(mapper, wordField, null);
			}
			for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
				String operation = entry.getKey();
				List compareValues = getList(mapper, entry.getValue(), null);
				for (Object fieldValue : fieldValues) {
					for (Object compareValue : compareValues) {
						switch (dataType) {
						case "String": {
							if (compareValue != null && fieldValue != null && !operation.equalsIgnoreCase("null")) {
								try {
									String compareString = (String) compareValue;
									String fieldString = (String) fieldValue;
									switch (operation) {
									case "in":
									case "eq": {
										if (compareString.equalsIgnoreCase(fieldString)) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									case "not in":
										if (!compareValues.containsAll(fieldValues)) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									case "ne": {
										if (!compareString.equalsIgnoreCase(fieldString)) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									}
								} catch (Exception e) {
									throw new Exception("Invalid operation or operands");
								}
							}
							if (compareValue != null) {
								try {
									switch (operation) {
									case "null": {
										boolean nullBool = Boolean.valueOf((String) compareValue);
										if (fieldValue == null && nullBool) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									}
								} catch (Exception e) {
									throw new Exception("Invalid operation or operands");
								}
							}
							break;
						}
						case "Number": {
							if (compareValue != null && fieldValue != null && !operation.equalsIgnoreCase("null")) {
								try {
									Double compareNumber = Double.valueOf((String) compareValue);
									Double fieldNumber = Double.valueOf((String) fieldValue);
									switch (operation) {
									case "eq": {
										if (compareNumber == fieldNumber) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									case "ne": {
										if (compareNumber != fieldNumber) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									case "ge": {
										if (fieldNumber >= compareNumber) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									case "gt": {
										if (fieldNumber > compareNumber) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									case "le": {
										if (fieldNumber <= compareNumber) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									case "lt": {
										if (fieldNumber < compareNumber) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									}
								} catch (Exception e) {
									throw new Exception("Invalid operation or operands");
								}
							}
							if (compareValue != null) {
								try {
									switch (operation) {
									case "null": {
										boolean nullBool = Boolean.valueOf((String) compareValue);
										if (fieldValue == null && nullBool) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									}
								} catch (Exception e) {
									throw new Exception("Invalid operation or operands");
								}
							}
							break;
						}
						case "Boolean": {
							if (compareValue != null && fieldValue != null) {
								try {
									boolean compareBoolean = Boolean.valueOf((String) compareValue);
									boolean fieldBoolean = Boolean.valueOf((String) fieldValue);
									switch (operation) {
									case "eq": {
										if (compareBoolean == fieldBoolean) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									case "ne": {
										if (compareBoolean != fieldBoolean) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									}
								} catch (Exception e) {
									throw new Exception("Invalid operation or operands");
								}
							}
							if (compareValue != null) {
								try {
									switch (operation) {
									case "null": {
										boolean nullBool = Boolean.valueOf((String) compareValue);
										if (fieldValue == null && nullBool) {
											complexity = complexity + defaultValue;
											break nextProperty;
										}
										break;
									}
									}
								} catch (Exception e) {
									throw new Exception("Invalid operation or operands");
								}
							}
							break;
						}
						}
					}
				}
			}
		}
		}

		BigDecimal bd = new BigDecimal(complexity);
		bd = bd.setScale(2, RoundingMode.HALF_UP);

		word.getMetadata().put(LanguageParams.word_complexity.name(), bd.doubleValue());
		// remove temporary "morphology" metadata
		word.getMetadata().remove(LanguageParams.morphology.name());
		updateWord(word, languageId, word.getIdentifier());
		return bd.doubleValue();
	}


	/**
	 * Creates proxy node and translation set if not present for the synset
	 * @param primaryMeaningId
	 * @param graphId
	 * @param indowordId
	 * @param englishTranslationId
	 */
	private void createProxyNodeAndTranslationSet(String primaryMeaningId, String graphId, int indowordId, int englishTranslationId)
	{
		String operation = "createProxyNodeAndTranslation";
		Request proxyReq = getRequest(graphId, GraphEngineManagers.NODE_MANAGER, operation);
		boolean proxy = proxyNodeExists(graphId,primaryMeaningId);
		Node node = new Node(primaryMeaningId,SystemNodeTypes.PROXY_NODE.name(),LanguageParams.Synset.name());
		node.setGraphId(graphId);
		node.setMetadata(new HashMap<>());
		proxyReq.put(GraphDACParams.node.name(), node);
		proxyReq = createTranslationCollection(proxyReq, graphId, indowordId, primaryMeaningId);
		String id = "";
		if(englishTranslationId!=0)
		{
			id = ""+englishTranslationId;
		}else{
			id = ""+indowordId;
		}
		LOGGER.info("Primary meaning id and indowordnetid:"+primaryMeaningId+":"+id);
		boolean create = false;
		String nodeId = getTranslationSetWithMember(primaryMeaningId,id,graphId);
		if(nodeId==null)
		{
			nodeId = getTranslationSet(id, graphId);
			if(nodeId==null)
			{
				create = true;
			}
			if(!create)
			{
				LOGGER.info("Translation set already exists for indowordnetid:"+nodeId);
				proxyReq.put(GraphDACParams.collection_id.name(), nodeId);
			}
			proxyReq.put("create", create);
			proxyReq.put("proxy", proxy);
			Response res = getResponse(proxyReq, LOGGER);
			if (!checkError(res)) {
				String proxyId = (String) res.get("node_id");
				LOGGER.info("Proxy node created:"+proxyId);
			}
		}else
		{
			LOGGER.info("Translation set already exists with this member:"+nodeId);
		}
	}



	/**
	 * Generates request object for creating translation set
	 * @param proxyReq
	 * @param indowordId
	 * @param synsetId
	 * @return
	 */
	private Request createTranslationCollection(Request proxyReq, String graphId, int indowordId, String synsetId)
	{
		Node translationSet = new Node();
		translationSet.setGraphId(graphId);
		String id = ""+indowordId;
		translationSet.setObjectType(LanguageObjectTypes.TranslationSet.name());
		Map<String,Object> metadata = new HashMap<String,Object>();
		metadata.put("indowordnetId", id);
		translationSet.setMetadata(metadata);
		List<String> members = null;
		members = Arrays.asList(synsetId);
		proxyReq.put(GraphDACParams.members.name(), members);
		proxyReq.put(GraphDACParams.translationSet.name(), translationSet);
		proxyReq.put(GraphDACParams.object_type.name(), LanguageObjectTypes.TranslationSet.name());
		proxyReq.put(GraphDACParams.member_type.name(), LanguageObjectTypes.Synset.name());
		return proxyReq;
	}


	/**
	 * Fetches translation set from graph if it already exists for the indowordnet id
	 * @param wordnetId
	 * @param graphId
	 * @return
	 */
	public String getTranslationSet(String wordnetId, String graphId){
		LOGGER.info("Logging wordnet id for getting translation set:"+wordnetId);
		Node node = null;
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.SET.name());
		sc.setObjectType(LanguageObjectTypes.TranslationSet.name());
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(new Filter("indowordnetId", SearchConditions.OP_EQUAL, wordnetId));
		MetadataCriterion mc = MetadataCriterion.create(filters);
		sc.addMetadata(mc);
		sc.setResultSize(1);
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		if (checkError(findRes))
			return null;
		else {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0){
				node = nodes.get(0);
				return node.getIdentifier();
			}
			return null;
		}
	}



	/**
	 * Fetches translation set if it exists with the same synset member
	 * @param id
	 * @param wordnetId
	 * @param graphId
	 * @return
	 */
	public String getTranslationSetWithMember(String id, String wordnetId, String graphId){
		LOGGER.info("Logging synsetid and wordnetid for getting with member:"+id+":"+wordnetId);
		Node node = null;
		RelationCriterion rc = new RelationCriterion("hasMember","Synset");
		List<String> identifiers = new ArrayList<String>();
		identifiers.add(id);
		rc.setIdentifiers(identifiers);
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.SET.name());
		sc.setObjectType(LanguageObjectTypes.TranslationSet.name());
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(new Filter("indowordnetId", SearchConditions.OP_EQUAL, wordnetId));
		MetadataCriterion mc = MetadataCriterion.create(filters);
		sc.addMetadata(mc);
		sc.addRelationCriterion(rc);
		sc.setResultSize(1);
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		if (checkError(findRes))
			return null;
		else {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0){
				node = nodes.get(0);
				return node.getIdentifier();
			}
			return null;
		}
	}



	/**
	 * Checks if a proxy node exists for the synset in translations graph
	 * @param graphId
	 * @param proxyId
	 * @return
	 */
	private boolean proxyNodeExists(String graphId, String proxyId)
	{
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getProxyNode");
		request.put(GraphDACParams.node_id.name(), proxyId);

		Response findRes = getResponse(request, LOGGER);
		if (checkError(findRes))
			return false;
		else {
			Node node = (Node) findRes.get(GraphDACParams.node.name());
			if (null != node)
				return true;
		}
		return true;
	}
}
