package org.ekstep.language.batch.mgr;

import com.ilimi.common.dto.Response;

/**
 * Interface IWordnetCSVManager defines tasks to be implemented to import words and Synsets data into the Graph.
 * Also, adds Citation and WordInfo indexes into ES
 *
 * @author rayulu, amarnath
 */
public interface IWordnetCSVManager {

	/**
	 * Creates the citations indexes from the words CSV and add them into ES.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordsCSV
	 *            the words CSV
	 * @return the response
	 */
	Response createWordnetCitations(String languageId, String wordsCSV);

	/**
	 * Creates the wordnet indexes from the words CSV and add them into ES.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordsCSV
	 *            the words CSV
	 * @return the response
	 */
	Response addWordnetIndexes(String languageId, String wordsCSV);

	/**
	 * Replace wordnet ids with word Ids from platform in the words and SynsetsCSV.
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
