package org.sunbird.platform.content;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.sunbird.platform.domain.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

/**
 * Integration Test for Youtube Asset
 *
 * @author Kumar Gauraw
 */
public class YouTubeAssetTest extends BaseTest {

    int rn = generateRandomInt(0, 999999);

    @Test
    public void testYoutubeAssetWithValidVideoUrlExpect200(){
        //Create Content
        setURI();
        String youtubeAssetReq = "{\"request\": {\"content\": {\"identifier\":\"LP_FT_" + rn + "\",\"name\": \"LP_FT_Youtube_Asset\",\"code\": \"lp.ft.youtube.asset\",\"mimeType\": \"video/x-youtube\",\"contentType\":\"Asset\",\"mediaType\":\"video\"}}}";
        Response res =
                given().
                spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                body(youtubeAssetReq).
                with().
                contentType(JSON).
                when().
                post("content/v3/create").
                then().log().all().
                extract().
                response();
        JsonPath jp = res.jsonPath();
        String identifier = jp.get("result.node_id");

        //Upload Youtube Video to the Content
        setURI();
        given().
        spec(getRequestSpecification(uploadContentType, userId, APIToken)).
        multiPart("fileUrl","https://youtu.be/WM4ys_PnrUY").
        when().
        post("/content/v3/upload/" + identifier).
        then().
        log().all().
        spec(get200ResponseSpec());

        delay(10000);
        // Get content and validate
        setURI();
        Response R1 = given().
                spec(getRequestSpecification(contentType, userId, APIToken)).
                when().
                get("/content/v3/read/" + identifier).
                then().
                log().all().
                spec(get200ResponseSpec()).
                extract().response();

        JsonPath getResponse = R1.jsonPath();
        Assert.assertEquals(getResponse.get("result.content.status"), "Live");
        Assert.assertEquals(getResponse.get("result.content.license"), "Standard YouTube License");
        Assert.assertEquals(getResponse.get("result.content.thumbnail"), "https://i.ytimg.com/vi/WM4ys_PnrUY/mqdefault.jpg");
        Assert.assertEquals(getResponse.get("result.content.duration"), "1918");

    }
}
