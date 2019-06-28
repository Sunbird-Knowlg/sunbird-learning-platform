package org.sunbird.integration.test;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.sunbird.test.common.Constant;
import org.sunbird.test.common.Platform;
import org.sunbird.test.common.TestActionUtil;

import java.util.HashMap;
import java.util.Map;

public class BaseCitrusTestRunner extends TestNGCitrusTestRunner{

    @Autowired
    protected TestContext testContext;

    public static final String REQUEST_FORM_DATA = "request.params";
    public static final String REQUEST_JSON = "request.json";
    public static final String RESPONSE_JSON = "response.json";

    public static final String KP_ENDPOINT = "restTestClient";
    public static final String KEYCLOAK_ENDPOINT = "keycloakTestClient";

    private static String keycloakAdminUser = Platform.config.hasPath("keycloak_admin_user") ? Platform.config.getString("keycloak_admin_user"): "";
    private static String keycloakAdminPass = Platform.config.hasPath("keycloak_admin_pass") ? Platform.config.getString("keycloak_admin_pass"): "";



    public BaseCitrusTestRunner() {}

    public void performMultipartTest(
            TestNGCitrusTestRunner runner,
            String templateDir,
            String testName,
            String requestUrl,
            String requestFile,
            Map<String, Object> requestHeaders,
            Boolean isAuthRequired,
            HttpStatus responseCode,
            String responseJson) {
        getTestCase().setName(testName);
        runner.http(
                builder ->
                        TestActionUtil.getMultipartRequestTestAction(
                                testContext,
                                builder,
                                KP_ENDPOINT,
                                templateDir,
                                testName,
                                requestUrl,
                                requestFile,
                                TestActionUtil.getHeaders(isAuthRequired, requestHeaders),
                                runner.getClass().getClassLoader()));
        runner.http(
                builder ->
                        TestActionUtil.getResponseTestAction(
                                builder, KP_ENDPOINT, templateDir, testName, responseCode, responseJson));
    }

    public void performPostTest(
            TestNGCitrusTestRunner runner,
            String templateDir,
            String testName,
            String requestUrl,
            String requestJson,
            String contentType,
            boolean isAuthRequired,
            HttpStatus responseCode,
            String responseJson) {
        getTestCase().setName(testName);
        runner.http(
                builder ->
                        TestActionUtil.getPostRequestTestAction(
                                builder,
                                KP_ENDPOINT,
                                templateDir,
                                testName,
                                requestUrl,
                                requestJson,
                                contentType,
                                TestActionUtil.getHeaders(isAuthRequired)));

        runner.http(
                builder ->
                        TestActionUtil.getResponseTestAction(
                                builder, KP_ENDPOINT, templateDir, testName, responseCode, responseJson));
    }

    public void performPatchTest(
            TestNGCitrusTestRunner runner,
            String templateDir,
            String testName,
            String requestUrl,
            String requestJson,
            String contentType,
            boolean isAuthRequired,
            HttpStatus responseCode,
            String responseJson) {
        getTestCase().setName(testName);
        runner.http(
                builder ->
                        TestActionUtil.getPatchRequestTestAction(
                                builder,
                                KP_ENDPOINT,
                                templateDir,
                                testName,
                                requestUrl,
                                requestJson,
                                contentType,
                                TestActionUtil.getHeaders(isAuthRequired)));
        runner.http(
                builder ->
                        TestActionUtil.getResponseTestAction(
                                builder, KP_ENDPOINT, templateDir, testName, responseCode, responseJson));
    }

    public void performDeleteTest(
            TestNGCitrusTestRunner runner,
            String templateDir,
            String testName,
            String requestUrl,
            String requestJson,
            String contentType,
            boolean isAuthRequired,
            HttpStatus responseCode,
            String responseJson) {
        getTestCase().setName(testName);
        runner.http(
                builder ->
                        TestActionUtil.getDeleteRequestTestAction(
                                builder,
                                KP_ENDPOINT,
                                templateDir,
                                testName,
                                requestUrl,
                                requestJson,
                                contentType,
                                TestActionUtil.getHeaders(isAuthRequired)));
        runner.http(
                builder ->
                        TestActionUtil.getResponseTestAction(
                                builder, KP_ENDPOINT, templateDir, testName, responseCode, responseJson));
    }

    public void getAuthToken(TestNGCitrusTestRunner runner, Boolean isAuthRequired) {

        if (isAuthRequired) {
            runner.http(builder -> TestActionUtil.getTokenRequestTestAction(builder, KEYCLOAK_ENDPOINT));
            runner.http(builder -> TestActionUtil.getTokenResponseTestAction(builder, KEYCLOAK_ENDPOINT));
        }
    }

    public void getAuthToken(
            TestNGCitrusTestRunner runner,
            String userName,
            String password,
            String userId,
            boolean isUserAuthRequired) {

        if (isUserAuthRequired) {
            getUserAuthToken(runner, keycloakAdminUser, keycloakAdminPass);
            updateUserRequiredLoginActionTest(runner, userId);
            getUserAuthToken(runner, userName, password);
        }
    }

    private void getUserAuthToken(TestNGCitrusTestRunner runner, String userName, String password) {
        runner.http(
                builder ->
                        TestActionUtil.getTokenRequestTestAction(
                                builder, KEYCLOAK_ENDPOINT));
        runner.http(builder -> TestActionUtil.getTokenResponseTestAction(builder, KEYCLOAK_ENDPOINT));
    }

    private void updateUserRequiredLoginActionTest(TestNGCitrusTestRunner runner, String userId) {
        String url = "/admin/realms/" + System.getenv("sunbird_sso_realm") + "/users/" + userId;
        String payLoad = "{\"requiredActions\":[]}";
        HashMap<String, Object> headers = new HashMap<>();
        headers.put(Constant.AUTHORIZATION, Constant.BEARER + "${accessToken}");
        runner.http(
                builder ->
                        TestActionUtil.getPutRequestTestAction(
                                builder, KEYCLOAK_ENDPOINT, url, headers, payLoad));
    }

    public void performGetTest(
            TestNGCitrusTestRunner runner,
            String templateDir,
            String testName,
            String requestUrl,
            Boolean isAuthRequired,
            HttpStatus responseCode,
            String responseJson) {
        getTestCase().setName(testName);
        getAuthToken(runner, isAuthRequired);
        runner.http(
                builder ->
                        TestActionUtil.performGetTest(
                                builder,
                                KP_ENDPOINT,
                                testName,
                                requestUrl,
                                TestActionUtil.getHeaders(isAuthRequired)));
        runner.http(
                builder ->
                        TestActionUtil.getResponseTestAction(
                                builder, KP_ENDPOINT, templateDir, testName, responseCode, responseJson));
    }

    public void performGetTest(
            TestNGCitrusTestRunner runner,
            String testName,
            String requestUrl,
            Boolean isAuthRequired,
            HttpStatus responseCode,
            String responseJson) {
        runner.http(
                builder ->
                        TestActionUtil.performGetTest(
                                builder,
                                KP_ENDPOINT,
                                testName,
                                requestUrl,
                                TestActionUtil.getHeaders(isAuthRequired)));
        runner.http(
                builder ->
                        TestActionUtil.getResponseTestAction(builder, KP_ENDPOINT, testName, responseCode));
    }
}
