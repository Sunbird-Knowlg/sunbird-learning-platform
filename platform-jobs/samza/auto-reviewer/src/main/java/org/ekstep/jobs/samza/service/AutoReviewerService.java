package org.ekstep.jobs.samza.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.SamzaCommonParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;

import java.util.List;
import java.util.Map;

public class AutoReviewerService  implements ISamzaService {

	private static JobLogger LOGGER = new JobLogger(AutoReviewerService.class);
	private Config config = null;
	private Integer MAX_ITERATION_COUNT = null;
	private ControllerUtil util = new ControllerUtil();

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		JSONUtils.loadProperties(config);
		LOGGER.info("Service config initialized");
		MAX_ITERATION_COUNT = (Platform.config.hasPath("max.iteration.count.samza.job")) ?
				Platform.config.getInt("max.iteration.count.samza.job") : 1;
		LearningRequestRouterPool.init();
		LOGGER.info("Learning Actor System initialized");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		LOGGER.info("AutoReviewerService ---> processMessage ---->>> start");
		if (null == message) {
			LOGGER.info("Null Event Received. So Skipped Processing.");
			return;
		}
		Map<String, Object> edata = (Map<String, Object>) message.get(SamzaCommonParams.edata.name());
		Map<String, Object> object = (Map<String, Object>) message.get(SamzaCommonParams.object.name());

		if (!validateEvent(edata, object)) {
			LOGGER.info("Event Ignored. Event Validation Failed for post-publish-processor operations.");
			return;
		}

		LOGGER.info("Event Received: "+message);
	}

	private boolean validateEvent(Map<String, Object> edata, Map<String, Object> object) {
		if (MapUtils.isEmpty(object) || StringUtils.isBlank((String) object.get("id")) ||
				MapUtils.isEmpty(edata) || StringUtils.isBlank((String) edata.get("action")))
			return false;
		List<String> actions = (List<String>) edata.get("action");
		Integer iteration = (Integer) edata.get(SamzaCommonParams.iteration.name());
		String contentType = (String) edata.get("contentType");
		return (CollectionUtils.isNotEmpty(actions) && iteration <= MAX_ITERATION_COUNT);
	}
}
