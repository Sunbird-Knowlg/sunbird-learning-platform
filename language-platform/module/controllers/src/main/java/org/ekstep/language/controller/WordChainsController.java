package org.ekstep.language.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.mgr.ICompositeSearchManager;
import org.ekstep.language.Util.WordChainConstants;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IWordChainsManager;
import org.ekstep.language.util.WordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;

@Controller
@RequestMapping("v2/language")
public class WordChainsController extends BaseLanguageController implements WordChainConstants{
	
	@Autowired
	private ICompositeSearchManager compositeSearchManager;
	
	@Autowired
	private IWordChainsManager wordChainsManager;
	
	@Autowired
	private WordUtil wordUtil;

    private static Logger LOGGER = LogManager.getLogger(WordChainsController.class.getName());
    

    @RequestMapping(value = "/traversals/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> getComplexity(@RequestBody Map<String, Object> map) {
        String apiId = "wordchain.get";
        Request request = getRequest(map);
        String language = (String) request.get(LanguageParams.language_id.name());
        // TODO: return error response if language value is blank
        request.setManagerName(LanguageActorNames.LEXILE_MEASURES_ACTOR.name());
        request.setOperation(LanguageOperations.computeComplexity.name());
        request.getContext().put(LanguageParams.language_id.name(), language);
        LOGGER.info("List | Request: " + request);
        try {
            Response response = getResponse(request, LOGGER);
            LOGGER.info("List | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/search/{languageId}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(@RequestBody Map<String, Object> map, 
    		@RequestHeader(value = "user-id") String userId, @PathVariable(value = "languageId") String languageId,
            HttpServletResponse resp) {
        String apiId = "composite-search.search";
        LOGGER.info(apiId + " | Request : " + map);
        try {
        	Request request = getRequest(map);
        	String traversalId = (String)request.get(ATTRIB_TRAVERSAL_ID);
        	if(traversalId == null){
        		throw new Exception("Traversal Id is manadatory");
        	}
        	request.put(ATTRIB_TRAVERSAL, true);
        	int wordChainsLimit = (int)request.get("limit");
        	request.put("limit", 500);
        	Map<String, Object> response = compositeSearchManager.searchForTraversal(request);
        	List<Map> words = (List<Map>) response.get("results");
        	Node ruleNode = wordUtil.getDataNode(languageId, traversalId);
        	
        	wordChainsManager.getWordChain(traversalId, wordChainsLimit, words, ruleNode);
        	System.out.println();
            return getResponseEntity(new Response(), apiId, null);
        } catch (Exception e) {
            LOGGER.error("Error: " + apiId, e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
}
