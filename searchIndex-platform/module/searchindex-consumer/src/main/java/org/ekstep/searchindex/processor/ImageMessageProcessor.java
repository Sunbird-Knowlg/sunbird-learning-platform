package org.ekstep.searchindex.processor;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.util.HttpDownloadUtility;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.searchindex.util.OptimizerUtil;
import org.ekstep.searchindex.util.*;
import com.ilimi.graph.dac.model.Node;

/**
 * The Class ImageMessageProcessor is a kafka consumer which 
 * provides implementations of the core Asset
 * upload operations defined in the IMessageProcessor along with the methods to
 * implement image tagging and flagging from Google Vision API
 * 
 * @author Rashmi
 * 
 * @see IMessageProcessor
 */
public class ImageMessageProcessor implements IMessageProcessor {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ImageMessageProcessor.class.getName());

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
			LOGGER.info("Reading from kafka consumer" + messageData);
			Map<String, Object> message = new HashMap<String, Object>();
			if (StringUtils.isNotBlank(messageData)) {
				LOGGER.debug("checking if kafka message is blank or not" + messageData);
				message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
				});
			}
			if (null != message) {
				processMessage(message);
			}
		} catch (Exception e) {
			LOGGER.error("Error while processing kafka message", e);
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
		
		Map<String, Object> edata = new HashMap<String,Object>();
		Map<String, Object> eks = new HashMap<String,Object>();
		
		LOGGER.info("processing kafka message" + message);
		if(null != message.get("edata")){
			LOGGER.info("checking if kafka message contains edata or not" + message.get("edata"));
			edata = (Map) message.get("edata");
			if(null != edata.get("eks")){
				LOGGER.debug("checking if edata has eks present in it" + eks);
				eks = (Map) edata.get("eks");
				if(null != eks){
					LOGGER.info("checking if node contains contentType as Asset and mediaType as image");
					if(null != eks.get("contentType") && null != eks.get("mediaType")){
						if ((StringUtils.equalsIgnoreCase(eks.get("contentType").toString(), "Asset"))
							&& (StringUtils.equalsIgnoreCase(eks.get("mediaType").toString(), "image"))) {

							LOGGER.info("Calling image optimiser to get optimized image resolutions");
							Map<String, String> variantsMap;
							try {
								variantsMap = OptimizerUtil.optimiseImage(eks.get("cid").toString());
								LOGGER.debug("optimized images returned from optimizer util" + variantsMap);
				
								if(null == variantsMap)
									variantsMap = new HashMap<String,String>();
								if (StringUtils.isBlank(variantsMap.get("medium"))) {
									LOGGER.debug("Checking if variantsMap contains medium resolution image", variantsMap);
									variantsMap.put("medium", edata.get("downloadUrl").toString());
									LOGGER.debug("adding image from node metadata if medium resolution image is empty", variantsMap);
								}
								String image_url = variantsMap.get("medium");
								LOGGER.info("calling processImage to initiate Google Vision Service");
								processImage(image_url, variantsMap, eks);
							} catch (Exception e) {
								LOGGER.error("Error while optimizing the images", e);
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * The processImage method holds the logic to update the node with variantsMap
	 * tags and flags to the asset node
	 * 
	 * @param image_url
	 * @param variantsMap
	 * @param eks
	 */
	@SuppressWarnings("static-access")
	private void processImage(String image_url, Map<String, String> variantsMap, Map<String, Object> eks) {

		Node node = OptimizerUtil.controllerUtil.getNode("domain", eks.get("cid").toString());
		LOGGER.info("Getting Node from graphDB based on assetId", node);
		try {
			util.loadProperties("consumer-config.properties");
			String key = util.getProperty("google.vision.tagging.enabled");
			if("true".equalsIgnoreCase(key)){ 
				
				Node data = callVisionService(image_url, node, variantsMap);
				
				LOGGER.info("Adding image variants to node", variantsMap);
				data.getMetadata().put(ContentAPIParams.variants.name(), variantsMap);

				OptimizerUtil.controllerUtil.updateNode(data);
				LOGGER.info("Updating the node after setting all required metadata", data);
			}
			else {
				
				LOGGER.info("Setting node status to Live");
				node.getMetadata().put(ContentAPIParams.status.name(), "Live");
				
				LOGGER.info("Adding image variants to node", variantsMap);
				node.getMetadata().put(ContentAPIParams.variants.name(), variantsMap);

				OptimizerUtil.controllerUtil.updateNode(node);
				LOGGER.info("Updating the node after setting all required metadata", node);
			}
		} catch (Exception e) {
			LOGGER.error("Error while updating the content node", e);
			e.printStackTrace();
		}
	}

	/**
	 * The method callVisionService holds the logic to call the google vision API
	 * get labels and flags for a given image and update the same on the node
	 * 
	 * @param image
	 * @param node
	 * @param variantsMap
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Node callVisionService(String image, Node node, Map<String, String> variantsMap) {

		LOGGER.info("Downloading the medium resolution image", image);
		File file = HttpDownloadUtility.downloadFile(image, tempFileLocation);

		Map<String, Object> labels = new HashMap<String, Object>();

		List<String> flags = new ArrayList<String>();

		LOGGER.info("Initilizing the Vision API");
		VisionApi vision;
		try {
			vision = new VisionApi(VisionApi.getVisionService());

			labels = vision.getTags(file, vision);
			LOGGER.info("Getting labels from Vision API", labels);

			flags = vision.getFlags(file, vision);
			LOGGER.info("Getting flags from Vision API", flags);

		} catch (IOException | GeneralSecurityException e) {
			LOGGER.error("Vision API returns security exception", e);
			e.printStackTrace();
		}
		List<String> keywords = new ArrayList<String>();
		if(null != node.getMetadata().get("keywords")){
			keywords = (List)node.getMetadata().get("keywords");
		}
		try {
			if(!labels.isEmpty()){
				for (Entry<String, Object> entry : labels.entrySet()) {
					List<String> list = (List) entry.getValue();
					if (null != list && (!list.isEmpty()))
						keywords.addAll(list);
				}
			}
			if(!keywords.isEmpty()){
				
				LOGGER.info("Updating node with the keywords", keywords);
				node.getMetadata().put("keywords", keywords);
				
				LOGGER.info("Setting node status to Live");
				node.getMetadata().put(ContentAPIParams.status.name(), "Live");
				
			}
			
			LOGGER.info("Checking for flaggedByList from the node");
			List<String> flaggedByList = new ArrayList<>();
			if (null != node.getMetadata().get("flaggedBy")) {
				flaggedByList.addAll((Collection<? extends String>) node.getMetadata().get("flaggedBy"));
			}
			
			LOGGER.info("Checking for Flags returned from Vision API is empty or not", flags);
			if (null != flags && (!flags.isEmpty())) {
				LOGGER.debug("setting Flags in node metadata", flags);
				node.getMetadata().put("flags", flags);
				flaggedByList.add("Ekstep");
				node.getMetadata().put("flaggedBy", flaggedByList);
				node.getMetadata().put("versionKey", node.getMetadata().get("versionKey"));
				node.getMetadata().put(ContentAPIParams.status.name(), "Flagged");
				node.getMetadata().put("lastFlaggedOn", new Date().toString());
			}
			LOGGER.info("Node updated with keywords and flags from vision API", node);
		} catch (Exception e) {
			LOGGER.info("error while setting node metadata", e);
			e.printStackTrace();
		}
		return node;
	}
}
