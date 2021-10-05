package org.sunbird.taxonomy.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.mgr.IURLManager;
import org.sunbird.common.mgr.impl.GeneralUrlManagerImpl;
import org.sunbird.common.mgr.impl.GoogleDriveUrlManagerImpl;
import org.sunbird.common.mgr.impl.YoutubeUrlManagerImpl;
import org.sunbird.taxonomy.enums.AssetParams;
import org.sunbird.taxonomy.mgr.IAssetManager;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;


/**
 * The Class <code>AssetManagerImpl</code> is the implementation of
 *  <code>IAssetManager</code> for all asset related operations.
 *
 * @see IAssetManager
 */
@Component
public class AssetManagerImpl implements IAssetManager {

	private final IURLManager youtubeUrlManager = new YoutubeUrlManagerImpl();
	private final IURLManager googleDriveUrlManager = new GoogleDriveUrlManagerImpl();
	private final IURLManager generalUrlManager = new GeneralUrlManagerImpl();
	/**
     * Validate License
     *
     * @param asset
     * @param field
     * @return
     * @throws Exception
     */
   @Override
    public Response urlValidate(Map<String, Object> asset, String field) {
	   IURLManager urlManager = getURLManager(getProvider(asset));
	   Map<String, Object> fieldMap = urlManager.validateURL(getUrl(asset), field);
	   Response response = new Response();
       response.getResult().put(field, fieldMap);
       return response;
    }
   
    /**
     * Read Url Metadata.
     *
     * @param asset
     * @return
     */
   @Override
   public Response metadataRead(Map<String, Object> asset) {
	   IURLManager urlManager = getURLManager(getProvider(asset));
       String url = getUrl(asset);
       Map<String, Object> metadata = urlManager.readMetadata(url);
       Response response = new Response();
       response.getResult().put("metadata", metadata);
       return response;
   }
    
   private String getProvider(Map<String, Object> asset) {
	   String provider = (String) asset.get(AssetParams.provider.name());
	   return Optional.ofNullable(provider).filter(StringUtils::isNotBlank).
			   orElseThrow(() -> new ClientException(ResponseCode.CLIENT_ERROR.name(), "Please specify provider"));
	   
   }

	private String getUrl(Map<String, Object> asset) {
		String url = (String) asset.get(AssetParams.url.name());
	    return Optional.ofNullable(url).filter(StringUtils::isNotBlank).
	            orElseThrow(() -> new ClientException(ResponseCode.CLIENT_ERROR.name(), "Please specify url"));
	}
	
	private IURLManager getURLManager(String provider) {
	   switch (StringUtils.lowerCase(provider)) {
	       case "youtube":
	    	   	return youtubeUrlManager;
	       case "googledrive":
	    	   return googleDriveUrlManager;
	    	   case "other":
	    		   return generalUrlManager;
	       default:
	 		throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid Provider");
	   }
	}
}
