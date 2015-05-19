package com.ilimi.taxonomy.controller.taxonomy.cucumber.test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import com.ilimi.taxonomy.controller.concept.cucumber.test.CucumberBaseTestIlimi;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class DeleteDefinationTest extends CucumberBaseTestIlimi{
	
	private String taxonomyId;
	private String objectType;
	
	@Before
    public void setup() throws IOException {
        initMockMVC();
    }
	
	@When("Taxonomy ID is (.*) and objectType ID  is (.*)$")
	public void getInputData(String taxonomyId, String objectType){
		this.taxonomyId = taxonomyId;
		this.objectType = objectType;
	}
	
	@Then("Delete the (.*) defination and get status (.*)$")
	public void deleteDefination(String objecttype, String status){
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/taxonomy/"+taxonomyId+"/defination/"+ objectType;
		header.put("user-id", "jeetu");
		ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
		try {
			actions.andExpect(status().isAccepted());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Then("Unable to delete the defination and get status (\\d+)")
	public void wrongObjectType(int status){
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();
		String path = "/taxonomy/NUMERACY/defination/Game";
		header.put("user-id", "jeetu");
		ResultActions actions = resultActionDelete(path, params, MediaType.APPLICATION_JSON, header, mockMvc);      
		try {
			actions.andExpect(status().is(status));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
