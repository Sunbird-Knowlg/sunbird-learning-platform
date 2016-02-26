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
    public IMimeTypeManager getImplForService(String mimeType){
    	IMimeTypeManager manager = ECMLMimeTypeMgr;
    	switch (StringUtils.lowerCase(mimeType)) {
			case "application/octet-stream":
				manager = ECMLMimeTypeMgr;
				break;
			case "html":
				manager = HTMLMimeTypeMgr;
				break;
			case "apk":
				manager = APKMimeTypeMgr;
				break;
			case "collection":
				manager = CollectionMimeTypeMgr;
				break;
			case "assets":
				manager = AssetsMimeTypeMgr;
				break;
			default:
				manager = ECMLMimeTypeMgr;
				break;
		}
       return manager;
    }
}

