package com.ilimi.taxonomy.controller.game;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GameServiceTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private ResultActions actions;

    @Before
    public void setup() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @When("^Subject is (.*)$")
    public void getGames(String subject) throws Exception {
        Request req = getRequestObject();
        if (StringUtils.equalsIgnoreCase("blank", subject)) {

        } else {
            req.put("subject", subject);
        }
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(req);
        actions = mockMvc.perform(MockMvcRequestBuilders.post("/v1/game/list").content(content).contentType(MediaType.APPLICATION_JSON));
    }

    @Then("^return status is (.*) and response code is (\\d+)$")
    public void assertOKResult(String status, int code) throws Exception {
        actions.andExpect(MockMvcResultMatchers.status().is(code));
        Response response = getResponseObject(actions.andReturn().getResponse().getContentAsString());
        assertResponse(response, "ekstep.lp.game.list", status);
    }

    @SuppressWarnings("rawtypes")
    @Then("^return status is (.*) and response code is (\\d+) and games list size is (.*) (\\d+)$")
    public void assertInvalidSubjectResult(String status, int code, String op, int size) throws Exception {
        actions.andExpect(MockMvcResultMatchers.status().is(code));
        Response response = getResponseObject(actions.andReturn().getResponse().getContentAsString());
        assertResponse(response, "ekstep.lp.game.list", status);
        Assert.assertNotNull(response.getResult());
        Object object = response.get("games");
        Assert.assertNotNull(object);
        Assert.assertTrue(object instanceof List);
        List list = (List) object;
        if (StringUtils.equalsIgnoreCase("gt", op)) {
            Assert.assertTrue(list.size() > size);
        } else if (StringUtils.equalsIgnoreCase("lt", op)) {
            Assert.assertTrue(list.size() < size);
        } else {
            Assert.assertEquals(size, list.size());
        }
    }

    @When("^Status is (.*)$")
    public void getGamesWithStatusFilter(String status) throws Exception {
        Request req = getRequestObject();
        if (StringUtils.equalsIgnoreCase("invalid", status)) {
            req.put("status", status);
        } else {
            req.put("status", Arrays.asList(status));
        }
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(req);
        actions = mockMvc.perform(MockMvcRequestBuilders.post("/v1/game/list").content(content).contentType(MediaType.APPLICATION_JSON));
    }

    @When("^(.*) is (.*) and Status is (.*)$")
    public void getGamesWithMetadataFilter(String field, String value, String status) throws Exception {
        Request req = getRequestObject();
        req.put("status", Arrays.asList(status));
        req.put("fields", Arrays.asList(field));
        req.put(field, Arrays.asList(value));
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(req);
        actions = mockMvc.perform(MockMvcRequestBuilders.post("/v1/game/list").content(content).contentType(MediaType.APPLICATION_JSON));
    }

    @When("^Fields parameter is (.*)$")
    public void getGamesWithReturnFields(String field) throws Exception {
        Request req = getRequestObject();
        req.put("status", Arrays.asList("Mock"));
        if (StringUtils.equalsIgnoreCase("invalid", field)) {
            req.put("fields", field);
        } else {
            req.put("fields", Arrays.asList(field));
        }
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(req);
        actions = mockMvc.perform(MockMvcRequestBuilders.post("/v1/game/list").content(content).contentType(MediaType.APPLICATION_JSON));
    }

    @Then("^return status is (.*) and error code is (.*) and message is (.*)$")
    public void assertErrorCode(String status, String code, String message) throws Exception {
        Response response = getResponseObject(actions.andReturn().getResponse().getContentAsString());
        assertResponse(response, "ekstep.lp.game.list", status);
        Assert.assertEquals(code, response.getParams().getErr());
        Assert.assertNotNull(response.getParams().getErrmsg());
        Assert.assertEquals(message, response.getParams().getErrmsg());
    }

    @SuppressWarnings("unchecked")
    @Then("^return status is (.*) and games list has only (.*)$")
    public void assertReturnFields(String status, String field) throws Exception {
        Response response = getResponseObject(actions.andReturn().getResponse().getContentAsString());
        assertResponse(response, "ekstep.lp.game.list", status);
        Object object = response.get("games");
        Assert.assertNotNull(object);
        Assert.assertTrue(object instanceof List);
        List<Map<String, Object>> list = (List<Map<String, Object>>) object;
        Assert.assertTrue(list.size() > 0);
        for (Map<String, Object> map : list) {
            Assert.assertTrue(map.size() == 1);
            Assert.assertTrue(map.containsKey(field));
        }
    }

    @SuppressWarnings("unchecked")
    @Then("^return status is (.*) and (.*) value is (.*)$")
    public void assertMetadataSearch(String status, String field, String value) throws Exception {
        Response response = getResponseObject(actions.andReturn().getResponse().getContentAsString());
        assertResponse(response, "ekstep.lp.game.list", status);
        Object object = response.get("games");
        Assert.assertNotNull(object);
        Assert.assertTrue(object instanceof List);
        List<Map<String, Object>> list = (List<Map<String, Object>>) object;
        Assert.assertTrue(list.size() > 0);
        for (Map<String, Object> map : list) {
            Assert.assertTrue(map.size() == 1);
            Assert.assertTrue(map.containsKey(field));
            Assert.assertEquals(value, map.get(field));
        }
    }

    private void assertResponse(Response response, String apiId, String status) {
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getParams());
        Assert.assertEquals(apiId, response.getId());
        Assert.assertEquals(status, response.getParams().getStatus());
    }

    private Request getRequestObject() {
        Request req = new Request();
        req.setId("ekstep.lp.game.list");
        req.setVer("1.0");
        req.setTs(getTimestamp());
        RequestParams params = new RequestParams();
        params.setMsgid(getUUID());
        return req;
    }

    private Response getResponseObject(String content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Response response = mapper.readValue(content, Response.class);
        return response;
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'XXX");
        return sdf.format(new Date());
    }

    private String getUUID() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }
}
