package org.sunbird.platform.test.common.util;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.sunbird.common.Platform;
import org.sunbird.platform.test.common.TestConstant;

import static com.jayway.restassured.RestAssured.baseURI;
import static com.jayway.restassured.RestAssured.given;

/**
 * Handles User Authentication Token.
 * @author Kumar Gauraw
 */
public class AuthTokenUtil {

    public static String getAuthToken() {
        String userName = Platform.config.getString("kp_sso_username");
        String userPassword = Platform.config.getString("kp_sso_password");
        String keyClockUrl = Platform.config.getString("kp_sso_url");
        String clientId = Platform.config.getString("kp_sso_client_id");
        String realme = Platform.config.getString("kp_sso_realm");

        baseURI = keyClockUrl;
        String requestUrl = "/realms/" + realme + "/protocol/openid-connect/token";
        String request = "client_id="
                + clientId
                + "&username="
                + userName
                + "&password="
                + userPassword
                + "&grant_type=password";
        Response response =
                        given().
                        spec(new RequestSpecBuilder().addHeader(TestConstant.CONTENT_TYPE, TestConstant.CONTENT_TYPE_FORM_URL_ENCODED).build()).
                        body(request).
                        when().
                        post(requestUrl).
                        then().log().all().
                        extract().
                        response();
        JsonPath jsonResponse = response.jsonPath();
        return jsonResponse.get("access_token");
    }
}
