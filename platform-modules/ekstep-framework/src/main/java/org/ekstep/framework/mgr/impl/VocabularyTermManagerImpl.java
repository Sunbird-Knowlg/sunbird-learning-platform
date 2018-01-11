/**
 * 
 */
package org.ekstep.framework.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.framework.mgr.IVocabularyTermMgr;
import org.springframework.stereotype.Component;

/**
 * @author pradyumna
 *
 */
@Component
public class VocabularyTermManagerImpl extends BaseFrameworkManager implements IVocabularyTermMgr {

	/* (non-Javadoc)
	 * @see org.ekstep.framework.mgr.IVocabularyTermMgr#create(org.ekstep.common.dto.Request)
	 */
	@Override
	public Response create(Request request) {
		List<Map<String, Object>> requestList = getRequestList(request);
		if (requestList.isEmpty() || requestList.get(0).isEmpty()) {
			return ERROR("ERR_INVALID_VOCABULARY_TERM_OBJECT", "Invalid Request", ResponseCode.CLIENT_ERROR);
		} else {
			return OK("node_id", "en_language");
		}
	}

	public Response suggest(Request request) {
		Map<String, Object> response = new HashMap<String, Object>();

		response.put("lemma", "language");
		response.put("score", "1.0");
		return OK("terms", response);

	}


	/**
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getRequestList(Request request) {
		List<Map<String, Object>> requestList = new ArrayList<Map<String, Object>>();
		if ((request.get("term") instanceof List) && !((List<Map<String, Object>>) request.get("term")).isEmpty())
			requestList = (List<Map<String, Object>>) request.get("term");
		else {
			Map<String, Object> map = (Map<String, Object>) request.get("term");
			if (!map.isEmpty())
				requestList.add(map);
		}
		return requestList;
	}

}
