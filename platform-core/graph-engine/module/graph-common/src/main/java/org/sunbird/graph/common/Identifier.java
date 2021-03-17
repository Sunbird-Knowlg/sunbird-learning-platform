package org.sunbird.graph.common;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.exception.GraphEngineErrorCodes;

public class Identifier {

	private static long environmentId = 10000000;
	private static String shardId = "1";
	private static AtomicInteger aInteger = new AtomicInteger(1); 

	static {
			if(Platform.config.hasPath("environment.id"))
				environmentId = Platform.config.getLong("environment.id");
			if(Platform.config.hasPath("shard.id"))
				shardId = Platform.config.getString("shard.id");
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
