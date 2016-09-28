package com.ilimi.taxonomy.content;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.ilimi.taxonomy.mgr.IMimeTypeManager;

@Component
public class ContentMimeTypeFactory {
	@Autowired @Qualifier("ECMLMimeTypeMgrImpl") IMimeTypeManager ECMLMimeTypeMgr;
    @Autowired @Qualifier("HTMLMimeTypeMgrImpl") IMimeTypeManager HTMLMimeTypeMgr;
    @Autowired @Qualifier("APKMimeTypeMgrImpl") IMimeTypeManager APKMimeTypeMgr;
    @Autowired @Qualifier("CollectionMimeTypeMgrImpl") IMimeTypeManager CollectionMimeTypeMgr;
    @Autowired @Qualifier("AssetsMimeTypeMgrImpl") IMimeTypeManager AssetsMimeTypeMgr;
    
    /**
     * Gets the impl for service.
     *
     * @param mimeType the mime type
     * @return the impl for service
     */
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
			default:
				manager = AssetsMimeTypeMgr;
				break;
		}
       return manager;
    }
}

