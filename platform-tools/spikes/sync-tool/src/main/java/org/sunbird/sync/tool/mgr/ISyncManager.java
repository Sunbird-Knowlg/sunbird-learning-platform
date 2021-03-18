package org.sunbird.sync.tool.mgr;

import java.util.List;

public interface ISyncManager {

	public void syncByIds(String graphId, List<String> identifiers) throws Exception;

	public void syncByFile(String graphId, String filePath, String fileType) throws Exception;

	public void syncByObjectType(String graphId, String objectType) throws Exception;

	public void syncByDateRange(String graphId, String startDate, String endDate, String objectType) throws Exception;
	
	public void syncGraph(String graphId, Integer delay, String[] objectType) throws Exception;
}
