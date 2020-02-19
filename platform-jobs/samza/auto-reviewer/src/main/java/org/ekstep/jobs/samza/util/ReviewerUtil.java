package org.ekstep.jobs.samza.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.util.ApiUtil;
import org.ekstep.common.util.PDFUtil;
import org.ekstep.content.publish.PublishManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public static List<String> getKeywords(String identifier, List<String> input) {
		System.out.println("Input Text For keywords generation for content id : "+identifier+" | Text Is : "+input);
		Set<String> result = new HashSet<String>();
		String str = "";
		if(CollectionUtils.isNotEmpty(input)){
			for(int i=0; i<=input.size();i++){
				Map<String, Object> request = new HashMap<String, Object>();
				request.put("text",input.get(i));
				System.out.println("Request for keyword generation :  "+request);
				Response resp = ApiUtil.makeKeyWordsPostRequest(identifier,request);
				System.out.println("resp: " + resp.getResponseCode());
				System.out.println("resp: " + resp.getResult());
				result.addAll((List<String>)resp.getResult().get("keywords"));
			}
			
		}
		System.out.println("result:: " + result);
		return new ArrayList<>(result);
	}
	
	public static Map<String, Object> getLanguageAnalysis(List<String> texts){
		StringBuilder tempText = new StringBuilder();
		if(CollectionUtils.isNotEmpty(texts)) {
			for(String text: texts) {
				tempText.append(text);
			}
			System.out.println("tempText:: " + tempText.toString());
			return ApiUtil.languageAnalysisAPI(tempText.toString());
		}else {
			return null;
		}
	}
	/*public static void getPdfText(String url) {//"/Users/amitpriyadarshi/Desktop/1.pdf",1
		String a = PDFUtil.readParaFromPDF(url, 1);
		Map<String, Object> b = getLanguageAnalysis(Arrays.asList(a));
		System.out.println(b);
	}
	public static void main(String[] args) {
		getPdfText("https://ntpproductionall.blob.core.windows.net/ntp-content-production/content/assets/do_31279219959874355212998/a-letter-from-obama-to-his-daughters.pdf");
	}*/
	
}
