package org.ekstep.samza.jobs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.PropertiesUtil;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.samza.jobs.util.BundleContentParams;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.cache.factory.JedisFactory;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.model.Node;

public class BundleContentService implements ISamzaService {

	static JobLogger LOGGER = new JobLogger(BundleContentService.class);

	private String tempFileLocation = "/tmp";

	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";

	/** The Default Manifest Version */
	private static final String DEFAULT_CONTENT_MANIFEST_VERSION = "1.2";
	
	private ControllerUtil util = new ControllerUtil();
	
	@SuppressWarnings("unused")
	private Config config = null;

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<String, String> entry : config.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		S3PropertyReader.loadProperties(props);
		Configuration.loadProperties(props);
		PropertiesUtil.loadProperties(props);
		LOGGER.info("Service config initialized");
		LearningRequestRouterPool.init();
		LOGGER.info("Akka actors initialized");
		JedisFactory.initialize(props);
		LOGGER.info("Redis connection factory initialized");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector)
			throws Exception {
		Map<String, Object> request = getBundleLifecycleData(message);

		if (null == request) {
			metrics.incSkippedCounter();
			return;
		}
		try {
			String bundleFileName = (String)request.get("fileName");
			List<String> nodeIds = (List) request.get(BundleContentParams.params.name());
			bundleContent(nodeIds, bundleFileName, metrics);
			metrics.incSuccessCounter();
		} catch (Exception e) {
			LOGGER.error("Failed to process message", request, e);
			metrics.incFailedCounter();
		}
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void bundleContent(List<String> contentIds, String bundleFileName, JobMetrics metrics) {
		Response response = util.getDataNodes(BundleContentParams.domain.name(), contentIds);
		Response listRes = util.copyResponse(response);
			List<Object> list = (List<Object>) response.get(BundleContentParams.node_list.name());
			List<Node> nodes = new ArrayList<Node>();
			List<Node> imageNodes = new ArrayList<Node>();
			if (null != list && !list.isEmpty()) {
				
				LOGGER.info("Iterating Over the List.");
				for (Object obj : list) {
					List<Node> nodelist = (List<Node>) obj;
					if (null != nodelist && !nodelist.isEmpty())
						nodes.addAll(nodelist);
				}

				LOGGER.info("Validating the Input Nodes.");
				if(!validateInputNodesForBundling(nodes)){
					metrics.incSkippedCounter();
				}

				for (Node node : nodes) {
					String contentImageId = getContentImageIdentifier(node.getIdentifier());
					Node getNodeResponse = util.getNode(BundleContentParams.domain.name(), contentImageId);
					if (null == node) {
						metrics.incSkippedCounter();
					}
					String body = util.getContentBody(node.getIdentifier());
					node.getMetadata().put(ContentAPIParams.body.name(), body);
					imageNodes.add(node);
					LOGGER.info("Body fetched from content store");
				}
				if (imageNodes.size() == 1 && StringUtils.isBlank(bundleFileName))
					bundleFileName = (String) imageNodes.get(0).getMetadata().get(BundleContentParams.name.name()) + "_"
							+ System.currentTimeMillis() + "_" + (String) imageNodes.get(0).getIdentifier();
			}
			bundleFileName = Slug.makeSlug(bundleFileName, true);
			String fileName = bundleFileName + ".ecar";
			LOGGER.info("Bundle File Name: " + bundleFileName);

			LOGGER.info("Preparing the Parameter Map for 'Bundle' Pipeline.");
			InitializePipeline pipeline = new InitializePipeline(tempFileLocation, "node");
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put(BundleContentParams.nodes.name(), imageNodes);
			parameterMap.put(BundleContentParams.bundleFileName.name(), fileName);
			parameterMap.put(BundleContentParams.contentIdList.name(), contentIds);
			parameterMap.put(BundleContentParams.manifestVersion.name(), DEFAULT_CONTENT_MANIFEST_VERSION);

			LOGGER.info("Calling Content Workflow 'Bundle' Pipeline.");
			listRes.getResult().putAll(pipeline.init(BundleContentParams.bundle.name(), parameterMap).getResult());
	}

	private boolean validateInputNodesForBundling(List<Node> nodes) {
		if (null != nodes && !nodes.isEmpty()) {
			for (Node node : nodes) {
				// Validating for Content Image Node
				if (null != node && isContentImageObject(node))
					return false;
			}
		}
		return true;
	}

	private String getContentImageIdentifier(String contentId) {
		String contentImageId = "";
		if (StringUtils.isNotBlank(contentId))
			contentImageId = contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		return contentImageId;
	}
	
	private boolean isContentImageObject(Node node) {
		boolean isContentImage = false;
		if (null != node && StringUtils.equalsIgnoreCase(node.getObjectType(),
				BundleContentParams.ContentImage.name()))
			isContentImage = true;
		return isContentImage;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getBundleLifecycleData(Map<String, Object> message) {
		String eid = (String) message.get("eid");
		if (null == eid || !StringUtils.equalsIgnoreCase(eid, BundleContentParams.BE_JOB_LOG.name())) {
			return null;
		}

		Map<String, Object> edata = (Map<String, Object>) message.get("edata");
		if (null == edata) {
			return null;
		}

		Map<String, Object> eks = (Map<String, Object>) edata.get("eks");
		if (null == eks) {
			return null;
		}
		Map<String,Object> data = (Map<String,Object>) eks.get("data");
		if(null == data)
			return null;
		return data;
	}
}
