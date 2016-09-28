package org.ekstep.language.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IParserManager;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ilimi.common.exception.ClientException;

/**
 * The Class ParserController, entry point for parser operation
 *
 * @author rayulu
 */
@Controller
@RequestMapping("v1/language/parser")
public class ParserController extends BaseController {

    /** The logger. */
    private static Logger LOGGER = LogManager.getLogger(SearchController.class.getName());

    /** The parser manger. */
    @Autowired
    private IParserManager parserManger;

    /**
     * Parses the content.
     *
     * @param map the map
     * @param userId the user id
     * @return the response entity
     */
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
            String content = (String) request.get(LanguageParams.content.name());
            if (StringUtils.isBlank(content))
                throw new ClientException(LanguageErrorCodes.ERR_INVALID_CONTENT.name(), "Cannot parse empty content");
            Boolean wordSuggestions = (Boolean) request.get("wordSuggestions");
            //Boolean relatedWords = (Boolean) request.get("relatedWords");
            Boolean translations = (Boolean) request.get("translations");
            Boolean equivalentWords = (Boolean) request.get("equivalentWords");
            Integer limit = (Integer) request.get("limit");
            Response response = parserManger.parseContent(languageId, content, wordSuggestions, false, translations,
                    equivalentWords, limit);
            LOGGER.info("Parser | Response: " + response);
            return getResponseEntity(response, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("Parser | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId,
                    (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }
}
