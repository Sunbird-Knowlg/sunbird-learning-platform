package org.sunbird.taxonomy.mgr;

import org.sunbird.common.dto.Response;

/**
 * The Interface ICompositeSearchManager defines the Graph sync operations that
 * needs to be implemented by implementing classes.
 * 
 * @author Rayulu
 * 
 */
public interface ICompositeSearchManager {

	/**
	 * Synchronizes all objects of the given object type from the Graph DB into
	 * ElastiSearch.
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @param start
	 *            the start
	 * @param total
	 *            the total
	 * @return the response
	 */
	Response sync(String graphId, String objectType, Integer start, Integer total, boolean delete) throws Exception ;

	/**
	 * Synchronizes objects fetched using the given identifiers list, from the
	 * Graph DB into ElastiSearch.
	 *
	 * @param graphId
	 *            the graph id
	 * @param identifiers
	 *            the identifiers
	 * @return the response
	 */
	Response syncObject(String graphId, String[] identifiers);
}
