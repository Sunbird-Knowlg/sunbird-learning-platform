package org.ekstep.learning.actor;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.contentstore.util.ContentStoreOperations;
import org.ekstep.contentstore.util.ContentStoreParams;
import org.ekstep.contentstore.util.ContentStoreUtil;
import org.ekstep.learning.common.enums.LearningErrorCodes;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.common.mgr.BaseGraphManager;

import akka.actor.ActorRef;

/**
 * The Class ContentStoreActor, provides akka actor functionality to access the
 * content store for body and other properties
 *
 * @author rayulu
 */
public class ContentStoreActor extends BaseGraphManager {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object msg) throws Exception {
		LOGGER.log("Received Command: " + msg);
		try {
			Request request = (Request) msg;
			String operation = request.getOperation();
			if (StringUtils.equalsIgnoreCase(ContentStoreOperations.updateContentBody.name(), operation)) {
				String contentId = (String) request.get(ContentStoreParams.content_id.name());
				String body = (String) request.get(ContentStoreParams.body.name());
				ContentStoreUtil.updateContentBody(contentId, body);
				OK(sender());
			} else if (StringUtils.equalsIgnoreCase(ContentStoreOperations.getContentBody.name(), operation)) {
				String contentId = (String) request.get(ContentStoreParams.content_id.name());
				String body = ContentStoreUtil.getContentBody(contentId);
				OK(ContentStoreParams.body.name(), body, sender());
			} else if (StringUtils.equalsIgnoreCase(ContentStoreOperations.getContentProperty.name(), operation)) {
				String contentId = (String) request.get(ContentStoreParams.content_id.name());
				String property = (String) request.get(ContentStoreParams.property.name());
				String value = ContentStoreUtil.getContentProperty(contentId, property);
				OK(ContentStoreParams.value.name(), value, sender());
			} else if (StringUtils.equalsIgnoreCase(ContentStoreOperations.getContentProperties.name(), operation)) {
				String contentId = (String) request.get(ContentStoreParams.content_id.name());
				List<String> properties = (List<String>) request.get(ContentStoreParams.properties.name());
				Map<String, Object> value = ContentStoreUtil.getContentProperties(contentId, properties);
				OK(ContentStoreParams.values.name(), value, sender());
			} else if (StringUtils.equalsIgnoreCase(ContentStoreOperations.updateContentProperty.name(), operation)) {
				String contentId = (String) request.get(ContentStoreParams.content_id.name());
				String property = (String) request.get(ContentStoreParams.property.name());
				String value = (String) request.get(ContentStoreParams.value.name());
				ContentStoreUtil.updateContentProperty(contentId, property, value);
				OK(sender());
			} else if (StringUtils.equalsIgnoreCase(ContentStoreOperations.updateContentProperties.name(), operation)) {
				String contentId = (String) request.get(ContentStoreParams.content_id.name());
				Map<String, Object> map = (Map<String, Object>) request.get(ContentStoreParams.properties.name());
				ContentStoreUtil.updateContentProperties(contentId, map);
				OK(sender());
			} else {
				LOGGER.log("Unsupported operation: " + operation);
				throw new ClientException(LearningErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			LOGGER.log("Error in ContentStoreActor", e.getMessage(), e);
			handleException(e, getSender());
		}
	}

	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
		// TODO Auto-generated method stub
	}

}
