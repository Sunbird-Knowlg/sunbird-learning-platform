package com.ilimi.graph.common;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.graph.common.exception.GraphEngineErrorCodes;
import com.ilimi.graph.common.mgr.Configuration;

public class Identifier {

	private static long environmentId = 10000000;
	private static String shardId = "1";
	private static AtomicInteger aInteger = new AtomicInteger(1); 

	static {
		try (InputStream inputStream = Configuration.class.getClassLoader().getResourceAsStream("graph.properties")) {
			if (null != inputStream) {
				Properties props = new Properties();
				props.load(inputStream);
				String envId = props.getProperty("environment.id");
				if (StringUtils.isNotBlank(envId)) {
					environmentId = Long.parseLong(envId);
				}
				String shard = props.getProperty("shard.id");
				if (StringUtils.isNotBlank(shard))
					shardId = shard;
			}
		} catch (Exception e) {
			PlatformLogger.log("Error! While Loading Graph Properties.", null, e);
		}
	}
	
	public static String getUniqueIdFromNeo4jId(long id) {
		long uid = environmentId + id;
		return uid + "" + shardId;
	}
	
	public static String getUniqueIdFromTimestamp() {
		long env = environmentId / 10000000;
		long uid = System.currentTimeMillis();
		uid = uid << 13;
		return env + "" + uid + "" + shardId + "" + aInteger.getAndIncrement();
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
