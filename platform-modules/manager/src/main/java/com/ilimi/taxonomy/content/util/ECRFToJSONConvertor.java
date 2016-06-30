package com.ilimi.taxonomy.content.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.ilimi.taxonomy.content.common.ElementMap;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Event;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;

public class ECRFToJSONConvertor {
	
	public String getContentJsonString(Plugin ecrf) {
		String content = "";
		Map<String, Object> map = new HashMap<String, Object>();
		Gson gson = new Gson(); 
		if (null != ecrf) {
			map.putAll(getElementMap(ecrf.getData()));
			map.putAll(getPluginInnerTextMap(ecrf.getInnerText()));
			map.putAll(getCDataMap(ecrf.getcData()));
			map.putAll(getManifestMap(ecrf.getManifest()));
			map.putAll(getControllersMap(ecrf.getControllers()));
			map.putAll(getPluginMaps(ecrf.getChildrenPlugin()));
			map.putAll(getEventsMap(ecrf.getEvents()));
		}
		content = gson.toJson(map);
		return content;
	}
	
	private Map<String, Object> getManifestMap(Manifest manifest) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != manifest) {
			Map<String, Object> manifestMap = new HashMap<String, Object>();
			manifestMap.putAll(getElementMap(manifest.getData()));
			manifestMap.putAll(getPluginInnerTextMap(manifest.getInnerText()));
			manifestMap.putAll(getCDataMap(manifest.getcData()));
			manifestMap.putAll(getMediasMap(manifest.getMedias()));
			map.put(ContentWorkflowPipelineParams.manifest.name(), manifestMap);
		}
		return map;
	}
	
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
	
	private Map<String, Object> getMediaMap(Media media) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != media) {
			map.putAll(getElementMap(media.getData()));
			map.putAll(getPluginInnerTextMap(media.getInnerText()));
			map.putAll(getCDataMap(media.getcData()));
			map.putAll(getChildrenPluginMap(media.getChildrenPlugin()));
		}
		return map;
	}
	
//	private Map<String, Object> getGroupedElementMap(List<Map<String, String>> elements) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		if (null != elements) {
//			Map<String, List<Map<String, String>>> groupingMap = new HashMap<String, List<Map<String, String>>>();
//			for (Map<String, String> element: elements) {
//				String groupKey = element.get(ContentWorkflowPipelineParams.group_element_name.name());
//				if (null == groupingMap.get(groupKey))
//					groupingMap.put(groupKey, new ArrayList<Map<String, String>>());
//				groupingMap.get(groupKey).add(element);
//				map = createGroupedElementMap(groupingMap);
//			}
//		}
//		return map;
//	}
	
//	private Map<String, Object> getGroupedElementMapByElementName(List<Map<String, String>> elements) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		if (null != elements) {
//			Map<String, List<Map<String, String>>> groupingMap = new HashMap<String, List<Map<String, String>>>();
//			for (Map<String, String> element: elements) {
//				String groupKey = element.get(ContentWorkflowPipelineParams.element_name.name());
//				if (null == groupingMap.get(groupKey))
//					groupingMap.put(groupKey, new ArrayList<Map<String, String>>());
//				groupingMap.get(groupKey).add(element);
//				map = createGroupedElementMap(groupingMap);
//			}
//		}
//		return map;
//	}
	
	private Map<String, Object> getGroupedPluginMap(List<Map<String, Object>> elements) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != elements) {
			Map<String, List<Map<String, Object>>> groupingMap = new HashMap<String, List<Map<String, Object>>>();
			for (Map<String, Object> element: elements) {
				for (Entry<String, Object> entry: element.entrySet()) {
					String groupKey = entry.getKey();
					if (null == groupingMap.get(groupKey))
						groupingMap.put(groupKey, new ArrayList<Map<String, Object>>());
					groupingMap.get(groupKey).add(element);
					map = createGroupedPluginMap(groupingMap);
				}
			}
		}
		return map;
	}
	
	private Map<String, Object> createGroupedPluginMap(Map<String, List<Map<String, Object>>> groupingMap) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != groupingMap) {
			for (Entry<String, List<Map<String, Object>>> entry: groupingMap.entrySet()) {
				List<Map<String, Object>> lstMap = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> maps = entry.getValue();
				for (Map<String, Object> m: maps) {
					lstMap.add(m);
				}
				map.put(entry.getKey(), lstMap);
			}
		}
		return map;
		
	}
	
//	private Map<String, Object> createGroupedElementMap(Map<String, List<Map<String, String>>> groupingMap) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		if (null != groupingMap) {
//			for (Entry<String, List<Map<String, String>>> entry: groupingMap.entrySet()) {
//				List<Map<String, String>> lstMap = new ArrayList<Map<String, String>>();
//				List<Map<String, String>> maps = entry.getValue();
//				for (Map<String, String> m: maps) {
//					lstMap.add(getElementMap(m));
//				}
//				map.put(entry.getKey(), lstMap);
//			}
//		}
//		return map;
//		
//	}
	
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
	
	private Map<String, Object> getControllerMap(Controller controller) {
		Map<String, Object> controllerMap = new HashMap<String, Object>();
		if (null != controller) {
			controllerMap.putAll(getElementMap(controller.getData()));
			controllerMap.putAll(getPluginInnerTextMap(controller.getInnerText()));
			controllerMap.putAll(getCDataMap(controller.getcData()));
		}
		return controllerMap;
	}
	
	private Map<String, Object> getPluginMaps(List<Plugin> plugins) {
		Map<String, Object> pluginMap = new HashMap<String, Object>();
		if (null != plugins) {
			List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
			for (Plugin plugin: plugins) {
				maps.add(getPluginMap(plugin));
			}
			pluginMap = getGroupedPluginMap(maps);
		}
		return pluginMap;
	}
	
	private Map<String, Object> getPluginMap(Plugin plugin) {
		Map<String, Object> pluginMap = new HashMap<String, Object>();
		if (null != plugin) {
			pluginMap.putAll(getElementMap(plugin.getData()));
			pluginMap.putAll(getPluginInnerTextMap(plugin.getInnerText()));
			pluginMap.putAll(getCDataMap(plugin.getcData()));
			pluginMap.putAll(getChildrenPluginMap(plugin.getChildrenPlugin()));
			pluginMap.putAll(getManifestMap(plugin.getManifest()));
			pluginMap.putAll(getControllersMap(plugin.getControllers()));
			pluginMap.putAll(getEventsMap(plugin.getEvents()));
		}
		return pluginMap;
	}
	
	private Map<String, Object> getPluginInnerTextMap(String innerText) {
		Map<String, Object> innerTextMap = new HashMap<String, Object>();
		if (null != innerTextMap) {
			innerTextMap.put(ContentWorkflowPipelineParams.__text.name(), innerText);
		}
		return innerTextMap;
	}
	
	private Map<String, Object> getCDataMap(String cDataText) {
		Map<String, Object> innerTextMap = new HashMap<String, Object>();
		if (null != innerTextMap) {
			innerTextMap.put(ContentWorkflowPipelineParams.__cdata.name(), cDataText);
		}
		return innerTextMap;
	}
	
//	private Map<String, Object> getNonPluginElementMap(List<Map<String, String>> nonPluginElements) {
//		Map<String, Object> nonPluginElementMap = new HashMap<String, Object>();
//		if (null != nonPluginElements)
//			nonPluginElementMap = getGroupedElementMapByElementName(nonPluginElements);
//		return nonPluginElementMap;
//	}
	
	private Map<String, Object> getChildrenPluginMap(List<Plugin> childrenPlugin) {
		Map<String, Object> childrenPluginMap = new HashMap<String, Object>();
		if (null != childrenPlugin) {
			List<Map<String, Object>> childPlugins = new ArrayList<Map<String, Object>>();
			for (Plugin plugin: childrenPlugin) {
				childPlugins.add(getPluginMap(plugin));
			}
			childrenPluginMap = getGroupedPluginMap(childPlugins);
		}
		return childrenPluginMap;
	}
	
	private Map<String, Object> getEventsMap(List<Event> events) {
		Map<String, Object> eventsMap = new HashMap<String, Object>();
		if (null != events) {
			List<Object> eventObjects = new ArrayList<Object>();
			for (Event event: events) {
				Map<String, Object> eventMap = new HashMap<String, Object>();
				eventMap.putAll(getElementMap(event.getData()));
				eventMap.putAll(getPluginInnerTextMap(event.getInnerText()));
				eventMap.putAll(getCDataMap(event.getcData()));
				eventMap.putAll(getChildrenPluginMap(event.getChildrenPlugin()));
				eventObjects.add(eventMap);
			}
			if (events.size() == 1)
				eventsMap.put(ContentWorkflowPipelineParams.event.name(), filterListForSingleItem(eventObjects));
			else if (events.size() > 1)
				eventsMap.put(ContentWorkflowPipelineParams.events.name(), filterListForSingleItem(eventObjects));
		}
		return eventsMap;
	}
	
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
	private Map<String, String> getElementMap(Map<String, String> data) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != data) {
			for (Entry<String, String> entry: data.entrySet()) {
				if (!ElementMap.isSystemGenerateAttribute(entry.getKey()))
					map.put(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}

}
