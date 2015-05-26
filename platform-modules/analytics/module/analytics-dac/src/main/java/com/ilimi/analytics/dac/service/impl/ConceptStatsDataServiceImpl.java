package com.ilimi.analytics.dac.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.genericdao.search.Search;
import com.ilimi.analytics.dac.dao.ConceptStatsDAO;
import com.ilimi.analytics.dac.dao.StudentConceptStatsDAO;
import com.ilimi.analytics.dac.dto.ConceptStatsDTO;
import com.ilimi.analytics.dac.dto.StatsDTO;
import com.ilimi.analytics.dac.entity.ConceptStatsEntity;
import com.ilimi.analytics.dac.service.IConceptStatsDataService;
import com.ilimi.analytics.enums.AnalyticsParams;
import com.ilimi.common.dto.Response;
import com.ilimi.dac.BaseDataAccessService;

@Component
@Transactional
public class ConceptStatsDataServiceImpl extends BaseDataAccessService implements IConceptStatsDataService {

	@Autowired
	private ConceptStatsDAO conceptStatsDAO;

	@Autowired
	private StudentConceptStatsDAO studentConceptStatsDAO;

	@Override
	public Response getConceptStats(List<String> conceptIds) {

		List<ConceptStatsDTO> conceptStatsList = new ArrayList<ConceptStatsDTO>();
		List<ConceptStatsEntity> entities = null;
		if (null == conceptIds || conceptIds.isEmpty()) {
			entities = conceptStatsDAO.findAll();
		} else {
			Search search = new Search();
			search.addFilterIn("conceptId", conceptIds);
			entities = conceptStatsDAO.search(search);
		}

		if (null == entities) {
			return OK(AnalyticsParams.concept_stats.name(), conceptStatsList);
		}

		for (ConceptStatsEntity entity : entities) {
			ConceptStatsDTO dto = new ConceptStatsDTO();
			dto.setConceptId(entity.getConceptId());
			dto.setGameId(entity.getGameId());
			dto.getStats().add(new StatsDTO("min", "Min Improvement", entity.getMinImprovement()));
			dto.getStats().add(new StatsDTO("max", "Max Improvement", entity.getMaxImprovement()));
			dto.getStats().add(new StatsDTO("mean", "Mean Improvement", entity.getMeanImprovement()));
			dto.getStats().add(new StatsDTO("sd", "Standard deviation", entity.getSdImprovement()));
			dto.getStats().add(new StatsDTO("effectSize", "Effect Size (T-Statistic)", entity.getEffectSize()));

			Search search = new Search();
			int totalSize = studentConceptStatsDAO.count(search);
			
			search.addFilterGreaterThan("improvePercent", 0);
			int count = studentConceptStatsDAO.count(search);
			dto.getStats().add(new StatsDTO("improved", "% of students improved", new Float(count)/totalSize * 100));

			search.clear();
			search.addFilterLessOrEqual("improvePercent", 0);
			count = studentConceptStatsDAO.count(search);
			dto.getStats().add(new StatsDTO("noImprovement", "% of students not improved", new Float(count)/totalSize  * 100));

			search.clear();
			search.addFilterLessOrEqual("improvePercent", 0.5);
			count = studentConceptStatsDAO.count(search);
			dto.getStats().add(new StatsDTO("improvedBy50", "% of students improved by 50%", new Float(count)/totalSize  * 100));
			conceptStatsList.add(dto);
		}

		return OK(AnalyticsParams.concept_stats.name(), conceptStatsList);
	}

}
