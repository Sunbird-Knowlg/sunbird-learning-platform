/**
 * 
 */
package org.ekstep.framework.mgr;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;

/**
 * @author pradyumna
 *
 */
public interface IVocabularyTermMgr {

	public Response create(Request request);

	public Response suggest(Request request);

}
