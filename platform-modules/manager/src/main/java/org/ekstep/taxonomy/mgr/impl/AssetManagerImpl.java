package org.ekstep.taxonomy.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.taxonomy.enums.AssetParams;
import org.ekstep.taxonomy.mgr.IAssetManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.ekstep.common.util.AssetUtil.getLicenseType;
import static org.ekstep.common.util.AssetUtil.isValidLicense;

/**
 * The Class <code>AssetManagerImpl</code> is the implementation of
 *  <code>IAssetManager</code> for all asset related operations.
 *
 * @see IAssetManager
 */
@Component
public class AssetManagerImpl implements IAssetManager {

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

    private Map<String, Object> getMetadata(Map<String, Object> asset) {
        Map<String, Object> metadata = new HashMap<>();
        String provider = getProvider(asset);
        String url = getUrl(asset);
        switch (StringUtils.lowerCase(provider)) {
            case "youtube": String licenseType = getLicenseType(provider, url);
                            metadata.put(AssetParams.license.name(), licenseType);
                            break;
            default       : throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid Provider");
        }
        return metadata;
    }

    /**
     * Validate License
     *
     * @param asset
     * @return
     * @throws Exception
     */
    @Override
    public Response licenseValidate(Map<String, Object> asset) {
        String provider = getProvider(asset);
        String url = getUrl(asset);
        String licenseType = getLicenseType(provider, url);
        boolean validLicense = isValidLicense(licenseType);
        Response response = new Response();
        response.getResult().put(AssetParams.validLicense.name(), validLicense);
        response.getResult().put(AssetParams.license.name(), licenseType);
        return response;
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
        Map<String, Object> metadata = getMetadata(asset);
        Response response = new Response();
        response.getResult().put(AssetParams.metadata.name(), metadata);
        return response;
    }
}
