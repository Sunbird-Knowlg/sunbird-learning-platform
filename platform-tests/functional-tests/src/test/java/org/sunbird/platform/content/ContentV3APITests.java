package org.sunbird.platform.content;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.sunbird.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

/**
 * Functional Test Cases for Content V3 API's
 *
 * @author Kumar Gauraw
 */
public class ContentV3APITests extends BaseTest {

    @Test
    public void testGetPreSignedUrlWithInvalidFileNameExpect400() {
        String createVideoContentReq = "{\"request\": {\"content\": {\"name\": \"Test Video Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"video/mp4\",\"contentType\":\"Resource\"}}}";
        String identifier = createContent(createVideoContentReq);
        String preSignedReq = "{\"request\": {\"content\": {\"fileName\": \" .mp4\"}}}";

        setURI();
        given().
                spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                body(preSignedReq).
                with().
                contentType(JSON).
                when().
                post("content/v3/upload/url/" + identifier).
                then().spec(get400ResponseSpec());
    }

    @Test
    public void testGetPreSignedUrlWithInvalidLongFileNameExpect400() {
        String createVideoContentReq = "{\"request\": {\"content\": {\"name\": \"Test Video Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"video/mp4\",\"contentType\":\"Resource\"}}}";
        String identifier = createContent(createVideoContentReq);
        String preSignedReq = "{\"request\": {\"content\": {\"fileName\": \"tIdPRlKzpJlti4NXsNJItjqPJB4iHJOx9mEOQphThOoPc2x6BbBF9lRPKcWk7ORteqwytBwVoOLLrFYi3fMaoUsUOBEiaz4c89I6Y3OfGtFAKXAO7eFXVXNrRLlwFnDp11wHvSmtqbNTKlycU3CELbfAXvbojuXVdDBi4W0EnSF0cMzpVeiL0ISPCPTVMiFpLabIbKlyvOiEB1taJdeGTcgGqaJdGGp2WVpnZOV56qqtPOvSg5kwB5naZ2qoQ0I4X.mp4\"}}}";

        setURI();
        given().
                spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                body(preSignedReq).
                with().
                contentType(JSON).
                when().
                post("content/v3/upload/url/" + identifier).
                then().spec(get400ResponseSpec());
    }

    @Test
    public void testGetPreSignedUrlWithValidFileNameExpect200() {
        String createVideoContentReq = "{\"request\": {\"content\": {\"name\": \"Test Video Resource Content\",\"code\": \"test.res.1\",\"mimeType\": \"video/mp4\",\"contentType\":\"Resource\"}}}";
        String identifier = createContent(createVideoContentReq);
        String preSignedReq = "{\"request\": {\"content\": {\"fileName\": \"test 123.mp4\"}}}";
        String preSignedUrl = getPreSignedUrl(identifier, preSignedReq);
        Assert.assertNotNull(preSignedUrl);
    }


    /**
     * @param request
     * @return
     */
    private String createContent(String request) {
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

    private String getPreSignedUrl(String contentId, String request) {
        String url;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                        body(request).
                        with().
                        contentType(JSON).
                        when().
                        post("content/v3/upload/url/" + contentId).
                        then().//log().all().
                        extract().
                        response();
        JsonPath jsonResponse = response.jsonPath();
        url = jsonResponse.get("result.pre_signed_url");
        return url;
    }
}
