package org.ekstep.language.mgr;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

/**
 * The Interface IDictionaryManager defines the behaviour of all dictionary
 * managers. Defines implementation for word, synsets and relations
 * manipulations.
 * 
 * @author Amarnath, Rayulu, Azhar
 */
public interface IDictionaryManager {

	/**
	 * Uploads the file.
	 *
	 * @param uploadedFile
	 *            the file to be uploaded
	 * @return the response
	 */
	Response upload(File uploadedFile);

	/**
	 * Find the word.
	 *
	 * @param languageId
	 *            the language id
	 * @param id
	 *            the word id
	 * @param fields
	 *            the fields to be returned in the result
	 * @param version
	 *            version number of the API
	 * @return the response
	 */
	Response find(String languageId, String id, String[] fields, String version);

	/**
	 * Find all the words. For version v2, finds all words and populates primary meanings and relations.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param fields
	 *            the fields to be returned in the result
	 * @param limit
	 *            the result limit
	 * @param version
	 *            version number of the API
	 * @return the response
	 */
	Response findAll(String languageId, String objectType, String[] fields, Integer limit, String version);

	/**
	 * Deletes relation between nodes.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param objectId1
	 *            the start node
	 * @param relation
	 *            the relation
	 * @param objectId2
	 *            the end node
	 * @return the response
	 */
	Response deleteRelation(String languageId, String objectType, String objectId1, String relation, String objectId2);

	/**
	 * List the words. For version 2, lists all words based on the filters and populates primary meanings and
	 * relations.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param request
	 *            the filter body
	 * @param version
	 *            version number of the API
	 * @return the response
	 */
	Response list(String languageId, String objectType, Request request, String version);

	/**
	 * Adds the relation between nodes.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param objectId1
	 *            the start node
	 * @param relation
	 *            the relation
	 * @param objectId2
	 *            the end node
	 * @return the response
	 */
	Response addRelation(String languageId, String objectType, String objectId1, String relation, String objectId2);

	/**
	 * Imports words and synsets from a CSV file.
	 *
	 * @param languageId
	 *            the language id
	 * @param inputStream
	 *            the input stream
	 * @return the response
	 * @throws Exception
	 *             the exception
	 */
	Response importWordSynset(String languageId, InputStream inputStream) throws Exception;

	/**
	 * Find words using the lemma in the CSV.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param is
	 *            the is
	 * @param out
	 *            the out
	 */
	void findWordsCSV(String languageId, String objectType, InputStream is, OutputStream out);

	/**
	 * Creates the word V2.
	 *
	 * @param languageId
	 *            the language id
	 * @param objectType
	 *            the object type
	 * @param request
	 *            the request
	 * @param forceUpdate
	 *            the force update
	 * @return the response
	 */
	Response createWordV2(String languageId, String objectType, Request request, boolean forceUpdate);

	/**
	 * Update word V2.
	 *
	 * @param languageId
	 *            the language id
	 * @param id
	 *            the id
	 * @param objectType
	 *            the object type
	 * @param request
	 *            the request
	 * @param forceUpdate
	 *            the force update
	 * @return the response
	 */
	Response updateWordV2(String languageId, String id, String objectType, Request request, boolean forceUpdate);

	
	/**
	 * Update word  paritally V2.
	 *
	 * @param languageId
	 *            the language id
	 * @param id
	 *            the id
	 * @param objectType
	 *            the object type
	 * @param request
	 *            the request
	 * @param forceUpdate
	 *            the force update
	 * @return the response
	 */
	Response partialUpdateWordV2(String languageId, String id, String objectType, Request request, boolean forceUpdate);
	
	/**
	 * Load english words arpabets map into Redis.
	 *
	 * @param in
	 *            the in
	 * @return the response
	 */
	Response loadEnglishWordsArpabetsMap(InputStream in);

	/**
	 * Gets the syllables from a word.
	 *
	 * @param languageID
	 *            the language ID
	 * @param word
	 *            the word
	 * @return the syllables
	 */
	Response getSyllables(String languageID, String word);

	/**
	 * Gets the arpabets from a word.
	 *
	 * @param languageID
	 *            the language ID
	 * @param word
	 *            the word
	 * @return the arpabets
	 */
	Response getArpabets(String languageID, String word);

	/**
	 * Gets the phonetic spelling by language for a word.
	 *
	 * @param languageID
	 *            the language ID
	 * @param word
	 *            the word
	 * @return the phonetic spelling by language
	 */
	Response getPhoneticSpellingByLanguage(String languageID, String word, boolean addEndVirama);

	/**
	 * Gets the similar sound words.
	 *
	 * @param languageId
	 *            the language id
	 * @param word
	 *            the word
	 * @return the similar sound words
	 */
	Response getSimilarSoundWords(String languageId, String word);

	/**
	 * Transliterates an english text into a given language
	 * 
	 * @param languageId
	 *            code of the language into which the text should be
	 *            transliterated
	 * @param addEndVirama
	 *            if virama should be added at end of the words that end with a
	 *            consonant
	 * @param map
	 *            request body containing the text to be transliterated
	 * @return the transliterated text
	 */
	Response transliterate(String languageId, Request request, boolean addEndVirama);
}
