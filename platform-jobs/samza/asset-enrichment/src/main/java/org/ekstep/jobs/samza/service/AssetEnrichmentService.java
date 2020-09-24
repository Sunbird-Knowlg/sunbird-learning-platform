package org.ekstep.jobs.samza.service;

import org.apache.commons.collections.MapUtils;
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
import org.ekstep.common.util.YouTubeUrlUtil;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.AssetEnrichmentEnums;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.jobs.samza.util.OptimizerUtil;
import org.ekstep.jobs.samza.util.VisionApi;
import org.ekstep.learning.contentstore.VideoStreamingJobRequest;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

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

public class AssetEnrichmentService implements ISamzaService {
	private VideoStreamingJobRequest streamJobRequest = null;

	private Config config = null;

	private ControllerUtil util = new ControllerUtil();

	private static JobLogger LOGGER = new JobLogger(AssetEnrichmentService.class);

	private static int MAXITERTIONCOUNT= 2;

	protected int getMaxIterations() {
		if(Platform.config.hasPath("max.iteration.count.samza.job"))
			return Platform.config.getInt("max.iteration.count.samza.job");
		else
			return MAXITERTIONCOUNT;
	}
	private static final Boolean CONTENT_UPLOAD_CONTEXT_DRIVEN = Platform.config.hasPath("content.upload.context.driven") ? Platform.config.getBoolean("content.upload.context.driven") : true;

	@Override
	public void initialize(Config config) throws Exception {
		this.config = config;
		JSONUtils.loadProperties(config);
		LOGGER.info("Service config initialized");
		LearningRequestRouterPool.init();
		LOGGER.info("Akka actors initialized");
		streamJobRequest  = new VideoStreamingJobRequest();
	}

	private boolean validateObject(Map<String, Object> edata) {

		if (null == edata)
			return false;
		if(StringUtils.isNotBlank((String)edata.get(AssetEnrichmentEnums.objectType.name())) &&
				StringUtils.isNotBlank((String)edata.get(AssetEnrichmentEnums.mediaType.name())) &&
				StringUtils.isNotBlank((String)edata.get(AssetEnrichmentEnums.status.name()))) {

			if(StringUtils.equalsIgnoreCase((String)edata.get(AssetEnrichmentEnums.objectType.name()), AssetEnrichmentEnums.Asset.name()) &&
					(StringUtils.equalsIgnoreCase((String)edata.get(AssetEnrichmentEnums.mediaType.name()), AssetEnrichmentEnums.image.name()) ||
							StringUtils.equalsIgnoreCase((String)edata.get(AssetEnrichmentEnums.mediaType.name()), AssetEnrichmentEnums.video.name()))){

				if(((Integer)edata.get(AssetEnrichmentEnums.iteration.name()) == 1 &&
						StringUtils.equalsIgnoreCase((String)edata.get(AssetEnrichmentEnums.status.name()), AssetEnrichmentEnums.Processing.name())) ||
						((Integer)edata.get(AssetEnrichmentEnums.iteration.name()) > 1 &&
						(Integer)edata.get(AssetEnrichmentEnums.iteration.name()) <= getMaxIterations() &&
						StringUtils.equalsIgnoreCase((String)edata.get(AssetEnrichmentEnums.status.name()), AssetEnrichmentEnums.FAILED.name()))) {
					return true;
				}
			}
		}
		return false;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		System.out.println(message);
		if(null == message) {
			LOGGER.info("Ignoring the message because it is not valid for assetenrichment.");
			metrics.incSkippedCounter();
			return;
		}
		Map<String, Object> edata = (Map<String, Object>) message.get(AssetEnrichmentEnums.edata.name());
		Map<String, Object> object = (Map<String, Object>) message.get(AssetEnrichmentEnums.object.name());

		if (!validateObject(edata) || null == object) {
			LOGGER.info("Ignoring the message because it is not valid for assetenrichment.");
			return;
		}
		try {
			String nodeId = (String) object.get(AssetEnrichmentEnums.id.name());
			Node node = util.getNode(AssetEnrichmentEnums.domain.name(), nodeId);
			if ((null != node) && (node.getObjectType().equalsIgnoreCase(AssetEnrichmentEnums.asset.name()))){
				String mediaType = (String)edata.get(AssetEnrichmentEnums.mediaType.name());
				if(CONTENT_UPLOAD_CONTEXT_DRIVEN && 
						StringUtils.isNoneBlank((String)node.getMetadata().get("artifactBasePath")) &&
						StringUtils.isNoneBlank((String)node.getMetadata().get("artifactUrl"))) {
					OptimizerUtil.replaceArtifactUrl(node);
				}
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
			edata.put(AssetEnrichmentEnums.status.name(), AssetEnrichmentEnums.FAILED.name());
		}
	}

	private void imageEnrichment(Node node) throws Exception{
		try {
			LOGGER.info("Processing image enrichment for node:" + node.getIdentifier());
			Map<String, String> variantsMap = OptimizerUtil.optimizeImage(node.getIdentifier(), this.config.get("lp.tempfile.location"), node);
			if (null == variantsMap)
				variantsMap = new HashMap<String, String>();
			if (StringUtils.isBlank(variantsMap.get(AssetEnrichmentEnums.medium.name()))) {
				String downloadUrl = (String) node.getMetadata().get(AssetEnrichmentEnums.downloadUrl.name());
				if (StringUtils.isNotBlank(downloadUrl)) {
					variantsMap.put(AssetEnrichmentEnums.medium.name(), downloadUrl);
				}
			}
			String image_url = variantsMap.get(AssetEnrichmentEnums.medium.name());
			processImage(image_url, variantsMap, node);
		}catch(Exception e) {
			LOGGER.info(
					"Something Went Wrong While Performing Asset Enrichment operation. | [Content Id: "
							+ node.getIdentifier() + "]",
					e.getMessage());
			node.getMetadata().put(AssetEnrichmentEnums.processingError.name(), e.getMessage());
			node.getMetadata().put(AssetEnrichmentEnums.status.name(), AssetEnrichmentEnums.Failed.name());
			Response res = util.updateNode(node);
			if(checkError(res))
				throw new ServerException(AssetEnrichmentEnums.PROCESSING_ERROR.name(), "Error! While Updating the Metadata | [Content Id: " +
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
			data.getMetadata().put(AssetEnrichmentEnums.variants.name(), variantsMap);
		} else {
			node.getMetadata().put(AssetEnrichmentEnums.status.name(), AssetEnrichmentEnums.Live.name());
			node.getMetadata().put(AssetEnrichmentEnums.variants.name(), variantsMap);
		}
		Response res = util.updateNode(node);
		if(checkError(res)) {
			throw new ServerException(AssetEnrichmentEnums.PROCESSING_ERROR.name(), "Error! While Updating the Metadata | [Content Id: " +
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
			if (null != node.getMetadata().get(AssetEnrichmentEnums.keywords.name())) {
				Object object = node.getMetadata().get(AssetEnrichmentEnums.keywords.name());
				if (object instanceof String[]) {
					String[] stringArray = (String[]) node.getMetadata().get(AssetEnrichmentEnums.keywords.name());
					List keywords = Arrays.asList(stringArray);
					node_keywords = setKeywords(keywords, labels);
				}
				if (object instanceof String) {
					String keyword = (String) node.getMetadata().get(AssetEnrichmentEnums.keywords.name());
					node_keywords.add(keyword);
					node_keywords = setKeywords(node_keywords, labels);
				}
			}
			if (!node_keywords.isEmpty()) {
				node.getMetadata().put(AssetEnrichmentEnums.keywords.name(), node_keywords);
			}
			node.getMetadata().put(AssetEnrichmentEnums.status.name(), AssetEnrichmentEnums.Live.name());
			List<String> flaggedByList = new ArrayList<>();
			if (null != node.getMetadata().get(AssetEnrichmentEnums.flaggedBy.name())) {
				flaggedByList.addAll((Collection<? extends String>) node.getMetadata().get(AssetEnrichmentEnums.flaggedBy.name()));
			}
			if (null != flags && (!flags.isEmpty())) {
				node.getMetadata().put(AssetEnrichmentEnums.flags.name(), flags);
				flaggedByList.add(AssetEnrichmentEnums.Ekstep.name());
				node.getMetadata().put(AssetEnrichmentEnums.flaggedBy.name(), flaggedByList);
				node.getMetadata().put(AssetEnrichmentEnums.versionKey.name(), node.getMetadata().get(AssetEnrichmentEnums.versionKey.name()));
				node.getMetadata().put(AssetEnrichmentEnums.status.name(), AssetEnrichmentEnums.Flagged.name());
				node.getMetadata().put(AssetEnrichmentEnums.lastFlaggedOn.name(), new Date().toString());
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
			String videoUrl = (String) node.getMetadata().get(AssetEnrichmentEnums.artifactUrl.name());
			if(StringUtils.isBlank(videoUrl)) {
				LOGGER.info("Content artifactUrl is blank.");
				throw new ClientException(AssetEnrichmentEnums.PROCESSING_ERROR.name(), "Content artifactUrl is blank.");
			}
			processVideo(node, tempFolder, videoUrl);
			pushStreamingUrlRequest (node,videoUrl);
		}catch(Exception e) {
			LOGGER.info("Something Went Wrong While Performing Asset Enrichment operation. | [Content Id: " +
					node.getIdentifier() + "]", e.getMessage());
			node.getMetadata().put(AssetEnrichmentEnums.processingError.name(), e.getMessage());
			node.getMetadata().put(AssetEnrichmentEnums.status.name(), AssetEnrichmentEnums.Failed.name());
			Response res = util.updateNode(node);
			if(checkError(res))
				throw new ServerException(AssetEnrichmentEnums.PROCESSING_ERROR.name(), "Error! While Updating the Metadata | [Content Id: " +
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

	private void processVideo(Node node, String tempFolder, String videoUrl) throws Exception {
		if (StringUtils.equalsIgnoreCase("video/x-youtube", (String) node.getMetadata().get(AssetEnrichmentEnums.mimeType.name()))) {
			Map<String, Object> data = YouTubeUrlUtil.getVideoInfo(videoUrl, "snippet,contentDetails", "thumbnail", "duration");
			if (MapUtils.isNotEmpty(data)) {
				if (data.containsKey("thumbnail"))
					node.getMetadata().put("thumbnail", (String) data.get("thumbnail"));
				if (data.containsKey("duration"))
					node.getMetadata().put("duration", data.get("duration"));
			}
		} else {
			File videoFile = HttpDownloadUtility.downloadFile(videoUrl, tempFolder);
			OptimizerUtil.videoEnrichment(node, tempFolder, videoFile);
		}
		node.getMetadata().put(AssetEnrichmentEnums.status.name(), AssetEnrichmentEnums.Live.name());
		Response res = util.updateNode(node);
		if (checkError(res)) {
			LOGGER.info("Error response during asset update to Live status : " + res.getParams().getErr() + " :: " + res.getParams().getErrmsg() + " :: " + res.getResult());
			throw new ServerException(AssetEnrichmentEnums.PROCESSING_ERROR.name(),
					"Error! While Updating the Metadata | [Content Id: " + node.getIdentifier() + "]");
		}

	}

	public void pushStreamingUrlRequest(Node node, String videoUrl) {
        List<String> streamableMimeType = Platform.config.hasPath("stream.mime.type") ?
                Arrays.asList(Platform.config.getString("stream.mime.type").split(",")) : Arrays.asList("video/mp4");
        if (streamableMimeType.contains((String) node.getMetadata().get(AssetEnrichmentEnums.mimeType.name()))) {
            streamJobRequest.insert(node.getIdentifier(), videoUrl,
                    (String) node.getMetadata().get(AssetEnrichmentEnums.channel.name()), "1.0");
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