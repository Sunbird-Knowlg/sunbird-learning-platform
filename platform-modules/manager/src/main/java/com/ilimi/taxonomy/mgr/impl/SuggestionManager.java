package com.ilimi.taxonomy.mgr.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.SuggestionConstants;
import com.ilimi.taxonomy.enums.SuggestionErrorCodeConstants;
import com.ilimi.taxonomy.mgr.ISuggestionManager;

@Component
public class SuggestionManager extends BaseSuggestionManager implements ISuggestionManager {

	/** The ControllerUtil */
	private ControllerUtil util = new ControllerUtil();

	/** The Class Logger. */
	private static LogHelper LOGGER = LogHelper.getInstance(SuggestionManager.class.getName());

	/** The ElasticSearchUtil */
	private static ElasticSearchUtil es = new ElasticSearchUtil();

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private static ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public Response saveSuggestion(Map<String, Object> request) {
		Response response = null;
		try {
			LOGGER.info("Fetching identifier from request");
			String identifier = (String) request.get("objectId");
			Node node = util.getNode(SuggestionConstants.GRAPH_ID, identifier);
			if (StringUtils.equalsIgnoreCase(identifier, node.getIdentifier())) {
				LOGGER.info("saving the suggestions to elsatic search index" + identifier);
				response = saveSuggestionToEs(request);
			} else {
				throw new ClientException(SuggestionErrorCodeConstants.Invalid_object_id.name(),
						"Content_Id doesnt exists | Invalid Content_id");
			}
		} catch (IOException e) {
			LOGGER.info("Error occured while processing request | Not a valid request");
			throw new ClientException(SuggestionErrorCodeConstants.Invalid_request.name(), "Error occured while processing request | Not a valid request");
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
		Response response = new Response();
		try {
			List<Object> result = getSuggestionByObjectId(request);
			response.put("suggestions", result);
		} catch (Exception e) {
			LOGGER.error("Exception occured while fetching suggestions for contentId", e);
			throw e;
		}
		LOGGER.info("Response received from the auditHistoryEsService as a result" + response);
		return response;
	}

	@Override
	public Response approveSuggestion(String suggestion_id, Map<String, Object> map) {
		if (StringUtils.isBlank(suggestion_id)) {
			throw new ClientException(SuggestionErrorCodeConstants.Missing_suggestion_id.name(),
					"Error! Invalid | Missing suggestion_id");
		}
		try {
			Node node = (Node)map.get("request");
			util.updateNode(node);		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "unchecked",  "unused" })
	@Override
	public Response rejectSuggestion(String suggestion_id, Map<String, Object> map) {
		Response response = new Response();
		Request request = new Request();
		if (StringUtils.isBlank(suggestion_id)) {
			throw new ClientException(SuggestionErrorCodeConstants.Missing_suggestion_id.name(),
					"Error! Invalid | Missing suggestion_id");
		}
		try {
			Map<String,Object> requestMap = validateRequest(map);
			String results = mapper.writeValueAsString(requestMap);
			es.updateDocument(SuggestionConstants.SUGGESTION_INDEX,SuggestionConstants.SUGGESTION_INDEX_TYPE, results,
			suggestion_id);
			response.put("message", "suggestion rejected successfully" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;

	}

	@Override
	public Response listSuggestion(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return null;
	}
}
