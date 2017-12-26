package org.ekstep.graph.service.common;

import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.exception.ClientException;

public class GraphMap {

	private static Map<String, String> validGraph = new HashMap<String, String>();

	static {
		// TODO: Populate Valid Graph Map from 'ConfigurationNode' with a MetaData value as 'validGraphs'
		// as following structured JSON
		// {
		// 	  "domain": ["1", "2", "3","4"],
		// 	  "hi": ["2", "3", "4", "5"],
		// 	  "en": ["2", "3", "4", "5"],
		// 	  "ta": ["2", "3", "4", "5"],
		// 	  "te": ["2", "3", "4", "5"],
		// 	  "ka": ["2", "3", "4", "5"],
		// 	  "as": ["2", "3", "4", "5"],
		// 	  "bo": ["2", "3", "4", "5"],
		// 	  "be": ["2", "3", "4", "5"],
		// 	  "gu": ["2", "3", "4", "5"]
		// }
		validGraph.put(DACParams.domain.name(), "");
		validGraph.put(DACParams.hi.name(), "");
		validGraph.put(DACParams.en.name(), "");
		validGraph.put(DACParams.te.name(), "");
		validGraph.put(DACParams.ka.name(), "");
		validGraph.put(DACParams.ta.name(), "");
		validGraph.put(DACParams.as.name(), "");
		validGraph.put(DACParams.be.name(), "");
		validGraph.put(DACParams.bo.name(), "");
		validGraph.put(DACParams.gu.name(), "");
	}
	
	public static boolean isValidGraph(String graphId) {
		return validGraph.containsKey(graphId);
	}
	
	public static String getGraphEndpoint(String graphId) {
		String url = "";
		if (!validGraph.containsKey(url))
			throw new ClientException(DACErrorCodeConstants.INVALID_GRAPH.name(), "");
		return url;
	}

}
