package com.ilimi.graph.common;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.graph.common.exception.GraphEngineErrorCodes;
import com.ilimi.graph.common.mgr.Configuration;

public class Identifier {
	
	private static LogHelper LOGGER = LogHelper.getInstance(Identifier.class.getName());

	private static long environmentId = 10000000;

	static {
		try (InputStream inputStream = Configuration.class.getClassLoader().getResourceAsStream("graph.properties")) {
			if (null != inputStream) {
				Properties props = new Properties();
				props.load(inputStream);
				String envId = props.getProperty("environment.id");
				if (StringUtils.isNotBlank(envId)) {
					environmentId = Long.parseLong(envId);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error! While Loading Graph Properties.", e);
		}
	}
	
	public static String getIdentifier(String graphId, long id) {
		long uid = environmentId + id;
		return getIdentifier(graphId, uid + "");
	}

	public static String getIdentifier(String graphId, String id) {
		if (StringUtils.isBlank(graphId))
			throw new ServerException(GraphEngineErrorCodes.ERR_INVALID_GRAPH_ID.name(),
					"Graph Id is required to generate an identifier");
		String prefix = "";
		if (graphId.length() >= 2)
			prefix = graphId.substring(0, 2);
		else
			prefix = graphId;
		String identifier = prefix + "_" + id;
		return identifier;
	}
}
