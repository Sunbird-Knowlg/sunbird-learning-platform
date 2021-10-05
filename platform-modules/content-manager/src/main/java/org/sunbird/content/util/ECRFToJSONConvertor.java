package org.sunbird.content.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.content.common.ElementMap;
import org.sunbird.content.entity.Controller;
import org.sunbird.content.entity.Event;
import org.sunbird.content.entity.Manifest;
import org.sunbird.content.entity.Media;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;

import com.google.gson.Gson;

/**
 * The Class ECRFToJSONConvertor is a utility 
 * used to convert ECRF to JSON
 * holds Util Methodsto get ContentMetadata and Properties
 */
public class ECRFToJSONConvertor {
	
	/**
	 * gets the ContentJSON from the ContentECRF
	 * 
	 * @param ecrf the ContentECRF
	 * @return ContentJSON
	 */
	public String getContentJsonString(Plugin ecrf) {
		String content = "";
		Map<String, Object> map = new HashMap<String, Object>();
		Gson gson = new Gson(); 
		if (null != ecrf) {
			map.putAll(getElementMap(ecrf.getData()));
			map.putAll(getInnerTextMap(ecrf.getInnerText()));
			map.putAll(getCDataMap(ecrf.getcData()));
			map.putAll(getManifestMap(ecrf.getManifest()));
			map.putAll(getControllersMap(ecrf.getControllers()));
			map.putAll(getPluginMaps(ecrf.getChildrenPlugin()));
			map.putAll(getEventsMap(ecrf.getEvents()));
		}
		Map<String, Object> themeMap = new HashMap<String, Object>();
		themeMap.put(ContentWorkflowPipelineParams.theme.name(), map);
		content = gson.toJson(themeMap);
		return content;
	}
	
	/**
	 * gets the ContentManifestJsonMap
	 * 
	 * @param manifest the ManifestJson
	 * @return map the ManifestJsonMap
	 */
	private Map<String, Object> getManifestMap(Manifest manifest) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != manifest && null != manifest.getMedias() && !manifest.getMedias().isEmpty()) {
			Map<String, Object> manifestMap = new HashMap<String, Object>();
			manifestMap.putAll(getElementMap(manifest.getData()));
			manifestMap.putAll(getInnerTextMap(manifest.getInnerText()));
			manifestMap.putAll(getCDataMap(manifest.getcData()));
			manifestMap.putAll(getMediasMap(manifest.getMedias()));
			map.put(ContentWorkflowPipelineParams.manifest.name(), manifestMap);
		}
		return map;
	}
	
	/**
	 * gets the ContentMediasMap
	 * 
	 * @param medias the Medias
	 * @return map the MediasJsonMap
	 */
	private Map<String, Object> getMediasMap(List<Media> medias) {
		Map<String, Object> mediasMap = new HashMap<String, Object>();
		if (null != medias) {
			List<Map<String, Object>> mediaMaps = new ArrayList<Map<String, Object>>();
			for (Media media: medias)
				mediaMaps.add(getMediaMap(media));
			mediasMap.put(ContentWorkflowPipelineParams.media.name(), mediaMaps);
		}
		return mediasMap;
	}
	
	/**
	 * gets the MediaMap
	 * 
	 * @param media the Media
	 * @return map the MediaJsonMap
	 */
	private Map<String, Object> getMediaMap(Media media) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != media) {
			map.putAll(getElementMap(media.getData()));
			map.putAll(getInnerTextMap(media.getInnerText()));
			map.putAll(getCDataMap(media.getcData()));
			map.putAll(getChildrenPluginMap(media.getChildrenPlugin()));
		}
		return map;
	}
	
	/**
	 * gets the ControllersMap
	 * 
	 * @param controllers the Controllers
	 * @return map the ControllersJsonMap
	 */
	private Map<String, Object> getControllersMap(List<Controller> controllers) {
		Map<String, Object> controllersMap = new HashMap<String, Object>();
		if (null != controllers) {
			List<Map<String, Object>> controllerMaps = new ArrayList<Map<String, Object>>();
			for (Controller controller: controllers)
				controllerMaps.add(getControllerMap(controller));
			controllersMap.put(ContentWorkflowPipelineParams.controller.name(), controllerMaps);
		}
		return controllersMap;
	}
	
	/**
	 * gets the ControllerMap
	 * 
	 * @param controller the Controller
	 * @return map the ControllerJsonMap
	 */
	private Map<String, Object> getControllerMap(Controller controller) {
		Map<String, Object> controllerMap = new HashMap<String, Object>();
		if (null != controller) {
			controllerMap.putAll(getElementMap(controller.getData()));
			controllerMap.putAll(getInnerTextMap(controller.getInnerText()));
			controllerMap.putAll(getCDataMap(controller.getcData()));
		}
		return controllerMap;
	}
	
	/**
	 * gets the PluginsMap
	 * 
	 * @param plugins the Plugins
	 * @return map the pluginsJsonMap
	 */
	private Map<String, Object> getPluginMaps(List<Plugin> plugins) {
		Map<String, List<Map<String, Object>>> map = new LinkedHashMap<String, List<Map<String, Object>>>();
		if (null != plugins) {
			for (Plugin plugin: plugins) {
				Object obj = plugin.getData().get(ContentWorkflowPipelineParams.cwp_element_name.name());
				String element = (null == obj ? null : obj.toString());
				if (StringUtils.isNotBlank(element)) {
					List<Map<String, Object>> pluginList = map.get(element);
					if (null == pluginList) {
						pluginList = new ArrayList<Map<String, Object>>();
						map.put(element, pluginList);
					}
					pluginList.add(getPluginMap(plugin));
				}
			}
		}
		return groupPlugins(map);
	}
	
	/**
	 * gets the PluginMap
	 * 
	 * @param plugins the Plugin
	 * @return map the pluginJsonMap
	 */
	private Map<String, Object> getPluginMap(Plugin plugin) {
		Map<String, Object> pluginMap = new HashMap<String, Object>();
		if (null != plugin) {
			pluginMap.putAll(getElementMap(plugin.getData()));
			pluginMap.putAll(getInnerTextMap(plugin.getInnerText()));
			pluginMap.putAll(getCDataMap(plugin.getcData()));
			pluginMap.putAll(getChildrenPluginMap(plugin.getChildrenPlugin()));
			pluginMap.putAll(getManifestMap(plugin.getManifest()));
			pluginMap.putAll(getControllersMap(plugin.getControllers()));
			pluginMap.putAll(getEventsMap(plugin.getEvents()));
		}
		return pluginMap;
	}
	
	/**
	 * gets the InnerTextMap
	 * 
	 * @param innerText the innerText
	 * @return map the innerText
	 */
	private Map<String, Object> getInnerTextMap(String innerText) {
		Map<String, Object> innerTextMap = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(innerText))
			innerTextMap.put(ContentWorkflowPipelineParams.__text.name(), innerText);
		return innerTextMap;
	}
	
	/**
	 * gets the CDataMap
	 * 
	 * @param CDataText the CDataText
	 * @return map the CDataMap
	 */
	private Map<String, Object> getCDataMap(String cDataText) {
		Map<String, Object> CDataMap = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(cDataText))
			CDataMap.put(ContentWorkflowPipelineParams.__cdata.name(), cDataText);
		return CDataMap;
	}
	
	/**
	 * gets the ChildrenPluginMap
	 * 
	 * @param ChildrenPlugins the ChildrenPlugins
	 * @return map the ChildrenPluginJsonMap
	 */
	private Map<String, Object> getChildrenPluginMap(List<Plugin> childrenPlugins) {
		Map<String, List<Map<String, Object>>> map = new LinkedHashMap<String, List<Map<String, Object>>>();
		if (null != childrenPlugins) {
			for (Plugin plugin: childrenPlugins) {
				Object obj = plugin.getData().get(ContentWorkflowPipelineParams.cwp_element_name.name());
				String element = (null == obj ? null : obj.toString());
				if (StringUtils.isNotBlank(element)) {
					List<Map<String, Object>> pluginList = map.get(element);
					if (null == pluginList) {
						pluginList = new ArrayList<Map<String, Object>>();
						map.put(element, pluginList);
					}
					pluginList.add(getPluginMap(plugin));
				}
			}
		}
		return groupPlugins(map);
	}
	
	/**
	 * gets the EventsMap
	 * 
	 * @param Events the Events
	 * @return map the EventsJsonMap
	 */
	private Map<String, Object> getEventsMap(List<Event> events) {
		Map<String, Object> eventsMap = new HashMap<String, Object>();
		if (null != events) {
			List<Object> eventObjects = new ArrayList<Object>();
			for (Event event: events) {
				Map<String, Object> eventMap = new HashMap<String, Object>();
				eventMap.putAll(getElementMap(event.getData()));
				eventMap.putAll(getInnerTextMap(event.getInnerText()));
				eventMap.putAll(getCDataMap(event.getcData()));
				eventMap.putAll(getChildrenPluginMap(event.getChildrenPlugin()));
				eventObjects.add(eventMap);
			}
			if (events.size() == 1)
				eventsMap.put(ContentWorkflowPipelineParams.event.name(), filterListForSingleItem(eventObjects));
			else if (events.size() > 1) {
				Map<String, Object> eventData = new HashMap<String, Object>();
				eventData.put(ContentWorkflowPipelineParams.event.name(), eventObjects);
				eventsMap.put(ContentWorkflowPipelineParams.events.name(), eventData);
			}
		}
		return eventsMap;
	}
	
	/**
	 * filters List for SingleItem
	 * 
	 * @param objects the ObjectsList
	 * @return singleItem from the objectList
	 */
	private Object filterListForSingleItem(List<Object> objects) {
		Object object = new Object();
		if (null != objects) {
			if (objects.size() == 1)
				object = objects.get(0);
			else
				object = objects;
		}
		return object;
	}
	
	/**
	 * gets the ElementMap
	 * 
	 * @param data the DataList
	 * @return map the ElementJsonMap
	 */
	private Map<String, Object> getElementMap(Map<String, Object> data) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != data) {
			for (Entry<String, Object> entry: data.entrySet()) {
				if (!ElementMap.isSystemGenerateAttribute(entry.getKey()) && null != entry.getValue() && StringUtils.isNotBlank(entry.getValue().toString()))
					map.put(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}
	
	/**
	 * groups the Plugins
	 * 
	 * @param map the pluginsMap
	 * @return map the groupedJsonMap
	 */
	private Map<String, Object> groupPlugins(Map<String, List<Map<String, Object>>> map) {
		Map<String, Object> groupedMap = new HashMap<String, Object>();
		if (null != map && !map.isEmpty()) {
			for (Entry<String, List<Map<String, Object>>> entry : map.entrySet()) {
				List<Map<String, Object>> list = entry.getValue();
				if (null != list && !list.isEmpty()) {
					if (list.size() == 1)
						groupedMap.put(entry.getKey(), list.get(0));
					else
						groupedMap.put(entry.getKey(), list);
				}
			}
		}
		return groupedMap;
	}

}
