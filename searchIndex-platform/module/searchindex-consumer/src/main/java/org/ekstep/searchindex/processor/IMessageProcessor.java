package org.ekstep.searchindex.processor;

import java.util.Map;

/**
 * The Interface IMessageProcessor defines the core Audit Logs operations that
 * needs to be implemented by implementing classes.
 * 
 */
public interface IMessageProcessor {

	/**
	 * The class processMessage is mainly responsible for processing the messages sent from
	 * consumers and saves the processed message to mysql DB
	 *
	 * @param MessageData
	 *            The messageData
	 */
	public void processMessage(String messageData);
	
	/**
	 * The class processMessage is mainly responsible for processing the messages sent from
	 * consumers and saves the processed message to mysql DB
	 *
	 * @param Message
	 *            The message
	 */
	public void processMessage(Map<String, Object> message) throws Exception;
}
