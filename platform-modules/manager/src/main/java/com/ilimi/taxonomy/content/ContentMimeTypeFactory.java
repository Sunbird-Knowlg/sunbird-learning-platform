package com.ilimi.taxonomy.content;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.mimetype.mgr.impl.APKMimeTypeMgrImpl;
import org.ekstep.content.mimetype.mgr.impl.AssetsMimeTypeMgrImpl;
import org.ekstep.content.mimetype.mgr.impl.CollectionMimeTypeMgrImpl;
import org.ekstep.content.mimetype.mgr.impl.ECMLMimeTypeMgrImpl;
import org.ekstep.content.mimetype.mgr.impl.HTMLMimeTypeMgrImpl;
import org.ekstep.content.mimetype.mgr.impl.PluginMimeTypeMgrImpl;

import com.ilimi.common.dto.CoverageIgnore;

/**
 * A factory for creating ContentMimeType objects.
 * 
 * @author Mohammad Azharuddin
 */
@Deprecated
public class ContentMimeTypeFactory {
	
	/** The ECML mime type mgr. */
	IMimeTypeManager ECMLMimeTypeMgr = new ECMLMimeTypeMgrImpl();
    
    /** The HTML mime type mgr. */
    IMimeTypeManager HTMLMimeTypeMgr =  new HTMLMimeTypeMgrImpl();
    
    /** The APK mime type mgr. */
    IMimeTypeManager APKMimeTypeMgr = new APKMimeTypeMgrImpl();
    
    /** The Collection mime type mgr. */
    IMimeTypeManager CollectionMimeTypeMgr = new CollectionMimeTypeMgrImpl();
    
    /** The Assets mime type mgr. */
    IMimeTypeManager AssetsMimeTypeMgr = new AssetsMimeTypeMgrImpl();
    
    /** The Plugin mime type mgr impl. */
    IMimeTypeManager PluginMimeTypeMgrImpl =  new PluginMimeTypeMgrImpl();
    
    /**
     * Gets the impl for service.
     *
     * @param mimeType the mime type
     * @return the impl for service
     */
    @CoverageIgnore
    public IMimeTypeManager getImplForService(String mimeType){
    	IMimeTypeManager manager = AssetsMimeTypeMgr;
    	switch (StringUtils.lowerCase(mimeType)) {
			case "application/vnd.ekstep.ecml-archive":
				manager = ECMLMimeTypeMgr;
				break;
			case "application/vnd.ekstep.html-archive":
				manager = HTMLMimeTypeMgr;
				break;
			case "application/vnd.android.package-archive":
				manager = APKMimeTypeMgr;
				break;
			case "application/vnd.ekstep.content-collection":
				manager = CollectionMimeTypeMgr;
				break;
			case "assets":
				manager = AssetsMimeTypeMgr;
				break;
			case "application/vnd.ekstep.plugin-archive":
				manager = PluginMimeTypeMgrImpl;
				break;
			default:
				manager = AssetsMimeTypeMgr;
				break;
		}
       return manager;
    }
}

