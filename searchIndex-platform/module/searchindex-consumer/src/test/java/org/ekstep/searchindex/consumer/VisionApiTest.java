package org.ekstep.searchindex.consumer;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ekstep.searchindex.util.VisionApi;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

public class VisionApiTest {
	
	Map<String,Object> tags = new HashMap<String,Object>();
	
	List<String> flags = new ArrayList<String>();
	
	VisionApi vision;
	
	public VisionApiTest(){
		try {
			 vision = new VisionApi(VisionApi.getVisionService());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void getTags(){
		try {
			File file = ResourceUtils.getFile(this.getClass().getResource("/images/share.jpg"));
			tags = (Map) vision.getTags(file, vision);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		List<String> keywords = new ArrayList<String>();
		for(Entry<String, Object> entry : tags.entrySet()){
			List<String> list = (List) entry.getValue();
			if (null != list && (!list.isEmpty()))
				keywords.addAll(list);
		}
		Assert.assertEquals(false, tags.isEmpty());
		Assert.assertEquals(false, keywords.isEmpty());
		Assert.assertEquals(true, keywords.contains("lion"));
		Assert.assertEquals(true,keywords.contains("mammal"));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void getFlags(){
		try {
			
			File file = ResourceUtils.getFile(this.getClass().getResource("/images/monster.jpg"));
			flags = (List) vision.getFlags(file, vision);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		List<String> expected = new ArrayList<String>();
		expected.add("Spoof");
		expected.add("violence");
		expected.add("Adult");
		expected.add("Medical");
		Assert.assertEquals(false, flags.isEmpty());
		Assert.assertEquals(false, flags.isEmpty());
		String flag = null;
		for(String str : flags){
		   flag = str;
		}
		Assert.assertEquals(true, expected.contains(flag));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = IOException.class)
	public void testVisionApiForOtherFiles() throws IOException, GeneralSecurityException{
		
		File file = ResourceUtils.getFile(this.getClass().getResource("/images/figure.xml"));
		tags = (Map) vision.getTags(file, vision);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = IOException.class)
	public void testVisionApiForAudio() throws IOException, GeneralSecurityException{
		
		File file = ResourceUtils.getFile(this.getClass().getResource("/images/sample.mp3"));
		tags = (Map) vision.getTags(file, vision);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testVisionForOtherFormat() throws IOException, GeneralSecurityException{
		
		File file = ResourceUtils.getFile(this.getClass().getResource("/images/bike.bmp"));
		tags = (Map) vision.getTags(file, vision);
		List<String> keywords = new ArrayList<String>();
		for(Entry<String, Object> entry : tags.entrySet()){
			List<String> list = (List) entry.getValue();
			if (null != list && (!list.isEmpty()))
				keywords.addAll(list);
		}
		Assert.assertEquals(false, tags.isEmpty());
		Assert.assertEquals(false, keywords.isEmpty());
		Assert.assertEquals(true, keywords.contains("road bicycle"));
	}
}
