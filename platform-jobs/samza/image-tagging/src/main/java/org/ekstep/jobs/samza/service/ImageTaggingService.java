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
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.ImageWorkflowEnums;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.OptimizerUtil;
import org.ekstep.jobs.samza.util.VisionApi;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.model.Node;

public class ImageTaggingService implements ISamzaService {

	private Config config = null;

	private ControllerUtil util = new ControllerUtil();

	static JobLogger LOGGER = new JobLogger(ImageTaggingService.class);
	
	private static final String tempFileLocation = "/data/contentBundle/";

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		Map<String, Object> props = new HashMap<String, Object>();
		for (Entry<String, String> entry : config.entrySet()) {
			props.put(entry.getKey(), entry.getValue());
		}
		S3PropertyReader.loadProperties(props);
		Configuration.loadProperties(props);
		LOGGER.info("Service config initialized");
		LearningRequestRouterPool.init();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) {
		Map<String, Object> edata = new HashMap<String, Object>();
		Map<String, Object> eks = new HashMap<String, Object>();
		try{
		if (null != message.get(ImageWorkflowEnums.edata.name())) {
			edata = (Map) message.get(ImageWorkflowEnums.edata.name());
			if (null != edata.get(ImageWorkflowEnums.eks.name())) {
				eks = (Map) edata.get(ImageWorkflowEnums.eks.name());
				if (null != eks) {
					if (null != eks.get(ImageWorkflowEnums.type.name()) && null != eks.get(ImageWorkflowEnums.subtype.name())) {
						if ((StringUtils.equalsIgnoreCase(eks.get(ImageWorkflowEnums.type.name()).toString(), ImageWorkflowEnums.Asset.name()))
								&& (StringUtils.equalsIgnoreCase(eks.get(ImageWorkflowEnums.subtype.name()).toString(), ImageWorkflowEnums.image.name()))) {
							Map<String, String> variantsMap;
							try {
								variantsMap = OptimizerUtil.optimiseImage(eks.get(ImageWorkflowEnums.id.name()).toString());
								String nodeId = (String) eks.get(ImageWorkflowEnums.id.name());
								Node node = util.getNode(ImageWorkflowEnums.domain.name(), nodeId);
								if (null == variantsMap)
									variantsMap = new HashMap<String, String>();
								if (StringUtils.isBlank(variantsMap.get(ImageWorkflowEnums.medium.name()))) {
									String downloadUrl = (String) node.getMetadata()
											.get(ImageWorkflowEnums.downloadUrl.name());
									if (StringUtils.isNotBlank(downloadUrl)) {
										variantsMap.put(ImageWorkflowEnums.medium.name(), downloadUrl);
									}
								}
								String image_url = variantsMap.get(ImageWorkflowEnums.medium.name());
								processImage(image_url, variantsMap, node);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void processImage(String image_url, Map<String, String> variantsMap, Node node) {
		try {
			String key = config.get("google.vision.tagging.enabled");
			LOGGER.info("Fetching google.vision property from config" + key);
			if ("true".equalsIgnoreCase(key)) {
				Node data = callVisionService(image_url, node, variantsMap);
				data.getMetadata().put(ImageWorkflowEnums.variants.name(), variantsMap);
				OptimizerUtil.controllerUtil.updateNode(data);
			} else {
				node.getMetadata().put(ImageWorkflowEnums.status.name(), ImageWorkflowEnums.Live.name());
				node.getMetadata().put(ImageWorkflowEnums.variants.name(), variantsMap);
				OptimizerUtil.controllerUtil.updateNode(node);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The method callVisionService holds the logic to call the google vision
	 * API get labels and flags for a given image and update the same on the
	 * node
	 * 
	 * @param image
	 * @param node
	 * @param variantsMap
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node callVisionService(String image, Node node, Map<String, String> variantsMap) {

		File file = HttpDownloadUtility.downloadFile(image, tempFileLocation);
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

		}
		return node;
	}

	/**
	 * This method holds logic to set keywords from Vision API with existing
	 * keywords from the node
	 * 
	 * @param keywords
	 *            The keywords
	 * 
	 * @param labels
	 *            The labels
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