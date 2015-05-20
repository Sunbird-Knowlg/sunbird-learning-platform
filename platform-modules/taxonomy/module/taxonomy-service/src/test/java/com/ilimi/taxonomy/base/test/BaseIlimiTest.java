package com.ilimi.taxonomy.base.test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.ilimi.common.dto.Response;


public class BaseIlimiTest {
	
	@Autowired 
    protected WebApplicationContext context;
    
	protected MockMvc mockMvc;
    
    @Before
    public void setup() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
	
	public ResultActions resultActionGet(String path, Map<String, String> params, MediaType contentType, Map<String, String> header, MockMvc mockMvc) {
    	ResultActions actions = null;
    	MockHttpServletRequestBuilder builder = get(path);
		for (Entry<String, String> entry : params.entrySet()) {
			builder.param(entry.getKey(), entry.getValue());
			System.out.println(entry.getKey() +"::"+entry.getValue());
		}
		builder.contentType(contentType);
		for (Entry<String, String> entry : header.entrySet()){
			builder.header(entry.getKey(), entry.getValue());
			System.out.println(entry.getKey() +"::"+entry.getValue());
		}
		try {
			if(builder != null)
				System.out.println("Hello");
				actions = mockMvc.perform(builder);			
			actions.andDo(MockMvcResultHandlers.print());
		} catch (Exception e) {
			System.out.println("jitendrasinghsankhwR");
			e.printStackTrace();
		} 		
    	return actions;
    }
	
	public ResultActions resultActionDelete(String path, Map<String, String> params, MediaType contentType, Map<String, String> header, MockMvc mockMvc) {
    	ResultActions actions = null;
    	MockHttpServletRequestBuilder builder = delete(path);
		for (Entry<String, String> entry : params.entrySet()) {
			builder.param(entry.getKey(), entry.getValue());
			System.out.println(entry.getKey() +"::"+entry.getValue());
		}
		builder.contentType(contentType);
		for (Entry<String, String> entry : header.entrySet()){
			builder.header(entry.getKey(), entry.getValue());
		}
		try {
			System.out.println(builder.toString());
			actions = mockMvc.perform(builder);			
			actions.andDo(MockMvcResultHandlers.print());
		} catch (Exception e) {
			System.out.println("jitendrasinghsankhwR");
			e.printStackTrace();
		} 		
    	return actions;
    }
    
	public ResultActions resultActionPatch(String contentString, String path, Map<String, String> params, MediaType contentType, Map<String, String> header, MockMvc mockMvc) {
    	ResultActions actions = null;
    	MockHttpServletRequestBuilder builder = patch(path);
		for (Entry<String, String> entry : params.entrySet()) {
			builder.param(entry.getKey(), entry.getValue());
			System.out.println(entry.getKey() +"::"+entry.getValue());
		}
		builder.contentType(contentType).content(contentString.getBytes());
		for (Entry<String, String> entry : header.entrySet()){
			builder.header(entry.getKey(), entry.getValue());
		}
		try {
			System.out.println(builder.toString());
			actions = mockMvc.perform(builder);			
			actions.andDo(MockMvcResultHandlers.print());
		} catch (Exception e) {
			System.out.println("jitendrasinghsankhwR");
			e.printStackTrace();
		} 		
    	return actions;
    }
	
    public ResultActions resultActionPost(String contentString, String path, Map<String, String> params, MediaType contentType, Map<String, String> header, MockMvc mockMvc) {
    	ResultActions actions = null;
    	MockHttpServletRequestBuilder builder = post(path);
		for (Entry<String, String> entry : params.entrySet()) {
			builder.param(entry.getKey(), entry.getValue());
			System.out.println(entry.getKey() +"::"+entry.getValue());
		}
		builder.contentType(contentType).content(contentString.getBytes());
		for (Entry<String, String> entry : header.entrySet()){
			builder.header(entry.getKey(), entry.getValue());
		}
		try {
			System.out.println(builder.toString());
			actions = mockMvc.perform(builder);			
			actions.andDo(MockMvcResultHandlers.print());
		} catch (Exception e) {
			System.out.println("jitendrasinghsankhwR");
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
			resp = objectMapper.readValue(content, Response.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return resp;
    }
    
}
