package org.sunbird.mvcjobs.samza.service;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.sunbird.jobs.samza.exception.PlatformErrorCodes;
import org.sunbird.jobs.samza.exception.PlatformException;
import org.sunbird.jobs.samza.service.ISamzaService;
import org.sunbird.jobs.samza.service.task.JobMetrics;
import org.sunbird.mvcjobs.samza.service.util.ContentUtil;
import org.sunbird.mvcjobs.samza.service.util.MVCProcessorCassandraIndexer;
import org.sunbird.mvcjobs.samza.service.util.MVCProcessorESIndexer;
import org.sunbird.jobs.samza.util.FailedEventsUtil;
import org.sunbird.jobs.samza.util.JSONUtils;
import org.sunbird.jobs.samza.util.JobLogger;
import org.sunbird.searchindex.util.CompositeSearchConstants;
import org.elasticsearch.client.transport.NoNodeAvailableException;

import java.util.Map;

public class MVCProcessorService implements ISamzaService {

	private JobLogger LOGGER = new JobLogger(MVCProcessorService.class);
	private Config config = null;
	private MVCProcessorESIndexer mvcIndexer = null;
	private SystemStream systemStream = null;
	private MVCProcessorCassandraIndexer cassandraManager ;
	public MVCProcessorService() {}

	public MVCProcessorService(MVCProcessorESIndexer mvcIndexer) throws Exception {
		this.mvcIndexer = mvcIndexer;
	}

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		JSONUtils.loadProperties(config);
		LOGGER.info("Service config initialized");
		systemStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
		mvcIndexer = mvcIndexer == null ? new MVCProcessorESIndexer(): mvcIndexer;
		mvcIndexer.createMVCSearchIndex();
		LOGGER.info(CompositeSearchConstants.MVC_SEARCH_INDEX + " created");
		cassandraManager  = new MVCProcessorCassandraIndexer();
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector)
			throws Exception {
		Object index = message.get("index");
		Boolean shouldindex = BooleanUtils.toBoolean(null == index ? "true" : index.toString());
		String identifier = (String) ((Map<String, Object>) message.get("object")).get("id");
		if (!BooleanUtils.isFalse(shouldindex)) {
			LOGGER.debug("Indexing event into ES");
			try {
				processMessage(message);
				LOGGER.debug("Record Added/Updated into mvc index for " + identifier);
				metrics.incSuccessCounter();
			} catch (PlatformException ex) {
				LOGGER.error("Error while processing message:", message, ex);
				metrics.incFailedCounter();
				FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
						PlatformErrorCodes.SYSTEM_ERROR.name(), ex);
			} catch (Exception ex) {
				LOGGER.error("Error while processing message:", message, ex);
				metrics.incErrorCounter();
				if (null != message) {
					String errorCode = ex instanceof NoNodeAvailableException ? PlatformErrorCodes.SYSTEM_ERROR.name()
							: PlatformErrorCodes.PROCESSING_ERROR.name();
					FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
							errorCode, ex);
				}
			}
		} else {
			LOGGER.info("Learning event not qualified for indexing");
		}
	}

	public void processMessage(Map<String, Object> message) throws Exception {
		if (message != null && message.get("eventData") != null) {
			Map<String, Object> eventData = (Map<String, Object>) message.get("eventData");
			String action = eventData.get("action").toString();
			String objectId = (String) ((Map<String, Object>) message.get("object")).get("id");
			if(!action.equalsIgnoreCase("update-content-rating")) {
				if (action.equalsIgnoreCase("update-es-index")) {
					eventData = ContentUtil.getContentMetaData(eventData, objectId);
				}
				LOGGER.info("MVCProcessorService :: processMessage  ::: Calling cassandra insertion for " + objectId);
				cassandraManager.insertIntoCassandra(eventData, objectId);
			}
			LOGGER.info("MVCProcessorService :: processMessage  ::: Calling elasticsearch insertion for " + objectId);
			mvcIndexer.upsertDocument(objectId, eventData);
		}
	}

}
