package org.sunbird.platform.content;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.taxonomy.controller.ContentV3Controller;

import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

/**
 * Functional Test (RestAssured) Requests for Content V3 APIs
 *
 * @see ContentV3Controller
 */
public class ContentV3API {

    private String BASE_PATH = "/content/v3";

    /**
     * Sends POST Request to Create Content V3 API
     *
     * @param requestBody
     * @return Response object
     */
    public Response create(String requestBody, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        return given().
                    spec(requestSpec).
                    body(requestBody).
                with().
                    contentType(JSON).
                when().
                    post(BASE_PATH + "/create").
                then().
                    // log().all().
                    spec(responseSpec).
                    extract().
                    response();
    }

//    private String getIdentifier(Response response) { return response.jsonPath().get("result.node_id"); }

//    public String createAndGetIdentifier(String requestBody, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
//        return getIdentifier(create(requestBody, requestSpec, responseSpec));
//    }

    private String[] getCreateResponseResultArray(Response response) { return new String[] { response.jsonPath().get("result.node_id"), response.jsonPath().get("result.versionKey") }; }

    public String[] createAndGetResult(String requestBody, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        return getCreateResponseResultArray(create(requestBody, requestSpec, responseSpec));
    }

    /**
     * Sends GET Request to Read Content V3 API {@link ContentV3Controller#find(String, String[], String)}
     *
     * @param identifier
     * @param requestSpec
     * @param responseSpec
     * @return Response object
     */
    public Response read(String identifier, String mode, RequestSpecification requestSpec, ResponseSpecification responseSpec, String... fields) {

        return  given().
                param("mode", StringUtils.isBlank(mode) ? "" : mode).
                param("fields", null != fields || 0 == fields.length ? "" : fields).
                spec(requestSpec).
                when().
                get(BASE_PATH + "/read/" + identifier).
                then().
                spec(responseSpec).
//                log().all().
                extract().
                response();
    }

    /**
     * Send POST Request to Review Content V3 API {@link ContentV3Controller#review(String, Map)}
     *
     * @param request
     * @param identifier
     * @param requestSpec
     * @param responseSpec
     * @return
     */
    public Response review(String identifier, String request, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        return given().
                    pathParam("id", identifier).
                    spec(requestSpec).
                    body(request).
                when().
                    post(BASE_PATH + "/review/{id}").
                then().
//                    log().all().
                    spec(responseSpec).
                    extract().
                    response();
    }

    /**
     * Sends PATCH Request to Update Content V3 API {@link ContentV3Controller#update(String, Map)}
     *
     * @param identifier
     * @param request
     * @param requestSpec
     * @param responseSpec
     * @return
     */
    public Response update(String identifier, String request, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        return given().
                    pathParam("id", identifier).
                    spec(requestSpec).
                    body(request).
                when().
                    patch(BASE_PATH + "/update/{id}").
                then().
                    // log().all().
                    spec(responseSpec).
                    extract().
                    response();
    }

    /**
     * Sends POST Request to Publish Content V3 API {@link ContentV3Controller#publish(String, Map)}
     *
     * @param identifier
     * @param request
     * @param requestSpec
     * @param responseSpec
     * @return
     *
     */
    public Response publish(String identifier, String request, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        return given().
                    pathParam("id", identifier).
                    spec(requestSpec).
                    body(request).
                with().
                    contentType(JSON).
                when().
                    post(BASE_PATH + "/publish/{id}").
                then().
                    // log().all().
                    spec(responseSpec).
                    extract().
                    response();
    }

    /**
     * Sends POST Request to Reserve Dialcode Content V3 API {@link ContentV3Controller#reserveDialCode(String, Map, String)}
     *
     * @param identifier
     * @param request
     * @param requestSpec
     * @param responseSpec
     * @return
     *
     */
    public Response reserveDialcode(String identifier, String request, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        return given().
                    pathParam("id", identifier).
                    spec(requestSpec).
                    body(request).
                with().
                    contentType(JSON).
                when().
                    post(BASE_PATH + "/dialcode/reserve/{id}").
                then().
//                     log().all().
                    spec(responseSpec).
                    extract().
                    response();
    }

    /**
     * Sends PATCH Request to Reserve Dialcode Content V3 API {@link ContentV3Controller#releaseDialcodes(String, String)}
     *
     * @param identifier
     * @param requestSpec
     * @param responseSpec
     * @return
     *
     */
    public Response releaseDialcodes(String identifier, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        return given().
                    pathParam("id", identifier).
                    spec(requestSpec).
                with().
                    contentType(JSON).
                when().
                    patch(BASE_PATH + "/dialcode/release/{id}").
                then().
//                     log().all().
                    spec(responseSpec).
                    extract().
                    response();
    }

    /**
     * Sends PATCH Request to Hierarchy Update Content V3 API {@link ContentV3Controller#updateHierarchy(Map)}
     *
     * @param requestBody
     * @param requestSpec
     * @param responseSpec
     * @return
     */
    public Response hierarchyUpdate(String requestBody, RequestSpecification requestSpec, ResponseSpecification responseSpec) {
        return given().
                    spec(requestSpec).
                    body(requestBody).
                with().
                    contentType(JSON).
                when().
                    patch(BASE_PATH + "/hierarchy/update").
                then().
//                     log().all().
                    spec(responseSpec).
                    extract().
                    response();
    }

}
