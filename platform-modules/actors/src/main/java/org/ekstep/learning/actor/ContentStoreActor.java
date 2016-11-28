package org.ekstep.learning.actor;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.contentstore.util.ContentStoreOperations;
import org.ekstep.contentstore.util.ContentStoreParams;
import org.ekstep.contentstore.util.ContentStoreUtil;
import org.ekstep.learning.common.enums.LearningErrorCodes;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
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
	private static Logger LOGGER = LogManager.getLogger(ContentStoreActor.class.getName());

	@Override
	public void onReceive(Object msg) throws Exception {
		LOGGER.info("Received Command: " + msg);
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
			} else {
				LOGGER.info("Unsupported operation: " + operation);
				throw new ClientException(LearningErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			LOGGER.error("Error in ContentStoreActor", e);
			handleException(e, getSender());
		}
	}

	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
		// TODO Auto-generated method stub
	}

}
