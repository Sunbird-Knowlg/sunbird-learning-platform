package org.sunbird.platform.content;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.sunbird.platform.domain.BaseTest;
import org.junit.Test;

import java.io.File;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

public class ContentAcceptFlagV3Test extends BaseTest {

    private static final String BASE_PATH = "/content/v3";
    private static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";

    private static ClassLoader classLoader = ContentAcceptFlagV3Test.class.getClassLoader();
    private static File filePath = new File(classLoader.getResource("UploadFiles/").getFile());

    private final String publishRequestBody = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishChecklist\":[\"Good Content\",\"Very Good\"],\"publishComment\":\"Good Work\"}}}";

    private String createDocumentContent() {
        int rn = generateRandomInt(0, 999999);
        String createDocumentContentRequestBody = "{\"request\": {\"content\": {\"identifier\":\"LP_FT_" + rn + "\", \"name\": \"Text Book 1\",\"code\": \"test.book.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
        Response response =  given().
                    spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                    body(createDocumentContentRequestBody).
                with().
                    contentType(JSON).
                when().
                    post(BASE_PATH + "/create").
                then().
                    body("responseCode", equalTo("OK")).
                    extract().
                    response();
        JsonPath jp = response.jsonPath();
        String nodeId = jp.get("result.node_id");
        return nodeId;
    }

    private String createCollectionContent() {
        int rn = generateRandomInt(0, 999999);
        String createCollectionContentRequestBody = "{\"request\": {\"content\": {\"identifier\":\"LP_FT_" + rn + "\", \"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_Dev\",\"name\": \"TestBook1\",\"language\":[\"English\"],\"contentType\": \"TextBook\",\"code\": \"testbook1\",\"tags\":[\"QA_Content\"],\"mimeType\": \"application/vnd.ekstep.content-collection\",\"children\":[]}}}";
        Response response =  given().
                    spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                    body(createCollectionContentRequestBody).
                with().
                    contentType(JSON).
                when().
                    post(BASE_PATH + "/create").
                then().
                    body("responseCode", equalTo("OK")).
                    extract().
                    response();
        JsonPath jp = response.jsonPath();
        String nodeId = jp.get("result.node_id");
        return nodeId;
    }

    private Response getContent(String contentId) {
        return  given().
                    spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                when().
                    get(BASE_PATH + "/read/" + contentId).
                then().
                    extract().
                response();
    }

    private void hierarchyUpdate(String collectionContentId) {
        String resourceContentId1 = createDocumentContent();
        String resourceContentId2 = createDocumentContent();
        String hierarchyUpdateRequestBody = "{\"request\":{\"data\":{\"nodesModified\":{\"TestBookUnit-0111\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01\",\"description\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-0211\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02\",\"description\":\"TTest_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-0311\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_03\",\"description\":\"TTest_Collection_TextBookUnit_03\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b82dd3aca\"}},\"TestBookUnit-0411\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_04\",\"description\":\"TTest_Collection_TextBookUnit_04\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4912-9b2b-8390b82dd3aca\"}},\"TestBookUnit-0511\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_05\",\"description\":\"TTest_Collection_TextBookUnit_05\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4912-9b2b-8390b82dd3aca\"}}},\"hierarchy\":{\"" + collectionContentId + "\":{\"name\":\"TextBook1-CreatedforRetireTesting\",\"contentType\":\"Collection\",\"children\":[\"TestBookUnit-0111\",\"TestBookUnit-0211\",\"TestBookUnit-0311\",\"TestBookUnit-0411\",\"TestBookUnit-0511\",\"" + resourceContentId1 + "\"],\"root\":true},\"TestBookUnit-0111\":{\"name\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"children\":[\"" + resourceContentId2 + "\"],\"root\":false},\"TestBookUnit-0211\":{\"name\":\"Test_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"children\":[\"" + resourceContentId2 +"\"],\"root\":false},\"TestBookUnit-0311\":{\"name\":\"Test_Collection_TextBookUnit_03\",\"contentType\":\"TextBookUnit\",\"children\":[\""+ resourceContentId2 +"\"],\"root\":false},\"TestBookUnit-0411\":{\"name\":\"Test_Collection_TextBookUnit_04\",\"contentType\":\"TextBookUnit\",\"children\":[\""+ resourceContentId2 +"\"],\"root\":false},\"TestBookUnit-0511\":{\"name\":\"Test_Collection_TextBookUnit_05\",\"contentType\":\"TextBookUnit\",\"children\":[\"" + resourceContentId2 + "\"],\"root\":false},\"" + resourceContentId1 + "\":{\"name\":\"Test_Resource_Content\",\"contentType\":\"Story\",\"children\":[],\"root\":false}}}}}";
        given().
                spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                body(hierarchyUpdateRequestBody).
            with().
                contentType(JSON).
            when().
                patch(BASE_PATH + "/hierarchy/update").
            then().
                body("responseCode", equalTo("OK"));
    }

    private void review(String contentId) {
        String reviewRequestBody = "{}";
        given().
                spec(getRequestSpecification(contentType, userId, APIToken)).
                body(reviewRequestBody).
            with().
                contentType(JSON).
            when().
                post(BASE_PATH + "/review/" + contentId).
            then().
                body("responseCode", equalTo("OK"));
    }

    private void publish(String contentId) {
        given().
                spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                body(publishRequestBody).
            with().
                contentType(JSON).
            when().
                post(BASE_PATH + "/publish/" + contentId).
            then().
                body("responseCode", equalTo("OK"));
    }

    private String getVersionKey(String contentId) {
        return getContent(contentId).jsonPath().get("result.content.versionKey");
    }

    private void update(String contentId) {
        String versionKey = getVersionKey(contentId);
        String updateRequestBody = "{\"request\": {\"content\": {\"versionKey\": \"" + versionKey + "\",\"screenshots\": null,\"name\": \"Image Node\"}}}";
        given().
                spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                body(updateRequestBody).
            with().
                contentType(JSON).
            when().
                patch(BASE_PATH + "/update/" + contentId).
            then().
                body("responseCode", equalTo("OK"));
    }

    private void upload(String contentId) {
        given().
                spec(getRequestSpecification(uploadContentType, userId, APIToken)).
                multiPart(new File(filePath + "/pdf.pdf")).
            when().
                post(BASE_PATH + "/upload/" + contentId).
            then().
                body("responseCode", equalTo("OK"));
    }

    private void retire(String contentId) {
        given().
                spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
            with().
                contentType(JSON).
            when().
                delete(BASE_PATH + "/retire/" + contentId).
            then().
                body("responseCode", equalTo("OK"));
    }

    private void flag(String contentId) {
        String versionKey = getVersionKey(contentId);
        String flagRequestBody = "{\"request\": {\"flagReasons\":[\"Copyright Violation\"],\"flaggedBy\":\"gauraw\",\"versionKey\": \"" + versionKey + "\"}}";
        given().
                spec(getRequestSpecification(contentType, userId, APIToken)).
                body(flagRequestBody).
            with().
                contentType(JSON).
            when().
                post(BASE_PATH + "/flag/" + contentId).
            then().
                body("responseCode", equalTo("OK"));
    }

    private void acceptFlag(String contentId) {
        String versionKey = getVersionKey(contentId);
        String acceptFlagRequestBody = "{\"request\": {\"versionKey\":\""+ versionKey +"\"}}";
        given().
                spec(getRequestSpecification(contentType, userId, APIToken)).
                body(acceptFlagRequestBody).
            with().
                contentType(JSON).
            when().
                post(BASE_PATH + "/flag/accept/" + contentId).
            then().
                body("responseCode", equalTo("OK"));
    }

    private Response acceptFlagAndGetResponse(String contentId) {
        String versionKey = getVersionKey(contentId);
        String acceptFlagRequestBody = "{\"request\": {\"versionKey\":\""+ versionKey +"\"}}";
        return given().
                spec(getRequestSpecification(contentType, userId, APIToken)).
                    body(acceptFlagRequestBody).
                with().
                    contentType(JSON).
                when().
                    post(BASE_PATH + "/flag/accept/" + contentId).
                then().
                    extract().response();
    }

    public Response getContentResponse(String contentId) {
        return given().
                    spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                with().
                    contentType(JSON).
                when().
                    get(BASE_PATH + "/read/" + contentId).
                then().
                    body("responseCode", equalTo("OK")).
                    extract().
                    response();
    }

    /**
     * Accepting Flagged Resource Content.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Retired   - Status : Original Content will be retired,
     *        FlagDraft - Status : Image Content will be Flagged Draft.
     */
    @Test
    public void acceptFlaggedDocumentContent() {
        setURI();
        String contentId = createDocumentContent();
        upload(contentId);
        publish(contentId);
        delay(25000);
        flag(contentId);
        acceptFlag(contentId);
        Response imageNodeResponse = getContentResponse(contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
        assertEquals("FlagDraft", imageNodeResponse.jsonPath().get("result.content.status"));
        Response response = getContentResponse(contentId);
        assertEquals("Retired", response.jsonPath().get("result.content.status"));
    }

    /**
     * Accepting Flagged Resource Content with Image Node.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Retired   - Status : Original Content will be retired,
     * 	      FlagDraft - Status : Image Content will be Flagged Draft.
     */
    @Test
    public void acceptFlaggedDocumentContentWithImageNode() {
        setURI();
        String contentId = createDocumentContent();
        upload(contentId);
        publish(contentId);
        delay(25000);
        update(contentId);
        flag(contentId);
        acceptFlag(contentId);
        Response imageNodeResponse = getContentResponse(contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
        assertEquals("FlagDraft", imageNodeResponse.jsonPath().get("result.content.status"));
        Response response = getContentResponse(contentId);
        assertEquals("Retired", response.jsonPath().get("result.content.status"));
    }

    /**
     * Accepting Flag for Resource Content with Draft Status.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Err Taxonomy Invalid Content - Error Code
     *        Client Error - Response Code
     */
    @Test
    public void acceptFlagForDocumentContentWithDraftStatus() {
        setURI();
        String contentId = createDocumentContent();
        upload(contentId);
        Response response = acceptFlagAndGetResponse(contentId);
        assertEquals("ERR_TAXONOMY_INVALID_CONTENT", response.jsonPath().get("params.err"));
        assertEquals("CLIENT_ERROR", response.jsonPath().get("responseCode"));
    }

    /**
     * Accepting Flag for Resource Content with Review Status.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Err Taxonomy Invalid Content - Error Code
     *        Client Error - Response Code
     */
    @Test
    public void acceptFlagForDocumentContentWithReviewStatus() {
        setURI();
        String contentId = createDocumentContent();
        upload(contentId);
        review(contentId);
        Response response = acceptFlagAndGetResponse(contentId);
        assertEquals("ERR_TAXONOMY_INVALID_CONTENT", response.jsonPath().get("params.err"));
        assertEquals("CLIENT_ERROR", response.jsonPath().get("responseCode"));
    }

    /**
     * Accepting Flag for Resource Content with Review Status.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Err Taxonomy Invalid Content - Error Code
     *        Client Error - Response Code
     */
    @Test
    public void acceptFlagForDocumentContentWithLiveStatus() {
        setURI();
        String contentId = createDocumentContent();
        upload(contentId);
        publish(contentId);
        delay(25000);
        Response response = acceptFlagAndGetResponse(contentId);
        assertEquals("ERR_TAXONOMY_INVALID_CONTENT", response.jsonPath().get("params.err"));
        assertEquals("CLIENT_ERROR", response.jsonPath().get("responseCode"));
    }

    /**
     * Accepting Flag for Resource Content with Live Status with Image Node.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Err Taxonomy Invalid Content - Error Code
     *        Client Error - Response Code
     */
    @Test
    public void acceptFlagForDocumentContentWithLiveStatusAndImageNode() {
        setURI();
        String contentId = createDocumentContent();
        upload(contentId);
        publish(contentId);
        delay(25000);
        update(contentId);
        Response response = acceptFlagAndGetResponse(contentId);
        assertEquals("ERR_TAXONOMY_INVALID_CONTENT", response.jsonPath().get("params.err"));
        assertEquals("CLIENT_ERROR", response.jsonPath().get("responseCode"));
    }

    /**
     * Accepting Flag for Resource Content with Retired Status.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Err Taxonomy Invalid Content - Error Code
     *        Client Error - Response Code
     */
    @Test
    public void acceptFlagForDocumentContentWithRetiredStatus() {
        setURI();
        String contentId = createDocumentContent();
        upload(contentId);
        retire(contentId);
        Response response = acceptFlagAndGetResponse(contentId);
        assertEquals("ERR_TAXONOMY_INVALID_CONTENT", response.jsonPath().get("params.err"));
        assertEquals("CLIENT_ERROR", response.jsonPath().get("responseCode"));
    }

    /**
     * Accepting Flagged Collection Content.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Retired - Status : Original Content will be retired,
     * 	      FlagDraft - Status : Image Content will be Flagged Draft.
     */
    @Test
    public void acceptFlaggedCollectionContent() {
        setURI();
        String collectionContentId = createCollectionContent();
        hierarchyUpdate(collectionContentId);
        publish(collectionContentId);
        delay(25000);
        flag(collectionContentId);
        acceptFlag(collectionContentId);
        Response imageNodeResponse = getContentResponse(collectionContentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
        assertEquals("FlagDraft", imageNodeResponse.jsonPath().get("result.content.status"));
        Response response = getContentResponse(collectionContentId);
        assertEquals("Retired", response.jsonPath().get("result.content.status"));
    }

    /**
     * Accepting Flagged Collection Content with Image Node.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Retired   - Status : Original Content will be retired,
     * 	      FlagDraft - Status : Image Content will be Flagged Draft.
     */
    @Test
    public void acceptFlaggedCollectionContentWithImageNode() {
        setURI();
        String collectionContentId = createCollectionContent();
        hierarchyUpdate(collectionContentId);
        publish(collectionContentId);
        delay(25000);
        update(collectionContentId);
        flag(collectionContentId);
        acceptFlag(collectionContentId);
        Response imageNodeResponse = getContentResponse(collectionContentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
        assertEquals("FlagDraft", imageNodeResponse.jsonPath().get("result.content.status"));
        Response response = getContentResponse(collectionContentId);
        assertEquals("Retired", response.jsonPath().get("result.content.status"));
    }

    /**
     * Accepting Flag for Collection Content with Draft Status.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Err Taxonomy Invalid Content - Error Code
     *        Client Error - Response Code
     */
    @Test
    public void acceptFlagForCollectionContentWithDraftStatus() {
        setURI();
        String collectionContentId = createCollectionContent();
        Response response = acceptFlagAndGetResponse(collectionContentId);
        assertEquals("CLIENT_ERROR", response.jsonPath().get("responseCode"));
        assertEquals("ERR_TAXONOMY_INVALID_CONTENT", response.jsonPath().get("params.err"));
    }

    /**
     * Accepting Flag for Collection Content with Review Status.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Err Taxonomy Invalid Content - Error Code
     *        Client Error - Response Code
     */
    @Test
    public void acceptFlagForCollectionContentWithReviewStatus() {
        setURI();
        String collectionContentId = createCollectionContent();
        hierarchyUpdate(collectionContentId);
        review(collectionContentId);
        Response response = acceptFlagAndGetResponse(collectionContentId);
        assertEquals("CLIENT_ERROR", response.jsonPath().get("responseCode"));
        assertEquals("ERR_TAXONOMY_INVALID_CONTENT", response.jsonPath().get("params.err"));
    }

    /**
     * Accepting Flag for Collection Content with Live Status.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Err Taxonomy Invalid Content - Error Code
     *        Client Error - Response Code
     */
    @Test
    public void acceptFlagForCollectionContentWithLiveStatus() {
        setURI();
        String collectionContentId = createCollectionContent();
        hierarchyUpdate(collectionContentId);
        publish(collectionContentId);
        delay(25000);
        Response response = acceptFlagAndGetResponse(collectionContentId);
        assertEquals("CLIENT_ERROR", response.jsonPath().get("responseCode"));
        assertEquals("ERR_TAXONOMY_INVALID_CONTENT", response.jsonPath().get("params.err"));
    }

    /**
     * Accepting Flag for Collection Content with Live Status with Image Node.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Err Taxonomy Invalid Content - Error Code
     *        Client Error - Response Code
     */
    @Test
    public void acceptFlagForCollectionContentWithLiveStatusAndImageNode() {
        setURI();
        String collectionContentId = createCollectionContent();
        hierarchyUpdate(collectionContentId);
        publish(collectionContentId);
        delay(25000);
        update(collectionContentId);
        Response response = acceptFlagAndGetResponse(collectionContentId);
        assertEquals("CLIENT_ERROR", response.jsonPath().get("responseCode"));
        assertEquals("ERR_TAXONOMY_INVALID_CONTENT", response.jsonPath().get("params.err"));
    }

    /**
     * Accepting Flag for Collection Content with Retired Status.
     * Given: Content Id
     * When:  Flag Accept API Hits
     * Then:  Err Taxonomy Invalid Content - Error Code
     *        Client Error - Response Code
     */
    @Test
    public void acceptFlagForCollectionContentWithRetiredStatus() {
        setURI();
        String collectionContentId = createCollectionContent();
        hierarchyUpdate(collectionContentId);
        publish(collectionContentId);
        delay(25000);
        update(collectionContentId);
        retire(collectionContentId);
        Response response = acceptFlagAndGetResponse(collectionContentId);
        assertEquals("CLIENT_ERROR", response.jsonPath().get("responseCode"));
        assertEquals("ERR_TAXONOMY_INVALID_CONTENT", response.jsonPath().get("params.err"));
    }

}

