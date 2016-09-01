package org.ekstep.language.controller;

import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class WordController is the entry point for all Word v1 CRUD operations.
 * Extends the Dictionary Controller which provides the basic operations.
 * 
 * @author Azhar
 * 
 */
@Controller
@RequestMapping("v1/language/dictionary/word")
public class WordController extends DictionaryController {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.language.controller.DictionaryController#getObjectType()
	 */
	@Override
	protected String getObjectType() {
		return LanguageObjectTypes.Word.name();
	}

}
