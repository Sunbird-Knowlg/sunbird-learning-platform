/**
 * 
 */
package managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.Slug;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.fasterxml.jackson.core.JsonProcessingException;

import common.Constants;
import common.DetectLanguage;
import common.VocabularyTermParam;
import play.api.mvc.Codec;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Result;

/**
 * @author pradyumna
 *
 */
public class VocabularyTermManager extends BasePlaySearchManager {

	private static final String SETTING = "{\"analysis\":{\"analyzer\":{\"vt_index_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"lowercase\",\"mynGram\"]},\"vt_search_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"standard\",\"lowercase\"]},\"keylower\":{\"tokenizer\":\"keyword\",\"filter\":\"lowercase\"}},\"filter\":{\"mynGram\":{\"type\":\"edge_ngram\",\"min_gram\":1,\"max_gram\":20,\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"]}}}}";
	private static final String MAPPING = "{\"dynamic_templates\":[{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"vt_index_analyzer\",\"search_analyzer\":\"vt_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"string\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"text\",\"analyzer\":\"vt_index_analyzer\",\"search_analyzer\":\"vt_search_analyzer\"}}}";
	private SearchProcessor processor = null;

	private DetectLanguage detectlanguage = null;

	private static final int DEFAULT_LIMIT = 50;

	/**
	 * @throws IOException
	 * 
	 */
	public VocabularyTermManager() {
		processor = new SearchProcessor(Constants.VOCABULARY_TERM_INDEX);
		ElasticSearchUtil.initialiseESClient(Constants.VOCABULARY_TERM_INDEX,
				Platform.config.getString("search.es_conn_info"));
		detectlanguage = new DetectLanguage();
		createIndex();

	}

	private void createIndex() {
		try {
			ElasticSearchUtil.addIndex(Constants.VOCABULARY_TERM_INDEX, Constants.VOCABULARY_TERM_INDEX_TYPE, SETTING,
					MAPPING);
		} catch (IOException e) {
			TelemetryManager
					.error("VocabularyTermManager : Error while adding index to elasticsearch : " + e.getMessage(), e);
		}
	}

	public Promise<Result> create(Request request) {
		if (null == request) {
			return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), "Invalid Request", ResponseCode.CLIENT_ERROR);
		}
		List<Map<String, Object>> termRequest = getRequestData(request);
		if (termRequest.isEmpty()) {
			return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), "Please provide atleast one term object",
					ResponseCode.CLIENT_ERROR);
		}
		try {
			List<String> termIds = new ArrayList<String>();
			List<String> errorMessage = new ArrayList<String>();
			for (Map<String, Object> term : termRequest) {
				if (StringUtils.isBlank((String) term.get(VocabularyTermParam.lemma.name()))) {
					if (termRequest.size() > 1)
						return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), "lemma is mandatory",
								ResponseCode.PARTIAL_SUCCESS, VocabularyTermParam.identifiers.name(), termIds);
					else
						return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), "Atleast one term is mandatory",
								ResponseCode.CLIENT_ERROR);
				}
				String lemma = (String) term.get(VocabularyTermParam.lemma.name());
				String language = (String) term.get(VocabularyTermParam.language.name());
				language = validateLanguageId(lemma, language, errorMessage);

				if (!errorMessage.isEmpty()) {
					if (termRequest.size() > 1)
						return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), errorMessage.get(0),
								ResponseCode.PARTIAL_SUCCESS, VocabularyTermParam.identifiers.name(), termIds);
					else
						return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), errorMessage.get(0),
								ResponseCode.CLIENT_ERROR);
				}

				String identifier = Slug.makeSlug(language + "_" + lemma, true);
				term.put(VocabularyTermParam.id.name(), identifier);
				term.put(VocabularyTermParam.language.name(), language);
				addDoc(identifier, term);
				termIds.add(identifier);
			}
			Response response = OK(VocabularyTermParam.identifiers.name(), termIds);
			return successResponse(response);
		} catch (Exception e) {
			TelemetryManager.error("VocabularyTermManager : create() : Exception : " + e.getMessage(), e);
			return ERROR(VocabularyTermParam.ERR_INTERNAL_ERROR.name(), "Something went wrong while processing",
					ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}

	/**
	 * @param request
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Promise<Result> suggest(Request request) {
		if (null == request) {
			return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), "Invalid Request", ResponseCode.CLIENT_ERROR);
		}
		try {
			validatesearchText(request.get(VocabularyTermParam.text.name()));
			Map<String, Object> map = getSearchData(request);
			int limit = getLimit(request.get(VocabularyTermParam.limit.name()),
					VocabularyTermParam.ERR_INVALID_REQUEST.name());
			map.remove(VocabularyTermParam.limit.name());
			F.Promise<SearchResponse> searchResponsePromise = F.Promise.wrap(searchLemma(map, limit));
			return searchResponsePromise.map(new F.Function<SearchResponse, Result>() {
				@Override
				public Result apply(SearchResponse searchResult) throws Throwable {
					List<Map> terms = getResultData(searchResult);
					Response response = OK();
					response.put(VocabularyTermParam.count.name(), terms.size());
					response.put(VocabularyTermParam.terms.name(), terms);
					String result = mapper.writeValueAsString(response);
					return ok(result).as("application/json");
				}
			});

		} catch (Exception e) {
			TelemetryManager.error("VocabularyTermManager : suggest() : Exception : " + e.getMessage(), e);
			if (e instanceof ClientException) {
				return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), e.getMessage(), ResponseCode.CLIENT_ERROR);
			}
			return ERROR(VocabularyTermParam.ERR_INTERNAL_ERROR.name(), "Something went wrong while processing",
					ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}

	}

	/**
	 * @param object
	 */
	private void validatesearchText(Object searchText) {
		if ((searchText instanceof String && StringUtils.isBlank((String) searchText))
				|| ((searchText instanceof Map) && (null == ((Map) searchText) || ((Map) searchText).isEmpty()))) {
			throw new ClientException(VocabularyTermParam.ERR_INVALID_REQUEST.name(), "Invalid Request");
		}
	}

	/**
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private scala.concurrent.Future<SearchResponse> searchLemma(Map<String, Object> map, int limit) throws Exception {
		SearchDTO searchDto = new SearchDTO();
		searchDto.setFuzzySearch(false);
		searchDto.setProperties(setSearchProperties(map));
		searchDto.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		searchDto.setFields(getFields());
		searchDto.setLimit(limit);
		return processor.processSearchQueryWithSearchResult(searchDto, false,
				Constants.VOCABULARY_TERM_INDEX, false);

	}

	/**
	 * @param request
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private Map<String, Object> getSearchData(Request request) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(VocabularyTermParam.text.name(), request.get(VocabularyTermParam.text.name()));
		List categories = (List) request.get(VocabularyTermParam.categories.name());
		if (null != categories && !categories.isEmpty()) {
			map.put(VocabularyTermParam.categories.name(), categories);
		} else {
			map.put(VocabularyTermParam.categories.name(), "keywords");
		}
		if (null != request.get(VocabularyTermParam.language.name())) {
			if (request.get(VocabularyTermParam.language.name()) instanceof List
					&& ((List) request.get(VocabularyTermParam.language.name())).isEmpty()) {
				map.put(VocabularyTermParam.language.name(), "en");
			} else if (StringUtils.isBlank((String) request.get(VocabularyTermParam.language.name()))) {
				map.put(VocabularyTermParam.language.name(), "en");
			} else {
				map.put(VocabularyTermParam.language.name(), request.get(VocabularyTermParam.language.name()));
			}
		} else {
			map.put(VocabularyTermParam.language.name(), "en");
		}

		return map;
	}

	/**
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getRequestData(Request request) {
		List<Map<String, Object>> termRequest = new ArrayList<Map<String, Object>>();
		if (request.get(VocabularyTermParam.terms.name()) instanceof List) {
			termRequest = (List<Map<String, Object>>) request.get(VocabularyTermParam.terms.name());
		} else {
			Map<String, Object> reqMap = (Map<String, Object>) request.get(VocabularyTermParam.terms.name());
			termRequest.add(reqMap);
		}
		return termRequest;
	}

	/**
	 * @param term
	 * @throws Exception
	 */
	private void addDoc(String id, Map<String, Object> term) throws Exception {
		Map<String, Object> indexDocument = getIndexDocument(term);
		String jsonIndexDocument = mapper.writeValueAsString(indexDocument);
		addDocument(id, jsonIndexDocument);
	}

	/**
	 * @param id
	 * @param jsonIndexDocument
	 * @throws IOException
	 */
	private void addDocument(String id, String jsonIndexDocument) throws IOException {
		ElasticSearchUtil.addDocumentWithId(Constants.VOCABULARY_TERM_INDEX, Constants.VOCABULARY_TERM_INDEX_TYPE, id,
				jsonIndexDocument);
	}

	/**
	 * @param term
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getIndexDocument(Map<String, Object> term) {
		Map<String, Object> indexDocument = new HashMap<String, Object>();

		indexDocument.put(VocabularyTermParam.id.name(), (String) term.get(VocabularyTermParam.id.name()));
		indexDocument.put(VocabularyTermParam.lemma.name(), (String) term.get(VocabularyTermParam.lemma.name()));
		indexDocument.put(VocabularyTermParam.language.name(), (String) term.get(VocabularyTermParam.language.name()));
		indexDocument.put(VocabularyTermParam.categories.name(),
				(List<String>) term.get(VocabularyTermParam.categories.name()));
		return indexDocument;
	}

	private Promise<Result> ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
		try {
			Response response = new Response();
			response.setParams(getErrorStatus(errorCode, errorMessage));
			response.setResponseCode(responseCode);
			String result = mapper.writeValueAsString(response);
			if (responseCode.equals(ResponseCode.CLIENT_ERROR)) {
				return F.Promise.pure(badRequest(result).as("application/json"));
			} else {
				return F.Promise.pure(internalServerError(result).as("application/json"));
			}
		} catch (JsonProcessingException e) {
			TelemetryManager.error("VocabularyTermManager : ERROR() : Exception : " + e.getMessage(), e);
		}
		return null;
	}

	private Promise<Result> ERROR(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier,
			Object vo) {
		try {
			Response response = new Response();
			response.put(responseIdentifier, vo);
			response.setParams(getErrorStatus(errorCode, errorMessage));
			response.setResponseCode(code);
			String result = mapper.writeValueAsString(response);
			if (code.equals(ResponseCode.CLIENT_ERROR)) {
				return F.Promise.pure(badRequest(result).as("application/json"));
			} else if (code.equals(ResponseCode.PARTIAL_SUCCESS)) {
				return F.Promise
						.pure(new Status(play.core.j.JavaResults.PartialContent(), result, Codec.javaSupported("utf-8"))
								.as("application/json"));
			} else {
				return F.Promise.pure(internalServerError(result).as("application/json"));
			}
		} catch (JsonProcessingException e) {
			TelemetryManager.error("VocabularyTermManager : ERROR() : Exception : " + e.getMessage(), e);
		}
		return null;
	}

	private Response OK() {
		Response response = new Response();
		response.setParams(getSucessStatus());
		return response;
	}

	private Response OK(String responseIdentifier, Object vo) {
		Response response = new Response();
		response.put(responseIdentifier, vo);
		response.setParams(getSucessStatus());
		return response;
	}

	private ResponseParams getSucessStatus() {
		ResponseParams params = new ResponseParams();
		params.setErr("0");
		params.setStatus(StatusType.successful.name());
		params.setErrmsg("Operation successful");
		return params;
	}

	private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
		ResponseParams params = new ResponseParams();
		params.setErr(errorCode);
		params.setStatus(StatusType.failed.name());
		params.setErrmsg(errorMessage);
		return params;
	}

	private Promise<Result> successResponse(Response response) {
		String result;
		try {
			result = mapper.writeValueAsString(response);
			return F.Promise.pure(ok(result).as("application/json"));
		} catch (JsonProcessingException e) {
			TelemetryManager.error("VocabularyTermManager : successResponse() : Exception : " + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * @param searchResult
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List<Map> getResultData(SearchResponse searchResult) {
		SearchHits searchHits = searchResult.getHits();
		SearchHit[] results = searchHits.getHits();
		List<Map> terms = new ArrayList<Map>();
		for (SearchHit result : results) {
			Map<String, Object> term = new HashMap<String, Object>();
			term.put(VocabularyTermParam.score.name(), result.getScore());
			term.put(VocabularyTermParam.lemma.name(), result.getSourceAsMap().get(VocabularyTermParam.lemma.name()));
			terms.add(term);
		}
		return terms;
	}

	@SuppressWarnings("rawtypes")
	private List<Map> setSearchProperties(Map<String, Object> map) throws Exception {
		List<Map> properties = new ArrayList<Map>();

		properties.addAll(lemmaProperties(map));

		Map<String, Object> property = new HashMap<String, Object>();
		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		property.put("propertyName", VocabularyTermParam.categories.name());
		property.put("values", map.get(VocabularyTermParam.categories.name()));
		properties.add(property);

		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		property.put("propertyName", VocabularyTermParam.language.name());
		property.put("values", map.get(VocabularyTermParam.language.name()));
		properties.add(property);

		return properties;
	}

	/**
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Map> lemmaProperties(Map<String, Object> map) throws Exception {
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		if (!(map.get(VocabularyTermParam.text.name()) instanceof Map)) {
			property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_STARTS_WITH);
			property.put("propertyName", VocabularyTermParam.lemma.name());
			property.put("values", map.get(VocabularyTermParam.text.name()));
			properties.add(property);
		} else {
			Map<String, Object> lemmaRequest = (Map<String, Object>) map.get(VocabularyTermParam.text.name());
			for (String key : lemmaRequest.keySet()) {
				property = new HashMap<String, Object>();
				property.put("propertyName", VocabularyTermParam.lemma.name());
				property.put("values", lemmaRequest.get(key));
				switch (key) {
				case "startsWith": {
					property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_STARTS_WITH);
					properties.add(property);
					break;
				}
				case "endsWith": {
					property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_ENDS_WITH);
					properties.add(property);
					break;
				}
				case "equals": {
					property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
					properties.add(property);
					break;
				}
				case "contains": {
					property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_CONTAINS);
					properties.add(property);
					break;
				}
				case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL_OPERATOR:
				case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL_TEXT:
				case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL_TEXT_LOWERCASE:
				case CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL_TEXT_UPPERCASE: {
					property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_NOT_EQUAL);
					properties.add(property);
					break;
				}
				default: {
					throw new Exception("Unsupported operation");
				}
				}
			}
		}
		return properties;

	}

	/**
	 * @return
	 */
	private List<String> getFields() {
		List<String> fields = new ArrayList<String>();
		fields.add(VocabularyTermParam.lemma.name());
		fields.add(VocabularyTermParam.categories.name());
		fields.add(VocabularyTermParam.language.name());
		return fields;
	}

	/**
	 * @param lemma
	 * @return
	 */
	private String validateLanguageId(String lemma, String language, List<String> errorMessage) {
		if (StringUtils.isBlank(language)) {
			String id = detectlanguage.getlanguageCode(lemma);
			if (StringUtils.isNotBlank(id)) {
				return id;
			} else {
				errorMessage
						.add("lemma should be in one of these languages " + detectlanguage.getLanguageMap().keySet());
				return id;
			}
		} else {
			if (detectlanguage.isValidLangId(language)) {
				return language;
			} else {
				errorMessage.add("language should be one among " + detectlanguage.getLanguageMap().values());
				return language;
			}
		}
	}

	private int getLimit(Object limit, String errCode) {
		int limitValue = DEFAULT_LIMIT;
		try {
			if (null != limit)
				limitValue = (int) limit;
		} catch (Exception e) {
			throw new ClientException(errCode, "Please provide valid limit.");
		}
		return limitValue;
	}
}
