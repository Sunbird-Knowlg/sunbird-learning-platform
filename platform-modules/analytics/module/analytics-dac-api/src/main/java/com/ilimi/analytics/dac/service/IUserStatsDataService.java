package com.ilimi.analytics.dac.service;

import java.util.List;

import com.ilimi.common.dto.Response;

public interface IUserStatsDataService {
	
	Response getUserStat(String uid);
	
	Response searchUserStats(List<String> uids);

}
