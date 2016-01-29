package org.ekstep.language.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.model.CitationBean;
import org.ekstep.language.model.WordIndexBean;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
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

public class WordUtil extends BaseManager {

	private ObjectMapper mapper = new ObjectMapper();
	private static Logger LOGGER = LogManager.getLogger(WordUtil.class
			.getName());

	@SuppressWarnings("unchecked")
	protected Request getRequest(Map<String, Object> requestMap) {
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
				try {
					RequestParams params = (RequestParams) mapper.convertValue(
							reqParams, RequestParams.class);
					request.setParams(params);
				} catch (Exception e) {
				}
			}
			Object requestObj = requestMap.get("request");
			if (null != requestObj) {
				try {
					String strRequest = mapper.writeValueAsString(requestObj);
					Map<String, Object> map = mapper.readValue(strRequest,
							Map.class);
					if (null != map && !map.isEmpty())
						request.setRequest(map);
				} catch (Exception e) {
				}
			}
		}
		return request;
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
	public String getWordIdentifierFromGraph(String languageId, String word) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, List> lemmaMap = new LinkedHashMap<String, List>();
		lemmaMap.put("lemma", getList(mapper, word, null));
		map.put("request", lemmaMap);
		Request request = getRequest(map);
		try {
			Response response = list(languageId,
					LanguageObjectTypes.Word.name(), request);
			LOGGER.info("Search | Response: " + response);
			List<Map<String, Object>> list = (List<Map<String, Object>>) response
					.get("words");
			if (list != null && !list.isEmpty()) {
				Map<String, Object> wordMap = list.get(0);
				return (String) wordMap.get("identifier");
			}
		} catch (Exception e) {
			LOGGER.error("Search | Exception: " + e.getMessage(), e);
			e.printStackTrace();
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
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class,
				searchCriteria, indexName, Constants.WORD_INDEX_TYPE);
		Map<String, Object> wordIdsMap = new HashMap<String, Object>();
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("wordId", wordIndex.getWordIdentifier());
			wordIdsMap.put(wordIndex.getWord(), wordMap);
		}
		if(wordIdsMap.get(word) != null){
			return (String) ((Map<String, Object>)wordIdsMap.get(word)).get("wordId");
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
	
	@SuppressWarnings("unchecked")
	public void addCitationsAndWordIndexToElasticSearch(
			List<CitationBean> citations, String language) {
		try {
			String citationIndexName = Constants.CITATION_INDEX_COMMON_NAME
					+ "_" + language;
			String wordIndexName = Constants.WORD_INDEX_COMMON_NAME + "_"
					+ language;
			
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<String> citiationIndexes = new ArrayList<String>();
			ArrayList<String> wordIndexes = new ArrayList<String>();
			ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
			
			createCitationIndex(citationIndexName,
					Constants.CITATION_INDEX_TYPE, elasticSearchUtil);
			createWordIndex(wordIndexName, Constants.WORD_INDEX_TYPE,
					elasticSearchUtil);
			
			for (CitationBean citation : citations) {
				if(citation.getDate() == null || citation.getDate().isEmpty()){
					citation.setDate(getFormattedDateTime(System
							.currentTimeMillis()));
				}
				
				String wordIdentifier = getWordIdentifierFromIndex(
						language, citation.getWord());
				String wordIndexJson;
				if (wordIdentifier != null) {
					if (citation.getRootWord() == null
							|| citation.getRootWord().isEmpty()) {
						String rootWord = getRootWordsFromIndex(
								citation.getWord(), language);
						citation.setRootWord(rootWord);
					}
				} else if (wordIdentifier == null) {
					if (citation.getRootWord() != null
							&& !citation.getRootWord().isEmpty()) {
						wordIdentifier = getWordIdentifierFromIndex(
								language, citation.getRootWord());
					}
					else{
						citation.setRootWord(citation.getWord());
					}
					if (wordIdentifier != null) {
						wordIndexJson = getWordIndex(
								citation.getWord(), citation.getRootWord(),
								wordIdentifier, mapper);
						wordIndexes.add(wordIndexJson);
					} else {
						Map<String, Object> wordMap = new HashMap<String, Object>();
						wordMap.put("lemma", citation.getRootWord());
						List<Map<String, Object>> wordList = new ArrayList<Map<String, Object>>();
						wordList.add(wordMap);
						Request request = new Request();
						request.put("words", wordList);
						Response response = create(language,
								LanguageObjectTypes.Word.name(), request);
						List<String> nodeIdList = (List<String>) response
								.get("node_id");
						if (nodeIdList != null && !nodeIdList.isEmpty()) {
							wordIdentifier = nodeIdList.get(0);
							wordIndexJson = null;
							if (wordIdentifier != null) {
								wordIndexJson = getWordIndex(
										citation.getRootWord(),
										citation.getRootWord(), wordIdentifier,
										mapper);
								wordIndexes.add(wordIndexJson);
								
								if(!citation.getWord().equalsIgnoreCase(citation.getRootWord())){
									wordIndexJson = getWordIndex(
											citation.getWord(),
											citation.getRootWord(), wordIdentifier,
											mapper);
									wordIndexes.add(wordIndexJson);
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
			elasticSearchUtil.bulkIndexWithAutoGenerateIndexId(
					citationIndexName, Constants.CITATION_INDEX_TYPE,
					citiationIndexes);
			elasticSearchUtil.bulkIndexWithAutoGenerateIndexId(wordIndexName,
					Constants.WORD_INDEX_TYPE, wordIndexes);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createWordIndex(String indexName, String indexType,
			ElasticSearchUtil elasticSearchUtil) {
		JSONBuilder settingBuilder = new JSONStringer();
		settingBuilder.object().key("settings").object().key("analysis")
				.object().key("filter").object().key("nfkc_normalizer")
				.object().key("type").value("icu_normalizer").key("name")
				.value("nfkc").endObject().endObject().key("analyzer").object()
				.key("ind_normalizer").object().key("tokenizer")
				.value("icu_tokenizer").key("filter").array()
				.value("nfkc_normalizer").endArray().endObject().endObject()
				.endObject().endObject().endObject();

		JSONBuilder mappingBuilder = new JSONStringer();
		mappingBuilder.object().key(indexType).object().key("properties")
				.object().key("word").object().key("type").value("string")
				.key("analyzer").value("ind_normalizer").key("fields").object()
				.key("hash").object().key("type").value("murmur3").endObject()
				.endObject().endObject().key("rootWord").object().key("type")
				.value("string").key("analyzer").value("ind_normalizer")
				.key("fields").object().key("hash").object().key("type")
				.value("murmur3").endObject().endObject().endObject()
				.key("date").object().key("type").value("date").key("format")
				.value("dd-MMM-yyyy HH:mm:ss").endObject().endObject()
				.endObject().endObject();

		elasticSearchUtil.addIndex(indexName, indexType,
				settingBuilder.toString(), mappingBuilder.toString());
	}

	public void createCitationIndex(String indexName, String indexType,
			ElasticSearchUtil elasticSearchUtil) {
		JSONBuilder settingBuilder = new JSONStringer();
		settingBuilder.object().key("settings").object().key("analysis")
				.object().key("filter").object().key("nfkc_normalizer")
				.object().key("type").value("icu_normalizer").key("name")
				.value("nfkc").endObject().endObject().key("analyzer").object()
				.key("ind_normalizer").object().key("tokenizer")
				.value("icu_tokenizer").key("filter").array()
				.value("nfkc_normalizer").endArray().endObject().endObject()
				.endObject().endObject().endObject();

		JSONBuilder mappingBuilder = new JSONStringer();
		mappingBuilder.object().key(indexType).object().key("properties")
				.object().key("word").object().key("type").value("string")
				.key("analyzer").value("ind_normalizer").key("fields").object()
				.key("hash").object().key("type").value("murmur3").endObject()
				.endObject().endObject().key("rootWord").object().key("type")
				.value("string").key("analyzer").value("ind_normalizer")
				.key("fields").object().key("hash").object().key("type")
				.value("murmur3").endObject().endObject().endObject()
				.key("date").object().key("type").value("date").key("format")
				.value("dd-MMM-yyyy HH:mm:ss").endObject().endObject()
				.endObject().endObject();

		elasticSearchUtil.addIndex(indexName, indexType,
				settingBuilder.toString(), mappingBuilder.toString());
	}
	
	
	@SuppressWarnings({ "unchecked" })
	public String getRootWordsFromIndex(String word, String languageId)
			throws IOException {
		ElasticSearchUtil util = new ElasticSearchUtil();
		String indexName = Constants.WORD_INDEX_COMMON_NAME + "_" + languageId;
		String textKeyWord = "word";
		Map<String, Object> searchCriteria = new HashMap<String, Object>();
		searchCriteria.put(textKeyWord, getList(mapper, word, null));
		List<Object> wordIndexes = util.textSearch(WordIndexBean.class,
				searchCriteria, indexName, Constants.WORD_INDEX_TYPE);
		Map<String, Object> rootWordsMap = new HashMap<String, Object>();
		for (Object wordIndexTemp : wordIndexes) {
			WordIndexBean wordIndex = (WordIndexBean) wordIndexTemp;
			Map<String, Object> wordMap = new HashMap<String, Object>();
			wordMap.put("rootWord", wordIndex.getRootWord());
			rootWordsMap.put(wordIndex.getWord(), wordMap);
		}
		
		if(rootWordsMap.get(word) != null){
			return (String) ((Map<String, Object>)rootWordsMap.get(word)).get("rootWord");
		}
		return null;
	}

	public String getWordIndex(String word, String rootWord,
			String wordIdentifier, ObjectMapper mapper)
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
		if (StringUtils.isBlank(languageId)
				|| !LanguageMap.containsLanguage(languageId))
			throw new ClientException(
					LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(),
					"Invalid Language Id");
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
				filters.add(new Filter(PARAM_STATUS, SearchConditions.OP_IN,
						statusList));
		}
		if (null != request.getRequest() && !request.getRequest().isEmpty()) {
			for (Entry<String, Object> entry : request.getRequest().entrySet()) {
				if (!StringUtils.equalsIgnoreCase(PARAM_FIELDS, entry.getKey())
						&& !StringUtils.equalsIgnoreCase(PARAM_LIMIT,
								entry.getKey())
						&& !StringUtils.equalsIgnoreCase(PARAM_STATUS,
								entry.getKey())
						&& !StringUtils.equalsIgnoreCase("word-lists",
								entry.getKey())
						&& !StringUtils.equalsIgnoreCase(PARAM_TAGS,
								entry.getKey())) {
					List<String> list = getList(mapper, entry.getValue(),
							entry.getKey());
					if (null != list && !list.isEmpty()) {
						filters.add(new Filter(entry.getKey(),
								SearchConditions.OP_IN, list));
					}
				} else if (StringUtils.equalsIgnoreCase(PARAM_TAGS,
						entry.getKey())) {
					List<String> tags = getList(mapper, entry.getValue(),
							entry.getKey());
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
		Request req = getRequest(languageId,
				GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		req.put(GraphDACParams.get_tags.name(), true);
		Response listRes = getResponse(req, LOGGER);
		if (checkError(listRes))
			return listRes;
		else {
			List<Node> nodes = (List<Node>) listRes
					.get(GraphDACParams.node_list.name());
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			if (null != nodes && !nodes.isEmpty()) {
				String[] fields = getFields(request);
				for (Node node : nodes) {
					Map<String, Object> map = convertGraphNode(node,
							languageId, fields);
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

	private Map<String, Object> convertGraphNode(Node node, String languageId,
			String[] fields) {
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
		getInRelationsData(node, synonyms, antonyms, hypernyms, hyponyms,
				homonyms, meronyms);
		getOutRelationsData(node, synonyms, antonyms, hypernyms, hyponyms,
				homonyms, meronyms);
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

	private void getInRelationsData(Node node,
			List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
			List<NodeDTO> hypernyms, List<NodeDTO> hyponyms,
			List<NodeDTO> homonyms, List<NodeDTO> meronyms) {
		if (null != node.getInRelations() && !node.getInRelations().isEmpty()) {
			for (Relation inRel : node.getInRelations()) {
				if (StringUtils.equalsIgnoreCase(
						RelationTypes.SYNONYM.relationName(),
						inRel.getRelationType())) {
					if (null != inRel.getStartNodeMetadata()
							&& !inRel.getStartNodeMetadata().isEmpty()) {
						inRel.getStartNodeMetadata().remove(
								SystemProperties.IL_FUNC_OBJECT_TYPE.name());
						inRel.getStartNodeMetadata().remove(
								SystemProperties.IL_SYS_NODE_TYPE.name());
						inRel.getStartNodeMetadata().remove(
								SystemProperties.IL_UNIQUE_ID.name());
						synonyms.add(inRel.getStartNodeMetadata());
					}
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.ANTONYM.relationName(),
						inRel.getRelationType())) {
					antonyms.add(new NodeDTO(inRel.getStartNodeId(), inRel
							.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel
									.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.HYPERNYM.relationName(),
						inRel.getRelationType())) {
					hypernyms.add(new NodeDTO(inRel.getStartNodeId(), inRel
							.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel
									.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.HYPONYM.relationName(),
						inRel.getRelationType())) {
					hyponyms.add(new NodeDTO(inRel.getStartNodeId(), inRel
							.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel
									.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.MERONYM.relationName(),
						inRel.getRelationType())) {
					meronyms.add(new NodeDTO(inRel.getStartNodeId(), inRel
							.getStartNodeName(),
							inRel.getStartNodeObjectType(), inRel
									.getRelationType()));
				}
			}
		}
	}

	private void getOutRelationsData(Node node,
			List<Map<String, Object>> synonyms, List<NodeDTO> antonyms,
			List<NodeDTO> hypernyms, List<NodeDTO> hyponyms,
			List<NodeDTO> homonyms, List<NodeDTO> meronyms) {
		if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			for (Relation outRel : node.getOutRelations()) {
				if (StringUtils.equalsIgnoreCase(
						RelationTypes.SYNONYM.relationName(),
						outRel.getRelationType())) {
					if (null != outRel.getEndNodeMetadata()
							&& !outRel.getEndNodeMetadata().isEmpty()) {
						outRel.getEndNodeMetadata().remove(
								SystemProperties.IL_FUNC_OBJECT_TYPE.name());
						outRel.getEndNodeMetadata().remove(
								SystemProperties.IL_SYS_NODE_TYPE.name());
						outRel.getEndNodeMetadata().remove(
								SystemProperties.IL_UNIQUE_ID.name());
						synonyms.add(outRel.getEndNodeMetadata());
					}
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.ANTONYM.relationName(),
						outRel.getRelationType())) {
					antonyms.add(new NodeDTO(outRel.getEndNodeId(), outRel
							.getEndNodeName(), outRel.getEndNodeObjectType(),
							outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.HYPERNYM.relationName(),
						outRel.getRelationType())) {
					hypernyms.add(new NodeDTO(outRel.getEndNodeId(), outRel
							.getEndNodeName(), outRel.getEndNodeObjectType(),
							outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.HYPONYM.relationName(),
						outRel.getRelationType())) {
					hyponyms.add(new NodeDTO(outRel.getEndNodeId(), outRel
							.getEndNodeName(), outRel.getEndNodeObjectType(),
							outRel.getRelationType()));
				} else if (StringUtils.equalsIgnoreCase(
						RelationTypes.MERONYM.relationName(),
						outRel.getRelationType())) {
					meronyms.add(new NodeDTO(outRel.getEndNodeId(), outRel
							.getEndNodeName(), outRel.getEndNodeObjectType(),
							outRel.getRelationType()));
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
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
	            	Map<String, Object> result = res.getResult();
	            	if(result != null){
	            		String nodeId = (String) result.get("node_id");
	            		if(nodeId != null){
	            			lstNodeId.add(nodeId);
	            		}
	            	}
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

	public ArrayList<String> validateCitationsList(List<CitationBean> citationBeanList) {
		ArrayList<String> errorMessageList = new ArrayList<String>();
		for(CitationBean citation: citationBeanList){
			if(citation.getWord() == null || citation.getWord().isEmpty()){
				errorMessageList.add("Word cannot be null");
			}
		}
		return errorMessageList;
	}
}
