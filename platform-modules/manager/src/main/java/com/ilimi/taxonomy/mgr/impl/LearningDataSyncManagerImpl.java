package com.ilimi.taxonomy.mgr.impl;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.springframework.stereotype.Component;

import com.ilimi.common.mgr.CompositeIndexSyncManager;
import com.ilimi.taxonomy.mgr.ICompositeSearchManager;

/**
 * The Class LearningDataSyncManagerImpl provides implementations of the sync
 * operations defined in ICompositeSearchManager.
 * 
 * @author Rayulu
 * 
 */
@Component
public class LearningDataSyncManagerImpl extends CompositeIndexSyncManager implements ICompositeSearchManager {

	private ElasticSearchUtil util = new ElasticSearchUtil();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.ICompositeSearchManager#sync(java.lang.String,
	 * java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public Response sync(String graphId, String objectType, Integer start, Integer total, boolean delete) throws Exception {
		if (delete)
			deleteIndexData(graphId, objectType);
		return syncDefinition(graphId, objectType, start, total);
	}

	private void deleteIndexData(String graphId, String objectType) throws Exception {
		if (StringUtils.isNotBlank(objectType) && StringUtils.isNotBlank(graphId)) {
			String query = "{\"query\": { \"bool\" : { \"must\" : [{\"match\": { \"objectType.raw\": \"" + objectType
					+ "\"}}, {\"match\": { \"graph_id.raw\": \"" + graphId + "\"}}]}}}";
			util.deleteDocumentsByQuery(query, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
					CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.taxonomy.mgr.ICompositeSearchManager#syncObject(java.lang.
	 * String, java.lang.String[])
	 */
	@Override
	public Response syncObject(String graphId, String[] identifiers) {
		return syncNode(graphId, identifiers);
	}

	@PreDestroy
	public void shutdown() {
		if (null != util)
			util.finalize();
	}
}
