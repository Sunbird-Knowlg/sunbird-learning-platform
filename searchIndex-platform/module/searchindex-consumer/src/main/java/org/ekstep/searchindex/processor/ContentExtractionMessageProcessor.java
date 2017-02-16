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

public class ContentExtractionMessageProcessor  implements IMessageProcessor {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ImageMessageProcessor.class.getName());

	/** The Constant tempFileLocation. */
	private static final String tempFileLocation = "/data/contentBundle/";

	/** The ObjectMapper */
	private static ObjectMapper mapper = new ObjectMapper();

	/** The Properties Util */
	private static PropertiesUtil util = new PropertiesUtil();
	
	/** The constructor */
	public ContentExtractionMessageProcessor() {
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
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		Map<String, Object> edata = new HashMap<String,Object>();
		Map<String, Object> eks = new HashMap<String,Object>();
		
		LOGGER.info("processing kafka message" + message);
		if(null != message.get("edata")){
			LOGGER.info("checking if kafka message contains edata or not" + message.get("edata"));
			edata = (Map) message.get("edata");
			if(null != edata.get("eks")){
				LOGGER.info("checking if edata has eks present in it" + eks);
				eks = (Map) edata.get("eks");
				if(null != eks){
					System.out.println(eks);
					Node node = OptimizerUtil.controllerUtil.getNode("domain", eks.get("cid").toString());
					System.out.println(node);
				}
			}
		}
	}

}
