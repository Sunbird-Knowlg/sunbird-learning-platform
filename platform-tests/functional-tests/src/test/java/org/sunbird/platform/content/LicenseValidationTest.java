package org.sunbird.platform.content;

import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.ResponseSpecification;
import org.sunbird.platform.domain.BaseTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

public class LicenseValidationTest extends BaseTest {

    private String createECMLContentRequestBody = "{\"request\": {\"content\": {\"identifier\":\"LP_FT-" + IDENTIFIER + "\",\"name\":\"LP_FT-" + IDENTIFIER + "\",\"mimeType\" : \"application/vnd.ekstep.ecml-archive\",\"contentType\": \"Resource\",\"name\": \"TestEcmlMediaYoutube\",\"Description\": \"Test ECML With Youtube Media\",\"code\": \"LP_FT_Code-"+ IDENTIFIER +"\"}}}";

    private final String YOUTUBE_URL = "__YOUTUBE_URL__";
    private String updateRequestBodyWithContentBody = "{\"request\":{\"content\":{\"versionKey\":\"" + VERSION_KEY + "\",\"body\":{\"theme\":{\"stage\":{\"config\":{\"_cdata\":{\"opacity\":100,\"strokeWidth\":1,\"stroke\":\"rgba(255,255,255,0)\",\"autoplay\":false,\"visible\":true,\"color\":\"#FFFFFF\",\"genieControls\":false,\"instructions\":\"\"}},\"manifest\":{\"media\":[{\"assetId\":\"4520367b-c86c-484c-8d88-1ed4d11e10dc\"},{\"assetId\":\"8290c06b-d9b2-45c4-be80-c807bc0bcb48\"}]},\"org.sunbird.video\":[{\"config\":{\"_cdata\":{\"autoplay\":true,\"controls\":false,\"muted\":false,\"visible\":true,\"url\":\"https://www.youtube.com/watch?v=NpnsqOCkhIs\"}},\"y\":\"7.9\",\"x\":\"10.97\",\"w\":\"46.34\",\"h\":\"47\",\"rotate\":\"0\",\"z-index\":\"0\",\"id\":\"4520367b-c86c-484c-8d88-1ed4d11e10dc\"},{\"config\":{\"_cdata\":{\"autoplay\":true,\"controls\":false,\"muted\":false,\"visible\":true,\"url\":\"https://drive.google.com/uc?export=download&id=1gY3Q06CqkW2DEW81p9TA87DUD4qWHSCO\"}},\"y\":\"57.08\",\"x\":\"55.95\",\"w\":\"38.42\",\"h\":\"38.97\",\"rotate\":\"0\",\"z-index\":\"1\",\"id\":\"8290c06b-d9b2-45c4-be80-c807bc0bcb48\"}],\"x\":\"0\",\"y\":\"0\",\"w\":\"100\",\"h\":\"100\",\"id\":\"ebb9ea83-b094-473b-8d26-0e4a370a9e0d\",\"rotate\":\"\"},\"manifest\":{\"media\":[{\"id\":\"8185eb7e-63d1-4ee9-a8ce-6c44473240d4\",\"plugin\":\"org.sunbird.navigation\",\"ver\":\"1.0\",\"src\":\"/content-plugins/org.sunbird.navigation-1.0/renderer/controller/navigation_ctrl.js\",\"type\":\"js\"},{\"id\":\"7d2d7ddb-ac38-4115-a445-d0725a0153b4\",\"plugin\":\"org.sunbird.navigation\",\"ver\":\"1.0\",\"src\":\"/content-plugins/org.sunbird.navigation-1.0/renderer/templates/navigation.html\",\"type\":\"js\"},{\"id\":\"org.sunbird.navigation\",\"plugin\":\"org.sunbird.navigation\",\"ver\":\"1.0\",\"src\":\"/content-plugins/org.sunbird.navigation-1.0/renderer/plugin.js\",\"type\":\"plugin\"},{\"id\":\"org.sunbird.navigation_manifest\",\"plugin\":\"org.sunbird.navigation\",\"ver\":\"1.0\",\"src\":\"/content-plugins/org.sunbird.navigation-1.0/manifest.json\",\"type\":\"json\"},{\"id\":\"7ebac830-9939-4c9e-8512-27e475440b7a\",\"plugin\":\"org.sunbird.video\",\"ver\":\"1.2\",\"src\":\"/content-plugins/org.sunbird.video-1.2/renderer/libs/video.js\",\"type\":\"js\"},{\"id\":\"535cbeab-46d0-4b32-a11a-854577dc15c6\",\"plugin\":\"org.sunbird.video\",\"ver\":\"1.2\",\"src\":\"/content-plugins/org.sunbird.video-1.2/renderer/libs/videoyoutube.js\",\"type\":\"js\"},{\"id\":\"347bb19a-2e4b-47ca-ab2b-eeeb3c81d1e4\",\"plugin\":\"org.sunbird.video\",\"ver\":\"1.2\",\"src\":\"/content-plugins/org.sunbird.video-1.2/renderer/libs/videojs.css\",\"type\":\"css\"},{\"id\":\"org.sunbird.video\",\"plugin\":\"org.sunbird.video\",\"ver\":\"1.2\",\"src\":\"/content-plugins/org.sunbird.video-1.2/renderer/videoplugin.js\",\"type\":\"plugin\"},{\"id\":\"org.sunbird.video_manifest\",\"plugin\":\"org.sunbird.video\",\"ver\":\"1.2\",\"src\":\"/content-plugins/org.sunbird.video-1.2/manifest.json\",\"type\":\"json\"},{\"id\":\"4520367b-c86c-484c-8d88-1ed4d11e10dc\",\"src\":\"" + YOUTUBE_URL + "\",\"assetId\":\"4520367b-c86c-484c-8d88-1ed4d11e10dc\",\"type\":\"youtube\"},{\"id\":\"8290c06b-d9b2-45c4-be80-c807bc0bcb48\",\"src\":\"https://drive.google.com/uc?export=download&id=1gY3Q06CqkW2DEW81p9TA87DUD4qWHSCO\",\"assetId\":\"8290c06b-d9b2-45c4-be80-c807bc0bcb48\",\"type\":\"video\"}]},\"plugin-manifest\":{\"plugin\":[{\"id\":\"org.sunbird.navigation\",\"ver\":\"1.0\",\"type\":\"plugin\",\"depends\":\"\"},{\"id\":\"org.sunbird.video\",\"ver\":\"1.2\",\"type\":\"widget\",\"depends\":\"\"}]},\"id\":\"theme\",\"version\":\"1.0\",\"startStage\":\"ebb9ea83-b094-473b-8d26-0e4a370a9e0d\",\"compatibilityVersion\":\"4\"}}}}}";

    private String publishRequestBody = "{\"request\": {\"content\" : {\"lastPublishedBy\" : \"LP_FT\"}}}";

    public ResponseSpecification get400YoutubeLicenseNotSupportedResponseSpec() {
        return new ResponseSpecBuilder().expectStatusCode(ResponseCode.CLIENT_ERROR.code()).
                expectBody("params.err", equalTo("INVALID_YOUTUBE_MEDIA")).
                expectBody("params.status", equalTo("failed")).
                expectBody("params.errmsg", equalTo("Error! License Not Supported.")).
                expectBody("responseCode", equalTo(ResponseCode.CLIENT_ERROR.name())).
                build();
    }

    @Test
    public void reviewEcmlWithYoutubeMediaInManifestWithSupportedLicense() {
        setURI();
        String createECMLContentRequestBody = this.createECMLContentRequestBody.replace(IDENTIFIER, "" + generateRandomInt(0, 999999));
        String[] result = contentV3API.createAndGetResult(createECMLContentRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];
        String versionKey = result[1];
        String youtubeUrl = "https://www.youtube.com/watch?v=NpnsqOCkhIs";
        String updateRequestBodyWithContentBody = this.updateRequestBodyWithContentBody.replace(VERSION_KEY, versionKey).replace(YOUTUBE_URL, youtubeUrl);
        contentV3API.update(identifier, updateRequestBodyWithContentBody, getRequestSpecification(contentType, userId, APIToken), get200ResponseSpec());
        Response response = contentV3API.review(identifier, "{}", getRequestSpecification(contentType, validuserId, APIToken), get200ResponseSpec());
        assertEquals(ResponseCode.OK.code(), response.statusCode());
    }

    @Test
    public void reviewEcmlWithYoutubeMediaInManifestWithUnsupportedLicense() {
        setURI();
        String createECMLContentRequestBody = this.createECMLContentRequestBody.replace(IDENTIFIER, "" + generateRandomInt(0, 999999));
        String[] result = contentV3API.createAndGetResult(createECMLContentRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];
        String versionKey = result[1];
        String youtubeUrl = "https://www.youtube.com/watch?v=gffirQukQvs";
        String updateRequestBodyWithContentBody = this.updateRequestBodyWithContentBody.replace(VERSION_KEY, versionKey).replace(YOUTUBE_URL, youtubeUrl);
        contentV3API.update(identifier, updateRequestBodyWithContentBody, getRequestSpecification(contentType, userId, APIToken), get200ResponseSpec());
        Response response = contentV3API.review(identifier, "{}", getRequestSpecification(contentType, validuserId, APIToken), get400YoutubeLicenseNotSupportedResponseSpec());
        assertEquals(ResponseCode.CLIENT_ERROR.code(), response.statusCode());
    }

    @Test
    public void publishEcmlWithYoutubeMediaInManifestWithSupportedLicense() {
        setURI();
        String createECMLContentRequestBody = this.createECMLContentRequestBody.replace(IDENTIFIER, "" + generateRandomInt(0, 999999));
        String[] result = contentV3API.createAndGetResult(createECMLContentRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];
        String versionKey = result[1];
        String youtubeUrl = "https://www.youtube.com/watch?v=NpnsqOCkhIs";
        String updateRequestBodyWithContentBody = this.updateRequestBodyWithContentBody.replace(VERSION_KEY, versionKey).replace(YOUTUBE_URL, youtubeUrl);
        contentV3API.update(identifier, updateRequestBodyWithContentBody, getRequestSpecification(contentType, userId, APIToken), get200ResponseSpec());
        Response response = contentV3API.publish(identifier, publishRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        assertEquals(ResponseCode.OK.code(), response.statusCode());
    }

    @Test
    public void publishEcmlWithYoutubeMediaInManifestWithUnsupportedLicense() {
        setURI();
        String createECMLContentRequestBody = this.createECMLContentRequestBody.replace(IDENTIFIER, "" + generateRandomInt(0, 999999));
        String[] result = contentV3API.createAndGetResult(createECMLContentRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];
        String versionKey = result[1];
        String youtubeUrl = "https://www.youtube.com/watch?v=gffirQukQvs";
        String updateRequestBodyWithContentBody = this.updateRequestBodyWithContentBody.replace(VERSION_KEY, versionKey).replace(YOUTUBE_URL, youtubeUrl);
        contentV3API.update(identifier, updateRequestBodyWithContentBody, getRequestSpecification(contentType, userId, APIToken), get200ResponseSpec());
        Response response = contentV3API.publish(identifier, publishRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get400YoutubeLicenseNotSupportedResponseSpec());
        assertEquals(ResponseCode.CLIENT_ERROR.code(), response.statusCode());
    }
}
