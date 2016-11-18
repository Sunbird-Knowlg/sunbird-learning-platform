package com.ilimi.taxonomy.content;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.CoverageIgnore;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;

/**
 * A factory for creating ContentMimeType objects.
 * 
 * @author Mohammad Azharuddin
 */
@Component
public class ContentMimeTypeFactory {
	
	/** The ECML mime type mgr. */
	@Autowired @Qualifier("ECMLMimeTypeMgrImpl") IMimeTypeManager ECMLMimeTypeMgr;
    
    /** The HTML mime type mgr. */
    @Autowired @Qualifier("HTMLMimeTypeMgrImpl") IMimeTypeManager HTMLMimeTypeMgr;
    
    /** The APK mime type mgr. */
    @Autowired @Qualifier("APKMimeTypeMgrImpl") IMimeTypeManager APKMimeTypeMgr;
    
    /** The Collection mime type mgr. */
    @Autowired @Qualifier("CollectionMimeTypeMgrImpl") IMimeTypeManager CollectionMimeTypeMgr;
    
    /** The Assets mime type mgr. */
    @Autowired @Qualifier("AssetsMimeTypeMgrImpl") IMimeTypeManager AssetsMimeTypeMgr;
    
    /** The Plugin mime type mgr impl. */
    @Autowired @Qualifier("PluginMimeTypeMgrImpl") IMimeTypeManager PluginMimeTypeMgrImpl;
    
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

