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
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.ImageWorkflowEnums;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.OptimizerUtil;
import org.ekstep.jobs.samza.util.VisionApi;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

public class ImageTaggingService implements ISamzaService {

	private Config config = null;

	private ControllerUtil util = new ControllerUtil();

	static JobLogger LOGGER = new JobLogger(ImageTaggingService.class);

	private static int MAXITERTIONCOUNT= 2;
	
	protected int getMaxIterations() {
		if(Platform.config.hasPath("max.iteration.count.samza.job")) 
			return Platform.config.getInt("max.iteration.count.samza.job");
		else 
			return MAXITERTIONCOUNT;
	}
	
	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		JSONUtils.loadProperties(config);
		LOGGER.info("Service config initialized");
		LearningRequestRouterPool.init();
		LOGGER.info("Akka actors initialized");
	}
	
	private boolean validateObject(Map<String, Object> edata) {
		
		if (null == edata) 
			return false;
		if(StringUtils.isNotBlank((String)edata.get(ImageWorkflowEnums.contentType.name())) && 
				StringUtils.isNotBlank((String)edata.get(ImageWorkflowEnums.mediaType.name())) &&
				StringUtils.isNotBlank((String)edata.get(ImageWorkflowEnums.status.name()))) {
			
			if(StringUtils.equalsIgnoreCase((String)edata.get(ImageWorkflowEnums.contentType.name()), ImageWorkflowEnums.Asset.name()) &&
					(StringUtils.equalsIgnoreCase((String)edata.get(ImageWorkflowEnums.mediaType.name()), ImageWorkflowEnums.image.name()) ||
							StringUtils.equalsIgnoreCase((String)edata.get(ImageWorkflowEnums.mediaType.name()), ImageWorkflowEnums.video.name()))){
				
				if(((Integer)edata.get(ImageWorkflowEnums.iteration.name()) == 1 && 
						StringUtils.equalsIgnoreCase((String)edata.get(ImageWorkflowEnums.status.name()), ImageWorkflowEnums.Processing.name())) || 
						((Integer)edata.get(ImageWorkflowEnums.iteration.name()) > 1 && 
						(Integer)edata.get(ImageWorkflowEnums.iteration.name()) <= getMaxIterations() && 
						StringUtils.equalsIgnoreCase((String)edata.get(ImageWorkflowEnums.status.name()), ImageWorkflowEnums.FAILED.name()))) {
					return true;
				}
			}
		}
		return false;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		if(null == message) {
			LOGGER.info("Ignoring the message because it is not valid for imagetagging.");
			metrics.incSkippedCounter();
			return;
		}
		Map<String, Object> edata = (Map<String, Object>) message.get(ImageWorkflowEnums.edata.name());
		Map<String, Object> object = (Map<String, Object>) message.get(ImageWorkflowEnums.object.name());
		
		if (!validateObject(edata) || null == object) {
			LOGGER.info("Ignoring the message because it is not valid for imagetagging.");
			return;
		}
		try {
			String nodeId = (String) object.get(ImageWorkflowEnums.id.name());
			Node node = util.getNode(ImageWorkflowEnums.domain.name(), nodeId);
			if ((null != node) && (node.getObjectType().equalsIgnoreCase(ImageWorkflowEnums.content.name()))){
				String mediaType = (String)edata.get(ImageWorkflowEnums.mediaType.name());
				if(StringUtils.equalsIgnoreCase(mediaType, "image"))
					imageEnrichment(node);
				else if (StringUtils.equalsIgnoreCase(mediaType, "video"))
					videoEnrichment(node);
				metrics.incSuccessCounter();
			} else {
				metrics.incSkippedCounter();
			}
		} catch (Exception e) {
			LOGGER.error("Error while processing message", message, e);
			metrics.incErrorCounter();
			edata.put(ImageWorkflowEnums.status.name(), ImageWorkflowEnums.FAILED.name());
		}
	}

	private void imageEnrichment(Node node) throws Exception{
		try {
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
		}catch(Exception e) {
			LOGGER.info(
					"Something Went Wrong While Performing Image Tagging operation. | [Content Id: "
							+ node.getIdentifier() + "]",
					e.getMessage());
			node.getMetadata().put(ImageWorkflowEnums.processingError.name(), e.getMessage());
			node.getMetadata().put(ImageWorkflowEnums.status.name(), ImageWorkflowEnums.Failed.name());
			Response res = util.updateNode(node);
			if(checkError(res))
				throw new ServerException(ImageWorkflowEnums.PROCESSING_ERROR.name(), "Error! While Updating the Metadata | [Content Id: " + 
						node.getIdentifier() + "] :: " + res.getParams().getErr() + " :: " + res.getParams().getErrmsg());
			throw e;
		}
	}

	private void processImage(String image_url, Map<String, String> variantsMap, Node node) throws Exception{
		String key = config.get("google.vision.tagging.enabled");
		LOGGER.info("Fetching google.vision property from config" + key);
		LOGGER.info("Image " + node.getIdentifier() + " channel:" + node.getMetadata().get("channel"));
		LOGGER.info("Image " + node.getIdentifier() + " appId:" + node.getMetadata().get("appId"));
		LOGGER.info("Image " + node.getIdentifier() + " consumerId:" + node.getMetadata().get("consumerId"));
		if ("true".equalsIgnoreCase(key)) {
			Node data = callVisionService(image_url, node, variantsMap);
			data.getMetadata().put(ImageWorkflowEnums.variants.name(), variantsMap);
		} else {
			node.getMetadata().put(ImageWorkflowEnums.status.name(), ImageWorkflowEnums.Live.name());
			node.getMetadata().put(ImageWorkflowEnums.variants.name(), variantsMap);
		}
		Response res = util.updateNode(node);
		if(checkError(res)) {
			throw new ServerException(ImageWorkflowEnums.PROCESSING_ERROR.name(), "Error! While Updating the Metadata | [Content Id: " + 
					node.getIdentifier() + "] :: " + res.getParams().getErr() + " :: " + res.getParams().getErrmsg());
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
	
	private void videoEnrichment(Node node) throws Exception{
		String tempFileLocation = StringUtils.isNotBlank(this.config.get("lp.tempfile.location"))?this.config.get("lp.tempfile.location"):"/tmp";  
		String tempFolder = tempFileLocation + File.separator + System.currentTimeMillis() + "_temp";
		try {
			String videoUrl = (String) node.getMetadata().get("artifactUrl");
			if(StringUtils.isBlank(videoUrl)) {
				LOGGER.info("Content artifactUrl is blank.");
				throw new ClientException(ImageWorkflowEnums.PROCESSING_ERROR.name(), "Content artifactUrl is blank.");
			}
			processVideo(node, tempFolder, videoUrl);
		}catch(Exception e) {
			LOGGER.info("Something Went Wrong While Performing Image Tagging operation. | [Content Id: " + 
					node.getIdentifier() + "]", e.getMessage());
			node.getMetadata().put(ImageWorkflowEnums.processingError.name(), e.getMessage());
			node.getMetadata().put(ImageWorkflowEnums.status.name(), ImageWorkflowEnums.Failed.name());
			Response res = util.updateNode(node);
			if(checkError(res))
				throw new ServerException(ImageWorkflowEnums.PROCESSING_ERROR.name(), "Error! While Updating the Metadata | [Content Id: " + 
						node.getIdentifier() + "] :: " + res.getParams().getErr() + " :: " + res.getParams().getErrmsg());
			throw e;
		}finally {
			try {
				deleteFolder(tempFolder);
				TelemetryManager.log("Deleted local Thumbnail files");
			} catch (Exception e) {
				TelemetryManager.error("Error! While deleting the Thumbnail Folder: " + tempFolder, e);
			}
		}
		
	}
	
	private void processVideo(Node node, String tempFolder, String videoUrl) throws Exception{
		
		File videoFile = HttpDownloadUtility.downloadFile(videoUrl, tempFolder);
		OptimizerUtil.videoEnrichment(node, tempFolder, videoFile);
		node.getMetadata().put(ImageWorkflowEnums.status.name(), ImageWorkflowEnums.Live.name());
		Response res = util.updateNode(node);
		if(checkError(res)) {
			throw new ServerException(ImageWorkflowEnums.PROCESSING_ERROR.name(),
					"Error! While Updating the Metadata | [Content Id: " + node.getIdentifier() + "]");
		}
		
	}
	
	private static void deleteFolder(String tempFolder) {
		File index = new File(tempFolder);
		String[]entries = index.list();
		for(String s: entries){
		    File currentFile = new File(index.getPath(),s);
		    currentFile.delete();
		}
	}
	
	protected boolean checkError(Response response) {
		ResponseParams params = response.getParams();
		return (null != params && StringUtils.equals(StatusType.failed.name(), params.getStatus()));
	}
}