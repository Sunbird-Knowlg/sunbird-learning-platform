package org.ekstep.jobs.samza.service;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.service.util.CompositeSearchIndexer;
import org.ekstep.jobs.samza.service.util.DialCodeIndexer;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;

public class CompositeSearchIndexerService implements ISamzaService {

	private JobLogger LOGGER = new JobLogger(CompositeSearchIndexerService.class);

	private ElasticSearchUtil esUtil = null;

	private CompositeSearchIndexer csIndexer = null;

	private DialCodeIndexer dcIndexer = null;

	@Override
	public void initialize(Config config) throws Exception {
		JSONUtils.loadProperties(config);
		LOGGER.info("Service config initialized");
		esUtil = new ElasticSearchUtil();
		LearningRequestRouterPool.init();
		LOGGER.info("Learning actors initialized");
		csIndexer = new CompositeSearchIndexer(esUtil);
		csIndexer.createCompositeSearchIndex();
		LOGGER.info(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX + " created");
		dcIndexer = new DialCodeIndexer(esUtil);
		dcIndexer.createDialCodeIndex();
		LOGGER.info(CompositeSearchConstants.DIAL_CODE_INDEX + " created");
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
			} catch (Exception ex) {
				LOGGER.error("Failed to process message", message, ex);
				metrics.incFailedCounter();
			}
		} else {
			LOGGER.info("Learning event not qualified for indexing");
			metrics.incSkippedCounter();
		}
	}

	public void processMessage(Map<String, Object> message, JobMetrics metrics) throws Exception {
		if (message != null && message.get("operationType") != null) {
			String nodeType = (String) message.get("nodeType");
			String objectType = (String) message.get("objectType");
			String graphId = (String) message.get("graphId");
			String uniqueId = (String) message.get("nodeUniqueId");
			switch (nodeType) {
			case CompositeSearchConstants.NODE_TYPE_SET:
			case CompositeSearchConstants.NODE_TYPE_DATA:
			case CompositeSearchConstants.NODE_TYPE_DEFINITION: {
				csIndexer.processESMessage(graphId, objectType, uniqueId, message, metrics);
				break;
			}
			case CompositeSearchConstants.NODE_TYPE_EXTERNAL: {
				dcIndexer.upsertDocument(uniqueId, message);
				break;
			}
			}
		}
	}

}
