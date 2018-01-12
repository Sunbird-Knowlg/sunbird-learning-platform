/**
 * 
 */
package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;

import managers.PlaySearchManager;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Result;

/**
 * @author pradyumna
 *
 */
public class VocabularyTermController extends SearchBaseController {

	private PlaySearchManager mgr = new PlaySearchManager();

	public Promise<Result> create() {
		String apiId = "vocabulary-term.create";
		Request request = getRequest(request().body(), apiId, request().uri());
		Response response = new Response();
		response.put("node_id", "en_addition");
		ResponseParams params = new ResponseParams();
		params.setStatus(StatusType.successful.name());
		response.setParams(params);

		String resultString = mgr.getResult(response, request.getId(), request.getVer(), null, null);
		return F.Promise.pure(ok(resultString).as("application/json"));
	}

	public Promise<Result> suggest() {
		String apiId = "vocabulary-term.suggest";
		Request request = getRequest(request().body(), apiId, request().uri());
		List<Map<String, Object>> terms = new ArrayList<Map<String, Object>>();
		Map<String, Object> resp = new HashMap<String, Object>();
		resp.put("lemma", "add");
		resp.put("score", "1.0");
		terms.add(resp);
		resp = new HashMap<String, Object>();
		resp.put("lemma", "addition");
		resp.put("score", "1.0");
		terms.add(resp);
		Response response = new Response();
		response.put("terms", terms);
		ResponseParams params = new ResponseParams();
		params.setStatus(StatusType.successful.name());
		response.setParams(params);

		String resultString = mgr.getResult(response, request.getId(), request.getVer(), null, null);
		return F.Promise.pure(ok(resultString).as("application/json"));
	}
}
