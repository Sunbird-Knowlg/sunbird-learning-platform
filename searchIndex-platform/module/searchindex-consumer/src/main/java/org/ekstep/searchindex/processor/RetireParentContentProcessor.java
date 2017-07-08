package org.ekstep.searchindex.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.util.GraphUtil;
import org.ekstep.searchindex.util.HTTPUtil;
import org.ekstep.searchindex.util.PropertiesUtil;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

// TODO: Auto-generated Javadoc
/**
 * The Class RetireParentContentProcessor, mark all parents to Draft status if
 * the content is Retired
 *
 * @author karthik
 */
public class RetireParentContentProcessor implements IMessageProcessor {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The mapper. */
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Instantiates a new flag parent processor.
	 */
	public RetireParentContentProcessor() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.searchindex.processor.IMessageProcessor#processMessage(java.
	 * lang.String)
	 */
	@Override
	public void processMessage(String messageData) {
		try {
			LOGGER.log("Reading from kafka consumer" , messageData);
			Map<String, Object> message = new HashMap<String, Object>();
			if (StringUtils.isNotBlank(messageData)) {
				message = mapper.readValue(messageData, new TypeReference<Map<String, Object>>() {
				});
			}
			if (null != message) {
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
	 * @see
	 * org.ekstep.searchindex.processor.IMessageProcessor#processMessage(java.
	 * util.Map)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		Map<String, Object> edata = new HashMap<String, Object>();
		Map<String, Object> eks = new HashMap<String, Object>();
		LOGGER.log("processing kafka message" + message);
		if (null != message.get("edata")) {
			edata = (Map) message.get("edata");
			if (null != edata.get("eks")) {
				eks = (Map) edata.get("eks");
				if (null != eks) {
					String contentId = (String) eks.get("cid");
					String status = (String) eks.get("state");
					String prevstatus = (String) eks.get("prevstate");
					if (null != contentId && null != status) {
						if (StringUtils.equalsIgnoreCase(status, "Retired")
								|| (StringUtils.equalsIgnoreCase(status, "Draft")
										&& StringUtils.equalsIgnoreCase(prevstatus, "Live"))) {
							LOGGER.log("content id - " + contentId + " status - " + status + " ");
							try {
								Map<String, Object> nodeMap = GraphUtil.getDataNode("domain", contentId);
								Node node = mapper.convertValue(nodeMap, Node.class);
								for (Relation relation : node.getInRelations())
									if (relation.getRelationType().equalsIgnoreCase("hasSequenceMember")) {
										String parentContentId = relation.getStartNodeId();
										LOGGER.log("content id - " + contentId + " has parent - parent content id -"
												+ parentContentId
												+ ", to make status as Draft as one of the child is Retired", parentContentId);
										Map<String, Object> parentNodeMap = GraphUtil.getDataNode("domain",
												parentContentId);
										Node parentNode = mapper.convertValue(parentNodeMap, Node.class);
										String versionKey = (String) parentNode.getMetadata().get("versionKey");
										updateContent(parentContentId, "Draft", versionKey);
									}
							} catch (Exception e) {
								LOGGER.log("Error while checking content node ", e.getMessage(), e);
							}

						}
					}
				}
			}
		}
	}

	private void updateContent(String contentId, String status, String versionKey) throws Exception {
		String url = PropertiesUtil.getProperty("platform-api-url") + "/v2/content/" + contentId;

		Map<String, Object> requestBodyMap = new HashMap<String, Object>();
		Map<String, Object> requestMap = new HashMap<String, Object>();
		Map<String, Object> content = new HashMap<String, Object>();
		content.put("status", status);
		content.put("versionKey", versionKey);
		requestMap.put("content", content);
		requestBodyMap.put("request", requestMap);

		String requestBody = mapper.writeValueAsString(requestBodyMap);

		HTTPUtil.makePatchRequest(url, requestBody);
	}

}
