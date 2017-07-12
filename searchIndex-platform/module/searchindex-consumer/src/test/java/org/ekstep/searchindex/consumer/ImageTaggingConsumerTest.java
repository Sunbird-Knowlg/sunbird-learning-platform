package org.ekstep.searchindex.consumer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.searchindex.processor.ImageMessageProcessor;
import org.ekstep.searchindex.util.OptimizerUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })

public class ImageTaggingConsumerTest {

	@Autowired
	private WebApplicationContext context;
	MockMvc mockMvc;
	private ResultActions actions;

	/** The Image Optimiser Util. */
	private OptimizerUtil util = new OptimizerUtil();
	
	/** The Constant tempFileLocation. */
	private static final String tempFileLocation = "/data/contentBundle/";
	
	/** The Message Processor */
	private ImageMessageProcessor processor = new ImageMessageProcessor();
	
	List<String> nodes = new ArrayList<String>();
	String node_id;
	
//	@Test
	public void createAsset() {
		MockMvc mockMvc;
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
		String request = "{\"request\":{\"content\":{\"body\":\"<theme></theme>\",\"status\":\"Mock\",\"description\":\"शेर का साथी हाjklncksldnclsथी\",\"subject\":\"literacy\",\"name\":\"शेर का साथी हाथी\",\"owner\":\"EkStep\",\"code\":\"org.ekstep.mar8.asset\",\"mimeType\":\"image/png\",\"identifier\":\"org.ekstep.10.asset\",\"contentType\":\"Asset\",\"osId\":\"org.ekstep.quiz.app\"}}}";
		try {
			String path = "/v2/content";
			actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi")
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON_UTF8).content(request));
			Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jsonToObject(actions);
		System.out.println("result:" + resp.getResult().get("set_id"));
		node_id = (String) resp.getResult().get("node_id");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void test(){
		try {
			String[] url = AWSUploader.uploadFile("testAsset", new File("src/test/resources/images/share.jpg"));
			String str = "{\"eid\":\"BE_CONTENT_LIFECYCLE\",\"ets\":1485328918529,\"ver\":\"2.0\",\"pdata\":{\"ver\":\"1.0\",\"pid\":\"\",\"id\":\"org.ekstep.content.platform\"},\"edata\":{\"eks\":{\"size\":9050.0,\"concepts\":null,\"flags\":null,\"downloadUrl\": \""+ url[1] +"\",\"state\":\"Processing\",\"prevstate\":\"Processing\",\"pkgVersion\":null,\"cid\":\"org.ekstep.10.asset\"}}}";
			processor.processMessage(str);
			Node node = util.controllerUtil.getNode("domain", "org.ekstep.10.asset");
			List<String> list = (List) node.getMetadata().get("keywords");
			List<String> flags = (List) node.getMetadata().get("flags");
			Assert.assertEquals(false, list.isEmpty());
			Assert.assertEquals(false, flags.isEmpty());
			System.out.println("keywords :" + list + "flags : " + flags);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// method to convert resultActions to required response
		public Response jsonToObject(ResultActions actions) {
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
