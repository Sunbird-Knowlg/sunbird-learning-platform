package org.ekstep.taxonomy.mgr.impl;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.*;
import org.ekstep.taxonomy.mgr.IContentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * The Class <code>ContentManagerImpl</code> is the implementation of
 * <code>IContentManager</code> for all the operation including CRUD operation
 * and High Level Operations. This implementation intern calls for
 * <code>IMimeTypeManager</code> implementation based on the
 * <code>MimeType</code>. For <code>Bundle</code> implementation it is directly
 * backed by Content Work-Flow Pipeline and other High Level implementation is
 * backed by the implementation of <code>IMimeTypeManager</code>.
 *
 * @author Azhar
 *
 * @see IContentManager
 */
@Component
public class DummyContentManagerImpl extends DummyBaseContentManager implements IContentManager {

	@Autowired private CreateManager createManager;

	@Autowired private FindManager findManager;

	@Autowired private UploadManager uploadManager;

	@Autowired private ReviewManager reviewManager;

	@Autowired private BundleManager bundleManager;

	@Autowired private PublishMgr publishManager;

	@Autowired private DialCodesManager dialCodesManager;

	@Autowired private PreSignedUrlManager preSignedUrlManager;

	@Autowired private OptimizeManager optimizeManager;

	@Autowired private CopyManager copyManager;

	@Autowired private RetireManager retireManager;

	@Autowired private AcceptFlagManager acceptFlagManager;

	@Autowired private HierarchyManager hierarchyManager;

	/*private BaseStorageService storageService;
	
	@PostConstxruct
	public void init() {
		String storageKey = Platform.config.getString("azure_storage_key");
		String storageSecret = Platform.config.getString("azure_storage_secret");
		storageService = StorageServiceFactory.getStorageService(new StorageConfig("azure", storageKey, storageSecret));
	}*/

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#upload(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(String contentId, File uploadedFile, String mimeType) {
		return this.uploadManager.upload(contentId, uploadedFile, mimeType);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#upload(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(String contentId, String fileUrl, String mimeType) {
		return this.uploadManager.upload(contentId, fileUrl, mimeType);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.ekstep.taxonomy.mgr.IContentManager#bundle(org.ekstep.common.dto.
	 * Request, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response bundle(Request request, String version) { return this.bundleManager.bundle(request, version); }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#optimize(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Response optimize(String contentId) { return this.optimizeManager.optimize(contentId); }

	@Override
	public Response preSignedURL(String contentId, String fileName, String type) {
		return this.preSignedUrlManager.get(contentId, fileName, type);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#publish(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Response publish(String contentId, Map<String, Object> requestMap) {
		return this.publishManager.publish(contentId, requestMap);
	}

	@Override
	public Response review(String contentId, Request request) throws Exception {
		return this.reviewManager.review(contentId, request);
	}

	@Override
	public Response getHierarchy(String contentId, String mode) {
        return this.hierarchyManager.get(contentId, mode);
	}

	public Response create(Map<String, Object> map, String channelId) {
		return this.createManager.create(map, channelId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response find(String contentId, String mode, List<String> fields) {
		return this.findManager.find(contentId, mode, fields);
	}

	@Override
	public Response updateAllContents(String originalId, Map<String, Object> map) throws Exception {
		return super.updateAllContents(originalId, map);
	}

	@SuppressWarnings("unchecked")
	public Response update(String contentId, Map<String, Object> map) throws Exception {
		return this.updateManager.update(contentId, map);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response updateHierarchy(Map<String, Object> data) {
		return this.hierarchyManager.update(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.taxonomy.mgr.IContentManager#linkDialCode(java.lang.String,
	 * java.util.List)
	 */
	@Override
	public Response linkDialCode(String channelId, Object reqObj) throws Exception {
		return this.dialCodesManager.link(channelId, reqObj);
	}

	@Override
	public Response copyContent(String contentId, Map<String, Object> requestMap, String mode) {
		return this.copyManager.copyContent(contentId, requestMap, mode);
	}

    /**
	 * @param contentId
	 * @return
	 */
    @Override
	public Response retire(String contentId) { return this.retireManager.retire(contentId); }
	
	/* (non-Javadoc)
	 * @see org.ekstep.taxonomy.mgr.IContentManager#acceptFlag(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Response acceptFlag(String contentId) { return this.acceptFlagManager.acceptFlag(contentId); }


    @Override
    public Response syncHierarchy(String identifier) {
        return this.hierarchyManager.sync(identifier);
    }

 	@Override
	public Response reserveDialCode(String contentId, String channelId, Map<String, Object> request) throws Exception {
		return this.dialCodesManager.reserve(contentId, channelId, request);
	}

	/**
	 * Implementation for {@link IContentManager#releaseDialcodes(String, String)}
	 *
	 * @param contentId
	 * @param channelId
	 * @return
	 * @throws Exception
	 */
	@Override
	public Response releaseDialcodes(String contentId, String channelId) throws Exception {
	    return this.dialCodesManager.release(contentId, channelId);
	}

}