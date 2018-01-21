package org.ekstep.taxonomy.controller.taxonomy.cucumber;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.taxonomy.base.test.BaseCucumberTest;
import org.junit.Assert;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;


@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class GetAllTaxonomyTest extends BaseCucumberTest{
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	@Then("I should get all taxonomy for graphId (.*) and (.*) and status is (.*)")
	public void getAllTaxonomy(String numeracyGraphId, String litracyGraphId, String status) {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/taxonomy";		
		params.put("tfields", "name");
		header.put("user-id", "ilimi");
		ResultActions actions = resultActionGet(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
		try {
			actions.andExpect(status().isOk());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		Response resp = jasonToObject(actions);
        Assert.assertEquals("ekstep.lp.taxonomy.list", resp.getId());
        Assert.assertEquals("1.0", resp.getVer());
        Assert.assertEquals(status, resp.getParams().getStatus());        
        Map<String, Object> result = resp.getResult();
        @SuppressWarnings("unchecked")
        List<Object>  taxonomy_list =  (ArrayList<Object>) result.get("taxonomy_list");
        @SuppressWarnings("unchecked")
		Map<String, Object> numeracyMap = (Map<String, Object>) taxonomy_list.get(0);
        @SuppressWarnings("unchecked")
		Map<String, Object> literacyMap = (Map<String, Object>) taxonomy_list.get(1);        
        Assert.assertEquals(numeracyGraphId, numeracyMap.get("graphId"));
        Assert.assertEquals(litracyGraphId, literacyMap.get("graphId"));
        
	}
	
}
