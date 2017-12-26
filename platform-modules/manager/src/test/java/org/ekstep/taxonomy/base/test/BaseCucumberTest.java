package org.ekstep.taxonomy.base.test;



import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Response;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


public abstract class BaseCucumberTest {
	
	@Autowired 
    protected WebApplicationContext context;
	
	protected MockMvc mockMvc;
	
	public abstract void setup() throws IOException;
	
	public void initMockMVC() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
	
	public ResultActions resultActionGet(String path, Map<String, String> params, MediaType contentType, Map<String, String> header, MockMvc mockMvc) {
    	ResultActions actions = null;
    	MockHttpServletRequestBuilder builder = get(path);
		for (Entry<String, String> entry : params.entrySet()) {
			builder.param(entry.getKey(), entry.getValue());
		}
		builder.contentType(contentType);
		for (Entry<String, String> entry : header.entrySet()){
			builder.header(entry.getKey(), entry.getValue());
		}
		try {
			if(builder != null)
				actions = mockMvc.perform(builder);			
		} catch (Exception e) {
			e.printStackTrace();
		} 		
    	return actions;
    }
	
	public ResultActions resultActionDelete(String path, Map<String, String> params, MediaType contentType, Map<String, String> header, MockMvc mockMvc) {
    	ResultActions actions = null;
    	MockHttpServletRequestBuilder builder = delete(path);
		for (Entry<String, String> entry : params.entrySet()) {
			builder.param(entry.getKey(), entry.getValue());
		}
		builder.contentType(contentType);
		for (Entry<String, String> entry : header.entrySet()){
			builder.header(entry.getKey(), entry.getValue());
		}
		try {
			actions = mockMvc.perform(builder);			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
    	return actions;
    }
    
	public ResultActions resultActionPatch(String contentString, String path, Map<String, String> params, MediaType contentType, Map<String, String> header, MockMvc mockMvc) {
    	ResultActions actions = null;
    	MockHttpServletRequestBuilder builder = patch(path);
		for (Entry<String, String> entry : params.entrySet()) {
			builder.param(entry.getKey(), entry.getValue());
		}
		builder.contentType(contentType).content(contentString.getBytes());
		for (Entry<String, String> entry : header.entrySet()){
			builder.header(entry.getKey(), entry.getValue());
		}
		try {									
			actions = mockMvc.perform(builder);			
		} catch (Exception e) {
			e.printStackTrace();
		} 		
    	return actions;
    }
	
    public ResultActions resultActionPost(String contentString, String path, Map<String, String> params, MediaType contentType, Map<String, String> header, MockMvc mockMvc) {
    	ResultActions actions = null;
    	MockHttpServletRequestBuilder builder = post(path);
		for (Entry<String, String> entry : params.entrySet()) {
			builder.param(entry.getKey(), entry.getValue());
		}
		builder.contentType(contentType).content(contentString.getBytes());
		for (Entry<String, String> entry : header.entrySet()){
			builder.header(entry.getKey(), entry.getValue());
		}
		try {
			actions = mockMvc.perform(builder);	
		} catch (Exception e) {
			e.printStackTrace();
		} 		
    	return actions;
    }
    
    public Response jasonToObject(ResultActions actions) {
    	String content = null;
		try {
			content = actions.andReturn().getResponse().getContentAsString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        ObjectMapper objectMapper = new ObjectMapper();
        Response resp = null;
        try {
			if(StringUtils.isNotBlank(content))
			    resp = objectMapper.readValue(content, Response.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return resp;
    }
    
    public void assertStatus(ResultActions actions, int code) {
        try {
            Assert.assertEquals(code, actions.andReturn().getResponse().getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
