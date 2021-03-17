package org.sunbird.taxonomy.mgr.impl;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.mgr.CompositeIndexSyncManager;
import org.sunbird.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.searchindex.util.CompositeSearchConstants;
import org.sunbird.taxonomy.mgr.ICompositeSearchManager;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Component;

/**
 * The Class LearningDataSyncManagerImpl provides implementations of the sync
 * operations defined in ICompositeSearchManager.
 * 
 * @author Rayulu
 * 
 */
@Component
public class LearningDataSyncManagerImpl extends CompositeIndexSyncManager implements ICompositeSearchManager {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sunbird.taxonomy.mgr.ICompositeSearchManager#sync(java.lang.String,
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
			BoolQueryBuilder query = QueryBuilders.boolQuery();
			query.must(QueryBuilders.matchQuery("objectType.raw", objectType));
			query.must(QueryBuilders.matchQuery("graph_id.raw", graphId));
			ElasticSearchUtil.deleteDocumentsByQuery(query, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
					CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sunbird.taxonomy.mgr.ICompositeSearchManager#syncObject(java.lang.
	 * String, java.lang.String[])
	 */
	@Override
	public Response syncObject(String graphId, String[] identifiers) {
		return syncNode(graphId, identifiers);
	}

	@PreDestroy
	public void shutdown() {
		ElasticSearchUtil.cleanESClient();
	}
}
