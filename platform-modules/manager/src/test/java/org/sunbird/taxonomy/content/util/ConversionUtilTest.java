package org.sunbird.taxonomy.content.util;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.sunbird.content.util.ConversionUtil;
import org.junit.Test;

/**
 * The Class ConversionUtilTest is a  test class used to test the 
 * ConversionUtil utility which is
 * used to convert any given map<String, String> to JSON
 */
public class ConversionUtilTest {
    
	Map<String, String> map = new HashMap<String, String>();
	String result = null;
	
	// valid json map 
	@Test
	public void convertMapToJSONTest_01(){
		map.put("code", "org.sunbird.mar8.story");
		map.put("status", "Mock");
		map.put("description","शेर का साथी हा");
		map.put("subject", "literacy");
		result = ConversionUtil.convertMapToJSON(map);
		assertEquals(result.isEmpty(), false);
	}
	// empty json map 
	@Test
	public void convertMapToJSONTest_02(){
		map.put("", "");
		result = ConversionUtil.convertMapToJSON(map);
		assertEquals(result.isEmpty(), false);
	}
	
	// empty json map
	@Test
	public void convertMapToJSONTest_03(){
		result = ConversionUtil.convertMapToJSON(map);
		assertEquals(result.isEmpty(), false);
	}
	
}
