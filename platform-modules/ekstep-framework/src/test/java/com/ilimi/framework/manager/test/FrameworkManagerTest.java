package com.ilimi.framework.manager.test;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.framework.mgr.impl.ChannelManagerImpl;
import com.ilimi.framework.mgr.impl.FrameworkManagerImpl;
import com.ilimi.framework.test.common.TestParams;
import com.ilimi.framework.test.common.TestSetup;



/**
 * Unit Test Cases for Framework API.
 * @author gauraw
 *
 */
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FrameworkManagerTest extends TestSetup{
	
	private static String frameworkId;
	private static String channelId;
	
	private static ChannelManagerImpl channelManager = new ChannelManagerImpl();
	private static FrameworkManagerImpl frameworkManager = new FrameworkManagerImpl();
	
	static ObjectMapper mapper = new ObjectMapper();
	
	private static final String createFrameworkReq = "{\"name\": \"NCERT01\",\"description\": \"NCERT framework of Karnatka\",\"code\": \"org.ekstep.framework.create\"}";
	private static final String createChannelReq = "{\"name\":\"channelKA\",\"description\":\"\",\"code\":\"channelKA\",\"identifier\":\"channelKA\"}";
	private static final String createFrameworkReqJson = "{\"name\": \"KASB01\",\"description\": \"State Board framework of Karnatka\",\"code\": \"org.ekstep.framework.create\"}";
	private static final String updateFrameworkJson = "{\"versionKey\": \"1511787372693\",\"description\": \" framework description\",\"categories\": [{\"identifier\": \"do_11238579307347148811\",\"name\": \"cat3\"}]}";
	
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void initTest() throws Exception {
		loadDefinition("definitions/channel_definition.json", "definitions/framework_definition.json", "definitions/categoryInstance_definition.json");
		createChannel();
		createFramework();

	}

	@AfterClass
	public static void finishTest() {
		
	}
	
	/**
	 * Create Framework with Valid request body, Valid Channel
	 * 
	 * */
	@Test
	public void testFramework_01(){
		
		try{
			Map<String, Object> requestMap = mapper.readValue(createFrameworkReqJson,
					new TypeReference<Map<String, Object>>() {});
			Response response = frameworkManager.createFramework(requestMap, channelId);
			String frameworkId = (String) response.getResult().get(TestParams.node_id.name());
			String versionKey = (String) response.getResult().get(TestParams.versionKey.name());
			assertTrue(StringUtils.isNotBlank(frameworkId));
			assertTrue(StringUtils.isNotBlank(versionKey));
		}catch(Exception e){
			System.out.println("FrameworkManagerTest:::testFramework_01:::Exception : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Create Framework with valid request body, invalid channel id (channel id doesn't exist)
	 * 
	 * */
	@Test
	public void testFramework_02(){
		
		try{
			Map<String, Object> requestMap = mapper.readValue(createFrameworkReqJson,
					new TypeReference<Map<String, Object>>() {});
			String channelId = "test1234";
			Response response = frameworkManager.createFramework(requestMap, channelId);
			String responseCode=(String) response.getResponseCode().toString();
			int resCode=response.getResponseCode().code();
			assertTrue(responseCode.equals("CLIENT_ERROR"));
			assertTrue(resCode==400);
		}catch(Exception e){
			System.out.println("FrameworkManagerTest:::testFramework_02:::Exception : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Create Framework with invalid request body, valid channel id
	 * 
	 * */
	@Test
	public void testFramework_03(){
		
		try{
			Map<String, Object> requestMap=null;
			Response response = frameworkManager.createFramework(requestMap, channelId);
			String responseCode=(String) response.getResponseCode().toString();
			int resCode=response.getResponseCode().code();
			assertTrue(responseCode.equals("CLIENT_ERROR"));
			assertTrue(resCode==400);
		}catch(Exception e){
			System.out.println("FrameworkManagerTest:::testFramework_03:::Exception : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Read Framework with valid framework id
	 * 
	 * */
	@Test
	public void testFramework_04(){
		
		try{
			Response response = frameworkManager.readFramework(frameworkId);
			String responseCode=(String) response.getResponseCode().toString();
			int resCode=response.getResponseCode().code();
			assertTrue(responseCode.equals("OK"));
			assertTrue(resCode==200);
		}catch(Exception e){
			System.out.println("FrameworkManagerTest:::testFramework_04:::Exception : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Read Framework with Invalid framework id
	 * @throws Exception 
	 * 
	 * */
	@Test
	public void testFramework_05() throws Exception{
		
			exception.expect(ResourceNotFoundException.class);
			String frameworkId="test1234"; // Invalid framework id
		Response response = frameworkManager.readFramework(frameworkId);
			String responseCode=(String) response.getResponseCode().toString();
			int resCode=response.getResponseCode().code();
			assertTrue(responseCode.equals("ERR_DATA_NOT_FOUND"));
			assertTrue(resCode==404);
		
	}
	
	
	/**
	 * Update Framework with Invalid Owner
	 * 
	 * 
	 * */
	@Test
	public void testFramework_07() throws Exception{
		
			Map<String, Object> requestMap = mapper.readValue(updateFrameworkJson,
					new TypeReference<Map<String, Object>>() {});
			String channelId="test1234";
			Response response = frameworkManager.updateFramework(frameworkId, channelId, requestMap);
			String responseCode=(String) response.getResponseCode().toString();
			int resCode=response.getResponseCode().code();
			assertTrue(responseCode.equals("CLIENT_ERROR"));
			assertTrue(resCode==400);
	}
	
	/**
	 * Retire Framework with Invalid Owner
	 * 
	 * 
	 * */
	@Test
	public void testFramework_08() throws Exception{
			
			String channelId="test1234"; // Invalid Owner
			Response response = frameworkManager.retireFramework(frameworkId, channelId);
			String responseCode=(String) response.getResponseCode().toString();
			int resCode=response.getResponseCode().code();
			assertTrue(responseCode.equals("CLIENT_ERROR"));
			assertTrue(resCode==400);
	}
	
	
	
	private static void createFramework() {

		try {
			Map<String, Object> requestMap = mapper.readValue(createFrameworkReq,
					new TypeReference<Map<String, Object>>() {
					});
			Response resp = frameworkManager.createFramework(requestMap, channelId);
			frameworkId = (String) resp.getResult().get("node_id");
		} catch (Exception e) {
			System.out.println("Exception Occured while creating Framework :" + e.getMessage());
			e.printStackTrace();
		}

	}

	private static void createChannel() {
		try {
			Map<String, Object> requestMap = mapper.readValue(createChannelReq,
					new TypeReference<Map<String, Object>>() {
					});

			Response resp = channelManager.createChannel(requestMap);
			channelId = (String) resp.getResult().get(TestParams.node_id.name());
			System.out.println("Channel Id: " + channelId);
		} catch (Exception e) {
			System.out.println("Exception Occured while creating Channel :" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	
}