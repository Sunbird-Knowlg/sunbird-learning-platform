package org.ekstep.searchindex.processor;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.ekstep.visionApi.*;

import com.ilimi.graph.dac.model.Node;

public class ImageMessageProcessor implements IMessageProcessor {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ImageMessageProcessor.class.getName());
	
	String pattern = "yyyy-MM-dd";
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern); 
	
	/** The Constant tempFileLocation. */
	private static final String tempFileLocation = "/data/contentBundle/";
	
	/** The ObjectMapper */
	private ObjectMapper mapper = new ObjectMapper();
	
	/** The Image Optimiser Util. */
	private OptimizerUtil util = new OptimizerUtil();
	
	/** The keywords List */
	private List<String> keywords = new ArrayList<String>();
	
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
			if(StringUtils.isNotBlank(messageData)){
				message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
				});
			}
			if (null != message)
				processMessage(message);
		} catch (Exception e) {
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
		
		LOGGER.info("Reading from kafka consumer" + message);
		Map<String,Object> edata = (Map) message.get("edata");
		Map<String,Object> eks = (Map) edata.get("eks");
		
		LOGGER.info("Calling image optimiser to get optimized image resolutions");
		Map<String, String> variantsMap;
		try {
			variantsMap = util.optimiseImage(eks.get("cid").toString());
			LOGGER.debug("optimized images returned from optimizer util" + variantsMap);
		
		if(!variantsMap.isEmpty()){
			LOGGER.debug("Checking if variantsMap is empty");
			
			if(variantsMap.get("medium").isEmpty()){
				
				LOGGER.debug("Checking if variantsMap contains medium resolution image", variantsMap);
				variantsMap.put("medium", eks.get("downloadUrl").toString());
				LOGGER.debug("adding image from node metadat if medium resolution image is empty", variantsMap);
			}
		}
			String image_url = variantsMap.get("medium");
			processImage(image_url, variantsMap, eks);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void processImage(String image_url, Map<String,String> variantsMap, Map<String,Object> eks){
		LOGGER.info("Downloading the medium resolution image",image_url);
		File file = HttpDownloadUtility.downloadFile(image_url, tempFileLocation);
		
		Map<String,Object> labels = new HashMap<String, Object>();
		
		Map<String, List<String>> flags = new HashMap<String, List<String>>();
		
		LOGGER.info("Initilizing the Vision API");
		VisionApi vision;
		try {
			vision = new VisionApi(VisionApi.getVisionService());
		
			labels = vision.getTags(file, vision);
			LOGGER.info("Getting labels from Vision API", labels);
			
			flags = vision.getFlags(file, vision);
			LOGGER.info("Getting flags from Vision API", flags);
			
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
			Node node = util.controllerUtil.getNode("domain", eks.get("cid").toString());
			LOGGER.info("Getting Node from graphDB based on assetId", node);
		
		for (Entry<String, Object> entry : labels.entrySet()) {
			keywords.add(entry.getValue().toString());
		}
		for(String data : keywords){
			LOGGER.debug("Adding keywords to to node", data);
			node.getMetadata().put("keywords", data);
		}
		
		LOGGER.info("Adding image variants to node", variantsMap);
		node.getMetadata().put(ContentAPIParams.variants.name(), variantsMap);
		
		LOGGER.info("Setting node status to Live");
		node.getMetadata().put(ContentAPIParams.status.name(), "Live");
		
		LOGGER.info("Checking for Flags returned from Vision API", flags.entrySet());
		for(Entry<String, List<String>> entry : flags.entrySet()){
			LOGGER.info("Checking for different flagReasons");
			if(StringUtils.equalsIgnoreCase(entry.getKey(), "Likely")){
				node.getMetadata().put("flaggedBy", "Ekstep");
				node.getMetadata().put("versionKey", node.getMetadata().get("versionKey"));
				node.getMetadata().put(ContentAPIParams.status.name(), "Flagged");
				node.getMetadata().put("lastFlaggedOn", simpleDateFormat.toPattern());
				node.getMetadata().put("flags", entry.getValue());
			}
			else if(StringUtils.equalsIgnoreCase(entry.getKey(), "Very_Likely")){
				node.getMetadata().put("flaggedBy", "Ekstep");
				node.getMetadata().put("versionKey", node.getMetadata().get("versionKey"));
				node.getMetadata().put(ContentAPIParams.status.name(), "Flagged");
				node.getMetadata().put("lastFlaggedOn", simpleDateFormat.toPattern());
				node.getMetadata().put("flags", entry.getValue());
			}
			else if(StringUtils.equalsIgnoreCase(entry.getKey(), "Possible")){
				node.getMetadata().put("flaggedBy", "Ekstep");
				node.getMetadata().put("versionKey", node.getMetadata().get("versionKey"));
				node.getMetadata().put(ContentAPIParams.status.name(), "Flagged");
				node.getMetadata().put("lastFlaggedOn", simpleDateFormat.toPattern());	
				node.getMetadata().put("flags", entry.getValue());
			}
		}
		util.controllerUtil.updateNode(node);
		LOGGER.info("Updating the node after setting all required metadata", node);

	}
}
