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
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.slugs.Slug;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.telemetry.logger.TelemetryManager;

import com.fasterxml.jackson.core.JsonProcessingException;

import common.Constants;
import common.DetectLanguage;
import common.VocabularyTermParam;
import io.searchbox.core.SearchResult;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Result;

/**
 * @author pradyumna
 *
 */
public class VocabularyTermManager extends BasePlaySearchManager {

	private static final String SETTING = "{\"settings\":{\"index\":{\"index\":\"" + Constants.VOCABULARY_TERM_INDEX
			+ "\",\"type\":\"" + Constants.VOCABULARY_TERM_INDEX_TYPE
			+ "\",\"analysis\":{\"analyzer\":{\"vt_index_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"lowercase\",\"mynGram\"]},\"vt_search_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"standard\",\"lowercase\"]},\"keylower\":{\"tokenizer\":\"keyword\",\"filter\":\"lowercase\"}},\"filter\":{\"mynGram\":{\"type\":\"edge_ngram\",\"min_gram\":1,\"max_gram\":20,\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"]}}}}}}";
	private static final String MAPPING = "{\"" + Constants.VOCABULARY_TERM_INDEX_TYPE
			+ "\":{\"dynamic_templates\":[{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"string\",\"copy_to\":\"all_fields\",\"analyzer\":\"vt_index_analyzer\",\"search_analyzer\":\"vt_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"string\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"string\",\"analyzer\":\"vt_index_analyzer\",\"search_analyzer\":\"vt_search_analyzer\"}}}}";
	private ElasticSearchUtil esUtil = null;
	private SearchProcessor processor = null;

	private DetectLanguage detectlanguage = null;

	private static final int DEFAULT_LIMIT = 50;

	/**
	 * @throws IOException
	 * 
	 */
	public VocabularyTermManager() {
		processor = new SearchProcessor();
		esUtil = new ElasticSearchUtil();
		detectlanguage = new DetectLanguage();
		createIndex();

	}

	private void createIndex() {
		try {
			esUtil.addIndex(Constants.VOCABULARY_TERM_INDEX, Constants.VOCABULARY_TERM_INDEX_TYPE, SETTING, MAPPING);
		} catch (IOException e) {
			TelemetryManager.error("");
		}
	}

	public Promise<Result> create(Request request) {
		if (null == request) {
			return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), "Invalid Request", ResponseCode.CLIENT_ERROR);
		}
		List<Map<String, Object>> termRequest = getRequestData(request);
		if (termRequest.isEmpty()) {
			return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), "Please Provide atleast One Term Object",
					ResponseCode.CLIENT_ERROR);
		}
		try {
			List<String> termIds = new ArrayList<String>();
			for (Map<String, Object> term : termRequest) {
				if (StringUtils.isBlank((String) term.get(VocabularyTermParam.lemma.name()))) {
					return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), "lemma, language is Mandatory",
							ResponseCode.PARTIAL_SUCCESS, VocabularyTermParam.identifiers.name(), termIds);
				}
				String language = null;
				if (StringUtils.isBlank((String) term.get(VocabularyTermParam.language.name()))) {
					language = detectlanguage.getlanguageCode((String) term.get(VocabularyTermParam.lemma.name()));
				} else {
					language = (String) term.get(VocabularyTermParam.language.name());
				}
				String id = language + "_" + (String) term.get(VocabularyTermParam.lemma.name());
				String identifier = Slug.makeSlug(id, true);
				addDoc(identifier, term);
				termIds.add(identifier);
			}
			Response response = OK(VocabularyTermParam.identifiers.name(), termIds);
			return successResponse(response);
		} catch (Exception e) {
			TelemetryManager.error("VocabularyTermManager : create() : Exception : " + e.getMessage(), e);
			return ERROR(VocabularyTermParam.ERR_INTERNAL_ERROR.name(), "Something went wrong while Processing",
					ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}
	}

	/**
	 * @param request
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Promise<Result> suggest(Request request, int limit) {
		if (null == request) {
			return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), "Invalid Request", ResponseCode.CLIENT_ERROR);
		}
		String searchText = (String) request.get(VocabularyTermParam.text.name());
		if (StringUtils.isBlank(searchText)) {
			return ERROR(VocabularyTermParam.ERR_INVALID_REQUEST.name(), "Invalid Request", ResponseCode.CLIENT_ERROR);
		}
		try {
			Map<String, Object> map = getSearchData(request);
			if (limit == 0) {
				limit = DEFAULT_LIMIT;
			}
			SearchResult searchResult = searchLemma(map, limit);
			List<Map> terms = getResultData(searchResult);
			Response response = OK();
			response.put(VocabularyTermParam.count.name(), terms.size());
			response.put(VocabularyTermParam.terms.name(), terms);
			return successResponse(response);
		} catch (Exception e) {
			e.printStackTrace();
			TelemetryManager.error("VocabularyTermManager : suggest() : Exception : " + e.getMessage(), e);
			return ERROR(VocabularyTermParam.ERR_INTERNAL_ERROR.name(), "Something went wrong while Processing",
					ResponseCode.SERVER_ERROR, e.getMessage(), null);
		}

	}

	/**
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private SearchResult searchLemma(Map<String, Object> map, int limit) throws Exception {
		SearchDTO searchDto = new SearchDTO();
		searchDto.setFuzzySearch(false);
		searchDto.setProperties(setSearchProperties(map));
		searchDto.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		searchDto.setFields(getFields());
		searchDto.setLimit(limit);
		return (SearchResult) processor.processSearchQueryWithSearchResult(searchDto, false,
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
		List language = (List) request.get(VocabularyTermParam.language.name());
		if (null != language && !language.isEmpty()) {
			map.put(VocabularyTermParam.language.name(), language);
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
		esUtil.addDocumentWithId(Constants.VOCABULARY_TERM_INDEX, Constants.VOCABULARY_TERM_INDEX_TYPE, id,
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Map> getResultData(SearchResult searchResult) {
		Map<String, Object> result_map = (Map) searchResult.getValue(VocabularyTermParam.hits.name());
		List<Map<String, Object>> results = (List) result_map.get(VocabularyTermParam.hits.name());
		List<Map> terms = new ArrayList<Map>();
		for (Map<String, Object> result : results) {
			Map<String, Object> term = new HashMap<String, Object>();
			Map<String, Object> doc = new HashMap<String, Object>();
			term.put(VocabularyTermParam.score.name(), result.get(VocabularyTermParam._score.name()));
			doc = (Map<String, Object>) result.get("_source");
			term.put(VocabularyTermParam.lemma.name(), doc.get(VocabularyTermParam.lemma.name()));
			terms.add(term);
		}

		return terms;
	}

	@SuppressWarnings("rawtypes")
	private List<Map> setSearchProperties(Map<String, Object> map) {
		List<Map> properties = new ArrayList<Map>();
		Map<String, Object> property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_CONTAINS);
		property.put("propertyName", VocabularyTermParam.lemma.name());
		property.put("values", map.get(VocabularyTermParam.text.name()));
		properties.add(property);

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
	 * @return
	 */
	private List<String> getFields() {
		List<String> fields = new ArrayList<String>();
		fields.add(VocabularyTermParam.lemma.name());
		fields.add(VocabularyTermParam.categories.name());
		fields.add(VocabularyTermParam.language.name());
		return fields;
	}
}
