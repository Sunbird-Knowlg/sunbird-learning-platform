/**
 * 
 */
package org.sunbird.common.mgr;

import java.util.Map;

/**
 * @author amitpriyadarshi
 *
 */
public interface IURLManager {

	/**
	 * @param url
	 * @param validationCriterion
	 * @return
	 */
	Map<String, Object> validateURL(String url, String validationCriterion);
	/**
	 * @param url
	 * @return
	 */
	Map<String, Object> readMetadata(String url);
}
