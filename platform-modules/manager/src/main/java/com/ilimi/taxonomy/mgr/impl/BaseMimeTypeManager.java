package com.ilimi.taxonomy.mgr.impl;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

public class BaseMimeTypeManager {
	
	public boolean isArtifactUrlSet(Map<String, Object> contentMap) {
		return false;
	}
	
	public boolean isValidZipFile(File file) {
		return false;
	}
	
	public void uploadFile(InputStream stream) {
		
	}
	
	public void uploadFile(String filePath) {
		
	}
	
	public Map<String, Object> setArtifactUrl(Map<String, Object> contentMap, String url) {
		Map<String, Object> map = new HashMap<String, Object>();
		return map;
	}
	
	public Response updateContentNode(Request request) {
		Response response = new Response();
		return response;
	}
	
	public Response getContentNode(Request request) {
		Response response = new Response();
		return response;
	}
	
	public boolean isJSONValid(String content) {
		return false;
	}
	
	public boolean isECMLValid(String content) {
		return false;
	}
	
	public static Map<String, Object> readECMLFile(String filePath) {
		Map<String, Object> ECMLMap = new HashMap<String, Object>();
		return ECMLMap;
	}
}
