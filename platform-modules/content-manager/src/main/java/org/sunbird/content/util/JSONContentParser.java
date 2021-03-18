package org.sunbird.content.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.MiddlewareException;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.common.ElementMap;
import org.sunbird.content.entity.Controller;
import org.sunbird.content.entity.Event;
import org.sunbird.content.entity.Manifest;
import org.sunbird.content.entity.Media;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.learning.common.enums.ContentErrorCodes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The Class JSONContentParser is a utility 
 * used to parse Content to JSON
 * holds Util Methods to get ContentMetadata and Properties
 */
public class JSONContentParser {
	
	/**
	 * parse the Content(JSON)
	 *
	 * @param json the Json
	 * reads the Json and converts it to JsonObject
	 * and process the ContentJsonObject
	 * @return content
	 */
	public Plugin parseContent(String json) {
		Plugin content = new Plugin();
		try {
			Gson gson = new Gson();
			JsonObject root = gson.fromJson(json, JsonObject.class);
			content = processContentDocument(root);
		} catch (MiddlewareException e) {
			throw e;
		} catch (Exception e) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_WP_JSON_PARSE_ERROR.name(),
					ContentErrorMessageConstants.JSON_PARSE_CONFIG_ERROR, e);
		}
		return content;
	}
	
	/**
	 * process the ContentDocument
	 *
	 * @param jsonObject the JsonObject
	 * if JsonObject is not null sets all pluginMetadata
	 * @return plugin
	 */
	private Plugin processContentDocument(JsonObject root) {
		Plugin plugin = new Plugin();
		if (null != root) {
			if (root.has(ContentWorkflowPipelineParams.theme.name())) {
				root = root.getAsJsonObject(ContentWorkflowPipelineParams.theme.name());
				plugin.setId(getId(root));
				plugin.setData(getData(root, ContentWorkflowPipelineParams.theme.name()));
				plugin.setInnerText(getInnerText(root));
				plugin.setcData(getCData(root, ContentWorkflowPipelineParams.__cdata.name()));
				plugin.setManifest(getContentManifest(root, true));
				plugin.setControllers(getControllers(root));
				plugin.setChildrenPlugin(getChildrenPlugins(root));
				plugin.setEvents(getEvents(root));
			}
		}
		return plugin;
	}
	
	/**
	 * gets the ContentManifest
	 *
	 * @param jsonObject the JsonObject
	 * if JsonObject is not null sets all ManifestMetadata
	 * @return Manifest
	 */
	private Manifest getContentManifest(JsonObject object, boolean validateMedia) {
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
						manifest.setMedias(getMediaFromObject(manifestObj.get(ContentWorkflowPipelineParams.media.name()), validateMedia));
					}
				}
			}
		}
		return manifest;
	}
	
	/**
	 * gets the MediaFromObject
	 *
	 * @param jsonObject the JsonObject
	 * if JsonObject is not null sets all MediaMetadata
	 * @return ListOfMedias
	 */
	private List<Media> getMediaFromObject(JsonElement object, boolean validateMedia) {
		List<Media> medias = new ArrayList<Media>();
		if (null != object) {
			if (object.isJsonArray()) {
				JsonArray mediaObjs = object.getAsJsonArray();
				for (int i = 0; i < mediaObjs.size(); i++) {
					JsonElement element = mediaObjs.get(i);
					if (null != element && element.isJsonObject())
						medias.add(getContentMedia(element.getAsJsonObject(), validateMedia));
				}
			} else if (object.isJsonObject()) {
				JsonObject mediaObj = object.getAsJsonObject();
				medias.add(getContentMedia(mediaObj, validateMedia));
			}
		}
		return medias;
	}
	
	/**
	 * gets the ContentMedia
	 * 
	 * @param jsonObject the mediaObj
	 * if mediaObject is not null sets all MediaMetadata
	 * else throw ClientException
	 * @return media
	 */
	private Media getContentMedia(JsonObject mediaObj, boolean validateMedia) {
		Media media = new Media();
		try {
			if (null != mediaObj) {
				JsonElement id = mediaObj.get(ContentWorkflowPipelineParams.id.name());
				JsonElement src = mediaObj.get(ContentWorkflowPipelineParams.src.name());
				JsonElement type = mediaObj.get(ContentWorkflowPipelineParams.type.name());
				if (validateMedia) {
					if (null == id || !id.isJsonPrimitive() || (StringUtils.isBlank(id.toString()) && isMediaIdRequiredForMediaType(type)))
						throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
								"Error! Invalid Media ('id' is required.)");
					if (null == src || !src.isJsonPrimitive() || StringUtils.isBlank(src.toString()))
						throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
								"Error! Invalid Media ('src' is required.)");
					if (null == type || !type.isJsonPrimitive() || StringUtils.isBlank(type.toString()))
						throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
								"Error! Invalid Media ('type' is required.)");
				}
				if (null != id)
					media.setId(id.getAsString());
				if (null != src)
					media.setSrc(src.getAsString());
				if (null != type)
					media.setType(type.getAsString());
				media.setData(getData(mediaObj, ContentWorkflowPipelineParams.media.name()));
				media.setInnerText(getInnerText(mediaObj));
				media.setcData(getCData(mediaObj, ContentWorkflowPipelineParams.__cdata.name()));
				media.setChildrenPlugin(getChildrenPlugins(mediaObj));
			}
		} catch(MiddlewareException e) {
			throw e;
		} catch(Exception e) {
			throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
					ContentErrorMessageConstants.INVALID_MEDIA);
		}
		return media;
	}
	
	/**
	 * gets the List of Controllers
	 *
	 * @param jsonObject the JsonObject
	 * if JsonObject is not null sets all controllerMetadata
	 * @return ControllersList
	 */
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
	
	/**
	 * validate Controller
	 *
	 * @param jsonObject the controllerObj
	 * gets the id and type from the controllerObj
	 * if its null throws ClientException
	 */
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
	
	/**
	 * gets the plugin
	 *
	 * @param jsonObject the JsonObject
	 * @param key the key
	 * if JsonObject is not null sets all pluginMetadata
	 * @return plugin
	 */
	private Plugin getPlugin(JsonObject object, String key) {
		Plugin plugin = new Plugin();
		if (null != object) {
			plugin.setId(getId(object));
			plugin.setData(getData(object, key));
			plugin.setInnerText(getInnerText(object));
			plugin.setcData(getCData(object, ContentWorkflowPipelineParams.__cdata.name()));
			plugin.setChildrenPlugin(getChildrenPlugins(object));
			plugin.setManifest(getContentManifest(object, false));
			plugin.setControllers(getControllers(object));
			plugin.setEvents(getEvents(object));
		}
		return plugin;
	}
	
	/**
	 * gets the list of ChildrenPlugins
	 *
	 * @param jsonObject the JsonObject
	 * @return childrenPluginList
	 */
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
	
	/**
	 * gets the Events
	 *
	 * @param jsonObject the JsonObject
	 * @return EventList
	 */
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
	
	/**
	 * gets the EventFromObject
	 *
	 * @param jsonObject the JsonObject
	 * @return Events
	 */
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
	
	/**
	 * gets the EventList
	 *
	 * @param jsonArray the array
	 * @return EventsList
	 */
	private List<Event> getEventList(JsonArray array) {
		List<Event> events = new ArrayList<Event>();
		for(int i = 0; i < array.size(); i++) {
			JsonElement eventObj = array.get(i).getAsJsonObject();
			if (null != eventObj && eventObj.isJsonObject())
				events.add(getEvent(eventObj.getAsJsonObject()));
		}
		return events;
	}
	
	/**
	 * gets the Event
	 *
	 * @param jsonObject the object
	 * @return Event
	 */
	private Event getEvent(JsonObject object) {
		Event event = new Event();
		event.setId(getId(object));
		event.setData(getData(object, ContentWorkflowPipelineParams.event.name()));
		event.setInnerText(getInnerText(object));
		event.setcData(getCData(object, ContentWorkflowPipelineParams.__cdata.name()));
		event.setChildrenPlugin(getChildrenPlugins(object));
		return event;
	}
	
	/**
	 * gets the InnerText
	 *
	 * @param jsonObject the object
	 * @return InnerText
	 */
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
	
	/**
	 * gets the Id(identifier)as String 
	 *
	 * @param jsonObject the object
	 * @return Id
	 */
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
	
	/**
	 * gets the data
	 *
	 * @param jsonObject the object
	 * @param elementName the elementName
	 * gets DataMap from JsonObject
	 * @return dataMap
	 */
	private Map<String, Object> getData(JsonObject object, String elementName) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != object && !StringUtils.isBlank(elementName)) {
			map = getMapFromJsonObj(object);
			map.put(ContentWorkflowPipelineParams.cwp_element_name.name(), elementName);
		}
		return map;
	}
	
	/**
	 * gets the Cdata
	 *
	 * @param jsonObject the object
	 * @param elementName the elementName
	 * @return CData
	 */
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
	
	/**
	 * gets Map from JsonObject
	 *
	 * @param jsonObject the object
	 * @return JsonMap
	 */
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
	
	/**
	 * Checks if is media id required for given media type.
	 *
	 * @param type the type
	 * @return true, if is media id required for media type
	 */
	private boolean isMediaIdRequiredForMediaType(JsonElement type) {
		boolean isMediaIdRequired = true;
		if (StringUtils.isNotBlank(type.getAsString()) 
				&& (StringUtils.equalsIgnoreCase(type.getAsString(), ContentWorkflowPipelineParams.js.name()) 
						|| StringUtils.equalsIgnoreCase(type.getAsString(), ContentWorkflowPipelineParams.css.name())))
			isMediaIdRequired = false;
			
		return isMediaIdRequired;
	}

}
