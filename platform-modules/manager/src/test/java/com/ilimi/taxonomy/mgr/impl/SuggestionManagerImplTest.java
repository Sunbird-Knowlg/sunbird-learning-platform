package com.ilimi.taxonomy.mgr.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.ilimi.taxonomy.mgr.ISuggestionManager;

/**
 * 
 * @author Mahesh Kumar Gangula
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class SuggestionManagerImplTest {

	@Autowired
	ISuggestionManager suggestionManager;
	
	/**
	 * TODO: Need to add the below test cases.
	 * 	1. Create a content before creating suggestion.
	 * 
	 * Create Suggestion:
	 *  1. Create suggestion on a invalid content - should return client error.
	 *  2. Create suggestion on a valid content - should return response
	 *  		check status=new
	 *  
	 * Approve Suggestion:
	 *  1. Approve suggestion - should return response and content should be updated.
	 *  2. Approve a suggestion which is already processed - return error saying already processed.
	 *  3. Approve suggestion with invalid suggestion id - return error.
	 *  
	 * Reject Suggestion:
	 *  1. Reject suggestion - should return response and content should be updated.
	 *  2. Reject a suggestion which is already processed - return error saying already processed.
	 *  3. Reject suggestion with invalid suggestion id - return error.
	 * 
	 */
	
	@Test
	@Ignore
	public void testCreateSuggestion() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", "Suggested Name");
		Map<String, Object> request = new HashMap<String, Object>();
		request.put("objectId", "");
		request.put("suggestedBy", "1234");
		request.put("command", "update");
		request.put("params", params);
		suggestionManager.saveSuggestion(request);
	}
}
