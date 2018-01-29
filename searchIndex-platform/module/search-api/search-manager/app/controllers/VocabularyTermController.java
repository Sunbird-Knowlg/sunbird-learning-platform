/**
 * 
 */
package controllers;


import org.ekstep.common.dto.Request;

import managers.VocabularyTermManager;
import play.libs.F.Promise;
import play.mvc.Result;

/**
 * @author pradyumna
 *
 */
public class VocabularyTermController extends SearchBaseController {

	private VocabularyTermManager mgr = new VocabularyTermManager();

	public Promise<Result> create() {
		String apiId = "vocabulary-term.create";
		Request request = getRequest(request().body(), apiId, request().uri());
		return mgr.create(request);
	}

	public Promise<Result> suggest() {
		String apiId = "vocabulary-term.suggest";
		Request request = getRequest(request().body(), apiId, request().uri());
		return mgr.suggest(request);
  }

}
