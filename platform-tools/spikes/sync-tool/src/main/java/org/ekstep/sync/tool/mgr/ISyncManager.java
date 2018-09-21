package org.ekstep.sync.tool.mgr;

public interface ISyncManager {

	public void syncByIds(String graphId, String[] ids) throws Exception;

	public void syncByFile(String graphId, String filePath, String objectType) throws Exception;

	public void syncByObjectType(String graphId, String objectType) throws Exception;

	public void syncByDateRange(String graphId, String startDate, String endDate, String objectType) throws Exception;
	
	public void syncGraph(String graphId, Integer delay, String[] objectType) throws Exception;
}
