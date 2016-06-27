package org.ekstep.compositesearch.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.ekstep.compositesearch.mgr.ICompositeSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.logger.LogHelper;

@Controller
@RequestMapping("v2/search")
public class CompositeSearchController extends BaseCompositeSearchController {

	private static LogHelper LOGGER = LogHelper.getInstance(CompositeSearchController.class.getName());

	@Autowired
	private ICompositeSearchManager compositeSearchManager;

	@RequestMapping(value = "/sync/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@PathVariable(value = "id") String graphId,
			@RequestParam(name = "objectType", required = false) String objectType,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse resp) {
		String apiId = "composite-search.sync";
		LOGGER.info(apiId + " | Graph : " + graphId + " | ObjectType: " + objectType);
		try {
			Request request = getRequest(map);
			Response response = compositeSearchManager.sync(graphId, objectType, request);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> search(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String apiId = "composite-search.search";
		LOGGER.info(apiId + " | Request : " + map);
		try {
			Request request = getRequest(map);
			Response response = compositeSearchManager.search(request);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/count", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> count(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String apiId = "composite-search.count";
		LOGGER.info(apiId + " | Request : " + map);
		try {
			Request request = getRequest(map);
			Response response = compositeSearchManager.count(request);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> searchTraversal(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String apiId = "composite-search.searchTraversal";
		LOGGER.info(apiId + " | Request : " + map);
		try {
			Request request = getRequest(map);
			Response response = new Response();
			ResponseParams params = new ResponseParams();
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			//if (request.get("traversalId") != null) {
			ObjectMapper mapper = new ObjectMapper();
			String json = "{  \"relations\": [    {      \"list\": [        \"en_2\",        \"en_5\",        \"en_1\"      ],      \"score\": 1000,      \"relation\": \"aksharaBoundary\",      \"title\": \"\"    },    {      \"list\": [        \"en_13\",        \"en_1\",        \"en_4\",        \"en_6\"      ],      \"score\": 800,      \"relation\": \"aksharaBoundary\",      \"title\": \"\"    }  ],  \"words\": [    {      \"identifier\": \"en_2\",      \"lemma\": \"cat\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/cat.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/cat.mp3\"      ]    },    {      \"identifier\": \"en_5\",      \"lemma\": \"tiger\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/tiger.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/tiger.mp3\"      ]    },    {      \"identifier\": \"en_1\",      \"lemma\": \"rat\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/rat.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/rat.mp3\"      ]    },    {      \"identifier\": \"en_13\",      \"lemma\": \"deer\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/deer.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/deer.mp3\"      ]    },    {      \"identifier\": \"en_4\",      \"lemma\": \"take\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/take.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/take.mp3\"      ]    },    {      \"identifier\": \"en_6\",      \"lemma\": \"elephant\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/elephant.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/elephant.mp3\"      ]    }  ]}";
			Map<String, Object> sampleResponseMap = mapper.readValue(json,
					new TypeReference<Map<String, Object>>() {
					});
			response.put("relations", sampleResponseMap.get("relations"));
			response.put("words", sampleResponseMap.get("words"));
			//}
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

}
