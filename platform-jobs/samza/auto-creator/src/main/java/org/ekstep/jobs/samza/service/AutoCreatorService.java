package org.ekstep.jobs.samza.service;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ServerException;
import org.ekstep.jobs.samza.exception.PlatformErrorCodes;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.AutoCreatorParams;
import org.ekstep.jobs.samza.util.ContentUtil;
import org.ekstep.jobs.samza.util.FailedEventsUtil;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.SamzaCommonParams;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoCreatorService implements ISamzaService {
	private static JobLogger LOGGER = new JobLogger(AutoCreatorService.class);
	private Config config = null;
	private SystemStream failedEventStream;
	private static Integer MAX_ITERATION_COUNT = null;
	private List<String> ALLOWED_OBJECT_TYPES = null;
	private ContentUtil contentUtil = null;

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		JSONUtils.loadProperties(config);
		LOGGER.info("Service config initialized");
		failedEventStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
		LOGGER.info("Stream initialized for Failed Events");
		MAX_ITERATION_COUNT = (Platform.config.hasPath("max.iteration.count.samza.job")) ?
				Platform.config.getInt("max.iteration.count.samza.job") : 2;
		ALLOWED_OBJECT_TYPES = Arrays.asList(Platform.config.getString("auto_creator.allowed_object_types").split(","));
		contentUtil = new ContentUtil();
		LOGGER.info("ContentUtil initialized.");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		if (null == message) {
			LOGGER.info("Null Event Received. So Skipped Processing.");
			return;
		}
		Map<String, Object> edata = (Map<String, Object>) message.get(SamzaCommonParams.edata.name());
		Map<String, Object> object = (Map<String, Object>) message.get(SamzaCommonParams.object.name());
		Map<String, Object> context = (Map<String, Object>) message.get(AutoCreatorParams.context.name());
		try {
			Integer currentIteration = (Integer) edata.get(SamzaCommonParams.iteration.name());
			String channel = (String) context.getOrDefault(AutoCreatorParams.channel.name(), "");
			String identifier = (String) object.getOrDefault(AutoCreatorParams.id.name(), "");
			String objectType = (String) edata.getOrDefault(AutoCreatorParams.objectType.name(), "");
			String repository = (String) edata.getOrDefault(AutoCreatorParams.repository.name(), "");
			Map<String, Object> metadata = (Map<String, Object>) edata.getOrDefault(AutoCreatorParams.metadata.name(), new HashMap<String, Object>());
			Map<String, Object> textbookInfo = (Map<String, Object>) edata.getOrDefault(AutoCreatorParams.textbookInfo.name(), new HashMap<String, Object>());

			if (!validateEvent(currentIteration, channel, identifier, objectType, metadata)) {
				LOGGER.info("Event Ignored. Event Validation Failed for auto-creator operation : " + edata.get("action") + " | Event : " + message);
				return;
			}

			switch (objectType.toLowerCase()) {
				case "content": {
					if (!(contentUtil.validateMetadata(metadata))) {
						LOGGER.info("Event Ignored. Event Metadata Validation Failed for :" + identifier + " | Metadata : " + metadata + " Required fields are : " + contentUtil.REQUIRED_METADATA_FIELDS);
						return;
					}
					contentUtil.process(channel, identifier, repository, metadata, textbookInfo);
					break;
				}
				default: {
					LOGGER.info("Event Ignored. Event objectType doesn't match with allowed objectType.");
				}
			}
		} catch (Exception e) {
			LOGGER.error("AutoCreatorService :: Message processing failed for mid : " + message.get("mid"), message, e);
			metrics.incErrorCounter();
			Integer currentIteration = (Integer) edata.get(SamzaCommonParams.iteration.name());
			if (currentIteration < MAX_ITERATION_COUNT) {
				((Map<String, Object>) message.get(SamzaCommonParams.edata.name())).put(SamzaCommonParams.iteration.name(), currentIteration + 1);
				FailedEventsUtil.pushEventForRetry(failedEventStream, message, metrics, collector,
						PlatformErrorCodes.PROCESSING_ERROR.name(), new ServerException("ERR_AUTO_CREATE", "Auto Creation Failed!"));
				LOGGER.info("Failed Event Sent To Kafka Topic : " + config.get("output.failed.events.topic.name") + " | for mid : " + message.get("mid"), message);
			}else{
				LOGGER.info("Event Reached Maximum Retry Limit having mid : " + message.get("mid"), message);
			}
		}
	}

	private Boolean validateEvent(Integer currentIteration, String channel, String identifier, String objectType, Map<String, Object> metadata) {
		if ((currentIteration <= MAX_ITERATION_COUNT) && (StringUtils.isNotBlank(channel) && StringUtils.isNotBlank(identifier) && MapUtils.isNotEmpty(metadata)) &&
				(StringUtils.isNotBlank(objectType) && ALLOWED_OBJECT_TYPES.contains(objectType))) {
			return true;
		}
		return false;
	}

}
