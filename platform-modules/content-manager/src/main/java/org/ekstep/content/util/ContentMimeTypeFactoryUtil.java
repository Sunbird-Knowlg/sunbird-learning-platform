package org.ekstep.content.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.content.mimetype.mgr.impl.APKMimeTypeMgrImpl;
import org.ekstep.content.mimetype.mgr.impl.AssetsMimeTypeMgrImpl;
import org.ekstep.content.mimetype.mgr.impl.CollectionMimeTypeMgrImpl;
import org.ekstep.content.mimetype.mgr.impl.DocumentMimeTypeManager;
import org.ekstep.content.mimetype.mgr.impl.ECMLMimeTypeMgrImpl;
import org.ekstep.content.mimetype.mgr.impl.HTMLMimeTypeMgrImpl;
import org.ekstep.content.mimetype.mgr.impl.PluginMimeTypeMgrImpl;
import org.ekstep.content.mimetype.mgr.impl.YoutubeMimeTypeManager;

import com.ilimi.common.dto.CoverageIgnore;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;

public class ContentMimeTypeFactoryUtil {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ContentMimeTypeFactoryUtil.class.getName());

	static IMimeTypeManager ecmlMimeTypeMgr = new ECMLMimeTypeMgrImpl();
	static IMimeTypeManager htmlMimeTypeMgr = new HTMLMimeTypeMgrImpl();
	static IMimeTypeManager apkMimeTypeMgr = new APKMimeTypeMgrImpl();
	static IMimeTypeManager collectionMimeTypeMgr = new CollectionMimeTypeMgrImpl();
	static IMimeTypeManager assetsMimeTypeMgr = new AssetsMimeTypeMgrImpl();
	static IMimeTypeManager pluginMimeTypeMgrImpl = new PluginMimeTypeMgrImpl();
    static IMimeTypeManager youtubeMimeTypeMgr = new YoutubeMimeTypeManager();
    static IMimeTypeManager documentMimeTypeMgr = new DocumentMimeTypeManager();
    
	@CoverageIgnore
    public static IMimeTypeManager getImplForService(String mimeType) {
		LOGGER.debug("MimeType: " + mimeType);
    	IMimeTypeManager manager = assetsMimeTypeMgr;
    	switch (StringUtils.lowerCase(mimeType)) {
			case "application/vnd.ekstep.ecml-archive":
				manager = ecmlMimeTypeMgr;
				break;
			case "application/vnd.ekstep.html-archive":
				manager = htmlMimeTypeMgr;
				break;
			case "application/vnd.android.package-archive":
				manager = apkMimeTypeMgr;
				break;
			case "application/vnd.ekstep.content-collection":
				manager = collectionMimeTypeMgr;
				break;
			case "assets":
				manager = assetsMimeTypeMgr;
				break;
			case "application/vnd.ekstep.plugin-archive":
				manager = pluginMimeTypeMgrImpl;
				break;
			case "video/youtube":
				manager = youtubeMimeTypeMgr;
				break;
			case "application/pdf":
				manager = documentMimeTypeMgr;
				break;
			case "application/msword":
				manager = documentMimeTypeMgr;
				break;
			default:
				manager = assetsMimeTypeMgr;
				break;
		}
       return manager;
    }
}
