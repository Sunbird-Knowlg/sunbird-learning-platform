package com.ilimi.taxonomy.content.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.ilimi.taxonomy.content.entity.Action;
import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Event;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;

public class EcrfToXmlConvertor {
	
	private static final String START_TAG_OPENING = "<";
	private static final String END_TAG_OPENING = "</";
	private static final String TAG_CLOSING = ">";
	private static final String ATTRIBUTE_KEY_VALUE_SAPERATOR = "=";
	private static final String BLANK_SPACE = " ";
	
	private static final List<String> systemGeneratedAttribute = new ArrayList<String>() {
		private static final long serialVersionUID = 7315113992066657012L;
		{
			add(ContentWorkflowPipelineParams.tag_name.name());
			add(ContentWorkflowPipelineParams.group_tag_name.name());
		}
	};
	
	public String getContentXmlString(Content ecrf) {
		StringBuilder xml = new StringBuilder();
		if (null != ecrf) {
			xml.append(getContentManifestXml(ecrf.getManifest()));
			xml.append(getContentControllersXml(ecrf.getControllers()));
			xml.append(getPluginsXml(ecrf.getPlugins()));
		}
		return xml.toString();
	}
	
	private StringBuilder getContentManifestXml(Manifest manifest) {
		StringBuilder xml = new StringBuilder();
		if (null != manifest) {
			xml.append(getContentMediasXml(manifest.getMedias()));
		}
		return xml;
	}
	
	private StringBuilder getContentMediasXml(List<Media> medias) {
		StringBuilder xml = new StringBuilder();
		if (null != medias && medias.size() > 0) {
			for (Media media: medias) {
				xml.append(getContentMediaXml(media));
			}
		}
		return xml;
	}
	
	private StringBuilder getContentMediaXml(Media media) {
		StringBuilder xml = new StringBuilder();
		if (null != media) {
			xml.append(getElementXml(media.getData()));
			xml.append(getGroupedElementXml(media.getChildrenData()));
			xml.append(getEndTag(ContentWorkflowPipelineParams.media.name()));
		}
		return xml;
	}
	
	private StringBuilder getGroupedElementXml(List<Map<String, String>> elements) {
		StringBuilder xml = new StringBuilder();
		if (null != elements && elements.size() > 0) {
			Map<String, List<Map<String, String>>> groupingTags = new HashMap<String, List<Map<String, String>>>();
			for (Map<String, String> element: elements) {
				String groupTag = element.get(ContentWorkflowPipelineParams.group_tag_name.name());
				if (null == groupingTags.get(groupTag))
					groupingTags.put(groupTag, new ArrayList<Map<String, String>>());
				groupingTags.get(groupTag).add(element);
				xml = createGroupedElementXML(groupingTags);
			}
		}
		return xml;
	}
	
	private StringBuilder createGroupedElementXML(Map<String, List<Map<String, String>>> elements) {
		StringBuilder xml = new StringBuilder();
		if (null != elements && elements.size() > 0) {
			for (Entry<String, List<Map<String, String>>> entry: elements.entrySet()) {
				xml.append(getStartTag(entry.getKey()));
				List<Map<String, String>> lstMap = entry.getValue();
				for (Map<String, String> map: lstMap) {
					xml.append(getElementXml(map));
				}
				xml.append(getEndTag(entry.getKey()));
			}
		}
		return xml;
	}
	
	private StringBuilder getContentControllersXml(List<Controller> controllers) {
		StringBuilder xml = new StringBuilder();
		if (null != controllers && controllers.size() > 0) {
			for (Controller controller: controllers)
				xml.append(getContentControllerXml(controller));
		}
		return xml;
	}
	
	private StringBuilder getContentControllerXml(Controller controller) {
		StringBuilder xml = new StringBuilder();
		if (null != controller) {
			xml.append(getElementXml(controller.getData()));
			xml.append(controller.getcData());
			xml.append(getEndTag(ContentWorkflowPipelineParams.controller.name()));
		}
		return xml;
	}
	
	private StringBuilder getPluginsXml(List<Plugin> plugins) {
		StringBuilder xml = new StringBuilder();
		if (null != plugins && plugins.size() > 0) {
			for (Plugin plugin: plugins)
				xml.append(getPluginXml(plugin));
		}
		return xml;
	}
	
	private StringBuilder getPluginXml(Plugin plugin) {
		StringBuilder xml = new StringBuilder();
		if (null != plugin) {
			xml.append(getElementXml(plugin.getData()));
			xml.append(getNonPluginElementsXml(plugin.getChildrenData()));
			xml.append(getChildrenPlugin(plugin.getChildrenPlugin()));
			xml.append(getEventsXml(plugin.getEvents()));
			xml.append(getPluginInnerText(plugin.getInnerText()));
			xml.append(getPluginEndTag(plugin));
		}
		return xml;
	}
	
	private StringBuilder getPluginInnerText(String text) {
		StringBuilder xml = new StringBuilder();
		if (!StringUtils.isBlank(text))
			xml.append(text);
		return xml;
	}
	
	private StringBuilder getChildrenPlugin(List<Plugin> childrenPlugin) {
		StringBuilder xml = new StringBuilder();
		if (null != childrenPlugin && childrenPlugin.size() > 0) {
			for (Plugin plugin: childrenPlugin)
				xml.append(getPluginXml(plugin));
		}
		return xml;
	}
	
	private StringBuilder getEventsXml(List<Event> events) {
		StringBuilder xml = new StringBuilder();
		if (null != events && events.size() > 0) {
			for (Event event: events)
				xml.append(getEventXml(event));
		}
		return xml;
	}
	
	private StringBuilder getEventXml(Event event) {
		StringBuilder xml = new StringBuilder();
		if (null != event) {
			xml.append(getElementXml(event.getData()));
			xml.append(getActionsXml(event.getActions()));
			xml.append(getEndTag(ContentWorkflowPipelineParams.event.name()));
		}
		return xml;
	}
	
	private StringBuilder getActionsXml(List<Action> actions) {
		StringBuilder xml = new StringBuilder();
		if (null != actions && actions.size() > 0) {
			for (Action action: actions)
				xml.append(getActionXml(action));
		}
		return xml;
	}
	
	private StringBuilder getActionXml(Action action) {
		StringBuilder xml = new StringBuilder();
		if (null != action) {
			xml.append(getElementXml(action.getData()));
			xml.append(getEndTag(ContentWorkflowPipelineParams.action.name()));
		}
		return xml;
	}
	
	private StringBuilder getNonPluginElementsXml(List<Map<String, String>> nonPluginElements) {
		StringBuilder xml = new StringBuilder();
		if (null != nonPluginElements && nonPluginElements.size() > 0) {
			for (Map<String, String> nonPluginElement: nonPluginElements)
				xml.append(getElementXml(nonPluginElement));
		}
		return xml;
	}
	
	private StringBuilder getElementXml(Map<String, String> data) {
		StringBuilder xml = new StringBuilder();
		if (null != data) {
			xml.append(START_TAG_OPENING + data.get(ContentWorkflowPipelineParams.tag_name.name()));
			for (Entry<String, String> entry: data.entrySet()) {
				if (!systemGeneratedAttribute.contains(entry.getKey())) {
					xml.append(entry.getKey() + ATTRIBUTE_KEY_VALUE_SAPERATOR + entry.getValue() + BLANK_SPACE);
				}
			}
			xml.append(TAG_CLOSING);
		}
		return xml;
	}
	
	private StringBuilder getPluginEndTag(Plugin plugin) {
		StringBuilder xml = new StringBuilder();
		if (null != plugin && null != plugin.getData().get(ContentWorkflowPipelineParams.tag_name.name())) {
			xml.append(plugin.getData().get(ContentWorkflowPipelineParams.tag_name.name()));
		}
		return xml;
	}
	
	private StringBuilder getEndTag(String elementName) {
		StringBuilder xml = new StringBuilder();
		if (!StringUtils.isBlank(elementName))
			xml.append(END_TAG_OPENING + elementName + TAG_CLOSING);
		return xml;
	}
	
	private StringBuilder getStartTag(String elementName) {
		StringBuilder xml = new StringBuilder();
		if (!StringUtils.isBlank(elementName))
			xml.append(START_TAG_OPENING + elementName + TAG_CLOSING);
		return xml;
	}

}
