package com.ilimi.taxonomy.content.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import com.ilimi.taxonomy.content.common.ElementMap;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Event;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;

public class ECRFToXMLConvertor {
	
	private static final String START_TAG_OPENING = "<";
	private static final String END_TAG_OPENING = "</";
	private static final String TAG_CLOSING = ">";
	private static final String ATTRIBUTE_KEY_VALUE_SAPERATOR = "=";
	private static final String BLANK_SPACE = " ";
	
	private static final char DOUBLE_QUOTE = '"';
	
	public String getContentXmlString(Plugin ecrfObject) {
		StringBuilder xml = new StringBuilder();
		if (null != ecrfObject) {
			xml.append(getElementXml(ecrfObject.getData()));
			xml.append(getInnerText(ecrfObject.getInnerText()));
			xml.append(getCData(ecrfObject.getcData()));
			xml.append(getContentManifestXml(ecrfObject.getManifest()));
			xml.append(getContentControllersXml(ecrfObject.getControllers()));
			xml.append(getPluginsXml(ecrfObject.getChildrenPlugin()));
			xml.append(getEventsXml(ecrfObject.getEvents()));
			xml.append(getEndTag(ecrfObject.getData().get(ContentWorkflowPipelineParams.element_name.name())));
		}
		return xml.toString();
	}
	
	private StringBuilder getContentManifestXml(Manifest manifest) {
		StringBuilder xml = new StringBuilder();
		if (null != manifest) {
			xml.append(getElementXml(manifest.getData()));
			xml.append(getInnerText(manifest.getInnerText()));
			xml.append(getCData(manifest.getcData()));
			xml.append(getContentMediasXml(manifest.getMedias()));
			xml.append(getEndTag(ContentWorkflowPipelineParams.manifest.name()));
		}
		return xml;
	}
	
	private StringBuilder getContentMediasXml(List<Media> medias) {
		StringBuilder xml = new StringBuilder();
		if (null != medias) {
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
			xml.append(getInnerText(media.getInnerText()));
			xml.append(getCData(media.getcData()));
			xml.append(getChildrenPlugin(media.getChildrenPlugin()));
			xml.append(getEndTag(ContentWorkflowPipelineParams.media.name()));
		}
		return xml;
	}
	
//	private StringBuilder getGroupedElementXml(List<Map<String, String>> elements) {
//		StringBuilder xml = new StringBuilder();
//		if (null != elements) {
//			Map<String, List<Map<String, String>>> groupingTags = new HashMap<String, List<Map<String, String>>>();
//			for (Map<String, String> element: elements) {
//				String groupTag = element.get(ContentWorkflowPipelineParams.group_element_name.name());
//				if (null == groupingTags.get(groupTag))
//					groupingTags.put(groupTag, new ArrayList<Map<String, String>>());
//				groupingTags.get(groupTag).add(element);
//				xml = createGroupedElementXML(groupingTags);
//			}
//		}
//		return xml;
//	}
	
//	private StringBuilder createGroupedElementXML(Map<String, List<Map<String, String>>> elements) {
//		StringBuilder xml = new StringBuilder();
//		if (null != elements) {
//			for (Entry<String, List<Map<String, String>>> entry: elements.entrySet()) {
//				if (!StringUtils.isBlank(entry.getKey())) {
//					xml.append(getStartTag(entry.getKey()));
//					List<Map<String, String>> lstMap = entry.getValue();
//					for (Map<String, String> map: lstMap) {
//						xml.append(getElementXml(map));
//						xml.append(getEndTag(map.get(ContentWorkflowPipelineParams.element_name.name())));
//					}
//					xml.append(getEndTag(entry.getKey()));
//				}
//			}
//		}
//		return xml;
//	}
	
	private StringBuilder getContentControllersXml(List<Controller> controllers) {
		StringBuilder xml = new StringBuilder();
		if (null != controllers) {
			for (Controller controller: controllers)
				xml.append(getContentControllerXml(controller));
		}
		return xml;
	}
	
	private StringBuilder getContentControllerXml(Controller controller) {
		StringBuilder xml = new StringBuilder();
		if (null != controller) {
			xml.append(getElementXml(controller.getData()));
			xml.append(getInnerText(controller.getInnerText()));
			xml.append(getCData(controller.getcData()));
			xml.append(getEndTag(ContentWorkflowPipelineParams.controller.name()));
		}
		return xml;
	}
	
	private StringBuilder getPluginsXml(List<Plugin> plugins) {
		StringBuilder xml = new StringBuilder();
		if (null != plugins) {
			for (Plugin plugin: plugins)
				xml.append(getPluginXml(plugin));
		}
		return xml;
	}
	
	private StringBuilder getPluginXml(Plugin plugin) {
		StringBuilder xml = new StringBuilder();
		if (null != plugin) {
			xml.append(getElementXml(plugin.getData()));
			xml.append(getInnerText(plugin.getInnerText()));
			xml.append(getCData(plugin.getcData()));
			xml.append(getChildrenPlugin(plugin.getChildrenPlugin()));
			xml.append(getContentManifestXml(plugin.getManifest()));
			xml.append(getContentControllersXml(plugin.getControllers()));
			xml.append(getEventsXml(plugin.getEvents()));
			xml.append(getPluginEndTag(plugin));
		}
		return xml;
	}
	
	private StringBuilder getCData(String cDataText) {
		StringBuilder xml = new StringBuilder();
		if (!StringUtils.isBlank(cDataText))
			xml.append(cDataText);
		return xml;
	}
	
	private StringBuilder getInnerText(String text) {
		StringBuilder xml = new StringBuilder();
		if (!StringUtils.isBlank(text))
			xml.append(StringEscapeUtils.escapeXml11(text));
		return xml;
	}
	
	private StringBuilder getChildrenPlugin(List<Plugin> childrenPlugin) {
		StringBuilder xml = new StringBuilder();
		if (null != childrenPlugin) {
			for (Plugin plugin: childrenPlugin)
				xml.append(getPluginXml(plugin));
		}
		return xml;
	}
	
	private StringBuilder getEventsXml(List<Event> events) {
		StringBuilder xml = new StringBuilder();
		if (null != events) {
			if (events.size() > 1) 
				xml.append(getStartTag(ContentWorkflowPipelineParams.events.name()));
			for (Event event: events)
				xml.append(getEventXml(event));
			if (events.size() > 1) 
				xml.append(getEndTag(ContentWorkflowPipelineParams.events.name()));
		}
		return xml;
	}
	
	private StringBuilder getEventXml(Event event) {
		StringBuilder xml = new StringBuilder();
		if (null != event) {
			xml.append(getElementXml(event.getData()));
			xml.append(getInnerText(event.getInnerText()));
			xml.append(getCData(event.getcData()));
			xml.append(getChildrenPlugin(event.getChildrenPlugin()));
			xml.append(getEndTag(ContentWorkflowPipelineParams.event.name()));
		}
		return xml;
	}
	
//	private StringBuilder getNonPluginElementsXml(List<Map<String, String>> nonPluginElements) {
//		StringBuilder xml = new StringBuilder();
//		if (null != nonPluginElements) {
//			for (Map<String, String> nonPluginElement: nonPluginElements) {
//				xml.append(getElementXml(nonPluginElement));
//				xml.append(getEndTag(nonPluginElement.get(ContentWorkflowPipelineParams.element_name.name())));
//			}
//		}
//		return xml;
//	}
	
	private StringBuilder getElementXml(Map<String, String> data) {
		StringBuilder xml = new StringBuilder();
		if (null != data) {
			xml.append(START_TAG_OPENING + data.get(ContentWorkflowPipelineParams.element_name.name()));
			for (Entry<String, String> entry: data.entrySet()) {
				if (!ElementMap.isSystemGenerateAttribute(entry.getKey())) {
					xml.append(BLANK_SPACE + entry.getKey() + ATTRIBUTE_KEY_VALUE_SAPERATOR + addQuote(entry.getValue()));
				}
			}
			xml.append(TAG_CLOSING);
		}
		return xml;
	}
	
	private StringBuilder getPluginEndTag(Plugin plugin) {
		StringBuilder xml = new StringBuilder();
		if (null != plugin && null != plugin.getData().get(ContentWorkflowPipelineParams.element_name.name())) {
			xml.append(getEndTag(plugin.getData().get(ContentWorkflowPipelineParams.element_name.name())));
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
	
	public static String addQuote(String str) {
		return DOUBLE_QUOTE + str + DOUBLE_QUOTE;
	}

}
