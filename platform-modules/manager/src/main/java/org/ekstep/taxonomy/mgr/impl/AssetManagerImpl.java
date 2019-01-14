package org.ekstep.taxonomy.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.util.YouTubeDataAPIV3Service;
import org.ekstep.taxonomy.enums.AssetParams;
import org.ekstep.taxonomy.mgr.IAssetManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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
        return urlValidate(getProvider(asset), getUrl(asset), field);
    }
    
    /**
     * Read Url Metadata.
     *
     * @param asset
     * @return
     * @throws Exception
     */
   @Override
   public Response metadataRead(Map<String, Object> asset) {
       String provider = getProvider(asset);
       String url = getUrl(asset);
       switch (StringUtils.lowerCase(provider)) {
       case "youtube":
    	   	return getMetadata(url);
       case "googledrive":
    	   throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid Provider");
       default:
 		throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid Provider");
       }
   }
   
    
    public static Response getMetadata(String url) {
		String licenseType = YouTubeDataAPIV3Service.getLicense(url);
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("license", licenseType);
        Response response = new Response();
        response.getResult().put("metadata", metadata);
        return response;
                  
    }
    
    private Response urlValidate(String provider, String url, String field) {
		switch (StringUtils.lowerCase(provider)) {
			case "youtube":
				return validateURL(url, field);
			case "googledrive":
				throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid Provider");
			default:
    	 		throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid Provider");
		}
	}
    
    private static Response validateURL(String url, String field) {
		Map<String, Object> fieldMap = new HashMap<>();
		if(StringUtils.equalsIgnoreCase("license", field)) {
			String licenseType = YouTubeDataAPIV3Service.getLicense(url);
	        boolean validLicense = YouTubeDataAPIV3Service.isValidLicense(licenseType);
	        fieldMap.put("value", licenseType);
	        fieldMap.put("valid", validLicense);
		}else {
			throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Passed field is not supported for validation.");
		}
		Response response = new Response();
        response.getResult().put(field, fieldMap);
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
}
