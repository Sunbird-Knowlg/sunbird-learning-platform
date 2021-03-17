/**
 * 
 */
package org.sunbird.framework.mgr;

import java.util.Map;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;

/**
 * @author pradyumna
 *
 */
public interface ITermManager {

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
	 * @throws Exception 
	 */
	Response updateTerm(String scopeId, String categoryId, String termId, Map<String, Object> map) throws Exception;

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
	 * @throws Exception 
	 */
	Response retireTerm(String scopeId, String categoryId, String termId) throws Exception;

	/**
	 * @param scopeId
	 * @param categoryId
	 * @param request
	 * @return
	 * @throws Exception
	 */
	Response createTerm(String scopeId, String categoryId, Request request) throws Exception;

}
