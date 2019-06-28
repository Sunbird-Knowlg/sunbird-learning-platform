package org.sunbird.test.common;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.HttpActionBuilder;
import com.consol.citrus.dsl.builder.HttpClientRequestActionBuilder;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TestActionUtil {


  private static String api_key = Platform.config.hasPath("api_key")? Platform.config.getString("api_key"): "";
  private static String keycloakAdminUser = Platform.config.hasPath("keycloak_admin_user") ? Platform.config.getString("keycloak_admin_user"): "";
  private static String keycloakAdminPass = Platform.config.hasPath("keycloak_admin_pass") ? Platform.config.getString("keycloak_admin_pass"): "";
  private static String sunbird_sso_realm = Platform.config.hasPath("sunbird_sso_realm") ? Platform.config.getString("sunbird_sso_realm"): "";
  private static String sunbird_sso_client_id = Platform.config.hasPath("sunbird_sso_client_id") ? Platform.config.getString("sunbird_sso_client_id"): "";

  public static TestAction getTokenRequestTestAction(
      HttpActionBuilder builder, String endpointName) {
    String urlPath =
        "/realms/" + sunbird_sso_realm + "/protocol/openid-connect/token";

    return builder
        .client(endpointName)
        .send()
        .post(urlPath)
        .contentType("application/x-www-form-urlencoded")
        .payload(
            "client_id="
                + sunbird_sso_client_id
                + "&username="
                + keycloakAdminUser
                + "&password="
                + keycloakAdminPass
                + "&grant_type=password");
  }

  public static TestAction getPutRequestTestAction(
      HttpActionBuilder builder,
      String endPoint,
      String url,
      Map<String, Object> headers,
      String payLoad) {
    HttpClientRequestActionBuilder requestActionBuilder = builder.client(endPoint).send().put(url);
    addHeaders(requestActionBuilder, headers);
    requestActionBuilder.contentType(Constant.CONTENT_TYPE_APPLICATION_JSON);
    requestActionBuilder.payload(payLoad);
    return requestActionBuilder;
  }

  public static TestAction getTokenResponseTestAction(
      HttpActionBuilder builder, String endpointName) {
    return builder
        .client(endpointName)
        .receive()
        .response(HttpStatus.OK)
        .messageType(MessageType.JSON)
        .extractFromPayload("$.access_token", "accessToken");
  }

  public static TestAction getPostRequestTestAction(
      HttpActionBuilder builder,
      String endpointName,
      String testTemplateDir,
      String testName,
      String url,
      String requestFile,
      String contentType,
      Map<String, Object> headers) {

    String requestFilePath =
        MessageFormat.format("{0}/{1}/{2}", testTemplateDir, testName, requestFile);
    System.out.println("requestFilePath = " + requestFilePath);
    HttpClientRequestActionBuilder requestActionBuilder =
        builder.client(endpointName).send().post(url).messageType(MessageType.JSON);
    if (StringUtils.isNotBlank(contentType)) {
      requestActionBuilder.contentType(contentType);
    }

    requestActionBuilder = addHeaders(requestActionBuilder, headers);

    return requestActionBuilder.payload(new ClassPathResource(requestFilePath));
  }

  public static TestAction getPatchRequestTestAction(
      HttpActionBuilder builder,
      String endpointName,
      String testTemplateDir,
      String testName,
      String url,
      String requestFile,
      String contentType,
      Map<String, Object> headers) {

    String requestFilePath =
        MessageFormat.format("{0}/{1}/{2}", testTemplateDir, testName, requestFile);
    HttpClientRequestActionBuilder requestActionBuilder =
        builder.client(endpointName).send().patch(url).messageType(MessageType.JSON);
    if (StringUtils.isNotBlank(contentType)) {
      requestActionBuilder.contentType(contentType);
    }

    requestActionBuilder = addHeaders(requestActionBuilder, headers);

    return requestActionBuilder.payload(new ClassPathResource(requestFilePath));
  }

  public static TestAction getDeleteRequestTestAction(
      HttpActionBuilder builder,
      String endpointName,
      String testTemplateDir,
      String testName,
      String url,
      String requestFile,
      String contentType,
      Map<String, Object> headers) {

    HttpClientRequestActionBuilder requestActionBuilder =
        builder.client(endpointName).send().delete(url).messageType(MessageType.JSON);
    if (StringUtils.isNotBlank(contentType)) {
      requestActionBuilder.contentType(contentType);
    }

    requestActionBuilder = addHeaders(requestActionBuilder, headers);

    if (StringUtils.isNotBlank(requestFile)) {
      String requestFilePath =
          MessageFormat.format("{0}/{1}/{2}", testTemplateDir, testName, requestFile);
      return requestActionBuilder.payload(new ClassPathResource(requestFilePath));
    }
    return requestActionBuilder;
  }

  public static TestAction getMultipartRequestTestAction(
      TestContext context,
      HttpActionBuilder builder,
      String endpointName,
      String testTemplateDir,
      String testName,
      String requestUrl,
      String requestFile,
      Map<String, Object> headers,
      ClassLoader classLoader) {
    String formDataFileFolderPath = MessageFormat.format("{0}/{1}", testTemplateDir, testName);
    String formDataFile =
        MessageFormat.format("{0}/{1}/{2}", testTemplateDir, testName, requestFile);
    System.out.println("formDataFile = " + formDataFile);

    MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

    try (Scanner scanner = new Scanner(new File(classLoader.getResource(formDataFile).getFile()))) {

      while (scanner.hasNext()) {
        String[] param = scanner.nextLine().split(Constant.EQUAL_SIGN);
        if (param != null && param.length == 2) {
          if (param[0].equalsIgnoreCase(Constant.MULTIPART_FILE_NAME)) {
            formData.add(
                Constant.MULTIPART_FILE_NAME,
                new ClassPathResource(formDataFileFolderPath + "/" + param[1]));
          } else {
            formData.add(param[0], TestActionUtil.getVariable(context, param[1]));
          }
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    HttpClientRequestActionBuilder actionBuilder =
        builder
            .client(endpointName)
            .send()
            .post(requestUrl)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .header(Constant.AUTHORIZATION, Constant.BEARER + api_key);

    if (null != headers) {
      actionBuilder = addHeaders(actionBuilder, headers);
    }
    return actionBuilder.payload(formData);
  }

  public static TestAction getResponseTestAction(
      HttpActionBuilder builder,
      String endpointName,
      String testTemplateDir,
      String testName,
      HttpStatus responseCode,
      String responseFile) {
    if (StringUtils.isBlank(responseFile)) {
      return getResponseTestAction(builder, endpointName, testName, responseCode);
    }

    String responseFilePath =
        MessageFormat.format("{0}/{1}/{2}", testTemplateDir, testName, responseFile);

    return builder
        .client(endpointName)
        .receive()
        .response(responseCode)
        .validator("defaultJsonMessageValidator")
        .messageType(MessageType.JSON)
        .payload(new ClassPathResource(responseFilePath));
  }

  public static TestAction getExtractFromResponseTestAction(
      TestContext testContext,
      HttpActionBuilder builder,
      String endpointName,
      HttpStatus responseCode,
      String extractFieldPath,
      String extractVariable) {
    ObjectMapper mapper = new ObjectMapper();
    return builder
        .client(endpointName)
        .receive()
        .response(responseCode)
        .validator("defaultJsonMessageValidator")
        .messageType(MessageType.JSON)
        .extractFromPayload(extractFieldPath, extractVariable)
        .validationCallback(
            new JsonMappingValidationCallback<Map>(Map.class, mapper) {
              @Override
              public void validate(Map response, Map<String, Object> headers, TestContext context) {
                String extractValue =
                    (String) context.getVariables().getOrDefault(extractVariable, extractVariable);
                testContext.getVariables().put(extractVariable, extractValue);
                System.out.println("extractVariable = " + extractValue);
              }
            });
  }

  public static Map<String, Object> getHeaders(boolean isAuthRequired) {
    Map<String, Object> headers = new HashMap<>();
    if (isAuthRequired) {
      headers.put(Constant.X_AUTHENTICATED_USER_TOKEN, "${accessToken}");
    }
    headers.put("X-Channel-Id", "channel");
    headers.put(Constant.AUTHORIZATION, Constant.BEARER + System.getenv("sunbird_api_key"));
    return headers;
  }

  public static Map<String, Object> getHeaders(
      boolean isAuthRequired, Map<String, Object> additionalHeaders) {
    if (null == additionalHeaders) {
      additionalHeaders = new HashMap<>();
    }
    additionalHeaders.putAll(getHeaders(isAuthRequired));
    return additionalHeaders;
  }

  private static HttpClientRequestActionBuilder addHeaders(
      HttpClientRequestActionBuilder actionBuilder, Map<String, Object> headers) {
    if (headers != null) {
      for (Map.Entry<String, Object> entry : headers.entrySet()) {
        actionBuilder = actionBuilder.header(entry.getKey(), entry.getValue());
      }
    }
    return actionBuilder;
  }

  public static String getVariable(TestContext testContext, String variableName) {
    String value;
    try {
      value = testContext.getVariable(variableName);
    } catch (CitrusRuntimeException exception) {
      value = variableName;
    }
    return value;
  }

  public static TestAction performGetTest(
      HttpActionBuilder builder,
      String endpointName,
      String testName,
      String requestUrl,
      Map<String, Object> headers) {
    HttpClientRequestActionBuilder actionBuilder =
        builder
            .client(endpointName)
            .send()
            .get(requestUrl)
            .messageType(MessageType.JSON)
            .header(Constant.AUTHORIZATION, Constant.BEARER + api_key);
    if (null != headers) {
      actionBuilder = addHeaders(actionBuilder, headers);
    }
    return actionBuilder;
  }

  public static TestAction getResponseTestAction(
      HttpActionBuilder builder, String endpointName, String testName, HttpStatus responseCode) {
    return builder
        .client(endpointName)
        .receive()
        .response(responseCode)
        .validator("defaultJsonMessageValidator");
  }
}
