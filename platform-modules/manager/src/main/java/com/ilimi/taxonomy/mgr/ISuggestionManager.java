package com.ilimi.taxonomy.mgr;

import java.util.Map;

import org.ekstep.common.dto.Response;

public interface ISuggestionManager {

	Response saveSuggestion(Map<String,Object> request);

	Response readSuggestion(String suggestion_id, String start_date, String end_date, String status);

	Response approveSuggestion(String suggestion_id, Map<String, Object> map);

	Response rejectSuggestion(String suggestion_id, Map<String, Object> map);

	Response listSuggestion(Map<String, Object> map);

}