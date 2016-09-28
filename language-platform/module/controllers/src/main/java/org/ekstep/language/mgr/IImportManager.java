package org.ekstep.language.mgr;

import java.io.InputStream;
import com.ilimi.common.dto.Response;

/**
 * The Interface IImportManager defines the different import operations
 * supported by the platform to import words and synsets.
 * 
 * @author Azhar, Amarnath
 * 
 */
public interface IImportManager {

	/**
	 * Transform data from a flat file into two separate files containing words
	 * and synsets data only respectively.
	 *
	 * @param languageId
	 *            the language id
	 * @param sourceId
	 *            the source id
	 * @param file
	 *            the file
	 * @return the response
	 */
	Response transformData(String languageId, String sourceId, InputStream file);

	/**
	 * Imports data from synset data file and word data file into the platform.
	 *
	 * @param languageId
	 *            the language id
	 * @param synsetStream
	 *            the synset stream
	 * @param wordStream
	 *            the word stream
	 * @return the response
	 */
	Response importData(String languageId, InputStream synsetStream, InputStream wordStream);

	/**
	 * Imports word and synset data from a JSON file. 
	 *
	 * @param languageId
	 *            the language id
	 * @param synsetsStreamInZIP
	 *            the synsets stream in ZIP
	 * @return the response
	 */
	Response importJSON(String languageId, InputStream synsetsStreamInZIP);

	/**
	 * Imports the data in the CSV.
	 *
	 * @param languageId
	 *            the language id
	 * @param file
	 *            the file
	 * @return the response
	 */
	Response importCSV(String languageId, InputStream file);

	/**
	 * Updates the definition in the graph anbd cache.
	 *
	 * @param languageId
	 *            the language id
	 * @param json
	 *            the json
	 * @return the response
	 */
	Response updateDefinition(String languageId, String json);

	/**
	 * Gets all definitions for a given graph Id.
	 *
	 * @param id
	 *            the id
	 * @return the response
	 */
	Response findAllDefinitions(String id);

	/**
	 * Finds the definition of the given object type.
	 *
	 * @param id
	 *            the id
	 * @param objectType
	 *            the object type
	 * @return the response
	 */
	Response findDefinition(String id, String objectType);
}