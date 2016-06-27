package org.ekstep.language.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;

@Controller
@RequestMapping("v2/language/traversals")
public class WordChainsController extends BaseLanguageController {

	private static Logger LOGGER = LogManager.getLogger(WordChainsController.class.getName());

	@RequestMapping(value = "/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> traversals(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String apiId = "composite-search.traversals";
		LOGGER.info(apiId + " | Request : " + map);
		try {
			Response response = new Response();
			ResponseParams params = new ResponseParams();
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			// if (request.get("traversalId") != null) {
			ObjectMapper mapper = new ObjectMapper();
			String json = "{  \"traversals\": [    {      \"name\": \"Consonant Rule\",      \"type\": \"ConsonantBoundary\",      \"traversalId\": \"rule_1\"    },    {      \"name\": \"AksharaRule\",      \"type\": \"AksharaBoundary\",      \"traversalId\": \"rule_2\"    },    {      \"name\": \"RhymingwordsRule\",      \"type\": \"RhymingSound\",      \"traversalId\": \"rule_3\"    }  ]}";
			Map<String, Object> sampleResponseMap = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
			});
			response.put("traversals", sampleResponseMap.get("traversals"));
			// }
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

}
