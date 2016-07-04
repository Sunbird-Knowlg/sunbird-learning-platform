package org.ekstep.language.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.mgr.ICompositeSearchManager;
import org.ekstep.language.mgr.IWordChainsManager;
import org.ekstep.language.util.IWordChainConstants;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;

@Controller
@RequestMapping("v2/language")
public class WordChainsController extends BaseLanguageController implements IWordChainConstants{
	
	@Autowired
	private ICompositeSearchManager compositeSearchManager;
	
	@Autowired
	private IWordChainsManager wordChainsManager;
	
	@Autowired
	private WordUtil wordUtil;
	
	//@Autowired
	private ObjectMapper objectMapper = new ObjectMapper();

    private static Logger LOGGER = LogManager.getLogger(WordChainsController.class.getName());
    

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
        	Node ruleNode = wordUtil.getDataNode(languageId, traversalId);
        	if(ruleNode == null){
        		throw new Exception("No Rule found for rule id: "+ traversalId);
        	}
        	
        	
        	String weightagesJson = (String) ruleNode.getMetadata().get(ATTRIB_WEIGHTAGES);
        	Map<String, Object> weightagesRequestMap = objectMapper.readValue(weightagesJson, new TypeReference<Map<String, Object>>() {});
        	
        	Map<String, Double> weightagesMap =  new HashMap<String, Double>();
        	
        	for(Map.Entry<String, Object> entry: weightagesRequestMap.entrySet()){
        		Double weightage = Double.parseDouble(entry.getKey());
        		if(entry.getValue() instanceof List){
        			List<String> fields = (List<String>) entry.getValue();
        			for(String field: fields){
        				weightagesMap.put(field, weightage);
        			}
        		}
        		else {
        			String field = (String) entry.getValue();
        			weightagesMap.put(field, weightage);
        		}
        	}
        	weightagesMap.put(ATTRIB_DEFAULT_WEIGHTAGE, 1.0);
        	request.put("weightages", weightagesMap);
        	request.put("graphId", languageId);
        	Map<String, Object> response = compositeSearchManager.searchForTraversal(request);
        	List<Map> words = (List<Map>) response.get("results");
        	Response wordChainResponse = wordChainsManager.getWordChain(traversalId, wordChainsLimit, words, ruleNode, languageId);
            return getResponseEntity(wordChainResponse, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Error: " + apiId, e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
}
