package org.ekstep.language.batch.mgr;

import com.ilimi.common.dto.Response;

/**
 * The Interface IWordnetCSVManager.
 *
 * @author rayulu, amarnath
 */
public interface IWordnetCSVManager {

	/**
	 * Creates the wordnet citations.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordsCSV
	 *            the words CSV
	 * @return the response
	 */
	Response createWordnetCitations(String languageId, String wordsCSV);

	/**
	 * Adds the wordnet indexes.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordsCSV
	 *            the words CSV
	 * @return the response
	 */
	Response addWordnetIndexes(String languageId, String wordsCSV);

	/**
	 * Replace wordnet ids.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordsCSV
	 *            the words CSV
	 * @param synsetCSV
	 *            the synset CSV
	 * @param outputDir
	 *            the output dir
	 * @return the response
	 */
	Response replaceWordnetIds(String languageId, String wordsCSV, String synsetCSV, String outputDir);
}
