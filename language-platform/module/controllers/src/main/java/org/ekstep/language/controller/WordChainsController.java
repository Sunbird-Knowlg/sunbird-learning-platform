package org.ekstep.language.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;

@Controller
@RequestMapping("v2/language")
public class WordChainsController extends BaseLanguageController {

	private static Logger LOGGER = LogManager.getLogger(WordChainsController.class.getName());

	@RequestMapping(value = "/traversals/{languageId}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> traversals() {
		String apiId = "ekstep.language.traversals";
		try {
			Response response = new Response();
			ResponseParams params = new ResponseParams();
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			ObjectMapper mapper = new ObjectMapper();
			String json = "{  \"traversals\": [    {      \"name\": \"Consonant Rule\",      \"type\": \"ConsonantBoundary\",      \"traversalId\": \"rule_1\"    },    {      \"name\": \"AksharaRule\",      \"type\": \"AksharaBoundary\",      \"traversalId\": \"rule_2\"    },    {      \"name\": \"RhymingwordsRule\",      \"type\": \"RhymingSound\",      \"traversalId\": \"rule_3\"    }  ]}";
			Map<String, Object> sampleResponseMap = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
			});
			response.put("traversals", sampleResponseMap.get("traversals"));
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@RequestMapping(value = "/search/{languageId}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> search(@RequestBody Map<String, Object> map) {
		String apiId = "ekstep.language.search";
		try {
			Response response = new Response();
			ResponseParams params = new ResponseParams();
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			ObjectMapper mapper = new ObjectMapper();
			String json = "{  \"relations\": [    {      \"list\": [        \"en_2\",        \"en_5\",        \"en_1\"      ],      \"score\": 1000,      \"relation\": \"aksharaBoundary\",      \"title\": \"\"    },    {      \"list\": [        \"en_13\",        \"en_1\",        \"en_4\",        \"en_6\"      ],      \"score\": 800,      \"relation\": \"aksharaBoundary\",      \"title\": \"\"    }  ],  \"words\": [    {      \"identifier\": \"en_2\",      \"lemma\": \"cat\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/cat.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/cat.mp3\"      ]    },    {      \"identifier\": \"en_5\",      \"lemma\": \"tiger\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/tiger.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/tiger.mp3\"      ]    },    {      \"identifier\": \"en_1\",      \"lemma\": \"rat\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/rat.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/rat.mp3\"      ]    },    {      \"identifier\": \"en_13\",      \"lemma\": \"deer\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/deer.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/deer.mp3\"      ]    },    {      \"identifier\": \"en_4\",      \"lemma\": \"take\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/take.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/take.mp3\"      ]    },    {      \"identifier\": \"en_6\",      \"lemma\": \"elephant\",      \"pictures\": [        \"https://s3.amazon.com/ekstep-public/language_assets/elephant.jpg\"      ],      \"pronunciations\": [        \"https://s3.amazon.com/ekstep-public/language_assets/elephant.mp3\"      ]    }  ]}";
			Map<String, Object> sampleResponseMap = mapper.readValue(json,
					new TypeReference<Map<String, Object>>() {
					});
			response.put("relations", sampleResponseMap.get("relations"));
			response.put("words", sampleResponseMap.get("words"));
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

}
