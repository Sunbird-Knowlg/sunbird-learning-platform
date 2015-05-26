package com.ilimi.analytics.manager;

import java.util.List;

import com.ilimi.common.dto.Response;

public interface IUserStatsManager {

	Response getUserStat(String uid);
	
	Response searchUserStats(List<String> uids);
}
