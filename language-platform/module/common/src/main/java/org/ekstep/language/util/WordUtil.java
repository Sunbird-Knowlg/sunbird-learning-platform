package org.ekstep.language.util;

import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.model.CitationBean;
import org.ekstep.language.model.WordIndexBean;
import org.ekstep.language.model.WordInfoBean;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
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
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.dac.model.TagCriterion;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.RelationDefinition;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

@Component
public class WordUtil extends BaseManager {

	private ObjectMapper mapper = new ObjectMapper();
	private static Logger LOGGER = LogManager.getLogger(WordUtil.class.getName());
    private static final String LEMMA_PROPERTY = "lemma";

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

	@SuppressWarnings("rawtypes")
	public List getList(ObjectMapper mapper, Object object, String propName) {
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

	@SuppressWarnings("unchecked")
	public String getWordIdentifierFromIndex(String languageId, String word) throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil();
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, getList(mapper, word, null));
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class, searchCriteria, indexName,
				Constants.WORD_INDEX_TYPE);
		Map<String, Object> wordIdsMap = new HashMap<String, Object>();
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("wordId", wordIndex.getWordIdentifier());
			wordIdsMap.put(wordIndex.getWord(), wordMap);
		}
		if (wordIdsMap.get(word) != null) {
			return (String) ((Map<String, Object>) wordIdsMap.get(word)).get("wordId");
		}
		return null;
	}

	public String getFormattedDateTime(long dateTime) {
		String dateTimeString = "";
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(dateTime);
			dateTimeString = formatter.format(cal.getTime());
		} catch (Exception e) {
			dateTimeString = "";
		}
		return dateTimeString;
	}

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
			for (int i = 0; i < fields.size(); i++)
				arr[i] = fields.get(i);
			return arr;
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
		List<NodeDTO> tools = new ArrayList<NodeDTO>();
        List<NodeDTO> workers = new ArrayList<NodeDTO>();
        List<NodeDTO> actions = new ArrayList<NodeDTO>();
        List<NodeDTO> objects = new ArrayList<NodeDTO>();
        List<NodeDTO> converse = new ArrayList<NodeDTO>();
		getInRelationsData(node, synonyms, antonyms, hypernyms, hyponyms, homonyms, meronyms, tools, workers, actions, objects, converse);
		getOutRelationsData(node, synonyms, antonyms, hypernyms, hyponyms, homonyms, meronyms, tools, workers, actions, objects, converse);
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
			List<NodeDTO> hypernyms, List<NodeDTO> hyponyms, List<NodeDTO> homonyms, List<NodeDTO> meronyms, List<NodeDTO> tools, List<NodeDTO> workers, List<NodeDTO> actions, List<NodeDTO> objects, List<NodeDTO> converse) {
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
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.TOOL.relationName(),
                        inRel.getRelationType())) {
                    tools.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.WORKER.relationName(),
                        inRel.getRelationType())) {
                    workers.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.ACTION.relationName(),
                        inRel.getRelationType())) {
                    actions.add(new NodeDTO(inRel.getStartNodeId(), inRel.getStartNodeName(),
                            inRel.getStartNodeObjectType(), inRel.getRelationType()));
                } else if (StringUtils.equalsIgnoreCase(RelationTypes.OBJECT.relationName(),
                        inRel.getRelationType())) {
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

	private void getOutRelationsData(Node node, List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
			List<NodeDTO> hypernyms, List<NodeDTO> hyponyms, List<NodeDTO> homonyms, List<NodeDTO> meronyms, List<NodeDTO> tools, List<NodeDTO> workers, List<NodeDTO> actions, List<NodeDTO> objects, List<NodeDTO> converse) {
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
				} else if (StringUtils.equalsIgnoreCase(RelationTypes.TOOL.relationName(),
                        outRel.getRelationType())) {
                    tools.add(new NodeDTO(outRel.getEndNodeId(), outRel.getEndNodeName(),
                            outRel.getEndNodeObjectType(), outRel.getRelationType()));
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Response create(String languageId, String objectType, Request request)
			throws JsonGenerationException, JsonMappingException, IOException {
		if (StringUtils.isBlank(languageId) || !LanguageMap.containsLanguage(languageId))
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node convertToGraphNode(Map<String, Object> map, DefinitionDTO definition)
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

	public ArrayList<String> validateCitationsList(List<CitationBean> citationBeanList) {
		ArrayList<String> errorMessageList = new ArrayList<String>();
		for (CitationBean citation : citationBeanList) {
			if (citation.getWord() == null || citation.getWord().isEmpty()) {
				errorMessageList.add("Word cannot be null");
			}
		}
		return errorMessageList;
	}
	
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
            Response searchRes = getResponse(request, LOGGER);
            if (!checkError(searchRes)) {
                List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
                if (null != nodes && nodes.size() > 0)
                    node = nodes.get(0);
            }
        }
        return node;
    }
	
	public Node getDataNode(String languageId, String nodeId) {
        Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode");        
        request.put(GraphDACParams.node_id.name(), nodeId);
    
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

	
	@SuppressWarnings("unchecked")
	public String getErrorMessage(Response response) {
		String errorMessage = "";
        ResponseParams params = response.getParams();
        if (null != params) {
        	errorMessage =  errorMessage + ": " + params.getErrmsg();
        }
        List<String> messages = (List<String>) response.get("messages");
        if(messages != null){
        	for(String message: messages){
        		errorMessage = errorMessage + ": " + message;
        	}
        }
        if(!errorMessage.isEmpty()){
        	return errorMessage.substring(2);
        }
        return null;
    }

	private List<String> processRelationWords(List<Map<String, Object>> synsetRelations, String languageId,
			StringBuffer errorMessages, DefinitionDTO wordDefintion) {
		List<String> wordIds = new ArrayList<String>();
		if (synsetRelations != null) {
			for (Map<String, Object> word : synsetRelations) {
				String wordId = createOrUpdateWordsWithoutPrimaryMeaning(word, languageId, errorMessages,
						wordDefintion);
				if (wordId != null) {
					wordIds.add(wordId);
				}
			}
		}
		return wordIds;
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
	
	private String createOrUpdateWordsWithoutPrimaryMeaning(Map<String, Object> word, String languageId,
			StringBuffer errorMessages, DefinitionDTO definition) {
		String lemma = (String) word.get(LanguageParams.lemma.name());
		if (lemma == null) {
			errorMessages.append("Lemma is mandatory");
			return null;
		} else {
			String language = LanguageMap.getLanguage(languageId).toUpperCase();
			boolean isValid = isValidWord(lemma, language);
			if (!isValid) {
				errorMessages.append("Lemma cannot be in a different language than " + language);
				return null;
			}
			String identifier = (String) word.get(LanguageParams.identifier.name());
			if(identifier == null){
				Node existingWordNode = searchWord(languageId, lemma);
				if(existingWordNode != null){
					identifier = existingWordNode.getIdentifier();
					word.put(LanguageParams.identifier.name(), identifier);
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
				errorMessages.append(getErrorMessage(wordResponse));
				return null;
			}
			String nodeId = (String) wordResponse.get(GraphDACParams.node_id.name());
			return nodeId;
		}
	}
	
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

	
	private Response createSynset(String languageId, Map<String, Object> synsetObj) throws Exception {
		String operation = "updateDataNode";
		String identifier = (String) synsetObj.get(LanguageParams.identifier.name());
		if (identifier == null || identifier.isEmpty()) {
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
	
	@SuppressWarnings("unchecked")
	public Response createOrUpdateWord(Map<String, Object> item, DefinitionDTO wordDefinition, String languageId,
			boolean createFlag, List<String> nodeIdList, Map<String, String> wordLemmaMap) {
		Response createRes = new Response();
		StringBuffer errorMessages = new StringBuffer();
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

			// create or update Primary meaning Synset
			List<String> synsetRelations = Arrays.asList(new String[]{LanguageParams.synonyms.name(), LanguageParams.hypernyms.name(), 
					LanguageParams.holonyms.name(), LanguageParams.antonyms.name(), LanguageParams.hyponyms.name(), LanguageParams.meronyms.name(), 
					LanguageParams.tools.name(), LanguageParams.workers.name(), LanguageParams.actions.name(), LanguageParams.objects.name()
					, LanguageParams.converse.name()});
			
			Map<String, List<String>> relationWordIdMap = new HashMap<String, List<String>>();
			
			for(String synsetRelationName: synsetRelations){
				List<Map<String, Object>> relations = (List<Map<String, Object>>) primaryMeaning
						.get(synsetRelationName);
				List<String> relationWordIds = processRelationWords(relations, languageId, errorMessages, wordDefinition);
				relationWordIdMap.put(synsetRelationName, relationWordIds);
				nodeIdList.addAll(relationWordIds);
				primaryMeaning.remove(synsetRelationName);
			}
			
			
			Response synsetResponse = createSynset(languageId, primaryMeaning);
			if (checkError(synsetResponse)) {
				return synsetResponse;
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
			for(String synsetRelationName: synsetRelations){
				List<String> wordIds = relationWordIdMap.get(synsetRelationName);
				String relationName = relationNameMap.get(synsetRelationName);
				addSynsetRelation(wordIds, relationName, languageId, primaryMeaningId,
						errorMessages);
			}		
			
			
			// update wordMap with primary synset data
			item.put(LanguageParams.primaryMeaningId.name(), primaryMeaningId);
			
			//get Synset data
			Node synsetNode = getDataNode(languageId, primaryMeaningId);
			if(synsetNode != null){
				Map<String, Object> synsetMetadata = synsetNode.getMetadata();
				String category = (String) synsetMetadata.get(LanguageParams.category.name());
				if (category != null) {
					item.put(LanguageParams.category.name(), category);
				}
				
				List<String> tags = synsetNode.getTags();
				if (tags != null) {
					List<String> wordTags = (List<String>) item.get(LanguageParams.tags.name());
					if(wordTags == null){
						wordTags =  new ArrayList<String>();
					}
					wordTags.addAll(tags);
					item.put(LanguageParams.tags.name(), wordTags);
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
			Node node = convertToGraphNode(languageId, LanguageParams.Word.name(), item, wordDefinition);
			node.setObjectType(LanguageParams.Word.name());
			String wordIdentifier = (String) item.get(LanguageParams.identifier.name());
			String lemma = (String) item.get(LanguageParams.lemma.name());
			if(wordIdentifier == null && createFlag){
				wordIdentifier = wordLemmaMap.get(lemma);
				if(wordIdentifier == null){
					Node existingWordNode = searchWord(languageId, lemma);
					if(existingWordNode != null){
						wordIdentifier = existingWordNode.getIdentifier();
						item.put(LanguageParams.identifier.name(), wordIdentifier);
						wordLemmaMap.put(lemma, wordIdentifier);
						createFlag = false;
					}
				}
			}
			if (createFlag) {
				createRes = createWord(node, languageId);
			} else {
				createRes = updateWord(node, languageId, wordIdentifier);
			}
			if (!checkError(createRes)) {
				String wordId = (String) createRes.get("node_id");
				wordLemmaMap.put(lemma, wordId);
				// add Primary Synonym Relation
				addSynonymRelation(languageId, wordId, primaryMeaningId);
				// add other meaning Synonym Relation
				for (String otherMeaningId : otherMeaningIds) {
					addSynonymRelation(languageId, wordId, otherMeaningId);
				}
			} else {
				errorMessages.append(getErrorMessage(createRes));
			}
		} catch (Exception e) {
			errorMessages.append(e.getMessage());
		}
		if(!errorMessages.toString().isEmpty()){
			createRes.put(LanguageParams.error_messages.name(), errorMessages.toString());
		}
		return createRes;
	}
	
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
	
	private Node getWord(String wordId, String languageId, StringBuffer errorMessages) {
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), wordId);
		request.put(GraphDACParams.get_tags.name(), true);
		Response getNodeRes = getResponse(request, LOGGER);
		if (checkError(getNodeRes)) {
			errorMessages.append(getNodeRes.getParams().getErrmsg());
		}
		Node node = (Node) getNodeRes.get(GraphDACParams.node.name());
		return node;
	}

	private void addSynsetRelation(List<String> wordIds, String relationType, String languageId, String synsetId,
			StringBuffer errorMessages) {
		for (String wordId : wordIds) {
			if (relationType.equalsIgnoreCase(RelationTypes.SYNONYM.relationName())) {
				Node wordNode = getWord(wordId, languageId, errorMessages);
				Map<String, Object> metadata = wordNode.getMetadata();
				if (metadata != null) {
					String primaryMeaningId = (String) metadata.get("primaryMeaningId");
					if (primaryMeaningId != null && !primaryMeaningId.equalsIgnoreCase(synsetId)) {
						errorMessages.append("Word :" + wordId + " has an existing different primary meaning");
						continue;
					} else if (primaryMeaningId == null) {
						metadata.put(LanguageParams.primaryMeaning.name(), synsetId);
						wordNode.setMetadata(metadata);
						wordNode.setObjectType(LanguageParams.Word.name());
						updateWord(wordNode, languageId, wordId);
					}
				}
			}
			addSynsetRelation(wordId, relationType, languageId, synsetId, errorMessages);
		}
	}

	private void addSynsetRelation(String wordId, String relationType, String languageId, String synsetId,
			StringBuffer errorMessages) {
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createRelation");
		request.put(GraphDACParams.start_node_id.name(), synsetId);
		request.put(GraphDACParams.relation_type.name(), relationType);
		request.put(GraphDACParams.end_node_id.name(), wordId);
		Response response = getResponse(request, LOGGER);
		if (checkError(response)) {
			errorMessages.append(response.getParams().getErrmsg());
		}
	}
	
}
