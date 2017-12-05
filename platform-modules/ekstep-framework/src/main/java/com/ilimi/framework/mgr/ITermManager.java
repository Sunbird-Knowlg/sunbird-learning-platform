/**
 * 
 */
package com.ilimi.framework.mgr;

import java.util.Map;

import com.ilimi.common.dto.Response;

/**
 * @author pradyumna
 *
 */
public interface ITermManager {

	/**
	 * @param categoryId
	 * @param map
	 * @return
	 */
	Response createTerm(String categoryId, Map<String, Object> map);

	/**
	 * @param graphId
	 * @param termId
	 * @return
	 */
	Response readTerm(String graphId, String termId, String scopeId);

	/**
	 * @param categoryId
	 * @param termId
	 * @param map
	 * @return
	 */
	Response updateTerm(String categoryId, String termId, Map<String, Object> map);

	/**
	 * @param categoryId
	 * @param map
	 * @param map
	 * @return
	 */
	Response searchTerms(String categoryId, Map<String, Object> map);

	/**
	 * @param categoryId
	 * @param termId
	 * @return
	 */
	Response retireTerm(String categoryId, String termId);

	Boolean validateRequest(String channelId, String categoryId);

	Boolean validateCategoryId(String categoryId);
	
	Boolean validateMasterTerm(String categoryId, String termLabel);
}
