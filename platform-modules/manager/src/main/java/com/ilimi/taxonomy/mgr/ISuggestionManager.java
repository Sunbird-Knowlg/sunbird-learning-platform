package com.ilimi.taxonomy.mgr;

import java.util.Map;

import com.ilimi.common.dto.Response;

public interface ISuggestionManager {

	Response createSuggestion(Map<String,Object> request);

}
