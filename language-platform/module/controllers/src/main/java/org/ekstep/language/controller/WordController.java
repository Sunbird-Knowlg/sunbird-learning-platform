package org.ekstep.language.controller;

import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("v1/language/dictionary/word")
public class WordController extends DictionaryController {

    @Override
    protected String getObjectType() {
        return LanguageObjectTypes.Word.name();
    }

    
    
}
