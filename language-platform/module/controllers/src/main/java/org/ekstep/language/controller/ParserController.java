package org.ekstep.language.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.WordMeasures;
import org.ekstep.language.measures.entity.ComplexityMeasures;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;

@Controller
@RequestMapping("v1/language/parser")
public class ParserController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(SearchController.class.getName());

    private Map<String, List<String>> suggestionsMap = new HashMap<String, List<String>>();
    private Map<String, List<String>> relatedWordsMap = new HashMap<String, List<String>>();
    private Map<String, List<String>> equivalentWordsMap = new HashMap<String, List<String>>();
    private Map<String, List<Map<String, Object>>> translationsMap = new HashMap<String, List<Map<String, Object>>>();

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> parseContent(@RequestBody Map<String, Object> map,
            @RequestHeader(value = "user-id") String userId) {
        String apiId = "parser";
        Request request = getRequest(map);
        try {
            String languageId = (String) request.get(LanguageParams.language_id.name());
            if (StringUtils.isBlank(languageId))
                throw new ClientException(LanguageErrorCodes.ERR_INVALID_LANGUAGE_ID.name(), "Invalid language id");
            if (!LanguageMap.containsLanguage(languageId))
                throw new ClientException(LanguageErrorCodes.ERR_UNSUPPORTED_LANGUAGE.name(), "Unsupported language");
            String content = (String) request.get(LanguageParams.content.name());
            if (StringUtils.isBlank(content))
                throw new ClientException(LanguageErrorCodes.ERR_INVALID_CONTENT.name(), "Cannot parse empty content");

            Boolean wordSuggestions = (Boolean) request.get("wordSuggestions");
            Boolean relatedWords = (Boolean) request.get("relatedWords");
            Boolean translations = (Boolean) request.get("translations");
            Boolean equivalentWords = (Boolean) request.get("equivalentWords");

            Map<String, Object> returnMap = new HashMap<String, Object>();
            StringTokenizer st = new StringTokenizer(content);
            while (st.hasMoreTokens()) {
                String str = st.nextToken();
                if (str.length() > 2) {
                    Map<String, Object> wordMap = new HashMap<String, Object>();
                    if (wordSuggestions)
                        wordMap.put("suggestions", suggestionsMap.get(languageId.toLowerCase()));
                    if (relatedWords)
                        wordMap.put("relatedWords", relatedWordsMap.get(languageId.toLowerCase()));
                    if (equivalentWords)
                        wordMap.put("equivalentWords", equivalentWordsMap.get(languageId.toLowerCase()));
                    if (translations)
                        wordMap.put("translations", translationsMap.get(languageId.toLowerCase()));
                    ComplexityMeasures measures = null;
                    if (StringUtils.equalsIgnoreCase("te", languageId)) {
                        measures = WordMeasures.getWordComplexity(languageId, str).getMeasures();
                    } else {
                        measures = new ComplexityMeasures(RandomUtils.nextDouble(0.1, 2.4), RandomUtils.nextDouble(1.0, 150.0));
                    }
                    wordMap.put("measures", measures);
                    returnMap.put(str, wordMap);
                }
            }
            Response response = new Response();
            ResponseParams resStatus = new ResponseParams();
            resStatus.setStatus(StatusType.successful.name());
            response.setParams(resStatus);
            response.setResponseCode(ResponseCode.OK);
            response.getResult().putAll(returnMap);
            LOGGER.info("Parser | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Parser | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
    
    {
        List<String> hiwords = new ArrayList<String>();
        hiwords.add("सालाना"); hiwords.add("वार्षिक"); hiwords.add("प्रकार");
        
        List<String> tewords = new ArrayList<String>();
        tewords.add("తండ్రి"); tewords.add("ఇనుము"); tewords.add("సంఘర్షణ");
        
        List<String> kawords = new ArrayList<String>();
        kawords.add("ಅಳತೊಡಗಿದ"); kawords.add("ಪ್ರಧಾನಿಯಾದ"); kawords.add("ಹಾರಿಸಿದರು");
        
        List<String> enwords = new ArrayList<String>();
        enwords.add("yearly"); enwords.add("annual"); enwords.add("something");
        
        suggestionsMap.put("hi", hiwords);
        suggestionsMap.put("te", tewords);
        suggestionsMap.put("ka", kawords);
        suggestionsMap.put("en", enwords);
        
        relatedWordsMap.put("hi", hiwords);
        relatedWordsMap.put("te", tewords);
        relatedWordsMap.put("ka", kawords);
        relatedWordsMap.put("en", enwords);
        
        equivalentWordsMap.put("hi", hiwords);
        equivalentWordsMap.put("te", tewords);
        equivalentWordsMap.put("ka", kawords);
        equivalentWordsMap.put("en", enwords);
        
        Map<String, Object> hiTranslations = new HashMap<String, Object>();
        hiTranslations.put("language_id", "hi"); hiTranslations.put("words", hiwords);
        
        Map<String, Object> teTranslations = new HashMap<String, Object>();
        teTranslations.put("language_id", "te"); teTranslations.put("words", tewords);
        
        Map<String, Object> kaTranslations = new HashMap<String, Object>();
        kaTranslations.put("language_id", "ka"); kaTranslations.put("words", kawords);
        
        Map<String, Object> enTranslations = new HashMap<String, Object>();
        enTranslations.put("language_id", "en"); enTranslations.put("words", enwords);
        
        List<Map<String, Object>> hiTrans = new ArrayList<Map<String, Object>>();
        hiTrans.add(teTranslations); hiTrans.add(kaTranslations); hiTrans.add(enTranslations);
        
        List<Map<String, Object>> teTrans = new ArrayList<Map<String, Object>>();
        teTrans.add(hiTranslations); teTrans.add(kaTranslations); teTrans.add(enTranslations);
        
        List<Map<String, Object>> kaTrans = new ArrayList<Map<String, Object>>();
        kaTrans.add(hiTranslations); kaTrans.add(enTranslations);
        
        List<Map<String, Object>> enTrans = new ArrayList<Map<String, Object>>();
        enTrans.add(hiTranslations); enTrans.add(kaTranslations);
        
        translationsMap.put("hi", hiTrans);
        translationsMap.put("te", teTrans);
        translationsMap.put("ka", kaTrans);
        translationsMap.put("en", enTrans);
    }

}
