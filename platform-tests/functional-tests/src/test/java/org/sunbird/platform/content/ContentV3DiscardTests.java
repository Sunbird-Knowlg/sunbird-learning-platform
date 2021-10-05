package org.sunbird.platform.content;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.platform.domain.BaseTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


import java.io.File;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;

/**
 * @author Rhea Fernandes
 */
public class ContentV3DiscardTests extends BaseTest {
    private static ClassLoader classLoader = ContentV3DiscardTests.class.getClassLoader();

    private static File path = new File(classLoader.getResource("UploadFiles/").getFile());

    private static final String contentId404 = "do_test404";
    private static final String collectionId404 = "do_test404";


    //Content Related mock data
    private static final String createContent = "{\n" +
            "\"request\": {\n" +
            "\t\"content\": {\n" +
            "            \"code\": \"PDF_1\",\n" +
            "            \"name\": \"Quantum Mechanics\",\n" +
            "            \"mimeType\": \"application/pdf\",\n" +
            "            \"contentType\": \"Resource\",\n" +
            "            \"description\": \"test resource\"\n" +
            "   }\n" +
            " }\n" +
            "}";
    private static final String updateContent = "{\n" +
            "  \"request\": {\n" +
            "    \"content\": {\n" +
            "      \"versionKey\": \"31b6fd1c4d64e745c867e61a45edc34a\",\n" +
            "      \"language\": \"English\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    //Collection related mock data
    private static final String createTextContent = "{\n" +
            "\"request\": {\n" +
            "  \"content\": {\n" +
            "      \"osId\": \"org.sunbird.quiz.app\",\n" +
            "      \"mediaType\": \"content\",\n" +
            "      \"visibility\": \"Default\",\n" +
            "      \"description\": \"Text Book in English for Class III\",\n" +
            "      \"name\": \"Marigold\",\n" +
            "      \"language\":[\"English\"],\n" +
            "      \"contentType\": \"TextBook\",\n" +
            "      \"code\": \"org.sunbird.feb16.story.test01\",\n" +
            "      \"tags\":[\"QA_Content\"],\n" +
            "       \"mimeType\": \"application/vnd.ekstep.content-collection\",\n" +
            "       \"children\":[\n" +
            "       ]\n" +
            "  }\n" +
            "}\n" +
            "}";


    @Test
    public void discardContentNotFound() {
        JsonPath resp = discard404Content(contentId404);
        assertEquals("failed", resp.get("params.status"));
    }

    @Test
    public void discardContentWithStatusDraft() {
        String contentId = createContent(contentType, createContent);
        JsonPath resp = discardContent(contentId);
        assertEquals(resp.get("params.status"), "successful");
        read404Content(contentId);
    }

    @Ignore
    @Test
    public void discardContentWithStatusFlagDraft() {
        String contentId = createContent(contentType, createContent);
        System.out.println("Content Id : " + contentId);
        uploadContent(contentId, "/pdf.pdf");
        publishContent(contentId, null, false);
        delay(20000);
        JsonPath response = readContent(contentId);
        flagContent(contentId, response.get("result.content.versionKey"));
        delay(80000);
        JsonPath resp = discardContent(contentId);
        assertEquals(resp.get("params.status"), "successful");
        JsonPath res = readContent(contentId);
        String actualStatus = res.get("result.content.status");
        assertEquals("Live", actualStatus);
        read404Content(contentId + ".img");

    }

    @Test
    public void discardContentWithStatusLiveAndDraft() {
        String contentId = createContent(contentType, createContent);
        uploadContent(contentId, "/pdf.pdf");
        publishContent(contentId, null, false);
        delay(20000);

        JsonPath response = readContent(contentId);
        updateContent(contentId, response.get("result.content.versionKey"));
        delay(1000);
        readContent(contentId + ".img");
        JsonPath resp = discardContent(contentId);
        assertEquals(resp.get("params.status"), "successful");
        read404Content(contentId + ".img");
        JsonPath res = readContent(contentId);
        String actualStatus = res.get("result.content.status");
        assertEquals("Live", actualStatus);
    }

    @Test
    public void discardContentWithStatusLiveAndReview() {
        String contentId = createContent(contentType, createContent);
        uploadContent(contentId, "/pdf.pdf");
        delay(5000);
        publishContent(contentId, null, false);
        delay(20000);

        JsonPath response = readContent(contentId);
        updateContent(contentId, response.get("result.content.versionKey"));
        reviewContent(contentId);
        JsonPath resp = discard400Content(contentId);
        assertEquals(resp.get("params.status"), "failed");
        JsonPath res = readContent(contentId);
        String actualStatus = res.get("result.content.status");
        assertEquals("Live", actualStatus);
        JsonPath respo = readContent(contentId + ".img");
        actualStatus = respo.get("result.content.status");
        assertEquals("Review", actualStatus );
    }

    @Test
    public void discardWithStatusRetired() {
        String contentId = createContent(contentType, createContent);
        uploadContent(contentId, "/pdf.pdf");
        delay(5000);
        publishContent(contentId, null, false);
        delay(15000);
        retireContent(contentId);
        discard400Content(contentId);
        JsonPath res = readContent(contentId);
        String actualStatus = res.get("result.content.status");
        assertEquals("Retired", actualStatus);
    }

    @Test
    public void discardCollectionWithStatusNotFound() {
        JsonPath resp = discard404Content(collectionId404);
        assertEquals("failed", resp.get("params.status"));
    }

    @Test
    public void discardCollectionWithStatusDraft() {
        String contentId = createContent(contentType, createTextContent);
        String updateTextContent = "{\"request\":{\"data\":{\"nodesModified\":{\""+contentId+"\":{\"isNew\":false,\"root\":true,\"reservedDialcodes\":{\"ZDYAKA\":0,\"DAFKJN\":1,\"ZNLDAJ\":2},\"metadata\":{\"name\":\"YO\"}},\"textbookunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1\",\"code\":\"testbook 0\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2\",\"code\":\"testbook 1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3\",\"code\":\"testbook 2\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1.1\",\"code\":\"testbook 1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2.1\",\"code\":\"testbook 2.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1\",\"code\":\"testbook 3.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubsubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1.1\",\"code\":\"testbook 3.1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}}},\"hierarchy\":{\""+contentId+"\":{\"name\":\"Test Undefined\",\"contentType\":\"TextBook\",\"children\":[\"textbookunit_0\",\"textbookunit_1\",\"textbookunit_2\"],\"root\":true},\"textbookunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_0\"],\"root\":false},\"textbookunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_1\"],\"root\":false},\"textbookunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_2\"],\"root\":false},\"textbooksubunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubsubunit_2\"],\"root\":false},\"textbooksubsubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false}},\"lastUpdatedBy\":\"pradyumna\"}}}";
        updateHierarchy(contentId, updateTextContent);
        discardContent(contentId);
        read404Hierarchy(contentId + ".img");
    }

    @Test
    public void discardCollectionWithStatusFlagDraft() {

    }

    @Test
    public void discardCollectionWithLiveAndDraft() {
        String contentId = createContent(contentType, createTextContent);
        String updateTextContent = "{\"request\":{\"data\":{\"nodesModified\":{\""+contentId+"\":{\"isNew\":false,\"root\":true,\"reservedDialcodes\":{\"ZDYAKA\":0,\"DAFKJN\":1,\"ZNLDAJ\":2},\"metadata\":{\"name\":\"YO\"}},\"textbookunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1\",\"code\":\"testbook 0\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2\",\"code\":\"testbook 1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3\",\"code\":\"testbook 2\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1.1\",\"code\":\"testbook 1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2.1\",\"code\":\"testbook 2.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1\",\"code\":\"testbook 3.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubsubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1.1\",\"code\":\"testbook 3.1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}}},\"hierarchy\":{\""+contentId+"\":{\"name\":\"Test Undefined\",\"contentType\":\"TextBook\",\"children\":[\"textbookunit_0\",\"textbookunit_1\",\"textbookunit_2\"],\"root\":true},\"textbookunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_0\"],\"root\":false},\"textbookunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_1\"],\"root\":false},\"textbookunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_2\"],\"root\":false},\"textbooksubunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubsubunit_2\"],\"root\":false},\"textbooksubsubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false}},\"lastUpdatedBy\":\"pradyumna\"}}}";
        updateHierarchy(contentId, updateTextContent);
        publishContent(contentId, null, false);
        delay(20000);
        JsonPath response = readContent(contentId);
        updateContent(contentId, response.get("result.content.versionKey"));
        discardContent(contentId);
        JsonPath res = readEditHierarchy(contentId);
        String actualStatus = res.get("result.content.status");
        assertEquals("Live", actualStatus);
        List<String> languages = res.get("result.content.language");
        System.out.println(languages);
        assertEquals(1, languages.size());
        assertEquals("[English]", languages.toString());
        JsonPath resp = readHierarchy(contentId);
        actualStatus = resp.get("result.content.status");
        assertEquals("Live", actualStatus);
    }

    @Test
    public void discardCollectionWithStatusLiveAndReview() {
        String contentId = createContent(contentType, createTextContent);
        String updateTextContent = "{\"request\":{\"data\":{\"nodesModified\":{\""+contentId+"\":{\"isNew\":false,\"root\":true,\"reservedDialcodes\":{\"ZDYAKA\":0,\"DAFKJN\":1,\"ZNLDAJ\":2},\"metadata\":{\"name\":\"YO\"}},\"textbookunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1\",\"code\":\"testbook 0\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2\",\"code\":\"testbook 1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3\",\"code\":\"testbook 2\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1.1\",\"code\":\"testbook 1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2.1\",\"code\":\"testbook 2.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1\",\"code\":\"testbook 3.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubsubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1.1\",\"code\":\"testbook 3.1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}}},\"hierarchy\":{\""+contentId+"\":{\"name\":\"Test Undefined\",\"contentType\":\"TextBook\",\"children\":[\"textbookunit_0\",\"textbookunit_1\",\"textbookunit_2\"],\"root\":true},\"textbookunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_0\"],\"root\":false},\"textbookunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_1\"],\"root\":false},\"textbookunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_2\"],\"root\":false},\"textbooksubunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubsubunit_2\"],\"root\":false},\"textbooksubsubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false}},\"lastUpdatedBy\":\"pradyumna\"}}}";
        updateHierarchy(contentId, updateTextContent);
        publishContent(contentId, null, false);
        delay(20000);
        JsonPath response = readContent(contentId);
        updateContent(contentId, response.get("result.content.versionKey"));
        reviewContent(contentId);
        discard400Content(contentId);
        JsonPath res = readEditHierarchy(contentId);
        String actualStatus = res.get("result.content.status");
        assertEquals("Review", actualStatus);
        JsonPath resp = readHierarchy(contentId);
        actualStatus = resp.get("result.content.status");
        assertEquals("Live", actualStatus);
    }

    @Test
    public void discardCollectionWithStatusRetired() {
        String contentId = createContent(contentType, createTextContent);
        String updateTextContent = "{\"request\":{\"data\":{\"nodesModified\":{\""+contentId+"\":{\"isNew\":false,\"root\":true,\"reservedDialcodes\":{\"ZDYAKA\":0,\"DAFKJN\":1,\"ZNLDAJ\":2},\"metadata\":{\"name\":\"YO\"}},\"textbookunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1\",\"code\":\"testbook 0\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2\",\"code\":\"testbook 1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3\",\"code\":\"testbook 2\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1.1\",\"code\":\"testbook 1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2.1\",\"code\":\"testbook 2.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1\",\"code\":\"testbook 3.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubsubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1.1\",\"code\":\"testbook 3.1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}}},\"hierarchy\":{\""+contentId+"\":{\"name\":\"Test Undefined\",\"contentType\":\"TextBook\",\"children\":[\"textbookunit_0\",\"textbookunit_1\",\"textbookunit_2\"],\"root\":true},\"textbookunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_0\"],\"root\":false},\"textbookunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_1\"],\"root\":false},\"textbookunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_2\"],\"root\":false},\"textbooksubunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubsubunit_2\"],\"root\":false},\"textbooksubsubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false}},\"lastUpdatedBy\":\"pradyumna\"}}}";
        updateHierarchy(contentId, updateTextContent);
        publishContent(contentId, null, false);
        delay(20000);
        retireContent(contentId);
        discard400Content(contentId);
        read404Hierarchy(contentId);
        readEdit404Hierarchy(contentId);
    }


    private JsonPath readContent(String contentId) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, userId, APIToken)).
                        when().
                        get("/content/v3/read/" + contentId).
                        then().
                        spec(get200ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

    private JsonPath read404Content(String contentId) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, userId, APIToken)).
                        when().
                        get("/content/v3/read/" + contentId).
                        then().
                        spec(get404ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

    private JsonPath discardContent(String contentId) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, userId, APIToken)).
                        body("{\n" +
                                "  \"request\": {}\n" +
                                "}").
                        with().
                        contentType(JSON).
                        when().
                        delete("/content/v3/discard/" + contentId).
                        then().log().all().
                        spec(get200ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

    private JsonPath discard400Content(String contentId) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, userId, APIToken)).
                        body("{\n" +
                                "  \"request\": {}\n" +
                                "}").
                        with().
                        contentType(JSON).
                        when().
                        delete("/content/v3/discard/" + contentId).
                        then().log().all().
                        spec(get400ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }
    private JsonPath discard404Content(String contentId) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, userId, APIToken)).
                        body("{\n" +
                                "  \"request\": {}\n" +
                                "}").
                        with().
                        contentType(JSON).
                        when().
                        delete("/content/v3/discard/" + contentId).
                        then().log().all().
                        spec(get404ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

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

    private JsonPath updateContent(String contentId, String versionKey) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                        body("{\n" +
                                "  \"request\": {\n" +
                                "    \"content\": {\n" +
                                "      \"versionKey\": \"" + versionKey + "\",\n" +
                                "      \"language\": [\"English\",\"Hindi\"]\n" +
                                "    }\n" +
                                "  }\n" +
                                "}").
                        with().
                        contentType(JSON).
                        when().
                        patch("/content/v3/update/" + contentId).
                        then().log().all().
                        spec(get200ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

    private JsonPath reviewContent(String contentId) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, userId, APIToken)).
                        body("{\n" +
                                "  \"request\": {\n" +
                                "    \"content\": {\n" +
                                "    }\n" +
                                "  }\n" +
                                "}").
                        with().
                        contentType(JSON).
                        when().
                        post("/content/v3/review/" + contentId).
                        then().
                        spec(get200ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

    private String updateHierarchy(String identifier, String request) {
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                        body(request).
                        with().
                        contentType(JSON).
                        when().
                        patch("content/v3/hierarchy/update").
                        then().log().all().
                        extract().
                        response();
        JsonPath jsonResponse = response.jsonPath();
        return jsonResponse.get("responseCode");
    }

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

    private JsonPath flagContent(String contentId, String versionKey) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, userId, APIToken)).
                        body("{\n" +
                                "  \"request\": {\n" +
                                "      \"flagReasons\":[\"Copyright Violation\"],\n" +
                                "      \"flaggedBy\":\"Anuj\",\n" +
                                "      \"versionKey\": \"" + versionKey + "\",\n" +
                                "      \"flags\": [\"Email test\"]\n" +
                                "  }\n" +
                                "}").
                        with().
                        contentType(JSON).
                        when().
                        post("/content/v3/flag/" + contentId).
                        then().
                        spec(get200ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

    private JsonPath retireContent(String contentId) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                        body("{}").
                        with().
                        contentType(JSON).
                        when().
                        delete("/content/v3/retire/" + contentId).
                        then().
                        spec(get200ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

    private JsonPath readHierarchy(String contentId) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, userId, APIToken)).
                        when().
                        get("/content/v3/hierarchy/" + contentId).
                        then().log().all().
                        spec(get200ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

    private JsonPath readEditHierarchy(String contentId) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, userId, APIToken)).
                        when().
                        get("/content/v3/hierarchy/" + contentId + "?mode=edit").
                        then().
                        spec(get200ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

    /**
     *
     * @param contentId
     * @return
     */
    private JsonPath readEdit404Hierarchy(String contentId) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, userId, APIToken)).
                        when().
                        get("/content/v3/hierarchy/" + contentId + "?mode=edit").
                        then().
                        spec(get404ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

    private JsonPath read404Hierarchy(String contentId) {
        JsonPath resp;
        setURI();
        Response response =
                given().
                        spec(getRequestSpecification(contentType, userId, APIToken)).
                        when().
                        get("/content/v3/hierarchy/" + contentId).
                        then().
                        spec(get404ResponseSpec()).
                        extract().response();

        resp = response.jsonPath();
        return resp;
    }

}

