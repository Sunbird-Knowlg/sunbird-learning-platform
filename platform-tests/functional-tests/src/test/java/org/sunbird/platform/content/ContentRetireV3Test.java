package org.sunbird.platform.content;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.platform.domain.BaseTest;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

public class ContentRetireV3Test extends BaseTest {

    private static final String BASE_PATH = "/content/v3";
    private static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";

    private static ClassLoader classLoader = ContentRetireV3Test.class.getClassLoader();
    private static File filePath = new File(classLoader.getResource("UploadFiles/").getFile());

    private final String createDocumentContentRequestBody = "{\"request\": {\"content\": {\"name\": \"Text Book 1\",\"code\": \"test.book.1\",\"mimeType\": \"application/pdf\",\"contentType\":\"Resource\"}}}";
    private final String publishRequestBody = "{\"request\": {\"content\": {\"publisher\": \"EkStep\",\"lastPublishedBy\": \"Ekstep\",\"publishChecklist\":[\"Good Content\",\"Very Good\"],\"publishComment\":\"Good Work\"}}}";
    private final String createCollectionContentRequestBody = "{\"request\": {\"content\": {\"mediaType\": \"content\",\"visibility\": \"Default\",\"description\": \"Test_Dev\",\"name\": \"TestBook1\",\"language\":[\"English\"],\"contentType\": \"TextBook\",\"code\": \"testbook1\",\"tags\":[\"QA_Content\"],\"mimeType\": \"application/vnd.ekstep.content-collection\",\"children\":[]}}}";

    private String createDocumentContent() {
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

    private void hierarchyUpdateWithCollectionChild(String collectionContentId, String childCollectionContentId) {
        String resourceContentId1 = createDocumentContent();
        String resourceContentId2 = createDocumentContent();
        String hierarchyUpdateRequestBody = "{\"request\":{\"data\":{\"nodesModified\":{\"TestBookUnit-0111\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01\",\"description\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-0211\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02\",\"description\":\"TTest_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-0311\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_03\",\"description\":\"TTest_Collection_TextBookUnit_03\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b82dd3aca\"}},\"TestBookUnit-0411\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_04\",\"description\":\"TTest_Collection_TextBookUnit_04\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4912-9b2b-8390b82dd3aca\"}},\"TestBookUnit-0511\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_05\",\"description\":\"TTest_Collection_TextBookUnit_05\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4912-9b2b-8390b82dd3aca\"}}},\"hierarchy\":{\"" + collectionContentId + "\":{\"name\":\"TextBook1-CreatedforRetireTesting\",\"contentType\":\"Collection\",\"children\":[\"TestBookUnit-0111\",\"TestBookUnit-0211\",\"TestBookUnit-0311\",\"TestBookUnit-0411\",\"TestBookUnit-0511\",\"" + resourceContentId1 + "\",\"" + childCollectionContentId + "\"],\"root\":true},\"TestBookUnit-0111\":{\"name\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"children\":[\"" + resourceContentId2 + "\"],\"root\":false},\"TestBookUnit-0211\":{\"name\":\"Test_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"children\":[\"" + resourceContentId2 +"\"],\"root\":false},\"TestBookUnit-0311\":{\"name\":\"Test_Collection_TextBookUnit_03\",\"contentType\":\"TextBookUnit\",\"children\":[\""+ resourceContentId2 +"\"],\"root\":false},\"TestBookUnit-0411\":{\"name\":\"Test_Collection_TextBookUnit_04\",\"contentType\":\"TextBookUnit\",\"children\":[\""+ resourceContentId2 +"\"],\"root\":false},\"TestBookUnit-0511\":{\"name\":\"Test_Collection_TextBookUnit_05\",\"contentType\":\"TextBookUnit\",\"children\":[\"" + resourceContentId2 + "\"],\"root\":false},\"" + resourceContentId1 + "\":{\"name\":\"Test_Resource_Content\",\"contentType\":\"Story\",\"children\":[],\"root\":false}}}}}";
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
            post("/content/v3/upload/" + contentId).
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
            post("/content/v3/flag/" + contentId).
        then().
            body("responseCode", equalTo("OK"));
    }

    private void retireContent(String contentId) {
        given().
            spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
        with().
            contentType(JSON).
        when().
            delete(BASE_PATH + "/retire/" + contentId).
        then().
            body("responseCode", equalTo("OK"));
    }

    public Response validateRetiredNode(String contentId) {
        Response response = given().
            spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
        with().
            contentType(JSON).
        when().
            get(BASE_PATH + "/read/" + contentId).
        then().
            body("responseCode", equalTo("OK")).
            extract().
            response();
        return response;
    }

    private void validateRetiredStatusRecursively(String contentId) {
        if(!StringUtils.endsWithIgnoreCase(contentId, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)) {
            validateRetiredStatusRecursively(contentId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
        }
        Response response = getContent(contentId);
        JsonPath jp = response.jsonPath();
        if("OK".equals(jp.get("responseCode"))) {
            Map<String, Object> content = jp.get("result.content");
            Optional.ofNullable(((List<Map<String, Object>>) content.get("children"))).ifPresent(children -> {
                if (!children.isEmpty()) {
                    children.stream().forEach(child -> {
                        try {
                            if ("Parent".equals(child.get("visibility"))) {
                                validateRetiredStatusRecursively((String) child.get("identifier"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
            if ("Parent".equals(content.get("visibility")))
                assertEquals("Retired", content.get("status"));
        }
    }

    private Response validateRetiredCollectionContent(String contentId) {
        validateRetiredStatusRecursively(contentId);
        return validateRetiredNode(contentId);
    }

    @Test
    public void retireFlaggedDocumentContent() {
        setURI();
        String contentId = createDocumentContent();
        upload(contentId);
        publish(contentId);
        delay(25000);
        flag(contentId);
        retireContent(contentId);
        Response response = validateRetiredNode(contentId);
        assertEquals("Retired", response.jsonPath().get("result.content.status"));
    }

    @Test
    public void retireLiveCollectionContentWithImageNode() {
        setURI();
        String collectionContentId = createCollectionContent();
        publish(collectionContentId);
        delay(25000);
        update(collectionContentId);
        retireContent(collectionContentId);
        Response response = validateRetiredCollectionContent(collectionContentId);
        assertEquals("Retired", response.jsonPath().get("result.content.status"));
    }

    @Test
    public void retireLiveCollectionContentWithChildren() {
        setURI();
        String collectionContentId = createCollectionContent();
        hierarchyUpdate(collectionContentId);
        publish(collectionContentId);
        delay(25000);
        update(collectionContentId);
        retireContent(collectionContentId);
        Response response = validateRetiredCollectionContent(collectionContentId);
        assertEquals("Retired", response.jsonPath().get("result.content.status"));
    }

    @Test
    public void retireLiveCollectionContentWithCollectionChildren() {
        setURI();
        String childCollectionContentId = createCollectionContent();
        hierarchyUpdate(childCollectionContentId);
        String collectionContentId = createCollectionContent();
        hierarchyUpdate(childCollectionContentId);
        hierarchyUpdateWithCollectionChild(collectionContentId, childCollectionContentId);
        publish(collectionContentId);
        delay(25000);
        update(collectionContentId);
        retireContent(collectionContentId);
        Response response = validateRetiredCollectionContent(collectionContentId);
        assertEquals("Retired", response.jsonPath().get("result.content.status"));
    }
}
