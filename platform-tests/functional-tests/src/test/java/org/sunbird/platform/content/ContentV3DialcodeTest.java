package org.sunbird.platform.content;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.hadoop.util.StringUtils;
import org.sunbird.platform.domain.BaseTest;
import org.sunbird.taxonomy.controller.ContentV3Controller;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;


/**
 * Functional test Cases Content Dialcodes Reserve API {@link ContentV3Controller#reserveDialCode(String, Map, String)} and
 * Dialcodes Release API {@link ContentV3Controller#releaseDialcodes(String, String)}.
 */
public class ContentV3DialcodeTest extends BaseTest {

    private String createTextBookRequest  = "{\"request\": {\"content\": {\"identifier\": \"LP_FT-" + IDENTIFIER + "\",\"mimeType\" : \"application/vnd.ekstep.content-collection\",\"contentType\": \"TextBook\",\"name\": \"LP_FT-"+ IDENTIFIER +"\",\"Description\": \"Test TextBook Content\",\"code\":\"LP_FT_CODE-"+ IDENTIFIER +"\"}}}";
    private String updateHierarchyReqBody = "{\"request\":{\"data\":{\"nodesModified\":{\"LP_FT-"+"__TextBookUnit_ID1__"+"\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"TextbookUnitunit1\",\"contentType\":\"TextBookUnit\",\"mimeType\":\"application/vnd.ekstep.content-collection\"}},\"LP_FT-"+"__TextBookUnit_ID2__"+"\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"TextbookUnitunit1\",\"contentType\":\"TextBookUnit\",\"mimeType\":\"application/vnd.ekstep.content-collection\"}},\"LP_FT-"+"__TextBookUnit_ID3__"+"\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"TextbookUnitunit1\",\"contentType\":\"TextBookUnit\",\"mimeType\":\"application/vnd.ekstep.content-collection\"}},\"LP_FT-"+"__TextBookUnit_ID4__"+"\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"TextbookUnitunit1\",\"contentType\":\"TextBookUnit\",\"mimeType\":\"application/vnd.ekstep.content-collection\"}},\"LP_FT-"+"__TextBookUnit_ID5__"+"\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"TextbookUnitunit1\",\"contentType\":\"TextBookUnit\",\"mimeType\":\"application/vnd.ekstep.content-collection\"}},\"LP_FT-"+"__TextBookUnit_ID6__"+"\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"TextbookUnitunit1\",\"contentType\":\"TextBookUnit\",\"mimeType\":\"application/vnd.ekstep.content-collection\"}}},\"hierarchy\":{\"LP_FT-"+"__TEXTBOOK_ID__"+"\":{\"contentType\":\"TextBook\",\"createdOn\":\"2018-02-05T16:38:10Z+05:30\",\"children\":[\"LP_FT-"+"__TextBookUnit_ID1__"+"\",\"LP_FT-"+"__TextBookUnit_ID2__"+"\",\"LP_FT-"+"__TextBookUnit_ID3__"+"\"],\"root\":true},\"LP_FT-"+"__TextBookUnit_ID1__"+"\":{\"name\":\"TextbookUnitunit1\",\"contentType\":\"TextBookUnit\",\"children\":[\"LP_FT-"+"__TextBookUnit_ID4__"+"\"],\"root\":false},\"LP_FT-"+"__TextBookUnit_ID2__"+"\":{\"name\":\"TextbookUnitunit1\",\"contentType\":\"TextBookUnit\",\"children\":[\"LP_FT-"+"__TextBookUnit_ID5__"+"\"],\"root\":false},\"LP_FT-"+"__TextBookUnit_ID3__"+"\":{\"name\":\"TextbookUnitunit1\",\"contentType\":\"TextBookUnit\",\"children\":[\"LP_FT-"+"__TextBookUnit_ID6__"+"\"],\"root\":false}},\"lastUpdatedBy\":\"shubham\"}}}";

    private final String COUNT = "__COUNT__";
    private String channelId = "in.ekstep";
    private String PUBLISHER = createPublisher();
    private String reserveDialcodesRequestBody = "{\"request\": {\"dialcodes\": {\"count\": "+ COUNT +",\"publisher\": \"" + PUBLISHER + "\"}}}";

    private String publishRequestBody = "{\"request\": {\"content\" : {\"lastPublishedBy\" : \"LP_FT\"}}}";
    private String updateRequestBody  = "{\"request\": {\"content\": {\"versionKey\":\"" + VERSION_KEY + "\", \"name\": \"UpdatedTextBook Name\"}}}";

    public RequestSpecification getContentDialcodeRequestSpecification(String contentType, String channelId) {
        RequestSpecBuilder builderreq = new RequestSpecBuilder();
        builderreq.addHeader("Content-Type", contentType);
        builderreq.addHeader("X-Channel-Id", channelId);
        RequestSpecification requestSpec = builderreq.build();
        return requestSpec;
    }

    private String createPublisher() {
        int rn = generateRandomInt(0, 999999);
        String createPublisherReq = "{\"request\": {\"publisher\": {\"identifier\":\"LP_FT-" + rn + "\",\"name\": \"LP_FT-"+ rn +"\"}}}";
        setURI();
        Response R = given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                body(createPublisherReq).with().contentType(JSON).when().post("/dialcode/v3/publisher/create").then().//log().all().
                extract().response();
        JsonPath jp = R.jsonPath();
        return jp.get("result.identifier");
    }

    private void linkDialCode(String identifier, String dialCodeId) {
        String dialCodeLinkReq = "{\"request\": {\"content\": {\"identifier\": [\"" + identifier + "\"],\"dialcode\": [\"" + dialCodeId + "\"]}}}";
        setURI();
        given().spec(getRequestSpecification(contentType, validuserId, APIToken, channelId, appId)).
                body(dialCodeLinkReq).with().contentType(JSON).when().post("/content/v3/dialcode/link").then().log().all().
                spec(get200ResponseSpec());
    }

    @Test
    public void reserve50DialcodesForTextBook() {
        setURI();
        String createTextBookrequest = this.createTextBookRequest.replace(IDENTIFIER, "" + generateRandomInt(0, 99999));
        String[] result = this.contentV3API.createAndGetResult(createTextBookrequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];
        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 50);
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        Map<String, Integer> reservedDialcodes = response.jsonPath().get("result.reservedDialcodes");
        assertEquals(50, reservedDialcodes.size());
    }

    @Test
    public void reserve50DialCodesForTextBookHavingImage() {
        setURI();
        String createTextBookrequest = this.createTextBookRequest.replace(IDENTIFIER, "" + generateRandomInt(0, 99999));
        String[] result = this.contentV3API.createAndGetResult(createTextBookrequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];

        this.contentV3API.publish(identifier, publishRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        Response response = null;
        String status = "";
        while(!StringUtils.equalsIgnoreCase("Live", status)) {
            delay(10000);
            response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
            status = response.jsonPath().get("result.content.status");
        }
        String versionKey = response.jsonPath().get("result.content.versionKey");
        String updateRequestBody = this.updateRequestBody.replace(VERSION_KEY, versionKey);
        this.contentV3API.update(identifier, updateRequestBody, getRequestSpecification(contentType, userId, APIToken), get200ResponseSpec());

        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 50);
        response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        Map<String, Integer> reservedDialcodes = response.jsonPath().get("result.reservedDialcodes");
        response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        Map<String, Integer> reservedDialcodesOriginalNode = response.jsonPath().get("result.content.reservedDialcodes");
        response = this.contentV3API.read(identifier, "edit", getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        Map<String, Integer> reservedDialcodesImageNode = response.jsonPath().get("result.content.reservedDialcodes");
        assertTrue(reservedDialcodesOriginalNode.keySet().containsAll(reservedDialcodes.keySet()));
        assertTrue(reservedDialcodesImageNode.keySet().containsAll(reservedDialcodes.keySet()));
    }

    @Test
    public void reserve300DialCodesForTextBook() {
        setURI();
        String createTextBookRequest = this.createTextBookRequest.replace(IDENTIFIER, "" + generateRandomInt(0, 99999));
        String[] result = this.contentV3API.createAndGetResult(createTextBookRequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];
        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 300);
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get400ResponseSpec());
        assertEquals("ERR_INVALID_COUNT", response.jsonPath().get("params.err"));
    }

    @Test
    public void reserveDialcodesTwiceWithSameCount() {
        setURI();
        String createTextBookRequest = this.createTextBookRequest.replace(IDENTIFIER, "" + generateRandomInt(0, 99999));
        String[] result = this.contentV3API.createAndGetResult(createTextBookRequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];

        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 20);
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> reservedDialcodes = response.jsonPath().get("result.reservedDialcodes");
        assertEquals(20, reservedDialcodes.size());

        response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get400ResponseSpec());
        List<String> reservedDialcodesNew = response.jsonPath().get("result.reservedDialcodes");
        assertEquals(20, reservedDialcodesNew.size());
        assertTrue(reservedDialcodesNew.containsAll(reservedDialcodes));

    }

    @Test
    public void reserveDialcodesTwiceWithHigherCountSecondTime() {
        setURI();
        String createTextBookRequest = this.createTextBookRequest.replace(IDENTIFIER, "" + generateRandomInt(0, 99999));
        String[] result = this.contentV3API.createAndGetResult(createTextBookRequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];

        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 20);
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> reservedDialcodes = response.jsonPath().get("result.reservedDialcodes");
        assertEquals(20, reservedDialcodes.size());

        reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 25);
        response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> reservedDialcodesNew = response.jsonPath().get("result.reservedDialcodes");
        assertEquals(25, reservedDialcodesNew.size());
        assertTrue(reservedDialcodesNew.containsAll(reservedDialcodes));

    }

    @Test
    public void reserveDialcodesTwiceWithLowerCountSecondTime() {
        setURI();
        String createTextBookRequest = this.createTextBookRequest.replace(IDENTIFIER, "" + generateRandomInt(0, 99999));
        String[] result = this.contentV3API.createAndGetResult(createTextBookRequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];

        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 20);
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> reservedDialcodes = response.jsonPath().get("result.reservedDialcodes");
        assertEquals(20, reservedDialcodes.size());

        reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 15);
        response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get400ResponseSpec());
        List<String> reservedDialcodesNew = response.jsonPath().get("result.reservedDialcodes");
        assertEquals(20, reservedDialcodesNew.size());
        assertTrue(reservedDialcodesNew.containsAll(reservedDialcodes));

    }

    @Test
    public void reserveZeroDialCodes() {
        setURI();
        String createTextBookRequest = this.createTextBookRequest.replace(IDENTIFIER, "" + generateRandomInt(0, 99999));
        String[] result = this.contentV3API.createAndGetResult(createTextBookRequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];

        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 0);
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get400ResponseSpec());
        assertEquals("ERR_INVALID_COUNT", response.jsonPath().get("params.err"));

    }

    @Test
    public void reserveDialcodesWithInvalidPublisher() {
        setURI();
        String createTextBookRequest = this.createTextBookRequest.replace(IDENTIFIER, "" + generateRandomInt(0, 99999));
        String[] result = this.contentV3API.createAndGetResult(createTextBookRequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];

        String reserveDialcodesRequestBody = "{\"request\": {\"dialcodes\": {\"count\":90,\"publisher\": \"INVALID_PUBLISHER\"}}}";
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get400ResponseSpec());
        assertEquals("ERR_INVALID_PUBLISHER", response.jsonPath().get("params.err"));
    }

    @Test
    public void releaseAllDialcodesForTextBook() {
        setURI();
        String createTextBookrequest = this.createTextBookRequest.replace(IDENTIFIER, "" + generateRandomInt(0, 99999));
        String[] result = this.contentV3API.createAndGetResult(createTextBookrequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];
        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 50);
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> reservedDialcodes = response.jsonPath().get("result.reservedDialcodes");
        Response responseRelease = this.contentV3API.releaseDialcodes(identifier, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> releasedDialcodes = responseRelease.jsonPath().get("result.releasedDialcodes");
        assertTrue(releasedDialcodes.containsAll(reservedDialcodes));
    }

    @Test
    public void releaseAllDialcodesForTextBookHavingImage() {
        setURI();
        String createTextBookrequest = this.createTextBookRequest.replace(IDENTIFIER, "" + generateRandomInt(0, 99999));
        String[] result = this.contentV3API.createAndGetResult(createTextBookrequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];

        this.contentV3API.publish(identifier, publishRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        Response response = null;
        String status = "";
        while(!StringUtils.equalsIgnoreCase("Live", status)) {
            delay(10000);
            response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
            status = response.jsonPath().get("result.content.status");
        }
        String versionKey = response.jsonPath().get("result.content.versionKey");
        String updateRequestBody = this.updateRequestBody.replace(VERSION_KEY, versionKey);
        this.contentV3API.update(identifier, updateRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());

        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 50);
        response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> reservedDialcodes = response.jsonPath().get("result.reservedDialcodes");

        Response responseRelease = this.contentV3API.releaseDialcodes(identifier, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> releasedDialcodes = responseRelease.jsonPath().get("result.releasedDialcodes");
        assertTrue(releasedDialcodes.containsAll(reservedDialcodes));

        response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        assertNull(response.jsonPath().get("result.content.reservedDialcodes"));

        response = this.contentV3API.read(identifier, "edit", getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        assertNull(response.jsonPath().get("result.content.reservedDialcodes"));
    }

    @Test
    public void releaseAllNotLinkedDialcodesForTextBook() {
        setURI();
        String textBookSuffix = "" + generateRandomInt(0, 99999);
        String createTextBookrequest = this.createTextBookRequest.replace(IDENTIFIER, textBookSuffix);
        String[] result = this.contentV3API.createAndGetResult(createTextBookrequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];

        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 50);
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> reservedDialcodes = response.jsonPath().get("result.reservedDialcodes");

        String textBookUnit1Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit2Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit3Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit4Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit5Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit6Suffix = "" + generateRandomInt(0, 999999);
        String updateHierarchyReqBody =  this.updateHierarchyReqBody.replace("__TEXTBOOK_ID__", textBookSuffix).
                replace("__TextBookUnit_ID1__", textBookUnit1Suffix).
                replace("__TextBookUnit_ID2__", textBookUnit2Suffix).
                replace("__TextBookUnit_ID3__", textBookUnit3Suffix).
                replace("__TextBookUnit_ID4__", textBookUnit4Suffix).
                replace("__TextBookUnit_ID5__", textBookUnit5Suffix).
                replace("__TextBookUnit_ID6__", textBookUnit6Suffix);
        this.contentV3API.hierarchyUpdate(updateHierarchyReqBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());

        response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> children = ((List<Map<String, Object>>) response.jsonPath().get("result.content.children")).
                stream().
                map(child -> (String) child.get("identifier")).
                collect(toList());

        linkDialCode(children.get(0), reservedDialcodes.get(0));

        response = this.contentV3API.releaseDialcodes(identifier, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> releasedDialcodes = response.jsonPath().get("result.releasedDialcodes");
        assertEquals(49, releasedDialcodes.size());
        assertFalse(releasedDialcodes.contains(reservedDialcodes.get(0)));

        response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> updatedReservedDialcodes = response.jsonPath().get("result.content.reservedDialcodes");
        assertEquals(1, updatedReservedDialcodes.size());
        assertTrue(updatedReservedDialcodes.contains(reservedDialcodes.get(0)));
    }

    @Test
    public void releaseAllNotLinkedDialcodesForLiveTextBook() {
        setURI();
        String textBookSuffix = "" + generateRandomInt(0, 99999);
        String createTextBookrequest = this.createTextBookRequest.replace(IDENTIFIER, textBookSuffix);
        String[] result = this.contentV3API.createAndGetResult(createTextBookrequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];

        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 50);
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> reservedDialcodes = response.jsonPath().get("result.reservedDialcodes");

        String textBookUnit1Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit2Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit3Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit4Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit5Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit6Suffix = "" + generateRandomInt(0, 999999);
        String updateHierarchyReqBody =  this.updateHierarchyReqBody.replace("__TEXTBOOK_ID__", textBookSuffix).
                replace("__TextBookUnit_ID1__", textBookUnit1Suffix).
                replace("__TextBookUnit_ID2__", textBookUnit2Suffix).
                replace("__TextBookUnit_ID3__", textBookUnit3Suffix).
                replace("__TextBookUnit_ID4__", textBookUnit4Suffix).
                replace("__TextBookUnit_ID5__", textBookUnit5Suffix).
                replace("__TextBookUnit_ID6__", textBookUnit6Suffix);
        this.contentV3API.hierarchyUpdate(updateHierarchyReqBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());

        this.contentV3API.publish(identifier, publishRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String status = "";
        while(!StringUtils.equalsIgnoreCase("Live", status)) {
            delay(10000);
            response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
            status = response.jsonPath().get("result.content.status");
        }

        response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> children = ((List<Map<String, Object>>) response.jsonPath().get("result.content.children")).
                stream().
                map(child -> (String) child.get("identifier")).
                collect(toList());

        linkDialCode(children.get(0), reservedDialcodes.get(0));

        response = this.contentV3API.releaseDialcodes(identifier, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> releasedDialcodes = response.jsonPath().get("result.releasedDialcodes");
        assertEquals(49, releasedDialcodes.size());
        assertFalse(releasedDialcodes.contains(reservedDialcodes.get(0)));

        response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> updatedReservedDialcodes = response.jsonPath().get("result.content.reservedDialcodes");
        assertEquals(1, updatedReservedDialcodes.size());
        assertTrue(updatedReservedDialcodes.contains(reservedDialcodes.get(0)));
    }

    @Test
    public void releaseAllNotLinkedDialcodesForTextBookHavingImage() {
        setURI();
        String textBookSuffix = "" + generateRandomInt(0, 99999);
        String createTextBookrequest = this.createTextBookRequest.replace(IDENTIFIER, textBookSuffix);
        String[] result = this.contentV3API.createAndGetResult(createTextBookrequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];

        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 50);
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> reservedDialcodes = response.jsonPath().get("result.reservedDialcodes");

        String textBookUnit1Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit2Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit3Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit4Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit5Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit6Suffix = "" + generateRandomInt(0, 999999);
        String updateHierarchyReqBody =  this.updateHierarchyReqBody.replace("__TEXTBOOK_ID__", textBookSuffix).
                replace("__TextBookUnit_ID1__", textBookUnit1Suffix).
                replace("__TextBookUnit_ID2__", textBookUnit2Suffix).
                replace("__TextBookUnit_ID3__", textBookUnit3Suffix).
                replace("__TextBookUnit_ID4__", textBookUnit4Suffix).
                replace("__TextBookUnit_ID5__", textBookUnit5Suffix).
                replace("__TextBookUnit_ID6__", textBookUnit6Suffix);
        this.contentV3API.hierarchyUpdate(updateHierarchyReqBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());

        this.contentV3API.publish(identifier, publishRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String status = "";
        while(!StringUtils.equalsIgnoreCase("Live", status)) {
            delay(10000);
            response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
            status = response.jsonPath().get("result.content.status");
        }

        String versionKey = response.jsonPath().get("result.content.versionKey");
        String updateRequestBody = this.updateRequestBody.replace(VERSION_KEY, versionKey);
        this.contentV3API.update(identifier, updateRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());

        response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> children = ((List<Map<String, Object>>) response.jsonPath().get("result.content.children")).
                stream().
                map(child -> (String) child.get("identifier")).
                collect(toList());

        linkDialCode(children.get(0), reservedDialcodes.get(0));

        response = this.contentV3API.releaseDialcodes(identifier, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> releasedDialcodes = response.jsonPath().get("result.releasedDialcodes");
        assertEquals(49, releasedDialcodes.size());
        assertFalse(releasedDialcodes.contains(reservedDialcodes.get(0)));

        response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        assertNotNull(response.jsonPath().get("result.content.reservedDialcodes"));
        List<String> updatedReservedDialcodes = response.jsonPath().get("result.content.reservedDialcodes");
        assertEquals(1, updatedReservedDialcodes.size());
        assertTrue(updatedReservedDialcodes.contains(reservedDialcodes.get(0)));

        response = this.contentV3API.read(identifier, "edit", getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        assertNotNull(response.jsonPath().get("result.content.reservedDialcodes"));
        List<String> updatedReservedDialcodesImageNode = response.jsonPath().get("result.content.reservedDialcodes");
        assertEquals(1, updatedReservedDialcodesImageNode.size());
        assertTrue(updatedReservedDialcodesImageNode.contains(reservedDialcodes.get(0)));
    }

    @Test
    public void releaseAllNotLinkedDialcodesWithTextBookHavingLinkedDialcodesAndImageNode() {
        setURI();
        String textBookSuffix = "" + generateRandomInt(0, 99999);
        String createTextBookrequest = this.createTextBookRequest.replace(IDENTIFIER, textBookSuffix);
        String[] result = this.contentV3API.createAndGetResult(createTextBookrequest, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String identifier = result[0];

        String reserveDialcodesRequestBody = this.reserveDialcodesRequestBody.replace(COUNT, "" + 50);
        Response response = this.contentV3API.reserveDialcode(identifier, reserveDialcodesRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> reservedDialcodes = response.jsonPath().get("result.reservedDialcodes");

        String textBookUnit1Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit2Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit3Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit4Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit5Suffix = "" + generateRandomInt(0, 999999);
        String textBookUnit6Suffix = "" + generateRandomInt(0, 999999);
        String updateHierarchyReqBody =  this.updateHierarchyReqBody.replace("__TEXTBOOK_ID__", textBookSuffix).
                replace("__TextBookUnit_ID1__", textBookUnit1Suffix).
                replace("__TextBookUnit_ID2__", textBookUnit2Suffix).
                replace("__TextBookUnit_ID3__", textBookUnit3Suffix).
                replace("__TextBookUnit_ID4__", textBookUnit4Suffix).
                replace("__TextBookUnit_ID5__", textBookUnit5Suffix).
                replace("__TextBookUnit_ID6__", textBookUnit6Suffix);
        this.contentV3API.hierarchyUpdate(updateHierarchyReqBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());

        this.contentV3API.publish(identifier, publishRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        String status = "";
        while(!StringUtils.equalsIgnoreCase("Live", status)) {
            delay(10000);
            response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
            status = response.jsonPath().get("result.content.status");
        }

        String versionKey = response.jsonPath().get("result.content.versionKey");
        String updateRequestBody = this.updateRequestBody.replace(VERSION_KEY, versionKey);
        this.contentV3API.update(identifier, updateRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());

        response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> children = ((List<Map<String, Object>>) response.jsonPath().get("result.content.children")).
                stream().
                map(child -> (String) child.get("identifier")).
                collect(toList());

        response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        versionKey = response.jsonPath().get("result.content.versionKey");
        updateRequestBody = this.updateRequestBody.replace(VERSION_KEY, versionKey);
        this.contentV3API.update(children.get(0), updateRequestBody, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());

        linkDialCode(identifier, reservedDialcodes.get(0));
        linkDialCode(children.get(0), reservedDialcodes.get(1));
        linkDialCode(children.get(1), reservedDialcodes.get(2));

        response = this.contentV3API.releaseDialcodes(identifier, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        List<String> releasedDialcodes = response.jsonPath().get("result.releasedDialcodes");
        assertEquals(47, releasedDialcodes.size());
        assertFalse(releasedDialcodes.contains(reservedDialcodes.get(0)));

        response = this.contentV3API.read(identifier, null, getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        assertNotNull(response.jsonPath().get("result.content.reservedDialcodes"));
        List<String> updatedReservedDialcodes = response.jsonPath().get("result.content.reservedDialcodes");
        assertEquals(3, updatedReservedDialcodes.size());
        assertTrue(updatedReservedDialcodes.contains(reservedDialcodes.get(0)));
        assertTrue(updatedReservedDialcodes.contains(reservedDialcodes.get(1)));
        assertTrue(updatedReservedDialcodes.contains(reservedDialcodes.get(2)));

        response = this.contentV3API.read(identifier, "edit", getRequestSpecification(contentType, validuserId, APIToken, channelId, appId), get200ResponseSpec());
        assertNotNull(response.jsonPath().get("result.content.reservedDialcodes"));
        List<String> updatedReservedDialcodesImageNode = response.jsonPath().get("result.content.reservedDialcodes");
        assertEquals(3, updatedReservedDialcodesImageNode.size());
        assertTrue(updatedReservedDialcodesImageNode.contains(reservedDialcodes.get(0)));
        assertTrue(updatedReservedDialcodesImageNode.contains(reservedDialcodes.get(1)));
        assertTrue(updatedReservedDialcodesImageNode.contains(reservedDialcodes.get(2)));
    }

}
