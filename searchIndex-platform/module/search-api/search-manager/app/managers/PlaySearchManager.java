package managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.compositesearch.enums.SearchOperations;

import com.ilimi.common.dto.Request;

import play.libs.F.Promise;
import play.mvc.Result;

public class PlaySearchManager extends BasePlaySearchManager {

	private static Logger LOGGER = LogManager.getLogger(PlaySearchManager.class.getName());

	public Promise<Result> search(Request request) {
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name(),
				SearchOperations.INDEX_SEARCH.name());
		Promise<Result> getRes = getSearchResponse(request, LOGGER);
		return getRes;
	}

	public Promise<Result> count(Request request) {
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name(), SearchOperations.COUNT.name());
		Promise<Result> getRes = getSearchResponse(request, LOGGER);
		return getRes;
	}
	
	public Promise<Result> metrics(Request request){
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.METRICS.name());
		Promise<Result> getRes = getSearchResponse(request, LOGGER);
		return getRes;
	}

}
