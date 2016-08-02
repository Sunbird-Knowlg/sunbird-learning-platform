package org.ekstep.language.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.compositesearch.mgr.ICompositeSearchManager;
import org.ekstep.language.mgr.IWordChainsManager;
import org.ekstep.language.util.IWordChainConstants;
import org.ekstep.language.util.WordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.model.node.DefinitionDTO;

@Controller
@RequestMapping("v2/language")
public class WordChainsController extends BaseLanguageController implements IWordChainConstants {

	@Autowired
	private ICompositeSearchManager compositeSearchManager;

	@Autowired
	private IWordChainsManager wordChainsManager;

	@Autowired
	private WordUtil wordUtil;

	// @Autowired
	private ObjectMapper objectMapper = new ObjectMapper();

	private static Logger LOGGER = LogManager.getLogger(WordChainsController.class.getName());

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> search(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String apiId = "composite-search.search";
		LOGGER.info(apiId + " | Request : " + map);
		try {
			String graphId = "domain";
			String objectType = null;
			int wordChainsLimit = 0;
			Node ruleNode = null;
			Map<String, Double> weightagesMap = new HashMap<String, Double>();
			Map<String, Object> baseConditions = new HashMap<String, Object>();
			
			Request request = getRequest(map);
			Boolean isFuzzySearch = (Boolean) request.get(ATTRIB_FUZZY);
			if (isFuzzySearch == null) {
				isFuzzySearch = false;
			}
			String traversalId = (String) request.get(ATTRIB_TRAVERSAL_ID);
			boolean wordChainsQuery = false;
			if (traversalId != null) {
				wordChainsQuery = true;
				isFuzzySearch =  true;
			}
			
			List<String> languageIds = (List<String>) request.get(ATTRIB_LANGUAGE_ID);
			if (languageIds != null && !languageIds.isEmpty()) {
				graphId = languageIds.get(0);
			}
			
			
			if (wordChainsQuery) {
				if (languageIds == null || languageIds.isEmpty()) {
					throw new Exception("At least One Language Id is manadatory");
				}
				
				request.put(ATTRIB_TRAVERSAL, wordChainsQuery);
				wordChainsLimit = (int) request.get("limit");

				ruleNode = wordUtil.getDataNode(graphId, traversalId);
				if (ruleNode == null) {
					throw new Exception("No Rule found for rule id: " + traversalId);
				}

				objectType = (String) ruleNode.getMetadata().get(ATTRIB_RULE_OBJECT_TYPE);
				
				Object objectStatus;
				String[] ruleObjectStatus = (String[]) ruleNode.getMetadata().get(ATTRIB_RULE_OBJECT_STATUS);
				if (ruleObjectStatus.length > 1) {
					objectStatus = Arrays.asList(ruleObjectStatus);
				} else if (ruleObjectStatus.length > 0) {
					objectStatus = ruleObjectStatus[0];
				} else {
					objectStatus = "Live";
				}
				baseConditions.put("status", objectStatus);

				int searchResultsLimit = getIntValue(ruleNode.getMetadata().get(ATTRIB_CHAIN_WORDS_SIZE));
				request.put("limit", searchResultsLimit);
			}

			if (isFuzzySearch) {
				if(objectType == null){
					Map<String, Object> filters = (Map<String, Object>) request.get(CompositeSearchParams.filters.name());
					if (null != filters && !filters.isEmpty()) {
						for (Entry<String, Object> entry : filters.entrySet()) {
							if (StringUtils.equals(GraphDACParams.objectType.name(), entry.getKey())) {
								if (null != entry.getValue()) {
									if (entry.getValue() instanceof List) {
										objectType = ((List<String>) entry.getValue()).get(0);
									} else if (entry.getValue() instanceof String) {
										objectType = (String) entry.getValue();
									}
								}
								break;
							}
						}
					}
				}

				if (objectType != null) {
					DefinitionDTO wordDefinition = wordUtil.getDefinitionDTO(objectType, graphId);

					String weightagesJson = (String) wordDefinition.getMetadata().get(ATTRIB_WEIGHTAGES);
					if (weightagesJson != null && !weightagesJson.isEmpty()) {
						Map<String, Object> weightagesRequestMap = objectMapper.readValue(weightagesJson,
								new TypeReference<Map<String, Object>>() {
								});

						for (Map.Entry<String, Object> entry : weightagesRequestMap.entrySet()) {
							Double weightage = Double.parseDouble(entry.getKey());
							if (entry.getValue() instanceof List) {
								List<String> fields = (List<String>) entry.getValue();
								for (String field : fields) {
									weightagesMap.put(field, weightage);
								}
							} else {
								String field = (String) entry.getValue();
								weightagesMap.put(field, weightage);
							}
						}
					}
				}
				weightagesMap.put(ATTRIB_DEFAULT_WEIGHTAGE, 1.0);
				baseConditions.put("weightages", weightagesMap);
				baseConditions.put("graph_id", graphId);
				baseConditions.put("objectType", objectType);
				request.put(ATTRIB_WEIGHTAGE_BASE_CONDITIONS, baseConditions);
			}
			
			Map<String, Object> response = compositeSearchManager.languageSearch(request);
			if (!wordChainsQuery) {
				return getResponseEntity(compositeSearchManager.getCompositeSearchResponse(response), apiId, null);
			}

			List<Map> words = (List<Map>) response.get("results");
			Response wordChainResponse = wordChainsManager.getWordChain(wordChainsLimit, words, ruleNode, graphId);
			return getResponseEntity(wordChainResponse, apiId, null);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	private int getIntValue(Object object) {
		int value;
		if (object instanceof Double) {
			value = ((Double) object).intValue();
		} else {
			value = (int) object;
		}
		return value;
	}

}
