package org.esktep.search.util;

import java.util.List;
import java.util.Map;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.compositesearch.enums.SearchOperations;
import org.ekstep.language.common.enums.LanguageParams;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;

// TODO: Auto-generated Javadoc
/**
 * The Class CompositeSearchUtil, utility class to provide search functionality
 *
 * @author karthik
 */
public class CompositeSearchUtil extends BaseSearchManager {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();
	
	/**
	 * Search.
	 *
	 * @param searchRequestMap the search request map
	 * @return the map
	 */
	public Map<String, Object> search(Map<String, Object> searchRequestMap){
		Request request=new Request();
		request.setRequest(searchRequestMap);
		request = setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.INDEX_SEARCH.name());
		Response searchResult= getSearchResponse(request, LOGGER);
		if (!checkError(searchResult)) {
			request = getSearchRequest(SearchActorNames.SEARCH_MANAGER.name(), SearchOperations.GROUP_SEARCH_RESULT_BY_OBJECTTYPE.name());
			request.put("searchResult", searchResult.getResult());
			Response getRes = getSearchResponse(request, LOGGER);
			if (!checkError(getRes)) {
				return getRes.getResult();
			}
		}
		return null;
	}
	
	/**
	 * Search words.
	 *
	 * @param searchRequestMap the search request map
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> searchWords(Map<String, Object> searchRequestMap){
		Map<String, Object> wordResults = search(searchRequestMap);
		
		if(wordResults!=null){
			return (List<Map<String, Object>>)wordResults.get(LanguageParams.words.name());
		}
		return null;
	}
}
