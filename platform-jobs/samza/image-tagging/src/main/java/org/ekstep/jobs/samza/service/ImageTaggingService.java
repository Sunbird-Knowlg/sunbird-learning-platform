package org.ekstep.jobs.samza.service;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.ImageWorkflowEnums;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.OptimizerUtil;
import org.ekstep.jobs.samza.util.VisionApi;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;

import com.ilimi.common.Platform;
import com.ilimi.graph.dac.model.Node;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueFactory;

public class ImageTaggingService implements ISamzaService {

	private Config config = null;

	private ControllerUtil util = new ControllerUtil();

	static JobLogger LOGGER = new JobLogger(ImageTaggingService.class);

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<String, String> entry : config.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		com.typesafe.config.Config conf = ConfigFactory.parseMap(props);
		Platform.loadProperties(conf);
		System.out.println("Configuration Initialized" + conf);
		LOGGER.info("Service config initialized");
		LearningRequestRouterPool.init();
		LOGGER.info("Akka actors initialized");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getImageLifecycleData(Map<String, Object> message) {
		String eid = (String) message.get("eid");
		if (null == eid || !StringUtils.equalsIgnoreCase(eid, ImageWorkflowEnums.BE_OBJECT_LIFECYCLE.name())) {
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
		if (null != eks.get(ImageWorkflowEnums.type.name()) && null != eks.get(ImageWorkflowEnums.subtype.name())) {
			if ((StringUtils.equalsIgnoreCase((String) eks.get("type"), "Asset"))
					&& (StringUtils.equalsIgnoreCase((String) eks.get("subtype"), "image"))
					&& (StringUtils.equalsIgnoreCase((String) eks.get("state"), "Processing"))) {
				return eks;
			}
		}
		return null;
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		Map<String, Object> eks = getImageLifecycleData(message);
		if (null == eks) {
			metrics.incSkippedCounter();
			return;
		}
		try {
			String nodeId = (String) eks.get(ImageWorkflowEnums.id.name());
			Node node = util.getNode(ImageWorkflowEnums.domain.name(), nodeId);
			if ((null != node) && (node.getObjectType().equalsIgnoreCase(ImageWorkflowEnums.content.name()))){
				imageEnrichment(node);
				metrics.incSuccessCounter();
			} else {
				metrics.incSkippedCounter();
			}
		} catch (Exception e) {
			LOGGER.error("Failed to process message", message, e);
			metrics.incFailedCounter();
		}
	}

	private void imageEnrichment(Node node) throws Exception {
		LOGGER.info("Processing image enrichment for node:" + node.getIdentifier());
		Map<String, String> variantsMap = OptimizerUtil.optimizeImage(node.getIdentifier(), this.config.get("lp.tempfile.location"), node);
		if (null == variantsMap)
			variantsMap = new HashMap<String, String>();
		if (StringUtils.isBlank(variantsMap.get(ImageWorkflowEnums.medium.name()))) {
			String downloadUrl = (String) node.getMetadata().get(ImageWorkflowEnums.downloadUrl.name());
			if (StringUtils.isNotBlank(downloadUrl)) {
				variantsMap.put(ImageWorkflowEnums.medium.name(), downloadUrl);
			}
		}
		String image_url = variantsMap.get(ImageWorkflowEnums.medium.name());
		processImage(image_url, variantsMap, node);
	}

	private void processImage(String image_url, Map<String, String> variantsMap, Node node) {
		try {
			String key = config.get("google.vision.tagging.enabled");
			LOGGER.info("Fetching google.vision property from config" + key);
			if ("true".equalsIgnoreCase(key)) {
				Node data = callVisionService(image_url, node, variantsMap);
				data.getMetadata().put(ImageWorkflowEnums.variants.name(), variantsMap);
				util.updateNode(data);
			} else {
				node.getMetadata().put(ImageWorkflowEnums.status.name(), ImageWorkflowEnums.Live.name());
				node.getMetadata().put(ImageWorkflowEnums.variants.name(), variantsMap);
				util.updateNode(node);
			}
		} catch (Exception e) {
			LOGGER.error("General Security Exception" + e.getMessage(), e);
		}
	}

	/**
	 * The method callVisionService holds the logic to call the google vision API get labels and flags for a given image
	 * and update the same on the node
	 * 
	 * @param image
	 * @param node
	 * @param variantsMap
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node callVisionService(String image, Node node, Map<String, String> variantsMap) {
		File file = HttpDownloadUtility.downloadFile(image, this.config.get("lp.tempfile.location"));
		Map<String, Object> labels = new HashMap<String, Object>();
		List<String> flags = new ArrayList<String>();
		VisionApi vision;
		try {
			vision = new VisionApi(VisionApi.getVisionService());
			labels = vision.getTags(file, vision);
			flags = vision.getFlags(file, vision);
		} catch (IOException | GeneralSecurityException e) {
			LOGGER.error("General Security Exception" + e.getMessage(), e);
		}
		try {
			List<String> node_keywords = new ArrayList<String>();
			if (null != node.getMetadata().get(ImageWorkflowEnums.keywords.name())) {
				Object object = node.getMetadata().get(ImageWorkflowEnums.keywords.name());
				if (object instanceof String[]) {
					String[] stringArray = (String[]) node.getMetadata().get(ImageWorkflowEnums.keywords.name());
					List keywords = Arrays.asList(stringArray);
					node_keywords = setKeywords(keywords, labels);
				}
				if (object instanceof String) {
					String keyword = (String) node.getMetadata().get(ImageWorkflowEnums.keywords.name());
					node_keywords.add(keyword);
					node_keywords = setKeywords(node_keywords, labels);
				}
			}
			if (!node_keywords.isEmpty()) {
				node.getMetadata().put(ImageWorkflowEnums.keywords.name(), node_keywords);
			}
			node.getMetadata().put(ImageWorkflowEnums.status.name(), ImageWorkflowEnums.Live.name());
			List<String> flaggedByList = new ArrayList<>();
			if (null != node.getMetadata().get(ImageWorkflowEnums.flaggedBy.name())) {
				flaggedByList.addAll((Collection<? extends String>) node.getMetadata().get(ImageWorkflowEnums.flaggedBy.name()));
			}
			if (null != flags && (!flags.isEmpty())) {
				node.getMetadata().put(ImageWorkflowEnums.flags.name(), flags);
				flaggedByList.add(ImageWorkflowEnums.Ekstep.name());
				node.getMetadata().put(ImageWorkflowEnums.flaggedBy.name(), flaggedByList);
				node.getMetadata().put(ImageWorkflowEnums.versionKey.name(), node.getMetadata().get(ImageWorkflowEnums.versionKey.name()));
				node.getMetadata().put(ImageWorkflowEnums.status.name(), ImageWorkflowEnums.Flagged.name());
				node.getMetadata().put(ImageWorkflowEnums.lastFlaggedOn.name(), new Date().toString());
			}
		} catch (Exception e) {
			LOGGER.error("General Security Exception" + e.getMessage(), e);
		}
		return node;
	}

	/**
	 * This method holds logic to set keywords from Vision API with existing keywords from the node
	 * 
	 * @param keywords The keywords
	 * 
	 * @param labels The labels
	 * 
	 * @return List of keywords
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<String> setKeywords(List<String> keywords, Map<String, Object> labels) {
		if (null != labels && !labels.isEmpty()) {
			for (Entry<String, Object> entry : labels.entrySet()) {
				List<String> list = (List) entry.getValue();
				if (null != list && (!list.isEmpty())) {
					for (String key : list) {
						if (!keywords.contains(key)) {
							keywords.addAll(list);
						}
					}
				}
			}
		}
		return keywords;
	}
}