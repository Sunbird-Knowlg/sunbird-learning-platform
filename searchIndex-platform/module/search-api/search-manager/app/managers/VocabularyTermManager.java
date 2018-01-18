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
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;

import com.fasterxml.jackson.core.JsonProcessingException;

import common.Constants;
import common.VocabularyTermParam;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Result;

/**
 * @author pradyumna
 *
 */
public class VocabularyTermManager extends BasePlaySearchManager {

	private static final String SETTING = "{\"settings\":{\"index\":{\"index\":\"vocabularyterm\",\"type\":\"vt\",\"analysis\":{\"analyzer\":{\"vt_index_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"lowercase\",\"mynGram\"]},\"vt_search_analyzer\":{\"type\":\"custom\",\"tokenizer\":\"standard\",\"filter\":[\"standard\",\"lowercase\"]},\"keylower\":{\"tokenizer\":\"keyword\",\"filter\":\"lowercase\"}},\"filter\":{\"mynGram\":{\"type\":\"nGram\",\"min_gram\":1,\"max_gram\":20,\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"]}}}}}}";
	private static final String MAPPING = "{\"" + Constants.VOCABULARY_TERM_INDEX_TYPE
			+ "\":{\"dynamic_templates\":[{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"string\",\"copy_to\":\"all_fields\",\"analyzer\":\"vt_index_analyzer\",\"search_analyzer\":\"vt_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"string\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"string\",\"analyzer\":\"vt_index_analyzer\",\"search_analyzer\":\"vt_search_analyzer\"}}}}";
	private ElasticSearchUtil esUtil = null;
	/**
	 * @throws IOException
	 * 
	 */
	public VocabularyTermManager() {
		esUtil = new ElasticSearchUtil();
		createIndex();
	}

	private void createIndex() {
		try {
			esUtil.addIndex(Constants.VOCABULARY_TERM_INDEX, Constants.VOCABULARY_TERM_INDEX_TYPE, SETTING, MAPPING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Promise<Result> create(Request request) {
		if (null == request) {
			return ERROR("ERR_INVALID_REQUEST", "Invalid Request", ResponseCode.CLIENT_ERROR);
		}
		List<Map<String, Object>> termRequest = getRequestData(request);
		if (termRequest.isEmpty()) {
			return ERROR("ERR_INVALID_REQUEST", "Please Provide atleast One Term Object", ResponseCode.CLIENT_ERROR);
		}
		try {
			List<String> termIds = new ArrayList<String>();
			for (Map<String, Object> term : termRequest) {
				if (StringUtils.isBlank((String) term.get(VocabularyTermParam.lemma.name()))) {
					return ERROR("ERR_INVALID_REQUEST", "lemma is Mandatory", ResponseCode.PARTIAL_SUCCESS);
				}
				String identifier = (String) term.get("id");
				if (StringUtils.isBlank(identifier)) {
					identifier = generateId();
				}
				addDoc(identifier, term);
				termIds.add(identifier);
			}
			Response response = OK(VocabularyTermParam.identifiers.name(), termIds);
			return successResponse(response);
		} catch (Exception e) {
			return ERROR("ERR_INTERNAL_ERROR", "Somehing went wrong while Processing", ResponseCode.SERVER_ERROR,
					e.getMessage(), e);
		}
	}

	/**
	 * @param request
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Promise<Result> suggest(Request request) {
		if (null == request) {
			return ERROR("ERR_INVALID_REQUEST", "Invalid Request", ResponseCode.CLIENT_ERROR);
		}
		String searchText = (String) request.get("text");
		if (StringUtils.isBlank(searchText)) {
			return ERROR("ERR_INVALID_REQUEST", "Invalid Request", ResponseCode.CLIENT_ERROR);
		}
		String query = getSuggestQuery(searchText);
		System.out.println("Query :  " + query);
		try {
			SearchResult searchResult = esUtil.search(Constants.VOCABULARY_TERM_INDEX, query);
			List<Map> terms = getResultData(searchResult);
			Response response = OK("terms", terms);
			response.put("count", searchResult.getTotal());
			return successResponse(response);
		} catch (IOException e) {
			return ERROR("ERR_INTERNAL_ERROR", "Somehing went wrong while Processing", ResponseCode.SERVER_ERROR,
					e.getMessage(), e);
		}
	}


	/**
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getRequestData(Request request) {
		List<Map<String, Object>> termRequest = new ArrayList<Map<String, Object>>();
		if (request.get("terms") instanceof List) {
			termRequest = (List<Map<String, Object>>) request.get("terms");
		} else {
			Map<String, Object> reqMap = (Map<String, Object>) request.get("terms");
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

		indexDocument.put("id", (String) term.get("id"));
		indexDocument.put("lemma", (String) term.get("lemma"));
		indexDocument.put("language", (String) term.get("language"));
		indexDocument.put("categories", (List<String>) term.get("categories"));
		return indexDocument;
	}

	/**
	 * @return
	 */
	private String generateId() {

		return null;
	}

	protected Promise<Result> ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
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
			e.printStackTrace();
		}
		return null;
	}

	protected Promise<Result> ERROR(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier,
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
			e.printStackTrace();
		}
		return null;
	}

	protected Response OK() {
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

	protected ResponseParams getSucessStatus() {
		ResponseParams params = new ResponseParams();
		params.setErr("0");
		params.setStatus(StatusType.successful.name());
		params.setErrmsg("Operation successful");
		return params;
	}

	protected ResponseParams getErrorStatus(String errorCode, String errorMessage) {
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
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param searchText
	 * @return
	 */
	private String getSuggestQuery(String searchText) {
		JSONBuilder builder = new JSONStringer();

		builder.object();
		builder.key(VocabularyTermParam.query.name()).object();
		builder.key(VocabularyTermParam.match_phrase_prefix.name()).object();
		builder.key(VocabularyTermParam.lemma.name()).object();
		builder.key(VocabularyTermParam.query.name()).value(searchText);
		builder.key(VocabularyTermParam.slop.name()).value(10);
		builder.endObject();
		builder.endObject();
		builder.endObject();
		builder.key(VocabularyTermParam.sort.name()).object();
		builder.key(VocabularyTermParam._score.name()).object();
		builder.key(VocabularyTermParam.order.name()).value(VocabularyTermParam.asc.name()).endObject();
		builder.key(VocabularyTermParam.lemma.name()).object();
		builder.key(VocabularyTermParam.order.name()).value(VocabularyTermParam.asc.name()).endObject();
		builder.endObject();
		builder.endObject();
		return builder.toString();
	}

	/**
	 * @param searchResult
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Map> getResultData(SearchResult searchResult) {
		List<Map> terms = new ArrayList<Map>();
		for (Hit hit : searchResult.getHits(Map.class)) {
			Map<String, Object> term = new HashMap<String, Object>();
			Map<String, Object> doc = new HashMap<String, Object>();
			term.put(VocabularyTermParam.score.name(), hit.score);
			doc = (Map<String, Object>) hit.source;
			term.put(VocabularyTermParam.lemma.name(), doc.get(VocabularyTermParam.lemma.name()));

			terms.add(term);
		}
		return terms;
	}
}
