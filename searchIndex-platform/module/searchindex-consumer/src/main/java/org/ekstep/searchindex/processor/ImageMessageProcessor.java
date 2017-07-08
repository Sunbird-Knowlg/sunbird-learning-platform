package org.ekstep.searchindex.processor;

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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.searchindex.util.OptimizerUtil;
import org.ekstep.searchindex.util.*;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class ImageMessageProcessor is a kafka consumer which provides
 * implementations of the core Asset upload operations defined in the
 * IMessageProcessor along with the methods to implement image tagging and
 * flagging from Google Vision API
 * 
 * @author Rashmi
 * 
 * @see IMessageProcessor
 */
public class ImageMessageProcessor implements IMessageProcessor {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The Constant tempFileLocation. */
	private static final String tempFileLocation = "/data/contentBundle/";

	/** The ObjectMapper */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The Properties Util */
	private static PropertiesUtil util = new PropertiesUtil();

	/** The constructor */
	public ImageMessageProcessor() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public void processMessage(String messageData) {
		try {
			LOGGER.log("Reading from kafka consumer" + messageData);
			Map<String, Object> message = new HashMap<String, Object>();
			if (StringUtils.isNotBlank(messageData)) {
				LOGGER.log("checking if kafka message is blank or not");
				message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
				});
			}
			if (null != message) {
				String eid = (String) message.get("eid");
				if (StringUtils.isNotBlank(eid) && StringUtils.equals("BE_CONTENT_LIFECYCLE", eid))
					processMessage(message);
			}
		} catch (Exception e) {
			LOGGER.log("Error while processing kafka message", e.getMessage(), e);
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.searchindex.processor #processMessage(java.lang.String
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void processMessage(Map<String, Object> message) {

		Map<String, Object> edata = new HashMap<String, Object>();
		Map<String, Object> eks = new HashMap<String, Object>();

		LOGGER.log("processing kafka message" + message);
		if (null != message.get("edata")) {
			LOGGER.log("checking if kafka message contains edata or not" + message.get("edata"));
			edata = (Map) message.get("edata");
			if (null != edata.get("eks")) {
				LOGGER.log("checking if edata has eks present in it" + eks);
				eks = (Map) edata.get("eks");
				if (null != eks) {
					LOGGER.log("checking if node contains contentType as Asset and mediaType as image");
					if (null != eks.get("contentType") && null != eks.get("mediaType")) {
						if ((StringUtils.equalsIgnoreCase(eks.get("contentType").toString(), "Asset"))
								&& (StringUtils.equalsIgnoreCase(eks.get("mediaType").toString(), "image"))) {

							LOGGER.log("Calling image optimiser to get optimized image resolutions");
							Map<String, String> variantsMap;
							try {
								variantsMap = OptimizerUtil.optimiseImage(eks.get("cid").toString());
								LOGGER.log("optimized images returned from optimizer util" , variantsMap.size());

								if (null == variantsMap)
									variantsMap = new HashMap<String, String>();
								if (StringUtils.isBlank(variantsMap.get("medium"))) {
									LOGGER.log("Checking if variantsMap contains medium resolution image",
											variantsMap);
									variantsMap.put("medium", edata.get("downloadUrl").toString());
									LOGGER.log("adding image from node metadata if medium resolution image is empty",
											variantsMap);
								}
								String image_url = variantsMap.get("medium");
								LOGGER.log("calling processImage to initiate Google Vision Service");
								processImage(image_url, variantsMap, eks);
							} catch (Exception e) {
								LOGGER.log("Error while optimizing the images", e.getMessage(), e);
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * The processImage method holds the logic to update the node with
	 * variantsMap tags and flags to the asset node
	 * 
	 * @param image_url
	 * @param variantsMap
	 * @param eks
	 */
	@SuppressWarnings("static-access")
	private void processImage(String image_url, Map<String, String> variantsMap, Map<String, Object> eks) {

		Node node = OptimizerUtil.controllerUtil.getNode("domain", eks.get("cid").toString());
		LOGGER.log("Getting Node from graphDB based on assetId", node);
		try {
			util.loadProperties("consumer-config.properties");
			String key = util.getProperty("google.vision.tagging.enabled");
			if ("true".equalsIgnoreCase(key)) {

				Node data = callVisionService(image_url, node, variantsMap);

				LOGGER.log("Adding image variants to node", variantsMap);
				data.getMetadata().put(ContentAPIParams.variants.name(), variantsMap);

				OptimizerUtil.controllerUtil.updateNode(data);
				LOGGER.log("Updating the node after setting all required metadata", data);
			} else {

				LOGGER.log("Setting node status to Live");
				node.getMetadata().put(ContentAPIParams.status.name(), "Live");

				LOGGER.log("Adding image variants to node", variantsMap);
				node.getMetadata().put(ContentAPIParams.variants.name(), variantsMap);

				OptimizerUtil.controllerUtil.updateNode(node);
				LOGGER.log("Updating the node after setting all required metadata", node);
			}
		} catch (Exception e) {
			LOGGER.log("Error while updating the content node", e.getMessage(), e);
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

		LOGGER.log("Downloading the medium resolution image", image);
		File file = HttpDownloadUtility.downloadFile(image, tempFileLocation);

		Map<String, Object> labels = new HashMap<String, Object>();

		List<String> flags = new ArrayList<String>();

		LOGGER.log("Initilizing the Vision API");
		VisionApi vision;
		try {
			vision = new VisionApi(VisionApi.getVisionService());

			labels = vision.getTags(file, vision);
			LOGGER.log("Getting labels from Vision API", labels);

			flags = vision.getFlags(file, vision);
			LOGGER.log("Getting flags from Vision API", flags);

		} catch (IOException | GeneralSecurityException e) {
			LOGGER.log("Vision API returns security exception", e.getMessage(), e);
		}
		
		try {
			List<String> node_keywords = new ArrayList<String>();
			if (null != node.getMetadata().get("keywords")) {
				Object object = node.getMetadata().get("keywords");

				LOGGER.log("checking if object is instanceof string[]");
				if (object instanceof String[]) {
					String[] stringArray = (String[]) node.getMetadata().get("keywords");
					LOGGER.log("converting string array to list" + stringArray);
					List keywords = Arrays.asList(stringArray);
					node_keywords = setKeywords(keywords, labels);
				}
				
				LOGGER.log("checking if object is instanceof string");
				if (object instanceof String) {
					String keyword = (String) node.getMetadata().get("keywords");
					LOGGER.log("keyword fetched from node" + keyword);
					node_keywords.add(keyword);
					node_keywords = setKeywords(node_keywords, labels);
				}
			}
			
			LOGGER.log("checking if keywords list is empty" + node_keywords);
			if (!node_keywords.isEmpty()) {
				LOGGER.log("Updating node with the keywords", node_keywords);
				node.getMetadata().put("keywords", node_keywords);
			}

			LOGGER.log("Setting node status to Live");
			node.getMetadata().put(ContentAPIParams.status.name(), "Live");

			LOGGER.log("Checking for flaggedByList from the node");
			List<String> flaggedByList = new ArrayList<>();
			if (null != node.getMetadata().get("flaggedBy")) {
				flaggedByList.addAll((Collection<? extends String>) node.getMetadata().get("flaggedBy"));
			}

			LOGGER.log("Checking for Flags returned from Vision API is empty or not", flags);
			if (null != flags && (!flags.isEmpty())) {
				LOGGER.log("setting Flags in node metadata", flags);
				node.getMetadata().put("flags", flags);
				flaggedByList.add("Ekstep");
				node.getMetadata().put("flaggedBy", flaggedByList);
				node.getMetadata().put("versionKey", node.getMetadata().get("versionKey"));
				node.getMetadata().put(ContentAPIParams.status.name(), "Flagged");
				node.getMetadata().put("lastFlaggedOn", new Date().toString());
			}
			LOGGER.log("Node updated with keywords and flags from vision API", node);
		} catch (Exception e) {
			LOGGER.log("error while setting node metadata", e);
		}
		return node;
	}

	/**
	 * This method holds logic to set keywords from Vision API with existing keywords 
	 * from the node
	 * 
	 * @param keywords
	 * 		The keywords 
	 * 
	 * @param labels
	 * 		The labels
	 * 
	 * @return List of keywords
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<String> setKeywords(List<String> keywords, Map<String, Object> labels) {

		LOGGER.log("checking if labels are empty");
		if (null != labels && !labels.isEmpty()) {
			
			LOGGER.log("iterating through labels map");
			for (Entry<String, Object> entry : labels.entrySet()) {
				
				LOGGER.log("getting list of label values" + entry.getValue());
				List<String> list = (List) entry.getValue();
				if (null != list && (!list.isEmpty())) {
					
					for (String key : list) {
						
						LOGGER.log("checking if key is already present in keywords list" + key);
						if (!keywords.contains(key)) {
							LOGGER.log("Adding labels to keywords list" + list);
							keywords.addAll(list);
						}
					}
				}
			}
		}
		return keywords;
	}
}
