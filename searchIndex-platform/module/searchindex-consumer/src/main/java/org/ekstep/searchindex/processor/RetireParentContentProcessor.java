package org.ekstep.searchindex.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.GraphUtil;

import com.ilimi.common.logger.LogHelper;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

// TODO: Auto-generated Javadoc
/**
 * The Class RetireParentContentProcessor.
 *
 * @author karthik
 */
public class RetireParentContentProcessor implements IMessageProcessor {

	/** The logger. */
	private static LogHelper LOGGER = LogHelper.getInstance(FlagParentContentProcessor.class.getName());

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
			LOGGER.info("Reading from kafka consumer" + messageData);
			Map<String, Object> message = new HashMap<String, Object>();
			if (StringUtils.isNotBlank(messageData)) {
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
	 * @see
	 * org.ekstep.searchindex.processor.IMessageProcessor#processMessage(java.
	 * util.Map)
	 */
	@Override
	public void processMessage(Map<String, Object> message) throws Exception {
		Map<String, Object> edata = new HashMap<String, Object>();
		Map<String, Object> eks = new HashMap<String, Object>();
		System.out.println(message.toString());
		LOGGER.info("processing kafka message" + message);
		if (null != message.get("edata")) {
			edata = (Map) message.get("edata");
			if (null != edata.get("eks")) {
				eks = (Map) edata.get("eks");
				if (null != eks) {
					String contentId = (String) eks.get("cid");
					String status = (String) eks.get("state");
					if (null != contentId && null != status) {
						if (StringUtils.equalsIgnoreCase(status, "Retired")) {
							LOGGER.info("content id - " + contentId + " status - " + status + " ");
							try {
								Map<String, Object> nodeMap = GraphUtil.getDataNode("domain", contentId);
								Node node = mapper.convertValue(nodeMap, Node.class);
								for (Relation relation : node.getInRelations())
									if (relation.getRelationType().equalsIgnoreCase("hasSequenceMember")) {
										String parentContentId = relation.getStartNodeId();
										//update parent node status to Draft
										String objectType = "Content";
										Map<String, Object> metadata = new HashMap<>();
										metadata.put("status", "Draft");
										GraphUtil.updateDataNode("domain", parentContentId, objectType, metadata);
									}
							} catch (Exception e) {
								LOGGER.error("Error while checking content node ", e);
								e.printStackTrace();
							}

						}
					}
				}
			}
		}
	}

	
}
