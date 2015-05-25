package com.ilimi.analytics.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.analytics.dac.service.IUserStatsDataService;
import com.ilimi.analytics.manager.IUserStatsManager;
import com.ilimi.common.dto.Response;

@Component
public class UserStatsManagerImpl implements IUserStatsManager {
	
	@Autowired
	private IUserStatsDataService dataService;

	@Override
	public Response getUserStat(String uid) {
		return dataService.getUserStat(uid);
	}

	@Override
	public Response searchUserStats(List<String> uids) {
		return dataService.searchUserStats(uids);
	}

}
