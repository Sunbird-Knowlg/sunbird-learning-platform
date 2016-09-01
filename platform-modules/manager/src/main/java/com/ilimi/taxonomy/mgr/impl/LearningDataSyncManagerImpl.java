package com.ilimi.taxonomy.mgr.impl;

import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.taxonomy.mgr.ICompositeSearchManager#sync(java.lang.String,
	 * java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public Response sync(String graphId, String objectType, Integer start, Integer total) {
		return syncDefinition(graphId, objectType, start, total);
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

}
