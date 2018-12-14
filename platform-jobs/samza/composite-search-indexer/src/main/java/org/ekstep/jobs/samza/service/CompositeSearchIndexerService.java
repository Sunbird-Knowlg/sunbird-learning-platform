package org.ekstep.jobs.samza.service;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.exception.PlatformErrorCodes;
import org.ekstep.jobs.samza.exception.PlatformException;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.service.util.CompositeSearchIndexer;
import org.ekstep.jobs.samza.service.util.DialCodeIndexer;
import org.ekstep.jobs.samza.service.util.DialCodeMetricsIndexer;
import org.ekstep.jobs.samza.util.FailedEventsUtil;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.elasticsearch.client.transport.NoNodeAvailableException;

import java.util.Map;

public class CompositeSearchIndexerService implements ISamzaService {

	private JobLogger LOGGER = new JobLogger(CompositeSearchIndexerService.class);

	private CompositeSearchIndexer csIndexer = null;
	private DialCodeIndexer dcIndexer = null;
	private DialCodeMetricsIndexer dcMetricsIndexer;
	private SystemStream systemStream = null;

	public CompositeSearchIndexerService() {}

	public CompositeSearchIndexerService(CompositeSearchIndexer csIndexer, DialCodeIndexer dcIndexer,
										 DialCodeMetricsIndexer dcMetricsIndexer) throws Exception {
		this.csIndexer = csIndexer;
		this.dcIndexer = dcIndexer;
		this.dcMetricsIndexer = dcMetricsIndexer;
	}

	@Override
	public void initialize(Config config) throws Exception {
		JSONUtils.loadProperties(config);
		LOGGER.info("Service config initialized");
		LearningRequestRouterPool.init();
		LOGGER.info("Learning actors initialized");
		systemStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
		csIndexer = csIndexer == null ? new CompositeSearchIndexer(): csIndexer;
		csIndexer.createCompositeSearchIndex();
		LOGGER.info(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX + " created");
		dcIndexer = dcIndexer == null ? new DialCodeIndexer() : dcIndexer;
		dcIndexer.createDialCodeIndex();
		LOGGER.info(CompositeSearchConstants.DIAL_CODE_INDEX + " created");
		dcMetricsIndexer = dcMetricsIndexer == null ? new DialCodeMetricsIndexer() : dcMetricsIndexer;
		dcMetricsIndexer.createDialCodeIndex();
		LOGGER.info(CompositeSearchConstants.DIAL_CODE_METRICS_INDEX + " created");
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector)
			throws Exception {
		Object index = message.get("index");
		Boolean shouldindex = BooleanUtils.toBoolean(null == index ? "true" : index.toString());
		if (!BooleanUtils.isFalse(shouldindex)) {
			LOGGER.debug("Indexing event into ES");
			try {
				processMessage(message, metrics);
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

	public void processMessage(Map<String, Object> message, JobMetrics metrics) throws Exception {
		if (message != null && message.get("operationType") != null) {
			String nodeType = (String) message.get("nodeType");
			String objectType = (String) message.get("objectType");
			String graphId = (String) message.get("graphId");
			String uniqueId = (String) message.get("nodeUniqueId");
			String messageId = (String) message.get("mid");
			switch (nodeType) {
				case CompositeSearchConstants.NODE_TYPE_SET:
				case CompositeSearchConstants.NODE_TYPE_DATA:
				case CompositeSearchConstants.NODE_TYPE_DEFINITION: {
					csIndexer.processESMessage(graphId, objectType, uniqueId, messageId, message, metrics);
					break;
				}
				case CompositeSearchConstants.NODE_TYPE_EXTERNAL: {
					dcIndexer.upsertDocument(uniqueId, message);
					break;
				}
				case CompositeSearchConstants.NODE_TYPE_DIALCODE_METRICS: {
					dcMetricsIndexer.upsertDocument(uniqueId, message);
					break;
				}
			}
		}
	}

}
