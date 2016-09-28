package org.ekstep.language.mgr;

import com.ilimi.common.dto.Response;

// TODO: Auto-generated Javadoc
/**
 * The Interface IParserManager, provides parser function
 *
 * @author rayulu
 */
public interface IParserManager {

	/**
	 * Parses the content and provides equivalent meaning for the words found in
	 * graph. it will provide other features like providing translation in
	 * future.
	 *
	 * @param languageId
	 *            the language id
	 * @param content
	 *            the content
	 * @param wordSuggestions
	 *            the word suggestions
	 * @param relatedWords
	 *            the related words
	 * @param translations
	 *            the translations
	 * @param equivalentWords
	 *            the equivalent words
	 * @param limit
	 *            the limit
	 * @return the response
	 */
	Response parseContent(String languageId, String content, Boolean wordSuggestions, Boolean relatedWords,
			Boolean translations, Boolean equivalentWords, Integer limit);
}
