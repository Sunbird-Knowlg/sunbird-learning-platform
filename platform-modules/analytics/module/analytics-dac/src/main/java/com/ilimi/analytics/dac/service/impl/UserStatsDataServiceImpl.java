package com.ilimi.analytics.dac.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.genericdao.search.Search;
import com.ilimi.analytics.dac.dao.UserStatsDAO;
import com.ilimi.analytics.dac.dto.UserStatsDTO;
import com.ilimi.analytics.dac.entity.UserStatsEntity;
import com.ilimi.analytics.dac.service.IUserStatsDataService;
import com.ilimi.analytics.enums.AnalyticsParams;
import com.ilimi.common.dto.Response;
import com.ilimi.dac.BaseDataAccessService;

@Component
@Transactional
public class UserStatsDataServiceImpl extends BaseDataAccessService implements
		IUserStatsDataService {

	@Autowired
	private UserStatsDAO userStatsDAO;

	@Override
	public Response getUserStat(String uid) {

		Search search = new Search();
		search.addFilterEqual("uid", uid);
		UserStatsEntity entity = userStatsDAO.searchUnique(search);
		UserStatsDTO dto = new UserStatsDTO();
		BeanUtils.copyProperties(entity, dto);
		return OK(AnalyticsParams.user_stat.name(), dto);
	}

	@Override
	public Response searchUserStats(List<String> uids) {

		List<UserStatsEntity> entities = null;
		if (null != uids && !uids.isEmpty()) {
			Search search = new Search();
			search.addFilterIn("uid", uids);
			entities = userStatsDAO.search(search);
		} else {
			entities = userStatsDAO.findAll();
		}

		List<UserStatsDTO> list = new ArrayList<UserStatsDTO>();
		for (UserStatsEntity entity : entities) {
			UserStatsDTO dto = new UserStatsDTO();
			BeanUtils.copyProperties(entity, dto);
			list.add(dto);
		}
		return OK(AnalyticsParams.user_stats.name(), list);
	}

}
