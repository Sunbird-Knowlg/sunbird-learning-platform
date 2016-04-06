package org.ekstep.language.mgr;

import com.ilimi.common.dto.Response;

public interface IParserManager {

    Response parseContent(String languageId, String content, Boolean wordSuggestions, Boolean relatedWords,
            Boolean translations, Boolean equivalentWords, Integer limit);
}
