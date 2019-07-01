package org.ekstep.platform.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.ekstep.platform.domain.BaseTest;
import org.junit.Test;

import java.io.File;
import java.util.Set;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.junit.Assert.*;


public class DeleteCacheV3Tests extends BaseTest {
    private static ClassLoader classLoader = DeleteCacheV3Tests.class.getClassLoader();
    private static File path = new File(classLoader.getResource("UploadFiles/").getFile());

    @Test
    public void testCacheDelete200ResponseKeysDeleted() {
        //Create Asset Content
        int rn = generateRandomInt(0, 999999);
        String createAssetContentReq = "{\"request\":{\"content\":{\"name\":\"Test Asset\",\"code\":\"test.asset.1\",\"mimeType\":\"image/jpeg\",\"contentType\":\"Asset\",\"mediaType\":\"image\"}}}";
        String assetId = createContent(contentType, createAssetContentReq);

        //Upload Asset
        String assetUrl = uploadContent(assetId, "/edu-success.jpeg");
        delay(15000);

        //Create Content
        String createResourceContentReq = "{\"request\":{\"content\":{\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.res.1\",\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"appIcon\":\"" + assetUrl + "\"}}}";
        String identifier = createContent(contentType, createResourceContentReq);

        // Upload Content
        uploadContent(identifier, "/pdf.pdf");

        //publish content
        publishContent(identifier, null, true);
        delay(15000);
        Set keys = RedisStoreUtil.keys("*" + identifier + "*");
        System.out.println(keys);
        assertNotEquals(0, keys.size());
        JsonPath response = deleteCacheContent(identifier);
        assertEquals(response.get("responseCode"),"OK");
        if(StringUtils.equalsIgnoreCase(response.get("responseCode"),"OK")) {
            Set keysNew = RedisStoreUtil.keys("*" + identifier + "*");
            assertEquals(0, keysNew.size());
        }
    }

    @Test
    public void testRedisCacheDeleteKeysWith500Response() {
        //Create Asset Content
        int rn = generateRandomInt(0, 999999);
        String createAssetContentReq = "{\"request\":{\"content\":{\"name\":\"Test Asset\",\"code\":\"test.asset.1\",\"mimeType\":\"image/jpeg\",\"contentType\":\"Asset\",\"mediaType\":\"image\"}}}";
        String assetId = createContent(contentType, createAssetContentReq);

        //Upload Asset
        String assetUrl = uploadContent(assetId, "/edu-success.jpeg");
        delay(15000);

        //Create Content
        String createResourceContentReq = "{\"request\":{\"content\":{\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.res.1\",\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"appIcon\":\"" + assetUrl + "\"}}}";
        String identifier = createContent(contentType, createResourceContentReq);

        // Upload Content
        uploadContent(identifier, "/pdf.pdf");

        //publish content
        publishContent(identifier, null, true);
        delay(15000);
        Set keys = RedisStoreUtil.keys("*" + identifier + "*");
        assertNotEquals(0, keys.size());
        JsonPath response = deleteCacheContent("error/" + identifier);
        assertEquals(response.get("responseCode"),"SERVER_ERROR");
    }

    @Test
    public void testCacheDelete200ResponseKeysNotDeleted() {
        //Create Asset Content
        int rn = generateRandomInt(0, 999999);
        String createAssetContentReq = "{\"request\":{\"content\":{\"name\":\"Test Asset\",\"code\":\"test.asset.1\",\"mimeType\":\"image/jpeg\",\"contentType\":\"Asset\",\"mediaType\":\"image\"}}}";
        String assetId = createContent(contentType, createAssetContentReq);

        //Upload Asset
        String assetUrl = uploadContent(assetId, "/edu-success.jpeg");
        delay(15000);

        //Create Content
        String createResourceContentReq = "{\"request\":{\"content\":{\"name\":\"LP_FT_" + rn + "\",\"code\":\"test.res.1\",\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"appIcon\":\"" + assetUrl + "\"}}}";
        String identifier = createContent(contentType, createResourceContentReq);

        // Upload Content
        uploadContent(identifier, "/pdf.pdf");

        //publish content
        publishContent(identifier, null, true);
        delay(15000);
        Set keys = RedisStoreUtil.keys("*" + identifier + "*");
        System.out.println(keys);
        assertNotEquals(0, keys.size());
        JsonPath response = deleteCacheContent(identifier + "adaj092");
        assertEquals(response.get("responseCode"),"OK");
        if(StringUtils.equalsIgnoreCase(response.get("responseCode"),"OK")) {
            Set keysNew = RedisStoreUtil.keys("*" + identifier + "*");
            assertNotEquals(0, keysNew.size());
        }
    }

    /**
     *
     * @param contentType
     * @param request
     * @return
     */
    private String createContent(String contentType, String request) {
        String identifier;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                        body(request).
                        with().
                        contentType(JSON).
                        when().
                        post("content/v3/create").
                        then().//log().all().
                        extract().
                        response();
        JsonPath jsonResponse = response.jsonPath();
        identifier = jsonResponse.get("result.node_id");
        return identifier;
    }

    /**
     *
     * @param identifier
     * @return
     */
    private JsonPath deleteCacheContent(String identifier) {
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                        body("{}").
                        with().
                        contentType(JSON).
                        when().
                        delete("/system/v3/cache/delete/" + identifier).
                        then().log().all().
                        extract().
                        response();
        return response.jsonPath();
    }

    /**
     *
     * @param identifier
     * @param filePath
     * @return
     */
    private String uploadContent(String identifier, String filePath) {
        String contentUrl;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(uploadContentType, userId, APIToken)).
                        multiPart(new File(path + filePath)).
                        when().
                        post("/content/v3/upload/" + identifier)
                        .then().
                        extract().response();
        JsonPath jsonResponse = response.jsonPath();
        contentUrl = jsonResponse.get("result.content_url");
        return contentUrl;
    }



    /**
     * @param contentId
     * @param request
     * @param isAuthRequired
     */
    private void publishContent(String contentId, String request, Boolean isAuthRequired) {
        String publishContentReq = request;
        if (StringUtils.isBlank(request))
            publishContentReq = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"EkStep\",\"publishChecklist\":[\"GoodQuality\",\"CorrectConcept\"],\"publishComment\":\"OK\"}}}";
        RequestSpecification spec = getRequestSpec(isAuthRequired);
        setURI();
        given().
                spec(spec).
                body(publishContentReq).
                with().
                contentType(JSON).
                when().
                post("content/v3/publish/" + contentId).
                then().log().all().
                spec(get200ResponseSpec());
    }


}
