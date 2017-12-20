/**
 * 
 */
package com.ilimi.framework.mgr;

import java.util.Map;

import org.ekstep.common.dto.Response;

/**
 * @author pradyumna
 *
 */
public interface ITermManager {

	/**
	 * @param scopeId
	 * @param categoryId
	 * @param request
	 * @return
	 */
	Response createTerm(String scopeId, String categoryId, Map<String, Object> request);

	/**
	 * @param graphId
	 * @param termId
	 * @return
	 */
	Response readTerm(String scopeId, String termId, String categoryId);

	/**
	 * @param categoryId
	 * @param termId
	 * @param map
	 * @return
	 */
	Response updateTerm(String scopeId, String categoryId, String termId, Map<String, Object> map);

	/**
	 * @param categoryId
	 * @param map
	 * @param map
	 * @return
	 */
	Response searchTerms(String scopeId, String categoryId, Map<String, Object> map);

	/**
	 * @param categoryId
	 * @param termId
	 * @return
	 */
	Response retireTerm(String scopeId, String categoryId, String termId);


}
