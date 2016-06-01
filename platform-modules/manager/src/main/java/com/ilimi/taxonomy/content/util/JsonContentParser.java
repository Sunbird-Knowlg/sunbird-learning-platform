package com.ilimi.taxonomy.content.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.common.ElementMap;
import com.ilimi.taxonomy.content.entity.Action;
import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Event;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

public class JsonContentParser {
	
	public Content parseContent(String json) {
		Content content = new Content();
		try {
			JSONObject root = new JSONObject(json);
			content = processContentDocument(root);
		} catch (JSONException e) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_WP_JSON_PARSE_ERROR.name(),
					ContentErrorMessageConstants.XML_PARSE_CONFIG_ERROR, e);
		}
		return content;
	}
	
	private Content processContentDocument(JSONObject root) {
		Content content = new Content();
		if (null != root) {
			content.setData(getData(root, ContentWorkflowPipelineParams.theme.name()));
			if (root.has(ContentWorkflowPipelineParams.manifest.name()))
				content.setManifest(getContentManifest(root.getJSONObject(ContentWorkflowPipelineParams.manifest.name())));
			if (root.has(ContentWorkflowPipelineParams.controller.name()))
				content.setControllers(getControllers(root.getJSONObject(ContentWorkflowPipelineParams.controller.name())));
			content.setPlugins(getPlugins(root));
		}
		return content;
	}
	
	private Manifest getContentManifest(JSONObject manifestObj) {
		Manifest manifest = new Manifest();
		if (null != manifestObj) {
			List<Media> medias = new ArrayList<Media>();
			JSONArray mediaObjs = manifestObj.getJSONArray(ContentWorkflowPipelineParams.media.name());
			for (int i = 0; i < mediaObjs.length(); i++)
				medias.add(getContentMedia(mediaObjs.getJSONObject(i)));
			manifest.setMedias(medias);
		}
		return manifest;
	}
	
	private Media getContentMedia(JSONObject mediaObj) {
		Media media = new Media();
		if (null != mediaObj) {
			media.setData(getData(mediaObj, ContentWorkflowPipelineParams.media.name()));
			media.setChildrenData(getChildrenData(mediaObj, ContentWorkflowPipelineParams.media.name()));
		}
		return media;
	}
	
	private List<Controller> getControllers(JSONObject controllerObj) {
		List<Controller> controllers = new ArrayList<Controller>();
		if (null !=  controllerObj) {
			JSONArray controllerObjs = controllerObj.getJSONArray(ContentWorkflowPipelineParams.controller.name());
			for (int i = 0; i < controllerObjs.length(); i++) {
				Controller controller = new Controller();
				controller.setData(getData(controllerObjs.getJSONObject(i), ContentWorkflowPipelineParams.controller.name()));
				controller.setcData(getCData(controllerObjs.getJSONObject(i), ContentWorkflowPipelineParams.__cdata.name()));
				controllers.add(controller);
			}
		}
		return controllers;
	}
	
	private List<Plugin> getPlugins(JSONObject object) {
		List<Plugin> plugins = new ArrayList<Plugin>();
		object = getPluginViewOfDocument(object);
		if (null != object) {
			Iterator<String> keysItr = object.keys();
		    while(keysItr.hasNext()) {
		    	String key = keysItr.next();
		        Object value = object.get(key);
		        if(value instanceof JSONArray) {
		        	JSONArray array = (JSONArray) value;
		        	for(int i = 0; i < array.length(); i++) {
		        		plugins.add(getPlugin((JSONObject) array.get(i), key));
		        	}
		        } else if(value instanceof JSONObject) {
		        	plugins.add(getPlugin((JSONObject) value, key));
		        }
		    }
		}
		return plugins;
	}
	
	private Plugin getPlugin(JSONObject object, String key) {
		Plugin plugin = new Plugin();
		if (null != object) {
			plugin.setData(getData(object, key));
			plugin.setChildrenData(getNonPluginChildren(object, key));
			plugin.setChildrenPlugin(getChildrenPlugins(object));
			plugin.setEvents(getEvents(object));
			plugin.setInnerText(getInnerText(object, ContentWorkflowPipelineParams.__text.name()));
		}
		return plugin;
	}
	
	private List<Map<String, String>> getNonPluginChildren(JSONObject object, String elementName) {
		List<Map<String, String>> nonPluginChildren = new ArrayList<Map<String, String>>();
		if (null != object && !StringUtils.isBlank(elementName))
			nonPluginChildren = toMap(object, elementName, true);
		return nonPluginChildren;
	}
	
	private List<Plugin> getChildrenPlugins(JSONObject object) {
		List<Plugin> childrenPlugins = new ArrayList<Plugin>();
		if (null != object) {
			Iterator<String> keysItr = object.keys();
		    while(keysItr.hasNext()) {
		        String key = keysItr.next();
		        if (ElementMap.isPlugin(key)) {
			        Object value = object.get(key);
			        if(value instanceof JSONArray) {
			        	JSONArray array = (JSONArray) value;
			        	for(int i = 0; i < array.length(); i++)
			        		childrenPlugins.add(getPlugin(array.getJSONObject(i), key));
			        }
			        else if(value instanceof JSONObject){
			        	childrenPlugins.add(getPlugin(object, key));
			        }
		        }
		    }
		}
		return childrenPlugins;
	}
	
	private List<Event> getEvents(JSONObject object) {
		List<Event> events = new ArrayList<Event>();
		if (null != object) {
			if (object.has(ContentWorkflowPipelineParams.events.name())) {
				JSONObject eventsObj = object.getJSONObject(ContentWorkflowPipelineParams.events.name());
				if (eventsObj.has(ContentWorkflowPipelineParams.event.name()))
					events.addAll(getEventFromObject(object.get(ContentWorkflowPipelineParams.event.name())));
			} else if (object.has(ContentWorkflowPipelineParams.event.name()))
				events.addAll(getEventFromObject(object.get(ContentWorkflowPipelineParams.event.name())));
		}
		return events;
	}
	
	private List<Event> getEventFromObject(Object object) {
		List<Event> events = new ArrayList<Event>();
		if (null != object) {
			if (object instanceof JSONArray) {
				events.addAll(getEventList((JSONArray) object));
			} else if (object instanceof JSONObject) {
				events.add(getEvent((JSONObject) object));
			} 
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
		List<Action> actions = new ArrayList<Action>();
		event.setData(getData(object, ContentWorkflowPipelineParams.event.name()));
		if (object.has(ContentWorkflowPipelineParams.action.name())) {
			Object actionObj = object.get(ContentWorkflowPipelineParams.action.name());
			if (actionObj instanceof JSONArray) {
				actions.addAll(getActions((JSONArray) actionObj));
			} else if (actionObj instanceof JSONObject) {
				actions.add(getAction((JSONObject) actionObj));
			} 
		}
		event.setActions(actions);
		return event;
	}
	
	private List<Action> getActions(JSONArray array) {
		List<Action> actions = new ArrayList<Action>();
		for(int i = 0; i < array.length(); i++)
			actions.add(getAction(array.getJSONObject(i)));
		return actions;
	}
	
	private Action getAction(JSONObject object) {
		Action action = new Action();
		action.setData(getData(object, ContentWorkflowPipelineParams.action.name()));
		return action;
	}
	
	private String getInnerText(JSONObject object, String elementName) {
		String innerText = "";
		if (null != object && !StringUtils.isBlank(elementName)) {
			String text = getMapFromJsonObj(object).get(elementName);
			if (!StringUtils.isBlank(innerText))
				innerText = text;
		}
		return innerText;
	}
	
	private JSONObject getPluginViewOfDocument(JSONObject object) {
		if (null != object) {
			//Remove Manifest From Document
			object.remove(ContentWorkflowPipelineParams.manifest.name());
			
			// Remove Controllers From Document
			object.remove(ContentWorkflowPipelineParams.controller.name());
			
			// Remove all (String) Attributes
			Set<String> keys = getMapFromJsonObj(object).keySet();
			for (String key: keys)
				object.remove(key);
		}
		return object;
	}
	
	private Map<String, String> getData(JSONObject object, String elementName) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != object && !StringUtils.isBlank(elementName)) {
			map = getMapFromJsonObj(object);
			map.put(ContentWorkflowPipelineParams.element_name.name(), elementName);
		}
		return map;
	}
	
	private String getCData(JSONObject object, String elementName) {
		String cData = "";
		if (null != object && !StringUtils.isBlank(elementName)){
			Map<String, Object> map = ConversionUtil.toMap(object);
			JSONObject obj = (JSONObject) map.get(elementName);
			if (null != obj)
				cData = obj.toString(); 
		}
		return cData;
	}
	
	private List<Map<String, String>> getChildrenData(JSONObject object, String elementName) {
		List<Map<String, String>> childrenMaps = new ArrayList<Map<String, String>>();
		if (null != object && !StringUtils.isBlank(elementName))
			childrenMaps = toMap(object, elementName, false);
		return childrenMaps;
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
	
	private List<Map<String, String>> toMap(JSONObject object, String parentKey, boolean isOnlyNonPluginChildrenAllowed) throws JSONException {
	    List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
	    Map<String, String> map = new HashMap<String, String>();
	    Iterator<String> keysItr = object.keys();
	    while(keysItr.hasNext()) {
	        String key = keysItr.next();
	        if (!isOnlyNonPluginChildrenAllowed && !ElementMap.isPlugin(key)) {
		        Object value = object.get(key);
		        if(value instanceof JSONArray)
		        	maps.addAll(toList((JSONArray) value, key, isOnlyNonPluginChildrenAllowed));
		        else if(value instanceof JSONObject)
		        	maps.addAll(toMap((JSONObject) value, key, isOnlyNonPluginChildrenAllowed));
		        else {
			        map.put(key, (String)value);
			        map.put(ContentWorkflowPipelineParams.element_name.name(), key);
			        map.put(ContentWorkflowPipelineParams.group_element_name.name(), parentKey);
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
	        if(value instanceof JSONArray) {
	        	list.addAll(toList((JSONArray) value, parentKey, isOnlyNonPluginChildrenAllowed));
	        } else if(value instanceof JSONObject) {
	        	list.addAll(toMap((JSONObject) value, parentKey, isOnlyNonPluginChildrenAllowed));
	        }
	    }
	    return list;
	}

}
