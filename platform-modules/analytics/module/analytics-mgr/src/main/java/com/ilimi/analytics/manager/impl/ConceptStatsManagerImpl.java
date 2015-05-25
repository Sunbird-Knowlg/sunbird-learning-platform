package com.ilimi.analytics.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.analytics.dac.service.IConceptStatsDataService;
import com.ilimi.analytics.manager.IConceptStatsManager;
import com.ilimi.common.dto.Response;

@Component
public class ConceptStatsManagerImpl implements IConceptStatsManager {

	@Autowired
	private IConceptStatsDataService conceptDataService;

	@Override
	public Response getConceptStats(List<String> conceptIds) {
		return conceptDataService.getConceptStats(conceptIds);
	}

}
