package com.ilimi.taxonomy.mgr;

import java.util.List;
import java.util.Map;

import com.ilimi.common.dto.Response;

public interface IAssetCleanUpManager {
	
	Response getObjectsOnS3(List<String> folders);
	
	Response synchGraphNodestoES(Map<String, Object> requestMap);
	
	Response searchElasticSearch(Map<String, Object> requestMap);
	
	Response deleteUnusedFilesOnS3();
	
}
