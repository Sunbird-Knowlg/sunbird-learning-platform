package com.ilimi.taxonomy.controller.taxonomy;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebAppConfiguration
@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class CreateTaxonomyTest {
	@Autowired 
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    @Before
    public void setup() throws IOException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    
    @Test
    public void createTaxonomy() throws Exception {
    	InputStream is = new FileInputStream("Numeracy-GraphEngine.csv");
    	MockMultipartFile firstFile = new MockMultipartFile("file", is);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/taxonomy/NUMERACY").file(firstFile).contentType(MediaType.MULTIPART_FORM_DATA).header("user-id", "jeetu")).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk());  
   }
    
    @Test
    public void emptyInputFile() throws Exception {
    	InputStream is = new FileInputStream("E:/Ilimi-EkStep/Learning-Platform/platform-modules/taxonomy/module/taxonomy-service/src/test/resources/hi.txt");
    	MockMultipartFile firstFile = new MockMultipartFile("file", is);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/taxonomy/NUMERACY").file(firstFile).contentType(MediaType.MULTIPART_FORM_DATA).header("user-id", "jeetu")).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk());  
   }
}
