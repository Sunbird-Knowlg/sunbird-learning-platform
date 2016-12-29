package org.ekstep.language.controller;

import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The Class WordController is the entry point for all Word v3 CRUD operations.
 * Extends the DictionaryControllerV3 which provides the basic operations.
 *
 * @author karthik
 */
@Controller
@RequestMapping("v3/words")
public class WordControllerV3 extends DictionaryControllerV3 {
	
	/**
	 * Gets the object type.
	 *
	 * @return the object type
	 */
	/* (non-Javadoc)
	 * @see org.ekstep.language.controller.DictionaryControllerV2#getObjectType()
	 */
	@Override
	protected String getObjectType() {
		return LanguageObjectTypes.Word.name();
	}

}
