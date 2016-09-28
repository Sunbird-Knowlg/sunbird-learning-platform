package org.ekstep.language.controller;

import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class WordController is the entry point for all Word v2 CRUD operations.
 * Extends the DictionaryControllerV2 which provides the basic operations.
 * 
 * @author Amarnath
 * 
 */
@Controller
@RequestMapping("v2/language/dictionary/word")
public class WordControllerV2 extends DictionaryControllerV2 {

	/* (non-Javadoc)
	 * @see org.ekstep.language.controller.DictionaryControllerV2#getObjectType()
	 */
	@Override
	protected String getObjectType() {
		return LanguageObjectTypes.Word.name();
	}

}
