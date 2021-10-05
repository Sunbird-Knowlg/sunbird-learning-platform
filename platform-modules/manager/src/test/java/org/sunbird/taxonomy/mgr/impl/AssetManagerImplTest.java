package org.sunbird.taxonomy.mgr.impl;

import org.sunbird.common.Platform;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.taxonomy.enums.AssetParams;
import org.sunbird.taxonomy.mgr.IAssetManager;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Unit Test Cases for {@link AssetManagerImpl#licenseValidate(Map)} and {@link AssetManagerImpl#metadataRead(Map)}
 *
 * @see AssetManagerImpl
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class AssetManagerImplTest {

    private final String learningValidLicensesProperty = "learning.valid-license";
    private List<String> validLicenses = Platform.config.hasPath(learningValidLicensesProperty) ? Platform.config.getStringList(learningValidLicensesProperty) : Arrays.asList("creativeCommon");

    private final String VALID_LICENSE = "validLicense";
    private final String LICENSE = "license";
    private final String VALUE = "value";
    private final String VALID = "valid";

    private enum ErrMsg {
        INVALID_PROVIDER("Invalid Provider"),
        SPECIFY_PROVIDER("Please specify provider"),
        SPECIFY_URL("Please specify url"),
        SPECIFY_VALID_YOUTUBE_URL("Please Provide Valid YouTube URL!");
        private String value;

        ErrMsg(String value) { this.value = value; }

        public String value() { return value; }
    }

    private enum ResponseCode {
        CLIENT_ERROR(400), OK(200);

        private int code;

        ResponseCode(int code) { this.code = code; }

        public int code() { return code; }
    }

    @Autowired
    private IAssetManager assetManager;

    @Test
    public void validateLicenseCreativeCommonsYouTubeLicense() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), AssetParams.youtube.name());
        asset.put(AssetParams.url.name(), "https://www.youtube.com/watch?v=NpnsqOCkhIs");

        Response response = assetManager.urlValidate(asset, "license");

        assertEquals(ResponseCode.OK.code(), response.getResponseCode().code());
        Map<String, Object> license = (Map<String, Object>)response.getResult().get(LICENSE);
        assertTrue( (boolean)license.get(VALID) );
        assertTrue(validLicenses.contains((String)license.get(VALUE)));
    }

    @Test
    public void validateLicenseNonCreativeCommonsYouTubeLicense() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), AssetParams.youtube.name());
        asset.put(AssetParams.url.name(), "https://www.youtube.com/watch?v=nA1Aqp0sPQo");

        Response response = assetManager.urlValidate(asset, "license");

        assertEquals(ResponseCode.OK.code(), response.getResponseCode().code());
        Map<String, Object> license = (Map<String, Object>)response.getResult().get(LICENSE);
        assertFalse( (boolean)license.get(VALID) );
        assertFalse(validLicenses.contains((String)license.get(VALUE)));
    }

    @Test
    public void validateLicenseNonYoutubeProvider() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), "testProvider");
        asset.put(AssetParams.url.name(), "https://www.youtube.com/watch?v=nA1Aqp0sPQo");
        try {
            assetManager.urlValidate(asset, "license");
        } catch(ClientException e) {
            assertEquals(ResponseCode.CLIENT_ERROR.code(), e.getResponseCode().code());
            assertEquals(ErrMsg.INVALID_PROVIDER.value(), e.getMessage());
        }
    }

    @Test
    public void validateLicenseWithNoProvider() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), "");
        asset.put(AssetParams.url.name(), "https://www.youtube.com/watch?v=nA1Aqp0sPQo");
        try {
            assetManager.urlValidate(asset, "license");
        } catch (ClientException e) {
            assertEquals(ResponseCode.CLIENT_ERROR.code(), e.getResponseCode().code());
            assertEquals(ErrMsg.SPECIFY_PROVIDER.value(), e.getMessage());
        }
    }

    @Test
    public void validateLicenseWithNoUrl() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), AssetParams.youtube.name());
        asset.put(AssetParams.url.name(), null);
        try {
            assetManager.urlValidate(asset, "license");
        } catch (ClientException e) {
            assertEquals(ResponseCode.CLIENT_ERROR.code(), e.getResponseCode().code());
            assertEquals(ErrMsg.SPECIFY_URL.value(), e.getMessage());
        }
    }

    @Test
    public void validateLicenseWithNotValidYoutubeUrl() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), AssetParams.youtube.name());
        asset.put(AssetParams.url.name(), "https://www.youtube.com/watch?v=nA1Aqfdsfsdfsdfp0sPQo");
        try {
            assetManager.urlValidate(asset, "license");
        } catch(ClientException e) {
            assertEquals(ResponseCode.CLIENT_ERROR.code(), e.getResponseCode().code());
            assertEquals(ErrMsg.SPECIFY_VALID_YOUTUBE_URL.value(), e.getMessage());
        }
    }

    @Test
    public void metadataReadCreativeCommonLicense() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), AssetParams.youtube.name());
        asset.put(AssetParams.url.name(), "https://www.youtube.com/watch?v=NpnsqOCkhIs");

        Response response = assetManager.metadataRead(asset);

        assertEquals(ResponseCode.OK.code(), response.getResponseCode().code());
        Map<String, Object> metadata = (Map<String, Object>)response.getResult().get(AssetParams.metadata.name());
        if (metadata != null && !metadata.isEmpty()) {
            String license = (String) metadata.get(AssetParams.license.name());
            assertTrue(validLicenses.contains(license));
        }
    }

    @Test
    public void metadataReadNonCreativeCommonLicenseVideo() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), AssetParams.youtube.name());
        asset.put(AssetParams.url.name(), "https://www.youtube.com/watch?v=nA1Aqp0sPQo");

        Response response = assetManager.metadataRead(asset);

        assertEquals(ResponseCode.OK.code(), response.getResponseCode().code());
        Map<String, Object> metadata = (Map<String, Object>)response.getResult().get(AssetParams.metadata.name());
        if (metadata != null && !metadata.isEmpty()) {
            String license = (String) metadata.get(AssetParams.license.name());
            assertFalse(validLicenses.contains(license));
        }
    }

    @Test
    public void metadataReadWithNonYoutubeProvider() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), "testProvider");
        asset.put(AssetParams.url.name(), "https://www.youtube.com/watch?v=nA1Aqp0sPQo");

        try {
            assetManager.urlValidate(asset, "license");
        } catch(ClientException e) {
            assertEquals(ResponseCode.CLIENT_ERROR.code(), e.getResponseCode().code());
            assertEquals(ErrMsg.INVALID_PROVIDER.value(), e.getMessage());
        }
    }

    @Test
    public void metadataReadWithNoProvider() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), "");
        asset.put(AssetParams.url.name(), "https://www.youtube.com/watch?v=nA1Aqp0sPQo");
        try {
            assetManager.urlValidate(asset, "license");
        } catch (ClientException e) {
            assertEquals(ResponseCode.CLIENT_ERROR.code(), e.getResponseCode().code());
            assertEquals(ErrMsg.SPECIFY_PROVIDER.value(), e.getMessage());
        }
    }

    @Test
    public void metadataReadWithNoUrl() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), AssetParams.youtube.name());
        asset.put(AssetParams.url.name(), null);
        try {
            assetManager.urlValidate(asset, "license");
        } catch (ClientException e) {
            assertEquals(ResponseCode.CLIENT_ERROR.code(), e.getResponseCode().code());
            assertEquals(ErrMsg.SPECIFY_URL.value(), e.getMessage());
        }
    }

    @Test
    public void metadataReadWithNotValidYoutubeUrl() throws Exception {
        Map<String, Object> asset = new HashMap<>();
        asset.put(AssetParams.provider.name(), AssetParams.youtube.name());
        asset.put(AssetParams.url.name(), "https://www.youtube.com/watch?v=nA1Aqfdsfsdfsdfp0sPQo");
        try {
            assetManager.urlValidate(asset, "license");
        } catch(ClientException e) {
            assertEquals(ResponseCode.CLIENT_ERROR.code(), e.getResponseCode().code());
            assertEquals(ErrMsg.SPECIFY_VALID_YOUTUBE_URL.value(), e.getMessage());
        }
    }

}