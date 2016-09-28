package org.ekstep.language.batch.mgr;

import com.ilimi.common.dto.Response;

/**
 * The Interface IBatchManager.defines the behaviour of all batch managers.
 * Defines implementation for batch updates of all word properties like
 * primaryMeaning, word_complexities, pos_list, word_chain wordSets etc
 *
 * @author rayulu, karthik
 */
public interface IBatchManager {

	/**
	 * Correct wordnet data.
	 * 
	 * @param languageId
	 *            the language id
	 * @return the response
	 */
	Response correctWordnetData(String languageId);

	/**
	 * Update word chain. Fetches all words in batch mode and do update WordSets
	 * of WordChian for each word
	 *
	 * @param languageId
	 *            the language id
	 * @param start
	 *            the start
	 * @param total
	 *            the total
	 * @return the response
	 */
	Response updateWordChain(String languageId, Integer start, Integer total);

	/**
	 * Update pictures.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response
	 */
	Response updatePictures(String languageId);

	/**
	 * Update pos list.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response
	 */
	Response updatePosList(String languageId);

	/**
	 * Update word features.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response
	 */
	Response updateWordFeatures(String languageId);

	/**
	 * Update frequency counts.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response
	 */
	Response updateFrequencyCounts(String languageId);

	/**
	 * Cleanup word net data.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response
	 */
	Response cleanupWordNetData(String languageId);

	/**
	 * Sets the primary meaning.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response
	 */
	Response setPrimaryMeaning(String languageId);

	/**
	 * Update word complexity.
	 *
	 * @param languageId
	 *            the language id
	 * @return the response
	 */
	Response updateWordComplexity(String languageId);
}
