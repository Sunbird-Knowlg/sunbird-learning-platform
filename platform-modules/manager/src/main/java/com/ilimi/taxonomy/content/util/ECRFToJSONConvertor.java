package com.ilimi.taxonomy.content.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

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
			map.putAll(getInnerTextMap(media.getInnerText()));
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
	
//	private Map<String, Object> getGroupedPluginMap(List<Map<String, Object>> elements) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		if (null != elements) {
//			Map<String, List<Map<String, Object>>> groupingMap = new HashMap<String, List<Map<String, Object>>>();
//			for (Map<String, Object> element: elements) {
//				for (Entry<String, Object> entry: element.entrySet()) {
//					String groupKey = entry.getKey();
//					if (null == groupingMap.get(groupKey))
//						groupingMap.put(groupKey, new ArrayList<Map<String, Object>>());
//					groupingMap.get(groupKey).add(element);
//					map = createGroupedPluginMap(groupingMap);
//				}
//			}
//		}
//		return map;
//	}
//	
//	private Map<String, Object> createGroupedPluginMap(Map<String, List<Map<String, Object>>> groupingMap) {
//		Map<String, Object> map = new HashMap<String, Object>();
//		if (null != groupingMap) {
//			for (Entry<String, List<Map<String, Object>>> entry: groupingMap.entrySet()) {
//				List<Map<String, Object>> lstMap = new ArrayList<Map<String, Object>>();
//				List<Map<String, Object>> maps = entry.getValue();
//				for (Map<String, Object> m: maps) {
//					lstMap.add(m);
//				}
//				map.put(entry.getKey(), lstMap);
//			}
//		}
//		return map;
//		
//	}
	
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
			controllerMap.putAll(getInnerTextMap(controller.getInnerText()));
			controllerMap.putAll(getCDataMap(controller.getcData()));
		}
		return controllerMap;
	}
	
	private Map<String, Object> getPluginMaps(List<Plugin> plugins) {
		Map<String, List<Map<String, Object>>> map = new HashMap<String, List<Map<String, Object>>>();
		if (null != plugins) {
			for (Plugin plugin: plugins) {
				String element = plugin.getData().get(ContentWorkflowPipelineParams.cwp_element_name.name());
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
	
	private Map<String, Object> getInnerTextMap(String innerText) {
		Map<String, Object> innerTextMap = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(innerText))
			innerTextMap.put(ContentWorkflowPipelineParams.__text.name(), innerText);
		return innerTextMap;
	}
	
	private Map<String, Object> getCDataMap(String cDataText) {
		Map<String, Object> innerTextMap = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(cDataText))
			innerTextMap.put(ContentWorkflowPipelineParams.__cdata.name(), cDataText);
		return innerTextMap;
	}
	
//	private Map<String, Object> getNonPluginElementMap(List<Map<String, String>> nonPluginElements) {
//		Map<String, Object> nonPluginElementMap = new HashMap<String, Object>();
//		if (null != nonPluginElements)
//			nonPluginElementMap = getGroupedElementMapByElementName(nonPluginElements);
//		return nonPluginElementMap;
//	}
	
	private Map<String, Object> getChildrenPluginMap(List<Plugin> childrenPlugins) {
		Map<String, List<Map<String, Object>>> map = new HashMap<String, List<Map<String, Object>>>();
		if (null != childrenPlugins) {
			for (Plugin plugin: childrenPlugins) {
				String element = plugin.getData().get(ContentWorkflowPipelineParams.cwp_element_name.name());
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
				if (!ElementMap.isSystemGenerateAttribute(entry.getKey()) && StringUtils.isNotBlank(entry.getValue()))
					map.put(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}
	
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
