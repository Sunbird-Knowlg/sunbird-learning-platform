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
import com.ilimi.taxonomy.content.common.ElementMap;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Event;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

public class JSONContentParser {
	
	public Plugin parseContent(String json) {
		Plugin content = new Plugin();
		try {
			JSONObject root = new JSONObject(json);
			content = processContentDocument(root);
		} catch (JSONException e) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_WP_JSON_PARSE_ERROR.name(),
					ContentErrorMessageConstants.XML_PARSE_CONFIG_ERROR, e);
		}
		return content;
	}
	
	private Plugin processContentDocument(JSONObject root) {
		Plugin plugin = new Plugin();
		if (null != root) {
			if (root.has(ContentWorkflowPipelineParams.theme.name())) {
				root = root.getJSONObject(ContentWorkflowPipelineParams.theme.name());
				plugin.setId(getId(root));
				plugin.setData(getData(root, ContentWorkflowPipelineParams.theme.name()));
				plugin.setInnerText(getInnerText(root, ContentWorkflowPipelineParams.__text.name()));
				plugin.setcData(getCData(root, ContentWorkflowPipelineParams.__cdata.name()));
				plugin.setManifest(getContentManifest(root));
				plugin.setControllers(getControllers(root));
				plugin.setChildrenPlugin(getChildrenPlugins(root));
				plugin.setEvents(getEvents(root));
			}
		}
		return plugin;
	}
	
	private Manifest getContentManifest(JSONObject object) {
		Manifest manifest = new Manifest();
		if (null != object) {
			if (object.has(ContentWorkflowPipelineParams.manifest.name())) {
				Object value = object.get(ContentWorkflowPipelineParams.manifest.name());
				if (value instanceof JSONArray) {
					throw new ClientException(ContentErrorCodeConstants.EXPECTED_JSON_OBJECT.name(), 
							ContentErrorMessageConstants.JSON_OBJECT_EXPECTED + 
							ContentWorkflowPipelineParams.manifest.name());
				} else if (value instanceof JSONObject) {
					JSONObject manifestObj = (JSONObject) value;
					if (manifestObj.has(ContentWorkflowPipelineParams.media.name())) {
						manifest.setId(getId(manifestObj));
						manifest.setData(getData(manifestObj, ContentWorkflowPipelineParams.manifest.name()));
						manifest.setInnerText(getInnerText(manifestObj, ContentWorkflowPipelineParams.__text.name()));
						manifest.setcData(getCData(manifestObj, ContentWorkflowPipelineParams.__cdata.name()));
						manifest.setMedias(getMediaFromObject(manifestObj.get(ContentWorkflowPipelineParams.media.name())));
					}
				}
			}
		}
		return manifest;
	}
	
	private List<Media> getMediaFromObject(Object object) {
		List<Media> medias = new ArrayList<Media>();
		if (null != object) {
			if (object instanceof JSONArray) {
				JSONArray mediaObjs = (JSONArray) object;
				for (int i = 0; i < mediaObjs.length(); i++)
					medias.add(getContentMedia(mediaObjs.getJSONObject(i)));
			} else if (object instanceof JSONObject) {
				JSONObject mediaObj = (JSONObject) object;
				medias.add(getContentMedia(mediaObj));
			}
		}
		return medias;
	}
	
	private Media getContentMedia(JSONObject mediaObj) {
		Media media = new Media();
		try {
			if (null != mediaObj) {
				String id = mediaObj.getString(ContentWorkflowPipelineParams.id.name());
				String src = mediaObj.getString(ContentWorkflowPipelineParams.src.name());
				String type = mediaObj.getString(ContentWorkflowPipelineParams.type.name());
				if (StringUtils.isBlank(id))
					throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
							"Error! Invalid Media ('id' is required.)");
				if (StringUtils.isBlank(src))
					throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
							"Error! Invalid Media ('src' is required.)");
				if (StringUtils.isBlank(type))
					throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
							"Error! Invalid Media ('type' is required.)");
				media.setId(id);
				media.setSrc(src);
				media.setType(type);
				media.setData(getData(mediaObj, ContentWorkflowPipelineParams.media.name()));
				media.setInnerText(getInnerText(mediaObj, ContentWorkflowPipelineParams.__text.name()));
				media.setcData(getCData(mediaObj, ContentWorkflowPipelineParams.__cdata.name()));
				media.setChildrenPlugin(getChildrenPlugins(mediaObj));
			}
		} catch(JSONException e) {
			throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
					ContentErrorMessageConstants.INVALID_MEDIA);
		}
		return media;
	}
	
	private List<Controller> getControllers(JSONObject object) {
		List<Controller> controllers = new ArrayList<Controller>();
		if (null !=  object) {
			if (object.has(ContentWorkflowPipelineParams.controller.name())) {
				Object value = object.get(ContentWorkflowPipelineParams.controller.name());
				if (value instanceof JSONArray) {
					JSONArray controllerObjs = (JSONArray) value;
					for (int i = 0; i < controllerObjs.length(); i++) {
						validateController(controllerObjs.getJSONObject(i));
						Controller controller = new Controller();
						controller.setId(getId(controllerObjs.getJSONObject(i)));
						controller.setData(getData(controllerObjs.getJSONObject(i), ContentWorkflowPipelineParams.controller.name()));
						controller.setInnerText(getInnerText(controllerObjs.getJSONObject(i), ContentWorkflowPipelineParams.__text.name()));
						controller.setcData(getCData(controllerObjs.getJSONObject(i), ContentWorkflowPipelineParams.__cdata.name()));
						controllers.add(controller);
					}
				} else if (value instanceof JSONObject) {
					JSONObject controllerObj = (JSONObject) value;
					validateController(controllerObj);
					Controller controller = new Controller();
					controller.setId(getId(controllerObj));
					controller.setData(getData(controllerObj, ContentWorkflowPipelineParams.controller.name()));
					controller.setInnerText(getInnerText(controllerObj, ContentWorkflowPipelineParams.__text.name()));
					controller.setcData(getCData(controllerObj, ContentWorkflowPipelineParams.__cdata.name()));
					controllers.add(controller);
				}
			}
		}
		return controllers;
	}
	
	private void validateController(JSONObject controllerObj) {
		String id = controllerObj.getString(ContentWorkflowPipelineParams.id.name());
		String type = controllerObj.getString(ContentWorkflowPipelineParams.type.name());
		if (StringUtils.isBlank(id))
			throw new ClientException(ContentErrorCodeConstants.INVALID_CONTROLLER.name(), 
					"Error! Invalid Controller ('id' is required.)");
		if (StringUtils.isBlank(type))
			throw new ClientException(ContentErrorCodeConstants.INVALID_CONTROLLER.name(), 
					"Error! Invalid Controller ('type' is required.)");
		if (!StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.items.name(), type) 
				&& !StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.data.name(), type))
			throw new ClientException(ContentErrorCodeConstants.INVALID_CONTROLLER.name(), 
					"Error! Invalid Controller ('type' should be either 'items' or 'data')");
	}
	
//	private List<Plugin> getPlugins(JSONObject object) {
//		List<Plugin> plugins = new ArrayList<Plugin>();
//		object = getPluginViewOfDocument(object);
//		if (null != object) {
//			Iterator<String> keysItr = object.keys();
//		    while(keysItr.hasNext()) {
//		    	String key = keysItr.next();
//		        Object value = object.get(key);
//		        if(value instanceof JSONArray) {
//		        	JSONArray array = (JSONArray) value;
//		        	for(int i = 0; i < array.length(); i++) {
//		        		plugins.add(getPlugin((JSONObject) array.get(i), key));
//		        	}
//		        } else if(value instanceof JSONObject)
//		        	plugins.add(getPlugin((JSONObject) value, key));
//		    }
//		}
//		return plugins;
//	}
	
	private Plugin getPlugin(JSONObject object, String key) {
		Plugin plugin = new Plugin();
		if (null != object) {
			plugin.setId(getId(object));
			plugin.setData(getData(object, key));
			plugin.setInnerText(getInnerText(object, ContentWorkflowPipelineParams.__text.name()));
			plugin.setcData(getCData(object, ContentWorkflowPipelineParams.__cdata.name()));
			plugin.setChildrenPlugin(getChildrenPlugins(object));
			plugin.setManifest(getContentManifest(object));
			plugin.setControllers(getControllers(object));
			plugin.setEvents(getEvents(object));
		}
		return plugin;
	}
	
//	private List<Map<String, String>> getNonPluginChildren(JSONObject object, String elementName) {
//		List<Map<String, String>> nonPluginChildren = new ArrayList<Map<String, String>>();
//		if (null != object && !StringUtils.isBlank(elementName))
//			nonPluginChildren = toMap(object, elementName, true);
//		return nonPluginChildren;
//	}
	
	private List<Plugin> getChildrenPlugins(JSONObject object) {
		List<Plugin> childrenPlugins = new ArrayList<Plugin>();
		if (null != object) {
			Iterator<String> keysItr = object.keys();
		    while(keysItr.hasNext()) {
		        String key = keysItr.next();
			        Object value = object.get(key);
			        if((value instanceof JSONArray) && ElementMap.isPlugin(key)) {
			        	JSONArray array = (JSONArray) value;
			        	for(int i = 0; i < array.length(); i++)
			        		childrenPlugins.add(getPlugin(array.getJSONObject(i), key));
			        }
			        else if((value instanceof JSONObject) && ElementMap.isPlugin(key))
			        	childrenPlugins.add(getPlugin((JSONObject) value, key));
		    }
		}
		return childrenPlugins;
	}
	
	private List<Event> getEvents(JSONObject object) {
		List<Event> events = new ArrayList<Event>();
		if (null != object) {
			if (object.has(ContentWorkflowPipelineParams.events.name())) {
				Object value = object.get(ContentWorkflowPipelineParams.events.name());
				if (value instanceof JSONArray) {
					JSONArray eventObjs = (JSONArray) value;
					for (int i = 0; i < eventObjs.length(); i++) {
						events.add(getEvent(eventObjs.getJSONObject(i)));
					}
				} else if (value instanceof JSONObject) {
					JSONObject eventsObj = (JSONObject) value;
					if (eventsObj.has(ContentWorkflowPipelineParams.event.name()))
						events.addAll(getEventFromObject(eventsObj.get(ContentWorkflowPipelineParams.event.name())));
				}
				
			} else if (object.has(ContentWorkflowPipelineParams.event.name()))
				events.addAll(getEventFromObject(object.get(ContentWorkflowPipelineParams.event.name())));
		}
		return events;
	}
	
	private List<Event> getEventFromObject(Object object) {
		List<Event> events = new ArrayList<Event>();
		if (null != object) {
			if (object instanceof JSONArray)
				events.addAll(getEventList((JSONArray) object));
			else if (object instanceof JSONObject)
				events.add(getEvent((JSONObject) object));
		}
		return events;
	}
	
	private List<Event> getEventList(JSONArray array) {
		List<Event> events = new ArrayList<Event>();
		for(int i = 0; i < array.length(); i++)
			events.add(getEvent(array.getJSONObject(i)));
		return events;
	}
	
	private Event getEvent(JSONObject object) {
		Event event = new Event();
		event.setId(getId(object));
		event.setData(getData(object, ContentWorkflowPipelineParams.event.name()));
		event.setInnerText(getInnerText(object, ContentWorkflowPipelineParams.__text.name()));
		event.setcData(getCData(object, ContentWorkflowPipelineParams.__cdata.name()));
		event.setChildrenPlugin(getChildrenPlugins(object));
		return event;
	}
	
	private String getInnerText(JSONObject object, String elementName) {
		String innerText = "";
		if (null != object && !StringUtils.isBlank(elementName)) {
			String text = getMapFromJsonObj(object).get(elementName);
			if (null != text)
				innerText = text;
		}
		return innerText;
	}
	
//	private JSONObject getPluginViewOfDocument(JSONObject object) {
//		if (null != object) {
//			//Remove Manifest From Document
//			object.remove(ContentWorkflowPipelineParams.manifest.name());
//			
//			// Remove Controllers From Document
//			object.remove(ContentWorkflowPipelineParams.controller.name());
//			
//			// Remove all (String) Attributes
//			Set<String> keys = getMapFromJsonObj(object).keySet();
//			for (String key: keys)
//				object.remove(key);
//		}
//		return object;
//	}
	
	private String getId(JSONObject object) {
		return getAttributeValueByName(object, ContentWorkflowPipelineParams.id.name());
	}
	
	private String getAttributeValueByName(JSONObject object, String attribute) {
		String value = "";
		if (null != object && !StringUtils.isBlank(attribute)) {
			Map<String, String> map = getMapFromJsonObj(object);
			String val = map.get(attribute);
			if (!StringUtils.isBlank(val))
				value = val;
		}
		return value;
	}
	
	private Map<String, String> getData(JSONObject object, String elementName) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != object && !StringUtils.isBlank(elementName)) {
			map = getMapFromJsonObj(object);
			map.put(ContentWorkflowPipelineParams.cwp_element_name.name(), elementName);
		}
		return map;
	}
	
	private String getCData(JSONObject object, String elementName) {
		String cData = "";
		if (null != object && !StringUtils.isBlank(elementName)){
			Map<String, Object> map = ConversionUtil.toMap(object);
			Object obj = map.get(elementName);
			if (null != obj)
				cData = obj.toString(); 
		}
		return cData;
	}
	
//	private List<Map<String, String>> getChildrenData(JSONObject object, String elementName) {
//		List<Map<String, String>> childrenMaps = new ArrayList<Map<String, String>>();
//		if (null != object && !StringUtils.isBlank(elementName))
//			childrenMaps = toMap(object, elementName, false);
//		return childrenMaps;
//	}
	
	private Map<String, String> getMapFromJsonObj(JSONObject object) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != object) {
			Iterator<String> keysItr = object.keys();
			while(keysItr.hasNext()) {
		        String key = keysItr.next();
		        Object value = object.get(key);
		        if(!(value instanceof JSONArray) && !(value instanceof JSONObject)) {
		        	if (null != value)
		        		map.put(key, String.valueOf(value));
		        	else
		        		map.put(key, "");
		        }
			}
		}
		return map;
	}
	
	private List<Map<String, String>> toMap(JSONObject object, String parentKey, boolean isOnlyNonPluginChildrenAllowed) throws JSONException {
	    List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
	    Iterator<String> keysItr = object.keys();
	    while(keysItr.hasNext()) {
	        String key = keysItr.next();
	        if (((isOnlyNonPluginChildrenAllowed == true && 
	        		!ElementMap.isPlugin(key) && 
	        		!ElementMap.isEvent(key) &&
	        		!ElementMap.isReservedWrapper(key)) || 
	        		(isOnlyNonPluginChildrenAllowed == true && !ElementMap.isPlugin(parentKey))) ||
	        		isOnlyNonPluginChildrenAllowed == false) {
		        Object value = object.get(key);
		        if(value instanceof JSONArray)
		        	maps.addAll(toList((JSONArray) value, key, isOnlyNonPluginChildrenAllowed));
		        else if(value instanceof JSONObject)
		        	maps.addAll(toMap((JSONObject) value, key, isOnlyNonPluginChildrenAllowed));
		        else if((isOnlyNonPluginChildrenAllowed == true && 
		        		!ElementMap.isPlugin(parentKey) &&
		        		!ElementMap.isEvent(parentKey) &&
		        		!ElementMap.isReservedWrapper(parentKey)) || isOnlyNonPluginChildrenAllowed == false){
		        	Map<String, String> map = new HashMap<String, String>();
			        map.put(key, (String)value);
			        map.put(ContentWorkflowPipelineParams.cwp_element_name.name(), key);
			        map.put(ContentWorkflowPipelineParams.cwp_group_element_name.name(), parentKey);
			        maps.add(map);
		        }
	        }
	    }
	    return maps;
	}

	private List<Map<String, String>> toList(JSONArray array, String parentKey, boolean isOnlyNonPluginChildrenAllowed) throws JSONException {
	    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	    for(int i = 0; i < array.length(); i++) {
	        Object value = array.get(i);
	        if(value instanceof JSONArray)
	        	list.addAll(toList((JSONArray) value, parentKey, isOnlyNonPluginChildrenAllowed));
	        else if(value instanceof JSONObject)
	        	list.addAll(toMap((JSONObject) value, parentKey, isOnlyNonPluginChildrenAllowed));
	    }
	    return list;
	}

}
