package com.ilimi.taxonomy.mgr.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.learning.util.ControllerUtil;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.dac.enums.SuggestionConstants;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.SuggestionErrorCodeConstants;
import com.ilimi.taxonomy.mgr.ISuggestionManager;

@Component
public class SuggestionManager extends BaseSuggestionManager implements ISuggestionManager {

	/** The ControllerUtil */
	private ControllerUtil util = new ControllerUtil();

	/** The Class Logger. */
	private static LogHelper LOGGER = LogHelper.getInstance(SuggestionManager.class.getName());

	Response response = new Response();

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	@Override
	public Response saveSuggestion(Map<String, Object> request) {
		String suggestionId = null;
		try {
			String identifier = (String) request.get("objectId");
			Node node = util.getNode(SuggestionConstants.GRAPH_ID, identifier);
			if (StringUtils.equalsIgnoreCase(identifier, node.getIdentifier())) {
				suggestionId = saveSuggestionToEs(request);
				response = setResponse(response, suggestionId);
			} else {
				throw new ClientException(SuggestionErrorCodeConstants.invalid_content_id.name(),
						"Content_Id doesnt exists | Invalid Content_id");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	
	@Override
	public Response readSuggestion(String objectId, String startTime, String endTime) {
		Request request = new Request();
		LOGGER.debug("Checking if received parameters are empty or not" + objectId);
		if (StringUtils.isNotBlank(objectId)) {
			request.put(CommonDACParams.object_id.name(), objectId);
		}
		request.put(CommonDACParams.start_date.name(), startTime);
		request.put(CommonDACParams.end_date.name(), endTime);

		LOGGER.info("Sending request to suggestionService" + request);
		Response response = null;
		try {
			List<Object> result = getSuggestionByObjectId(request);
			response.getResult().put("suggestions", result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("Response received from the auditHistoryEsService as a result" + response);
		return response;
	}
}
