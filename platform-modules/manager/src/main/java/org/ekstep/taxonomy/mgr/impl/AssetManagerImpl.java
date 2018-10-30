package org.ekstep.taxonomy.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.taxonomy.enums.AssetParams;
import org.ekstep.taxonomy.mgr.IAssetManager;
import org.ekstep.taxonomy.util.YouTubeDataAPIV3Service;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * The Class <code>AssetManagerImpl</code> is the implementation of
 *  <code>IAssetManager</code> for all asset related operations.
 *
 * @see IAssetManager
 */
@Service
public class AssetManagerImpl implements IAssetManager {

    private List<String> validLicenses;

    @PostConstruct
    public void init() {
        validLicenses = Platform.config.hasPath("learning.valid-license") ? Platform.config.getStringList("learning.valid-license") : Arrays.asList("creativeCommon");
    }

    private String getProvider(Map<String, Object> asset) throws Exception {
        String provider = (String) asset.get(AssetParams.provider.name());
        if(null == provider || StringUtils.isBlank(provider))
            throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Please specify provider");
        return provider;
    }

    private String getUrl(Map<String, Object> asset) throws Exception {
        String url = (String) asset.get(AssetParams.url.name());
        if(null == url || StringUtils.isBlank(url))
            throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Please specify url");
        return url;
    }

    private String getLicenseType(Map<String, Object> asset) throws Exception {
        String licenseType;
        switch (StringUtils.lowerCase(getProvider(asset))) {
            case "youtube": licenseType = YouTubeDataAPIV3Service.getLicense(getUrl(asset));
                            break;
            default       : throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid Provider");
        }
        return licenseType;
    }

    /**
     * Validate License
     *
     * @param asset
     * @return
     * @throws Exception
     */
    @Override
    public Response licenseValidate(Map<String, Object> asset) throws Exception {
        String licenseType = getLicenseType(asset);
        boolean validLicense = validLicenses.contains(licenseType);
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
    public Response metadataRead(Map<String, Object> asset) throws Exception {
        String licenseType = getLicenseType(asset);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(AssetParams.license.name(), licenseType);
        Response response = new Response();
        response.getResult().put(AssetParams.metadata.name(), metadata);
        return response;
    }
}
