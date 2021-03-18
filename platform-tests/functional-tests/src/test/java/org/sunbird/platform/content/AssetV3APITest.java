package org.sunbird.platform.content;

import com.jayway.restassured.response.Response;
import org.sunbird.common.Platform;
import org.sunbird.platform.domain.BaseTest;
import org.sunbird.taxonomy.enums.AssetParams;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Functional Test Cases for Asset V3 APIs
 *
 * @see org.sunbird.taxonomy.controller.AssetV3Controller
 */
public class AssetV3APITest extends BaseTest {
    private final String learningValidLicensesProperty = "learning.valid-license";
    private List<String> validLicenses = Platform.config.hasPath(learningValidLicensesProperty) ? Platform.config.getStringList(learningValidLicensesProperty) : Arrays.asList("creativeCommon");

    private final String BASE_PATH = "/asset/v3";
    private final String VALIDATE_LICENSE_REQUEST_PATH = "/license/validate";
    private final String METADATA_READ_REQUEST_PATH = "/metadata/read";

    private final String PROVIDER = "__PROVIDER__";
    private final String URL      = "__URL__";

    private String assetMetadataRequest = "{\"request\" : {\"asset\" : {\"provider\": \"" + PROVIDER + "\",\"url\": \"" + URL + "\"}}}";

    private enum ErrMsg {
        INVALID_PROVIDER("Invalid Provider"),
        SPECIFY_PROVIDER("Please specify provider"),
        SPECIFY_URL("Please specify url"),
        SPECIFY_VALID_YOUTUBE_URL("Please Provide Valid YouTube URL!");

        private String value;

        ErrMsg(String value) { this.value = value; }

        public String value() { return value; }
    }

    private Response validateLicense(String request) {
       setURI();
       return given().
               spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
               body(request).
            with().
                contentType(JSON).
            when().
                post(BASE_PATH + VALIDATE_LICENSE_REQUEST_PATH).
            then().
                // log().all().
                // body("responseCode", equalTo("OK")).
                extract().
                response();
    }

    private Response metadataRead(String request) {
        setURI();
        return given().
                spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                body(request).
            with().
                contentType(JSON).
            when().
                post(BASE_PATH + METADATA_READ_REQUEST_PATH).
            then().
                // log().all().
                // body("responseCode", equalTo("OK")).
                extract().
                response();
    }

    @Test
    public void validateLicenseCreativeCommonsYouTubeLicense() {
        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, AssetParams.youtube.name()).
                replace(URL, "https://www.youtube.com/watch?v=NpnsqOCkhIs");

        Response response = validateLicense(assetMetadataRequest);

        assertEquals(ResponseCode.OK.code(), response.statusCode());
        assertTrue(response.jsonPath().get("result.validLicense"));
        assertTrue(validLicenses.contains(response.jsonPath().get("result.license")));
    }

    @Test
    public void validateLicenseNonCreativeCommonsYouTubeLicense() {
        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, AssetParams.youtube.name()).
                replace(URL, "https://www.youtube.com/watch?v=nA1Aqp0sPQo");

        Response response = validateLicense(assetMetadataRequest);

        assertEquals(ResponseCode.OK.code(), response.statusCode());
        assertFalse(response.jsonPath().get("result.validLicense"));
        assertFalse(validLicenses.contains(response.jsonPath().get("result.license")));
    }

    @Test
    public void validateLicenseNonYoutubeProvider() {

        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, "testProvider").
                replace(URL, "https://www.youtube.com/watch?v=nA1Aqp0sPQo");

        Response response = validateLicense(assetMetadataRequest);

        assertEquals(ResponseCode.CLIENT_ERROR.code(), response.statusCode());
        assertEquals(ErrMsg.INVALID_PROVIDER.value(), response.jsonPath().get("params.errmsg"));
    }

    @Test
    public void validateLicenseWithNoProvider() {
        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, "").
                replace(URL, "https://www.youtube.com/watch?v=nA1Aqp0sPQo");

        Response response = validateLicense(assetMetadataRequest);

        assertEquals(ResponseCode.CLIENT_ERROR.code(), response.statusCode());
        assertEquals(ErrMsg.SPECIFY_PROVIDER.value(), response.jsonPath().get("params.errmsg"));
    }

    @Test
    public void validateLicenseWithNoUrl() {
        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, AssetParams.youtube.name()).
                replace(URL, "");

        Response response = validateLicense(assetMetadataRequest);

        assertEquals(ResponseCode.CLIENT_ERROR.code(), response.statusCode());
        assertEquals(ErrMsg.SPECIFY_URL.value(), response.jsonPath().get("params.errmsg"));
    }

    @Test
    public void validateLicenseWithNotValidYoutubeUrl() {
        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, AssetParams.youtube.name()).
                replace(URL, "https://www.youtube.com/watch?v=nA1Aqfdsfsdfsdfp0sPQo");

        Response response = validateLicense(assetMetadataRequest);

        assertEquals(ResponseCode.CLIENT_ERROR.code(), response.statusCode());
        assertEquals(ErrMsg.SPECIFY_VALID_YOUTUBE_URL.value(), response.jsonPath().get("params.errmsg"));
    }

    @Test
    public void metadataReadCreativeCommonLicense() {
        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, AssetParams.youtube.name()).
                replace(URL, "https://www.youtube.com/watch?v=NpnsqOCkhIs");

        Response response = metadataRead(assetMetadataRequest);

        assertEquals(ResponseCode.OK.code(), response.statusCode());
        assertTrue(validLicenses.contains(response.jsonPath().get("result.metadata.license")));
    }

    @Test
    public void metadataReadNonCreativeCommonLicenseVideo() {
        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, AssetParams.youtube.name()).
                replace(URL, "https://www.youtube.com/watch?v=nA1Aqp0sPQo");

        Response response = metadataRead(assetMetadataRequest);

        assertEquals(ResponseCode.OK.code(), response.statusCode());
        assertFalse(validLicenses.contains(response.jsonPath().get("result.metadata.license")));
    }

    @Test
    public void metadataReadWithNonYoutubeProvider() {
        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, "testProvider").
                replace(URL, "https://www.youtube.com/watch?v=nA1Aqp0sPQo");

        Response response = metadataRead(assetMetadataRequest);

        assertEquals(ResponseCode.CLIENT_ERROR.code(), response.statusCode());
        assertEquals(ErrMsg.INVALID_PROVIDER.value(), response.jsonPath().get("params.errmsg"));
    }

    @Test
    public void metadataReadWithNoProvider() {
        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, "").
                replace(URL, "https://www.youtube.com/watch?v=nA1Aqp0sPQo");

        Response response = metadataRead(assetMetadataRequest);

        assertEquals(ResponseCode.CLIENT_ERROR.code(), response.statusCode());
        assertEquals(ErrMsg.SPECIFY_PROVIDER.value(), response.jsonPath().get("params.errmsg"));
    }

    @Test
    public void metadataReadWithNoUrl() {
        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, AssetParams.youtube.name()).
                replace(URL, "");

        Response response = metadataRead(assetMetadataRequest);

        assertEquals(ResponseCode.CLIENT_ERROR.code(), response.statusCode());
        assertEquals(ErrMsg.SPECIFY_URL.value(), response.jsonPath().get("params.errmsg"));
    }

    @Test
    public void metadataReadWithInValidYoutubeUrl() {
        String assetMetadataRequest = this.assetMetadataRequest.replace(PROVIDER, AssetParams.youtube.name()).
                replace(URL, "https://www.youtube.com/watch?v=nA1Aqfdsfsdfsdfp0sPQo");

        Response response = metadataRead(assetMetadataRequest);

        assertEquals(ResponseCode.CLIENT_ERROR.code(), response.statusCode());
        assertEquals(ErrMsg.SPECIFY_VALID_YOUTUBE_URL.value(), response.jsonPath().get("params.errmsg"));
    }
}
