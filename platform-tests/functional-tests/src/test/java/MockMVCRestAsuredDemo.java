
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
//import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

//import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:servlet-context.xml"})
@WebAppConfiguration
public class MockMVCRestAsuredDemo {
	
	@Autowired
    private WebApplicationContext context;
	
	@Before public void	
    rest_assured_is_initialized_with_the_web_application_context_before_each_test() {
		System.out.println("In Before");
        RestAssuredMockMvc.webAppContextSetup(context);
    }

    @After public void
    rest_assured_is_reset_after_each_test() {
    	System.out.println("In After");
        RestAssuredMockMvc.reset();
    }
	
	@Test
	public void TestDomainGraphSuccess(){
		given().
			log().all().  
		    header("userid", "rayuluv").
		    header("Content-Type", "application/json").		    
		when().			  
			get("v2/domain").		        
		then().
			log().all();	
	}

}
