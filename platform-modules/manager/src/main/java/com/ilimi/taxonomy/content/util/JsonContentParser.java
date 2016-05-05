package com.ilimi.taxonomy.content.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

public class JsonContentParser {
	
	public Content parseContent(String json) {
		Content content = new Content();
		try {
			JSONObject root = new JSONObject(json);
			content = processContentDocument(root);
		} catch (JSONException ex) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_WP_JSON_PARSE_ERROR.name(),
					ContentErrorMessageConstants.XML_PARSE_CONFIG_ERROR);
		}
		return content;
	}
	
	private Content processContentDocument(JSONObject root) {
		Content content = new Content();
		if (null != root) {
			content.setManifest(getContentManifest(root.getJSONObject(ContentWorkflowPipelineParams.manifest.name())));
		}
		return content;
	}
	
	private Manifest getContentManifest(JSONObject manifestObj) {
		Manifest manifest = new Manifest();
		if (null != manifestObj) {
			List<Media> medias = new ArrayList<Media>();
			JSONArray mediaObjs = manifestObj.getJSONArray(ContentWorkflowPipelineParams.media.name());
			for (int i = 0; i < mediaObjs.length(); i++) {
				medias.add(getContentMedia(mediaObjs.getJSONObject(i), ContentWorkflowPipelineParams.media.name()));
			}
		}
		return manifest;
	}
	
	private Media getContentMedia(JSONObject mediaObj, String elementName) {
		Media media = new Media();
		if (null != mediaObj) {
			media.setData(getData(mediaObj, elementName));
			media.setChildrenData(getChildrenData(mediaObj, elementName, elementName));
		}
		return media;
	}
	
	private Map<String, String> getData(JSONObject object, String elementName) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != object && !StringUtils.isBlank(elementName)) {
			map = getMapFromJsonObj(object);
			map.put(ContentWorkflowPipelineParams.element_name.name(), elementName);
		}
		return map;
	}
	
	private List<Map<String, String>> getChildrenData(JSONObject object, String elementName, String groupElementName) {
		List<Map<String, String>> lstmap = new ArrayList<Map<String, String>>();
		if (null != object && !StringUtils.isBlank(elementName)) {
			Iterator<String> keysItr = object.keys();
			while(keysItr.hasNext()) {
		        String key = keysItr.next();
		        Object value = object.get(key);
		        if(value instanceof JSONArray) {
		        	JSONArray objects = (JSONArray) value;
		        	for (int i = 0; i < objects.length(); i++) {
		        		Object obj = objects.get(i);
		        		if (obj instanceof JSONObject) {
		        			Map<String, String> map = getMapFromJsonObj((JSONObject) obj);
		        			//map.put(Content, value)
		        		}
		        	}
		        } else if(value instanceof JSONObject) {
		        	Map<String, String> map = getMapFromJsonObj(object);
		        	map.put(ContentWorkflowPipelineParams.element_name.name(), key);
		        	if (!StringUtils.isBlank(groupElementName)) 
		        		map.put(ContentWorkflowPipelineParams.group_element_name.name(), groupElementName);
		        	lstmap.add(map);
		        }
			}
		}
		return lstmap;
	}
	
	private Map<String, String> getMapFromJsonObj(JSONObject object) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != object) {
			Iterator<String> keysItr = object.keys();
			while(keysItr.hasNext()) {
		        String key = keysItr.next();
		        Object value = object.get(key);
		        if(!(value instanceof JSONArray) && !(value instanceof JSONObject))
		        	map.put(key, (String) value);
			}
		}
		return map;
	}
	

}
