package org.ekstep.graph.service.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class GraphUtil.
 * 
 * @author Mohammad Azharuddin
 */
public class GraphUtil {
	
	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(GraphUtil.class.getName());
	
	/**
	 * Gets the graph id.
	 *
	 * @return the graph id
	 */
	public static String getGraphId() {
		String graphId = "domain";
		// TODO: Write the Logic to return environment aware Graph Id i.e. different for Test, and Production environment.  
		LOGGER.info("Returning Graph Id: " + graphId);
		
		return graphId;
	}

}
