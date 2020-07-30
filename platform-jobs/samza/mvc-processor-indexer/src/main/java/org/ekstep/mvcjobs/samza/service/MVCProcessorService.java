package org.ekstep.mvcjobs.samza.service;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.exception.PlatformErrorCodes;
import org.ekstep.jobs.samza.exception.PlatformException;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.mvcjobs.samza.service.util.MVCProcessorCassandraIndexer;
import org.ekstep.mvcjobs.samza.service.util.MVCProcessorESIndexer;
import org.ekstep.jobs.samza.util.FailedEventsUtil;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.util.CompositeSearchConstants;
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
		if (!BooleanUtils.isFalse(shouldindex)) {
			LOGGER.debug("Indexing event into ES");
			try {
				processMessage(message);
				LOGGER.debug("Composite record added/updated");
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
			String nodeType =  eventData.get("nodeType") != null ? (String) eventData.get("nodeType") : CompositeSearchConstants.NODE_TYPE_DATA;
			String uniqueId = (String) ((Map<String, Object>) message.get("object")).get("id");
			switch (nodeType) {
				case CompositeSearchConstants.NODE_TYPE_SET:
				case CompositeSearchConstants.NODE_TYPE_DATA: {
					LOGGER.info("MVCProcessorService :: processMessage  ::: CAlling cassandra insertion ");
					eventData = cassandraManager.insertintoCassandra(eventData,uniqueId);
					LOGGER.info("MVCProcessorService :: processMessage  ::: CAlling elasticsearch insertion ");
					mvcIndexer.upsertDocument(uniqueId,eventData);
					break;
				}
				default: break;
			}
		}
	}

}
