package org.sunbird.content.operation.finalizer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.sunbird.learning.util.ControllerUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({})
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class ReviewFinalizerTest extends GraphEngineTestSetup{

	ObjectMapper mapper = new ObjectMapper();

	@BeforeClass
	public static void create() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/content_image_definition.json");
	}

	@AfterClass
	public static void destroy() {}
	
	
	// Hierarchy is not available for content in cassandra.
	@Rule
	@Test(expected = ClientException.class)
	public void validateResourceTest1() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		ReviewFinalizer reviewFinalizer = new ReviewFinalizer(controllerUtil);
		ReviewFinalizer reviewFinalizer1 = PowerMockito.spy(reviewFinalizer);
		
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("failed");
		response.setParams(params);
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416.img");
		
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416");
		
		Method method = ReviewFinalizer.class.getDeclaredMethod("validateResource", String.class);
		method.setAccessible(true);
		method.invoke(reviewFinalizer1, "do_11294852598716006416");
	}
	
	// Hierarchy is available for content in cassandra, but it's value is null
	@Test
	public void validateResourceTest2() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		ReviewFinalizer reviewFinalizer = new ReviewFinalizer(controllerUtil);
		ReviewFinalizer reviewFinalizer1 = PowerMockito.spy(reviewFinalizer);
		
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("failed");
		response.setParams(params);
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416.img");
		
		response = new Response();
		params = new ResponseParams();
		params.setStatus("successful");
		response.setParams(params);
		response.put("hierarchy", null);
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416");
		
		Method method = ReviewFinalizer.class.getDeclaredMethod("validateResource", String.class);
		method.setAccessible(true);
		method.invoke(reviewFinalizer1, "do_11294852598716006416");
	}
	
	// Hierarchy is available for content in cassandra, but hierarchy has child as null
	@Test
	public void validateResourceTest3() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
		
		String result = "{\"identifier\":\"do_11294852598716006416\",\"children\":null}";
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		ReviewFinalizer reviewFinalizer = new ReviewFinalizer(controllerUtil);
		ReviewFinalizer reviewFinalizer1 = PowerMockito.spy(reviewFinalizer);
		
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("failed");
		response.setParams(params);
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416.img");
		
		response = new Response();
		params = new ResponseParams();
		params.setStatus("successful");
		response.setParams(params);
		response.put("hierarchy", mapper.readValue(result, HashMap.class));
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416");
		
		Method method = ReviewFinalizer.class.getDeclaredMethod("validateResource", String.class);
		method.setAccessible(true);
		method.invoke(reviewFinalizer1, "do_11294852598716006416");
	}
	
	// Hierarchy is available for content in cassandra, but hierarchy has all published resource
	@Test
	public void validateResourceTest4() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
		
		String result = "{\"identifier\":\"do_11294852598716006416\",\"children\":[{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_11294919133176627219\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491358449008641116\",\"visibility\":\"Default\",\"pkgVersion\":1,\"name\":\"Test_PDF\",\"status\":\"Live\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360531415041117\",\"visibility\":\"Default\",\"pkgVersion\":3,\"name\":\"Test_PDF\",\"status\":\"Live\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360969687041118\",\"visibility\":\"Default\",\"pkgVersion\":1,\"name\":\"Test_PDF\",\"status\":\"Live\"}],\"name\":\"Unit 1\",\"contentType\":\"TextBookUnit\",\"status\":\"Live\"},{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_112949191331774464110\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491362874818561119\",\"visibility\":\"Default\",\"name\":\"Test_PDF\",\"status\":\"Live\"}],\"name\":\"Unit 2\",\"contentType\":\"TextBookUnit\",\"status\":\"Draft\"}]}";
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		ReviewFinalizer reviewFinalizer = new ReviewFinalizer(controllerUtil);
		ReviewFinalizer reviewFinalizer1 = PowerMockito.spy(reviewFinalizer);
		
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("failed");
		response.setParams(params);
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416.img");
		
		response = new Response();
		params = new ResponseParams();
		params.setStatus("successful");
		response.setParams(params);
		response.put("hierarchy", mapper.readValue(result, HashMap.class));
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416");
		
		Method method = ReviewFinalizer.class.getDeclaredMethod("validateResource", String.class);
		method.setAccessible(true);
		method.invoke(reviewFinalizer1, "do_11294852598716006416");
	}
	
	// Hierarchy is available for content in cassandra, hierarchy has non published resource - but error while fetching data from neo4j
	@Rule
	@Test(expected = ServerException.class)
	public void validateResourceTest5() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
		
		String result = "{\"identifier\":\"do_11294852598716006416\",\"children\":[{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_11294919133176627219\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491358449008641116\",\"visibility\":\"Default\",\"pkgVersion\":1,\"name\":\"Test_PDF\",\"status\":\"Draft\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360531415041117\",\"visibility\":\"Default\",\"pkgVersion\":3,\"name\":\"Test_PDF\",\"status\":\"Draft\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360969687041118\",\"visibility\":\"Default\",\"pkgVersion\":1,\"name\":\"Test_PDF\",\"status\":\"Live\"}],\"name\":\"Unit 1\",\"contentType\":\"TextBookUnit\",\"status\":\"Draft\"},{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_112949191331774464110\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491362874818561119\",\"visibility\":\"Default\",\"name\":\"Test_PDF\",\"status\":\"Draft\"}],\"name\":\"Unit 2\",\"contentType\":\"TextBookUnit\",\"status\":\"Draft\"}]}";
		Response response = new Response();
		response.setResponseCode(ResponseCode.SERVER_ERROR);
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(response);
		ReviewFinalizer reviewFinalizer = new ReviewFinalizer(controllerUtil);
		ReviewFinalizer reviewFinalizer1 = PowerMockito.spy(reviewFinalizer);
		
		response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("failed");
		response.setParams(params);
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416.img");
		
		response = new Response();
		params = new ResponseParams();
		params.setStatus("successful");
		response.setParams(params);
		response.put("hierarchy", mapper.readValue(result, HashMap.class));
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416");
		
		Method method = ReviewFinalizer.class.getDeclaredMethod("validateResource", String.class);
		method.setAccessible(true);
		method.invoke(reviewFinalizer1, "do_11294852598716006416");
	}
	
	// Hierarchy is available for content in cassandra, hierarchy has non published resource - no node object fetched
	@Rule
	@Test(expected = ServerException.class)
	public void validateResourceTest6() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
		
		String result = "{\"identifier\":\"do_11294852598716006416\",\"children\":[{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_11294919133176627219\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491358449008641116\",\"visibility\":\"Default\",\"pkgVersion\":1,\"name\":\"Test_PDF\",\"status\":\"Draft\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360531415041117\",\"visibility\":\"Default\",\"pkgVersion\":3,\"name\":\"Test_PDF\",\"status\":\"Draft\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360969687041118\",\"visibility\":\"Default\",\"pkgVersion\":1,\"name\":\"Test_PDF\",\"status\":\"Live\"}],\"name\":\"Unit 1\",\"contentType\":\"TextBookUnit\",\"status\":\"Draft\"},{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_112949191331774464110\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491362874818561119\",\"visibility\":\"Default\",\"name\":\"Test_PDF\",\"status\":\"Draft\"}],\"name\":\"Unit 2\",\"contentType\":\"TextBookUnit\",\"status\":\"Draft\"}]}";
		Response response = new Response();
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(response);
		ReviewFinalizer reviewFinalizer = new ReviewFinalizer(controllerUtil);
		ReviewFinalizer reviewFinalizer1 = PowerMockito.spy(reviewFinalizer);
		
		response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("failed");
		response.setParams(params);
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416.img");
		
		response = new Response();
		params = new ResponseParams();
		params.setStatus("successful");
		response.setParams(params);
		response.put("hierarchy", mapper.readValue(result, HashMap.class));
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416");
		
		Method method = ReviewFinalizer.class.getDeclaredMethod("validateResource", String.class);
		method.setAccessible(true);
		method.invoke(reviewFinalizer1, "do_11294852598716006416");
	}
	
	// Hierarchy is available for content in cassandra, hierarchy has non published resource - less node object fetched
	@Rule
	@Test(expected = ServerException.class)
	public void validateResourceTest7() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
		
		String result = "{\"identifier\":\"do_11294852598716006416\",\"children\":[{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_11294919133176627219\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491358449008641116\",\"visibility\":\"Default\",\"pkgVersion\":1,\"name\":\"Test_PDF\",\"status\":\"Draft\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360531415041117\",\"visibility\":\"Default\",\"pkgVersion\":3,\"name\":\"Test_PDF\",\"status\":\"Draft\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360969687041118\",\"visibility\":\"Default\",\"pkgVersion\":1,\"name\":\"Test_PDF\",\"status\":\"Live\"}],\"name\":\"Unit 1\",\"contentType\":\"TextBookUnit\",\"status\":\"Draft\"},{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_112949191331774464110\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491362874818561119\",\"visibility\":\"Default\",\"name\":\"Test_PDF\",\"status\":\"Draft\"}],\"name\":\"Unit 2\",\"contentType\":\"TextBookUnit\",\"status\":\"Draft\"}]}";
		Response response = new Response();
		
		Node resourceNode1 = new Node("domain", new HashMap<String, Object>() {
			{
				put("IL_UNIQUE_ID", "do_1129491362874818561119");
				put("IL_SYS_NODE_TYPE", "DATA_NODE");
				put("IL_FUNC_OBJECT_TYPE", "Content");
				put("visibility", "Default");
				put("status", "Draft");
				put("pkgVersion", null);
				put("mimeType", "application/pdf");
			}
		});
		Node resourceNode2 = new Node("domain", new HashMap<String, Object>() {
			{
				put("IL_UNIQUE_ID", "do_1129491358449008641116");
				put("IL_SYS_NODE_TYPE", "DATA_NODE");
				put("IL_FUNC_OBJECT_TYPE", "Content");
				put("visibility", "Default");
				put("status", "Live");
				put("pkgVersion", 1.0);
				put("mimeType", "application/pdf");
			}
		});
		response.put("node_list", Arrays.asList(resourceNode1, resourceNode2));
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(response);
		ReviewFinalizer reviewFinalizer = new ReviewFinalizer(controllerUtil);
		ReviewFinalizer reviewFinalizer1 = PowerMockito.spy(reviewFinalizer);
		
		response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("failed");
		response.setParams(params);
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416.img");
		
		response = new Response();
		params = new ResponseParams();
		params.setStatus("successful");
		response.setParams(params);
		response.put("hierarchy", mapper.readValue(result, HashMap.class));
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416");
		
		Method method = ReviewFinalizer.class.getDeclaredMethod("validateResource", String.class);
		method.setAccessible(true);
		method.invoke(reviewFinalizer1, "do_11294852598716006416");
	}
	
	// Hierarchy is available for content in cassandra, hierarchy has resource - with 3 Draft and 1 Live resource - Issue with two resource
	@Rule
	@Test(expected = ClientException.class)
	public void validateResourceTest8() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
		
		String result = "{\"identifier\":\"do_11294852598716006416\",\"children\":[{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_11294919133176627219\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491358449008641116\",\"visibility\":\"Default\",\"pkgVersion\":1.0,\"name\":\"Test_PDF\",\"status\":\"Draft\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360531415041117\",\"visibility\":\"Default\",\"pkgVersion\":3.0,\"name\":\"Test_PDF\",\"status\":\"Draft\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360969687041118\",\"visibility\":\"Default\",\"pkgVersion\":1.0,\"name\":\"Test_PDF\",\"status\":\"Live\"}],\"name\":\"Unit 1\",\"contentType\":\"TextBookUnit\",\"status\":\"Draft\"},{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_112949191331774464110\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491362874818561119\",\"visibility\":\"Default\",\"name\":\"Test_PDF\",\"status\":\"Draft\"}],\"name\":\"Unit 2\",\"contentType\":\"TextBookUnit\",\"status\":\"Draft\"}]}";
		
		Response response = new Response();
		Node resourceNode1 = new Node("domain", new HashMap<String, Object>() {
			{
				put("IL_UNIQUE_ID", "do_1129491362874818561119");
				put("IL_SYS_NODE_TYPE", "DATA_NODE");
				put("IL_FUNC_OBJECT_TYPE", "Content");
				put("visibility", "Default");
				put("status", "Draft");
				put("pkgVersion", null);
				put("mimeType", "application/pdf");
			}
		});
		Node resourceNode2 = new Node("domain", new HashMap<String, Object>() {
			{
				put("IL_UNIQUE_ID", "do_1129491358449008641116");
				put("IL_SYS_NODE_TYPE", "DATA_NODE");
				put("IL_FUNC_OBJECT_TYPE", "Content");
				put("visibility", "Default");
				put("status", "Live");
				put("pkgVersion", (Double)1.0);
				put("mimeType", "application/pdf");
			}
		});
		Node resourceNode3 = new Node("domain", new HashMap<String, Object>() {
			{
				put("IL_UNIQUE_ID", "do_1129491360531415041117");
				put("IL_SYS_NODE_TYPE", "DATA_NODE");
				put("IL_FUNC_OBJECT_TYPE", "Content");
				put("visibility", "Default");
				put("status", "Live");
				put("pkgVersion", (Double)4.0);
				put("mimeType", "application/pdf");
			}
		});
		response.put("node_list", Arrays.asList(resourceNode1, resourceNode2, resourceNode3));
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(response);
		ReviewFinalizer reviewFinalizer = new ReviewFinalizer(controllerUtil);
		ReviewFinalizer reviewFinalizer1 = PowerMockito.spy(reviewFinalizer);
		
		response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("failed");
		response.setParams(params);
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416.img");
		
		response = new Response();
		params = new ResponseParams();
		params.setStatus("successful");
		response.setParams(params);
		response.put("hierarchy", mapper.readValue(result, HashMap.class));
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416");
		
		Method method = ReviewFinalizer.class.getDeclaredMethod("validateResource", String.class);
		method.setAccessible(true);
		method.invoke(reviewFinalizer1, "do_11294852598716006416");
	}
	
	// Hierarchy is available for content in cassandra, hierarchy has resource - with 3 Draft and 1 Live resource - Issue with no resource
	@Test
	public void validateResourceTest9() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
		
		String result = "{\"identifier\":\"do_11294852598716006416\",\"children\":[{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_11294919133176627219\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491358449008641116\",\"visibility\":\"Default\",\"pkgVersion\":1.0,\"name\":\"Test_PDF\",\"status\":\"Draft\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360531415041117\",\"visibility\":\"Default\",\"pkgVersion\":3.0,\"name\":\"Test_PDF\",\"status\":\"Draft\"},{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491360969687041118\",\"visibility\":\"Default\",\"pkgVersion\":1.0,\"name\":\"Test_PDF\",\"status\":\"Live\"}],\"name\":\"Unit 1\",\"contentType\":\"TextBookUnit\",\"status\":\"Draft\"},{\"parent\":\"do_1129491370933207041121\",\"identifier\":\"do_112949191331774464110\",\"visibility\":\"Parent\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"children\":[{\"mimeType\":\"application/pdf\",\"contentType\":\"Resource\",\"identifier\":\"do_1129491362874818561119\",\"visibility\":\"Default\",\"name\":\"Test_PDF\",\"status\":\"Draft\"}],\"name\":\"Unit 2\",\"contentType\":\"TextBookUnit\",\"status\":\"Draft\"}]}";
		
		Response response = new Response();
		Node resourceNode1 = new Node("domain", new HashMap<String, Object>() {
			{
				put("IL_UNIQUE_ID", "do_1129491362874818561119");
				put("IL_SYS_NODE_TYPE", "DATA_NODE");
				put("IL_FUNC_OBJECT_TYPE", "Content");
				put("visibility", "Default");
				put("status", "Live");
				put("pkgVersion", (Double)1.0);
				put("mimeType", "application/pdf");
			}
		});
		Node resourceNode2 = new Node("domain", new HashMap<String, Object>() {
			{
				put("IL_UNIQUE_ID", "do_1129491358449008641116");
				put("IL_SYS_NODE_TYPE", "DATA_NODE");
				put("IL_FUNC_OBJECT_TYPE", "Content");
				put("visibility", "Default");
				put("status", "Live");
				put("pkgVersion", (Double)2.0);
				put("mimeType", "application/pdf");
			}
		});
		Node resourceNode3 = new Node("domain", new HashMap<String, Object>() {
			{
				put("IL_UNIQUE_ID", "do_1129491360531415041117");
				put("IL_SYS_NODE_TYPE", "DATA_NODE");
				put("IL_FUNC_OBJECT_TYPE", "Content");
				put("visibility", "Default");
				put("status", "Live");
				put("pkgVersion", (Double)4.0);
				put("mimeType", "application/pdf");
			}
		});
		response.put("node_list", Arrays.asList(resourceNode1, resourceNode2, resourceNode3));
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		PowerMockito.when(controllerUtil.getDataNodes(Mockito.anyString(), Mockito.anyList())).thenReturn(response);
		ReviewFinalizer reviewFinalizer = new ReviewFinalizer(controllerUtil);
		ReviewFinalizer reviewFinalizer1 = PowerMockito.spy(reviewFinalizer);
		
		response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("failed");
		response.setParams(params);
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416.img");
		
		response = new Response();
		params = new ResponseParams();
		params.setStatus("successful");
		response.setParams(params);
		response.put("hierarchy", mapper.readValue(result, HashMap.class));
		PowerMockito.doReturn(response).when(reviewFinalizer1).getCollectionHierarchy("do_11294852598716006416");
		
		Method method = ReviewFinalizer.class.getDeclaredMethod("validateResource", String.class);
		method.setAccessible(true);
		method.invoke(reviewFinalizer1, "do_11294852598716006416");
	}
}
