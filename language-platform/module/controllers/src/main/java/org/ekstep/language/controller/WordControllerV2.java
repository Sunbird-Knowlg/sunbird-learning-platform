package org.ekstep.language.controller;

import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("v2/language/dictionary/word")
public class WordControllerV2 extends DictionaryControllerV2 {

    @Override
    protected String getObjectType() {
        return LanguageObjectTypes.Word.name();
    }
    
}
