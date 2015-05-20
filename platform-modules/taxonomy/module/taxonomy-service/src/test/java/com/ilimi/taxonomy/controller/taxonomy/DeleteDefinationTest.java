package com.ilimi.taxonomy.controller.taxonomy;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.base.test.BaseIlimiTest;

@WebAppConfiguration
@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DeleteDefinationTest extends BaseIlimiTest{
	
	private void basicAssertion(Response resp){
		Assert.assertEquals("ekstep.lp.learning-object.update", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
	}
	
    @Test
    public void deleteDefination() {
        Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/taxonomy/NUMERACY/defination/Game";
		header.put("user-id", "ilimi");
		ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
		try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Response resp = jasonToObject(actions);
		basicAssertion(resp);
   }
    
    @Test
    public void taxonomyIdNotFound() {
        Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/taxonomy/NUMERAC/defination/Game";
		header.put("user-id", "ilimi");
		ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
		try {
			actions.andExpect(status().is(404));
		} catch (Exception e) {
			e.printStackTrace();
		}
   }
}
