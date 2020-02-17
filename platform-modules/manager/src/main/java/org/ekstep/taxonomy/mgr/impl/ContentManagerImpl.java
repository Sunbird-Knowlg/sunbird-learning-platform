package org.ekstep.taxonomy.mgr.impl;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.ContentEventManager;
import org.ekstep.content.mgr.impl.ContentManger;
import org.ekstep.content.mgr.impl.ContentPluginManager;
import org.ekstep.content.mgr.impl.DialCodesManager;
import org.ekstep.content.mgr.impl.HierarchyManager;
import org.ekstep.content.mgr.impl.UploadManager;
import org.ekstep.taxonomy.mgr.IContentManager;
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
public class ContentManagerImpl extends BaseContentManager implements IContentManager {

	private final HierarchyManager hierarchyManager = new HierarchyManager();
	private final DialCodesManager dialCodesManager = new DialCodesManager();
	private final ContentManger contentManger = new ContentManger();
	private final UploadManager uploadManager = new UploadManager();
	private final ContentEventManager contentEventManager = new ContentEventManager();
	private final ContentPluginManager contentPluginManager = new ContentPluginManager();

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
	@Override
	public Response bundle(Request request, String version) { return this.contentPluginManager.bundle(request, version); }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#optimize(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Response optimize(String contentId) { return this.contentPluginManager.optimize(contentId); }

	@Override
	public Response preSignedURL(String contentId, String fileName, String type, Boolean idValReq) {
		return this.contentPluginManager.preSignedUrl(contentId, fileName, type, idValReq);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ekstep.taxonomy.mgr.IContentManager#publish(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Response publish(String contentId, Map<String, Object> requestMap) {
		return this.contentEventManager.publish(contentId, requestMap);
	}

	@Override
	public Response review(String contentId, Request request) throws Exception {
		return this.contentEventManager.review(contentId, request);
	}

	public Response create(Map<String, Object> map, String channelId) throws Exception {
		return this.contentManger.create(map, channelId);
	}

	@Override
	public Response find(String contentId, String mode, List<String> fields) {
		return this.contentManger.find(contentId, mode, fields);
	}

	@Override
	public Response updateAllContents(String originalId, Map<String, Object> map) throws Exception {
		return this.contentManger.updateAllContents(originalId, map);
	}

	@Override
	public Response update(String contentId, Map<String, Object> map) throws Exception {
		return this.contentManger.update(contentId, map);
	}

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
	public Response linkDialCode(String channelId, Object reqObj, String mode, String contentId) throws Exception {
		return this.dialCodesManager.link(channelId, reqObj, mode, contentId);
	}

	@Override
	public Response copyContent(String contentId, Map<String, Object> requestMap, String mode) {
		return this.contentPluginManager.copyContent(contentId, requestMap, mode);
	}

    /**
	 * @param contentId
	 * @return
	 */
    @Override
	public Response retire(String contentId) { return this.contentManger.retire(contentId); }
	
	/* (non-Javadoc)
	 * @see org.ekstep.taxonomy.mgr.IContentManager#acceptFlag(java.lang.String)
	 */
	@Override
	public Response acceptFlag(String contentId) { return this.contentEventManager.acceptFlag(contentId); }

	@Override
	public Response flag(String contentId, Map<String, Object> requestMap) throws Exception {
		return this.contentEventManager.flag(contentId, requestMap);
	}

	@Override
	public Response rejectFlag(String contentId) throws Exception {
		return this.contentEventManager.rejectFlag(contentId);
	}

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

    @Override
    public Response getContentHierarchy(String contentId, String bookMarkId, String mode, List<String> fields) throws
			Exception {
        return this.hierarchyManager.getContentHierarchy(contentId, bookMarkId, mode, fields);
    }

	/**
	 * Implementation for {@link IContentManager#discardContent(String)}
	 * @param contentId
	 * @return
	 * @throws Exception
	 */
    @Override
	public Response discardContent(String contentId) throws Exception {
		return this.contentManger.discard(contentId);
	}
	/**
	 * Implementation for {@link IContentManager#rejectContent(String, Map)}
	 * @param contentId
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	@Override
	public Response rejectContent(String contentId, Map<String, Object> requestMap) throws Exception {
		return this.contentManger.reject(contentId, requestMap);
	}

}