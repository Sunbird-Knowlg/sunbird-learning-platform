package org.ekstep.language.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.mgr.ICompositeSearchManager;
import org.ekstep.language.common.enums.LanguageObjectTypes;
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
import com.ilimi.graph.model.node.DefinitionDTO;

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
	@RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(@RequestBody Map<String, Object> map, 
    		@RequestHeader(value = "user-id") String userId,
            HttpServletResponse resp) {
        String apiId = "composite-search.search";
        LOGGER.info(apiId + " | Request : " + map);
        try {
        	Request request = getRequest(map);
        	
        	List<String> languageIds = (List<String>)request.get(ATTRIB_LANGUAGE_ID);
        	if(languageIds == null || languageIds.isEmpty()){
        		throw new Exception("At least One Language Id is manadatory");
        	}
        	String languageId = languageIds.get(0);
        	
        	String traversalId = (String)request.get(ATTRIB_TRAVERSAL_ID);
        	
        	Boolean isFuzzySearch = (Boolean)request.get(ATTRIB_FUZZY);
        	if(isFuzzySearch == null){
        		isFuzzySearch = false;
        	}
        	
        	Map<String, Double> weightagesMap =  new HashMap<String, Double>();
        	if(isFuzzySearch){
	        	DefinitionDTO wordDefinition = wordUtil.getDefinitionDTO(LanguageObjectTypes.Word.name(), languageId);
	        	if(wordDefinition == null){
	        		throw new Exception("No Definition found for" + LanguageObjectTypes.Word.name() + " for the language: "+  languageId);
	        	}
	        	
	        	String weightagesJson = (String) wordDefinition.getMetadata().get(ATTRIB_WEIGHTAGES);
	        	if(weightagesJson == null || weightagesJson.isEmpty()){
	        		throw new Exception("No weightages found for rule id: "+ traversalId);
	        	}
	        	
	        	Map<String, Object> weightagesRequestMap = objectMapper.readValue(weightagesJson, new TypeReference<Map<String, Object>>() {});
	        	
	        	
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
        	}
        	
        	if(traversalId == null){
        		LOGGER.info("Proceeding with normal search");
        		Response response = compositeSearchManager.search(request);
    			return getResponseEntity(response, apiId, null);
        	}
        	
        	request.put(ATTRIB_TRAVERSAL, true);
        	int wordChainsLimit = (int)request.get("limit");
        	
        	Node ruleNode = wordUtil.getDataNode(languageId, traversalId);
        	if(ruleNode == null){
        		throw new Exception("No Rule found for rule id: "+ traversalId);
        	}
        	
        	int searchResultsLimit = getIntValue(ruleNode.getMetadata().get(ATTRIB_CHAIN_WORDS_SIZE));
        	request.put("limit", searchResultsLimit);
        	
        	
        	
        	String objectType = (String) ruleNode.getMetadata().get(ATTRIB_RULE_OBJECT_TYPE);
        	Object objectStatus;
        	
        	String[] ruleObjectStatus = (String[]) ruleNode.getMetadata().get(ATTRIB_RULE_OBJECT_STATUS);
        	if(ruleObjectStatus.length > 1){
        		objectStatus = Arrays.asList(ruleObjectStatus);
        	}
        	else if(ruleObjectStatus.length > 0){
        		objectStatus = ruleObjectStatus[0];
        	}
        	else{
        		objectStatus = "Live";
        	}
        	
        	Map<String, Object> traversalProperties = new HashMap<String, Object>();
        	traversalProperties.put("weightages", weightagesMap);
        	traversalProperties.put("graph_id", languageId);
        	traversalProperties.put("objectType", objectType);
        	traversalProperties.put("status", objectStatus);

        	request.put(ATTRIB_TRAVERSAL_PROPERTIES, traversalProperties);
        	
        	Map<String, Object> response = compositeSearchManager.weightedSearch(request);
        	List<Map> words = (List<Map>) response.get("results");
        	Response wordChainResponse = wordChainsManager.getWordChain(wordChainsLimit, words, ruleNode, languageId);
            return getResponseEntity(wordChainResponse, apiId, null);
        } catch (Exception e) {
        	e.printStackTrace();
            LOGGER.error("Error: " + apiId, e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
    
    private int getIntValue(Object object) {
		int value;
		if(object instanceof Double){
			value = ((Double)object).intValue();
		}
		else{
			value = (int) object;
		}
		return value;
	}
    
}
