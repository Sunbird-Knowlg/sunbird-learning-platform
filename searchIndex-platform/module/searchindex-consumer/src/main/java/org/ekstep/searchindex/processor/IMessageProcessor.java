package org.ekstep.searchindex.processor;

import java.util.Map;

/**
 * The Interface IMessageProcessor defines the core kafka consumer operations that
 * needs to be implemented by implementing classes.
 * 
 */
public interface IMessageProcessor {

	/**
	 * The class processMessage is mainly responsible for processing the messages sent from
	 * consumers based on required specifications
	 *
	 * @param MessageData
	 *            The messageData
	 */
	public void processMessage(String messageData);
	
	/**
	 * The class processMessage is mainly responsible for processing the messages sent from
	 * consumers based on required specifications
	 *
	 * @param Message
	 *            The message
	 */
	public void processMessage(Map<String, Object> message) throws Exception;
}
