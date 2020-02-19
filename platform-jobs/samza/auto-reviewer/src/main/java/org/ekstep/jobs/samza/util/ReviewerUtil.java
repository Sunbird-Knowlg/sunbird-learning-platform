package org.ekstep.jobs.samza.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.util.ApiUtil;
import org.ekstep.content.publish.PublishManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewerUtil {

	protected static ObjectMapper objectMapper = new ObjectMapper();

	public static List<String> getECMLText(String identifier) {
		//String body = getContentBody(identifier);
		String body = PublishManager.getContentBody(identifier);
		List<String> totalTextList = new ArrayList<String>();
		if(StringUtils.isNotBlank(body)){
			try {
				Map<String, Object> ecmlMap = objectMapper.readValue(body, Map.class);
				List<Map<String, Object>> stages = ((List<Map<String, Object>>)((Map<String, Object>)ecmlMap.get("theme")).get("stage"));
				for(Map<String, Object> stage: stages) {
					List<Map<String, Object>> textList = (List<Map<String, Object>>) stage.get("org.ekstep.text");
					if(CollectionUtils.isNotEmpty(textList)){
						for(Map<String, Object> textMap: textList) {
							if(MapUtils.isNotEmpty((Map<String, Object>)textMap.get("config"))) {
								String textString = (String) ((Map<String, Object>)textMap.get("config")).get("__cdata");
								if(StringUtils.isNotBlank(textString)) {
									Map<String, Object> actualTextMap = objectMapper.readValue(textString, Map.class);
									String textToBeTranslated = (String) actualTextMap.get("text");
									if(StringUtils.isNotBlank(textToBeTranslated)){
										totalTextList.add(textToBeTranslated);
									}
								}
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return totalTextList;
	}

	public static List<String> getKeywords(List<String> input) {
		List<String> result = new ArrayList<>();
		String str = "";
		if(CollectionUtils.isNotEmpty(result)){
			for(int i=0; i<=input.size();i++){
				str+= input.get(i);
				if(i%3==0)
					break;
			}
			Map<String, Object> request = new HashMap<String, Object>();
			request.put("text",str);
			Response resp = ApiUtil.makeKeyWordsPostRequest(request);
			result.addAll((List<String>)resp.getResult().get("keywords"));
		}
		return result;
	}

}
