package org.sunbird.content.mgr.impl.operation.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.util.HttpRestUtil;
import org.sunbird.content.util.PublishFinalizeUtil;
import org.sunbird.itemset.publish.ItemsetPublishManager;
import org.sunbird.learning.util.CloudStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpRestUtil.class})
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class RetireOperationTest {

	private static RetireOperation operation = new RetireOperation();
	private static ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testGetSearchRequest() throws Exception {
		Method method = RetireOperation.class.getDeclaredMethod("getSearchRequest", String.class);
		method.setAccessible(true);
		Map<String, Object> result = (Map<String, Object>) method.invoke(operation, "do_1234");
		assertTrue(MapUtils.isNotEmpty(result));
		String origin = (String)((Map<String, Object>)((Map<String, Object>)result.get("request")).get("filters")).get("origin");
		assertTrue(StringUtils.equalsIgnoreCase("do_1234",origin));
	}

	@Test
	public void testGetShallowCopy() throws Exception {
		PowerMockito.mockStatic(HttpRestUtil.class);
		when(HttpRestUtil.makePostRequest(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap())).thenReturn(getSearchResponse());
		Method method = RetireOperation.class.getDeclaredMethod("getShallowCopy", String.class);
		method.setAccessible(true);
		List<String> ids = (List<String>) method.invoke(operation, "do_1234");
		assertTrue(CollectionUtils.isNotEmpty(ids));
		assertTrue(ids.contains("do_21301022167565926411356"));
	}


	private Response getSearchResponse() throws Exception {
		String resp = "{\n" +
				"    \"id\": \"api.search-service.search\",\n" +
				"    \"ver\": \"3.0\",\n" +
				"    \"ts\": \"2020-04-29T17:27:48ZZ\",\n" +
				"    \"params\": {\n" +
				"        \"resmsgid\": \"4c58cf0a-4c07-4133-9a1f-6814794101c2\",\n" +
				"        \"msgid\": null,\n" +
				"        \"err\": null,\n" +
				"        \"status\": \"successful\",\n" +
				"        \"errmsg\": null\n" +
				"    },\n" +
				"    \"responseCode\": \"OK\",\n" +
				"    \"result\": {\n" +
				"        \"count\": 2,\n" +
				"        \"content\": [\n" +
				"            {\n" +
				"                \"identifier\": \"do_21301020852588544011345\",\n" +
				"                \"framework\": \"ka_k-12_1\",\n" +
				"                \"subject\": \"Science\",\n" +
				"                \"origin\": \"do_1234\",\n" +
				"                \"channel\": \"0124784842112040965\",\n" +
				"                \"originData\": \"{\\\"name\\\":\\\"april29\\\",\\\"copyType\\\":\\\"shallow\\\",\\\"license\\\":\\\"CC BY 4.0\\\",\\\"organisation\\\":[\\\"Framework Org\\\"],\\\"author\\\":\\\"KIRUBAAA\\\",\\\"pkgVersion\\\":1.0}\",\n" +
				"                \"medium\": \"English\",\n" +
				"                \"pkgVersion\": 2.0,\n" +
				"                \"board\": \"State (Karnataka)\",\n" +
				"                \"objectType\": \"Content\",\n" +
				"                \"status\": \"Live\"\n" +
				"            },\n" +
				"            {\n" +
				"                \"identifier\": \"do_21301022167565926411356\",\n" +
				"                \"framework\": \"ka_k-12_1\",\n" +
				"                \"subject\": \"Science\",\n" +
				"                \"origin\": \"do_1234\",\n" +
				"                \"channel\": \"012936585426509824100\",\n" +
				"                \"originData\": \"{\\\"name\\\":\\\"april29\\\",\\\"copyType\\\":\\\"shallow\\\",\\\"license\\\":\\\"CC BY 4.0\\\",\\\"organisation\\\":[\\\"Framework Org\\\"],\\\"author\\\":\\\"KIRUBAAA\\\",\\\"pkgVersion\\\":1.0}\",\n" +
				"                \"medium\": \"English\",\n" +
				"                \"pkgVersion\": 1.0,\n" +
				"                \"board\": \"State (Karnataka)\",\n" +
				"                \"objectType\": \"Content\",\n" +
				"                \"status\": \"Live\"\n" +
				"            }\n" +
				"        ]\n" +
				"    }\n" +
				"}";
		return mapper.readValue(resp, Response.class);
	}
}
