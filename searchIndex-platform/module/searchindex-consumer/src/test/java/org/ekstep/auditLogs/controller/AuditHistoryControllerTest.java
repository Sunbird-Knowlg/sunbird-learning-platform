package org.ekstep.auditLogs.controller;
import static org.junit.Assert.assertEquals;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.ilimi.common.dto.Response;

@FixMethodOrder(MethodSorters.DEFAULT)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class AuditHistoryControllerTest {

	@Autowired
	private WebApplicationContext context;
	private ResultActions actions;
    final private static String graphId = "test";
    final private String objectId = "test_word";
    final private String objectType = "Word";
    final private String InvalidObjectId = "xyz";
   
    @Test
    public void before(){
    	AuditHistoryHelper helper = new AuditHistoryHelper();
    	helper.create();
    	helper.createNodeProperties();
    	helper.update();
    	helper.updateNodeRelation();
    	helper.updateNodeTag();
    }
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void getAllAuditLogs() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/" + graphId + "/all?start=2016-12-10T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.getMessage();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List<Object> audit_record = (List) result.get("audit_history_record");
		assertEquals(false, audit_record.isEmpty());
		for (Object record : audit_record) {
			Map<String, Object> map = (Map) record;
			assertEquals(true, map.containsKey("logRecord"));
			assertEquals(graphId, map.get("graphId"));
			assertEquals(objectType, map.get("objectType"));
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void getAllAuditLogsByInvalidGraphId() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/logs/all?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List<Object> audit_record = (List) result.get("audit_history_record");
		assertEquals(false, audit_record.isEmpty());
	}
	
	@Test
	public void getAllAuditLogsByInvalidUrl() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audits/logs/all?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getAllAuditLogsWithBlankGraphId() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/all?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void getAuditLogsByValidObjectType() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/test/" + objectType + "?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List<Object> audit_record = (List) result.get("audit_history_record");
		assertEquals(false, audit_record.isEmpty());
		for (Object record : audit_record) {
			Map<String, Object> map = (Map) record;
			assertEquals(true, map.containsKey("logRecord"));
			assertEquals(graphId, map.get("graphId"));
			assertEquals(objectType, map.get("objectType"));
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void getAuditLogsByInValidObjectType() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/test/domains?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List<Object> audit_record = (List) result.get("audit_history_record");
		assertEquals(true, audit_record.isEmpty());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void getAllAuditLogsByInValidObjectTypeGraphId() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/testData/domains?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List<Object> audit_record = (List) result.get("audit_history_record");
		assertEquals(false, audit_record.isEmpty());
	}
	
	@Test
	public void getAllAuditLogsWithoutObjectType() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/test?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getAllAuditLogsWithoutStartDate() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/test";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getAllAuditLogsWithInvalidStartDate() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/test?start=2017-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getAllAuditLogsWithInvalidDateFormat() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/test?start=2017/05/26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void getAllAuditLogsByValidObjectId() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/history/" + graphId + "/" +objectId + "?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List<Object> audit_record = (List) result.get("audit_history_record");
		assertEquals(false, audit_record.isEmpty());
		for (Object record : audit_record) {
			Map<String, Object> map = (Map) record;
			assertEquals(true, map.containsKey("logRecord"));
			assertEquals(graphId, map.get("graphId"));
			assertEquals(true, map.get("objectId").toString().startsWith(objectId));
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void getAllAuditLogsByInValidObjectId() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/history/" + graphId + "/" + InvalidObjectId + "?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List<Object> audit_record = (List) result.get("audit_history_record");
		assertEquals(true, audit_record.isEmpty());
		assertEquals(0, audit_record.size());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void getAllAuditLogsWithoutObjectId() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/history/" + graphId + "?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response response = jsonToObject(actions);
		Assert.assertEquals("successful", response.getParams().getStatus());
		Map<String, Object> result = response.getResult();
		List<Object> audit_record = (List) result.get("audit_history_record");
		assertEquals(false, audit_record.isEmpty());
	}

	@Test
	public void getAllAuditLogsWithoutObjectIdandGraphId() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/hisjtory?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getAllAuditLogsWithInvalidUrl() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String path = "/v1/audit/hisjtory/" + graphId + "/" + InvalidObjectId + "?start=2016-05-26T13:00:00";
		try {
			actions = mockMvc.perform(MockMvcRequestBuilders.get(path).header("user-id", "ilimi"));
			Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Response jsonToObject(ResultActions actions) {
		String content = null;
		Response resp = null;
		try {
			content = actions.andReturn().getResponse().getContentAsString();
			ObjectMapper objectMapper = new ObjectMapper();
			if (StringUtils.isNotBlank(content))
				resp = objectMapper.readValue(content, Response.class);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}
}