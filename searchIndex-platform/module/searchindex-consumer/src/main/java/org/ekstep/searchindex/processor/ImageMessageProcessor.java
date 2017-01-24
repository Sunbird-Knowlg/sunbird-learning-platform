package org.ekstep.searchindex.processor;

import java.io.File;
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
import org.ekstep.learning.util.BaseLearningManager;
import org.ekstep.searchindex.util.ConsumerUtil;
import org.ekstep.searchindex.util.OptimizerUtil;
import org.ekstep.visionApi.VisionApi;

import com.ilimi.graph.dac.model.Node;

import apoc.date.Date;

public class ImageMessageProcessor implements IMessageProcessor {

	/** The logger. */
	@SuppressWarnings("unused")
	private static Logger LOGGER = LogManager.getLogger(ImageMessageProcessor.class.getName());
	
	String pattern = "yyyy-MM-dd";
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern); 
	
	/** The Constant tempFileLocation. */
	private static final String tempFileLocation = "/data/contentBundle/";
	
	@SuppressWarnings("unused")
	private ConsumerUtil consumerUtil = new ConsumerUtil();
	private ObjectMapper mapper = new ObjectMapper();
	@SuppressWarnings("unused")
	private BaseLearningManager mgr = new BaseLearningManager() {};
	
	private OptimizerUtil util = new OptimizerUtil();
	
	public ImageMessageProcessor() {
		super();
	}

	@Override
	public void processMessage(String messageData) {
		try {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		Map<String,Object> edata = (Map) message.get("edata");
		Map<String,Object> eks = (Map) edata.get("eks");
		Map<String, String> variantsMap = util.optimiseImage(eks.get("cid").toString());
		if(variantsMap.get("medium").isEmpty()){
			variantsMap.put("medium", eks.get("downloadUrl").toString());
		}
		String medium = variantsMap.get("medium");
		File file = HttpDownloadUtility.downloadFile(medium, tempFileLocation);
		VisionApi vision = new VisionApi(VisionApi.getVisionService());
		
		Map<String,Object> labels = vision.getTags(file, vision);
		System.out.println("labels" + labels);
		
		Map<String, List<String>> flags = vision.getFlags(file, vision);
		System.out.println("flags" + flags);
		
		Node node = util.controllerUtil.getNode("domain", eks.get("cid").toString());
		List<String> keywords = new ArrayList<String>();
		for (Entry<String, Object> entry : labels.entrySet()) {
			keywords.add(entry.getValue().toString());
		}
		node.getMetadata().put("keywords", keywords);
		node.getMetadata().put(ContentAPIParams.variants.name(), variantsMap);
		node.getMetadata().put(ContentAPIParams.status.name(), "Live");
		
		for(Entry<String, List<String>> entry : flags.entrySet()){
			if(StringUtils.equalsIgnoreCase(entry.getKey(), "Likely")){
				node.getMetadata().put("flaggedBy", "Ekstep");
				node.getMetadata().put("versionKey", node.getMetadata().get("versionKey"));
				node.getMetadata().put(ContentAPIParams.status.name(), "Flagged");
				node.getMetadata().put("lastFlaggedOn", simpleDateFormat.toPattern());
				node.getMetadata().put("flagList", entry.getValue());
			}
			else if(StringUtils.equalsIgnoreCase(entry.getKey(), "Very_Likely")){
				node.getMetadata().put("flaggedBy", "Ekstep");
				node.getMetadata().put("versionKey", node.getMetadata().get("versionKey"));
				node.getMetadata().put(ContentAPIParams.status.name(), "Flagged");
				node.getMetadata().put("lastFlaggedOn", simpleDateFormat.toPattern());
				node.getMetadata().put("flagList", entry.getValue());
			}
			else if(StringUtils.equalsIgnoreCase(entry.getKey(), "Possible")){
				node.getMetadata().put("flaggedBy", "Ekstep");
				node.getMetadata().put("versionKey", node.getMetadata().get("versionKey"));
				node.getMetadata().put(ContentAPIParams.status.name(), "Flagged");
				node.getMetadata().put("lastFlaggedOn", simpleDateFormat.toPattern());	
				node.getMetadata().put("flagList", entry.getValue());
			}
		}
		util.controllerUtil.updateNode(node);

	}
}
