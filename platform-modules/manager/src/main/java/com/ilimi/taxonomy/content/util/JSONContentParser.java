package com.ilimi.taxonomy.content.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.common.ElementMap;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Event;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

public class JSONContentParser {
	
	public Plugin parseContent(String json) {
		Plugin content = new Plugin();
		try {
			Gson gson = new Gson();
			JsonObject root = gson.fromJson(json, JsonObject.class);
			content = processContentDocument(root);
		} catch (Exception e) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_WP_JSON_PARSE_ERROR.name(),
					ContentErrorMessageConstants.XML_PARSE_CONFIG_ERROR, e);
		}
		return content;
	}
	
	private Plugin processContentDocument(JsonObject root) {
		Plugin plugin = new Plugin();
		if (null != root) {
			if (root.has(ContentWorkflowPipelineParams.theme.name())) {
				root = root.getAsJsonObject(ContentWorkflowPipelineParams.theme.name());
				plugin.setId(getId(root));
				plugin.setData(getData(root, ContentWorkflowPipelineParams.theme.name()));
				plugin.setInnerText(getInnerText(root));
				plugin.setcData(getCData(root, ContentWorkflowPipelineParams.__cdata.name()));
				plugin.setManifest(getContentManifest(root));
				plugin.setControllers(getControllers(root));
				plugin.setChildrenPlugin(getChildrenPlugins(root));
				plugin.setEvents(getEvents(root));
			}
		}
		return plugin;
	}
	
	private Manifest getContentManifest(JsonObject object) {
		Manifest manifest = new Manifest();
		if (null != object) {
			if (object.has(ContentWorkflowPipelineParams.manifest.name())) {
				JsonElement value = object.get(ContentWorkflowPipelineParams.manifest.name());
				if (value.isJsonArray()) {
					throw new ClientException(ContentErrorCodeConstants.EXPECTED_JSON_OBJECT.name(), 
							ContentErrorMessageConstants.JSON_OBJECT_EXPECTED + 
							ContentWorkflowPipelineParams.manifest.name());
				} else if (value.isJsonObject()) {
					JsonObject manifestObj = value.getAsJsonObject();
					if (manifestObj.has(ContentWorkflowPipelineParams.media.name())) {
						manifest.setId(getId(manifestObj));
						manifest.setData(getData(manifestObj, ContentWorkflowPipelineParams.manifest.name()));
						manifest.setInnerText(getInnerText(manifestObj));
						manifest.setcData(getCData(manifestObj, ContentWorkflowPipelineParams.__cdata.name()));
						manifest.setMedias(getMediaFromObject(manifestObj.get(ContentWorkflowPipelineParams.media.name())));
					}
				}
			}
		}
		return manifest;
	}
	
	private List<Media> getMediaFromObject(JsonElement object) {
		List<Media> medias = new ArrayList<Media>();
		if (null != object) {
			if (object.isJsonArray()) {
				JsonArray mediaObjs = object.getAsJsonArray();
				for (int i = 0; i < mediaObjs.size(); i++) {
					JsonElement element = mediaObjs.get(i);
					if (null != element && element.isJsonObject())
						medias.add(getContentMedia(element.getAsJsonObject()));
				}
			} else if (object.isJsonObject()) {
				JsonObject mediaObj = object.getAsJsonObject();
				medias.add(getContentMedia(mediaObj));
			}
		}
		return medias;
	}
	
	private Media getContentMedia(JsonObject mediaObj) {
		Media media = new Media();
		try {
			if (null != mediaObj) {
				JsonElement id = mediaObj.get(ContentWorkflowPipelineParams.id.name());
				JsonElement src = mediaObj.get(ContentWorkflowPipelineParams.src.name());
				JsonElement type = mediaObj.get(ContentWorkflowPipelineParams.type.name());
				if (null == id || !id.isJsonPrimitive() || StringUtils.isBlank(id.toString()))
					throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
							"Error! Invalid Media ('id' is required.)");
				if (null == src || !src.isJsonPrimitive() || StringUtils.isBlank(src.toString()))
					throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
							"Error! Invalid Media ('src' is required.)");
				if (null == type || !type.isJsonPrimitive() || StringUtils.isBlank(type.toString()))
					throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
							"Error! Invalid Media ('type' is required.)");
				media.setId(id.getAsString());
				media.setSrc(src.getAsString());
				media.setType(type.getAsString());
				media.setData(getData(mediaObj, ContentWorkflowPipelineParams.media.name()));
				media.setInnerText(getInnerText(mediaObj));
				media.setcData(getCData(mediaObj, ContentWorkflowPipelineParams.__cdata.name()));
				media.setChildrenPlugin(getChildrenPlugins(mediaObj));
			}
		} catch(Exception e) {
			throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
					ContentErrorMessageConstants.INVALID_MEDIA);
		}
		return media;
	}
	
	private List<Controller> getControllers(JsonObject object) {
		List<Controller> controllers = new ArrayList<Controller>();
		if (null !=  object) {
			if (object.has(ContentWorkflowPipelineParams.controller.name())) {
				JsonElement value = object.get(ContentWorkflowPipelineParams.controller.name());
				if (value.isJsonArray()) {
					JsonArray controllerObjs = value.getAsJsonArray();
					for (int i = 0; i < controllerObjs.size(); i++) {
						JsonElement element = controllerObjs.get(i);
						if (null == element || !element.isJsonObject())
							throw new ClientException(ContentErrorCodeConstants.INVALID_CONTROLLER.name(), 
									"Error! Invalid Controller (controller structure is invalid.)");
						JsonObject controllerObj = element.getAsJsonObject();
						validateController(controllerObj);
						Controller controller = new Controller();
						controller.setId(getId(controllerObj));
						controller.setData(getData(controllerObj, ContentWorkflowPipelineParams.controller.name()));
						controller.setInnerText(getInnerText(controllerObj));
						controller.setcData(getCData(controllerObj, ContentWorkflowPipelineParams.__cdata.name()));
						controllers.add(controller);
					}
				} else if (value.isJsonObject()) {
					JsonObject controllerObj = value.getAsJsonObject();
					validateController(controllerObj);
					Controller controller = new Controller();
					controller.setId(getId(controllerObj));
					controller.setData(getData(controllerObj, ContentWorkflowPipelineParams.controller.name()));
					controller.setInnerText(getInnerText(controllerObj));
					controller.setcData(getCData(controllerObj, ContentWorkflowPipelineParams.__cdata.name()));
					controllers.add(controller);
				}
			}
		}
		return controllers;
	}
	
	private void validateController(JsonObject controllerObj) {
		JsonElement id = controllerObj.get(ContentWorkflowPipelineParams.id.name());
		JsonElement type = controllerObj.get(ContentWorkflowPipelineParams.type.name());
		if (null == id || !id.isJsonPrimitive() || StringUtils.isBlank(id.toString()))
			throw new ClientException(ContentErrorCodeConstants.INVALID_CONTROLLER.name(), 
					"Error! Invalid Controller ('id' is required.)");
		if (null == type || !type.isJsonPrimitive() || StringUtils.isBlank(type.toString()))
			throw new ClientException(ContentErrorCodeConstants.INVALID_CONTROLLER.name(), 
					"Error! Invalid Controller ('type' is required.)");
		String ctype = type.getAsString();
		if (!StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.items.name(), ctype) 
				&& !StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.data.name(), ctype))
			throw new ClientException(ContentErrorCodeConstants.INVALID_CONTROLLER.name(), 
					"Error! Invalid Controller ('type' should be either 'items' or 'data')");
	}
	
	private Plugin getPlugin(JsonObject object, String key) {
		Plugin plugin = new Plugin();
		if (null != object) {
			plugin.setId(getId(object));
			plugin.setData(getData(object, key));
			plugin.setInnerText(getInnerText(object));
			plugin.setcData(getCData(object, ContentWorkflowPipelineParams.__cdata.name()));
			plugin.setChildrenPlugin(getChildrenPlugins(object));
			plugin.setManifest(getContentManifest(object));
			plugin.setControllers(getControllers(object));
			plugin.setEvents(getEvents(object));
		}
		return plugin;
	}
	
	private List<Plugin> getChildrenPlugins(JsonObject object) {
		List<Plugin> childrenPlugins = new LinkedList<Plugin>();
		if (null != object) {
			Set<Entry<String, JsonElement>> entries = object.entrySet();
			if (null != entries && !entries.isEmpty()) {
				for (Entry<String, JsonElement> entry : entries) {
					String key = entry.getKey();
					JsonElement element = entry.getValue();
					if (null != element) {
						if (element.isJsonArray() && ElementMap.isPlugin(key)) {
							JsonArray array = element.getAsJsonArray();
							for(int i = 0; i < array.size(); i++) {
								if (null != array.get(i) && array.get(i).isJsonObject())
									childrenPlugins.add(getPlugin(array.get(i).getAsJsonObject(), key));
							}
						} else if (element.isJsonObject() && ElementMap.isPlugin(key)) {
							childrenPlugins.add(getPlugin(element.getAsJsonObject(), key));
						}
					}
				}
			}
		}
		return childrenPlugins;
	}
	
	private List<Event> getEvents(JsonObject object) {
		List<Event> events = new ArrayList<Event>();
		if (null != object) {
			if (object.has(ContentWorkflowPipelineParams.events.name())) {
				JsonElement value = object.get(ContentWorkflowPipelineParams.events.name());
				if (value.isJsonArray()) {
					JsonArray eventObjs = value.getAsJsonArray();
					for (int i = 0; i < eventObjs.size(); i++) {
						JsonElement eventObj = eventObjs.get(i).getAsJsonObject();
						if (null != eventObj && eventObj.isJsonObject())
							events.add(getEvent(eventObjs.get(i).getAsJsonObject()));
					}
				} else if (value.isJsonObject()) {
					JsonObject eventsObj = value.getAsJsonObject();
					if (eventsObj.has(ContentWorkflowPipelineParams.event.name()))
						events.addAll(getEventFromObject(eventsObj.get(ContentWorkflowPipelineParams.event.name())));
				}
				
			} else if (object.has(ContentWorkflowPipelineParams.event.name()))
				events.addAll(getEventFromObject(object.get(ContentWorkflowPipelineParams.event.name())));
		}
		return events;
	}
	
	private List<Event> getEventFromObject(JsonElement object) {
		List<Event> events = new ArrayList<Event>();
		if (null != object) {
			if (object.isJsonArray())
				events.addAll(getEventList(object.getAsJsonArray()));
			else if (object.isJsonObject())
				events.add(getEvent(object.getAsJsonObject()));
		}
		return events;
	}
	
	private List<Event> getEventList(JsonArray array) {
		List<Event> events = new ArrayList<Event>();
		for(int i = 0; i < array.size(); i++) {
			JsonElement eventObj = array.get(i).getAsJsonObject();
			if (null != eventObj && eventObj.isJsonObject())
				events.add(getEvent(eventObj.getAsJsonObject()));
		}
		return events;
	}
	
	private Event getEvent(JsonObject object) {
		Event event = new Event();
		event.setId(getId(object));
		event.setData(getData(object, ContentWorkflowPipelineParams.event.name()));
		event.setInnerText(getInnerText(object));
		event.setcData(getCData(object, ContentWorkflowPipelineParams.__cdata.name()));
		event.setChildrenPlugin(getChildrenPlugins(object));
		return event;
	}
	
	private String getInnerText(JsonObject object) {
		String elementName = ContentWorkflowPipelineParams.__text.name();
		String innerText = "";
		if (null != object && !StringUtils.isBlank(elementName)) {
			if (object.has(elementName)) {
				JsonElement element = object.get(elementName);
				if (null != element && element.isJsonPrimitive())
					innerText = element.getAsString();
			}
		}
		return innerText;
	}
	
	private String getId(JsonObject object) {
		String attribute = ContentWorkflowPipelineParams.id.name();
		String value = "";
		if (null != object && !StringUtils.isBlank(attribute)) {
			if (object.has(attribute)) {
				JsonElement element = object.get(attribute);
				if (null != element && element.isJsonPrimitive())
					value = element.getAsString();
			}
		}
		return value;
	}
	
	private Map<String, Object> getData(JsonObject object, String elementName) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != object && !StringUtils.isBlank(elementName)) {
			map = getMapFromJsonObj(object);
			map.put(ContentWorkflowPipelineParams.cwp_element_name.name(), elementName);
		}
		return map;
	}
	
	private String getCData(JsonObject object, String elementName) {
		String cData = "";
		if (null != object && !StringUtils.isBlank(elementName)){
			if (object.has(elementName)) {
				JsonElement c = object.get(elementName);
				if (null != c) {
					if (c.isJsonObject() || c.isJsonArray())
						cData = c.toString();
					else if (c.isJsonPrimitive())
						cData = c.getAsString();
				}
			}
		}
		return cData;
	}
	
	private Map<String, Object> getMapFromJsonObj(JsonObject object) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != object) {
			Set<Entry<String, JsonElement>> entries = object.entrySet();
			if (null != entries && !entries.isEmpty()) {
				for (Entry<String, JsonElement> entry : entries) {
					String key = entry.getKey();
					JsonElement element = entry.getValue();
					if (null != element && element.isJsonPrimitive() 
							&& !StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.__cdata.name(), key)
							&& !StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.__text.name(), key)) {
						map.put(key, element.getAsString());
					}
				}
			}
		}
		return map;
	}

}
