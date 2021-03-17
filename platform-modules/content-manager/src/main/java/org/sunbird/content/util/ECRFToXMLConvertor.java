package org.sunbird.content.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.sunbird.content.common.ElementMap;
import org.sunbird.content.entity.Controller;
import org.sunbird.content.entity.ECRFObject;
import org.sunbird.content.entity.Event;
import org.sunbird.content.entity.Manifest;
import org.sunbird.content.entity.Media;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;

/**
 * The Class ECRFToXMLConvertor is a utility 
 * used to convert ECRF to XML
 * holds Util Methods to get ContentMetadata and Properties
 */
public class ECRFToXMLConvertor {
	
	/** The Constant START_TAG_OPENING */
	private static final String START_TAG_OPENING = "<";
	
	/** The Constant END_TAG_OPENING */
	private static final String END_TAG_OPENING = "</";
	
	/** The Constant TAG_CLOSING */
	private static final String TAG_CLOSING = ">";
	
	/** The Constant ATTRIBUTE_KEY_VALUE_SAPERATOR */
	private static final String ATTRIBUTE_KEY_VALUE_SAPERATOR = "=";
	
	/** The Constant BLANK_SPACE */
	private static final String BLANK_SPACE = " ";
	
	/** The Constant DOUBLE_QUOTE */
	private static final char DOUBLE_QUOTE = '"';
	
	/**
	 * gets the ContentXML from the ContentECRF
	 * 
	 * @param ecrf the ContentECRF
	 * @return ContentXML
	 */
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
			xml.append(getEndTag(ecrfObject.getData().get(ContentWorkflowPipelineParams.cwp_element_name.name())));
		}
		return xml.toString();
	}
	
	/**
	 * gets the ContentManifestXml
	 * 
	 * @param manifest the Manifest
	 * @return xml the ManifestXml
	 */
	private StringBuilder getContentManifestXml(Manifest manifest) {
		StringBuilder xml = new StringBuilder();
		if (null != manifest && null != manifest.getMedias() && !manifest.getMedias().isEmpty()) {
			xml.append(getElementXml(manifest.getData()));
			xml.append(getInnerText(manifest.getInnerText()));
			xml.append(getCData(manifest.getcData()));
			xml.append(getContentMediasXml(manifest.getMedias()));
			xml.append(getECRFComponentEndTag(manifest));
		}
		return xml;
	}
	
	/**
	 * gets the ContentMediasXml
	 * 
	 * @param medias the MediasList
	 * @return xml the ContentMediaXml
	 */
	private StringBuilder getContentMediasXml(List<Media> medias) {
		StringBuilder xml = new StringBuilder();
		if (null != medias) {
			for (Media media: medias) {
				xml.append(getContentMediaXml(media));
			}
		}
		return xml;
	}
	
	/**
	 * gets the ContentMediaXml
	 * 
	 * @param medias the Media
	 * @return xml the ContentMediaXml
	 */
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
	
	/**
	 * gets the ContentControllersXml
	 * 
	 * @param controllers the ControllersList
	 * @return xml the ContentControllerXml
	 */
	private StringBuilder getContentControllersXml(List<Controller> controllers) {
		StringBuilder xml = new StringBuilder();
		if (null != controllers) {
			for (Controller controller: controllers)
				xml.append(getContentControllerXml(controller));
		}
		return xml;
	}
	
	/**
	 * gets the ContentControllerXml
	 * 
	 * @param controller the Controller
	 * @return xml the ContentControllerXml
	 */
	private StringBuilder getContentControllerXml(Controller controller) {
		StringBuilder xml = new StringBuilder();
		if (null != controller) {
			xml.append(getElementXml(controller.getData()));
			xml.append(getInnerText(controller.getInnerText()));
			xml.append(getCData(controller.getcData()));
			xml.append(getECRFComponentEndTag(controller));
		}
		return xml;
	}
	
	/**
	 * gets the PluginsXml
	 * 
	 * @param plugins the PluginsList
	 * @return xml the pluginsXml
	 */
	private StringBuilder getPluginsXml(List<Plugin> plugins) {
		StringBuilder xml = new StringBuilder();
		if (null != plugins) {
			for (Plugin plugin: plugins)
				xml.append(getPluginXml(plugin));
		}
		return xml;
	}
	
	/**
	 * gets the PluginXml
	 * 
	 * @param plugin the Plugin
	 * @return xml the pluginXml
	 */
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
			xml.append(getECRFComponentEndTag(plugin));
		}
		return xml;
	}
	
	/**
	 * gets the CData
	 * 
	 * @param CDataText the CDataText
	 * @return xml the CData
	 */
	private StringBuilder getCData(String cDataText) {
		StringBuilder xml = new StringBuilder();
		if (!StringUtils.isBlank(cDataText))
			xml.append("<![CDATA[" + cDataText + "]]>");
		return xml;
	}
	
	/**
	 * gets the InnerText
	 * 
	 * @param text the Text
	 * @return xml the InnerText
	 */
	private StringBuilder getInnerText(String text) {
		StringBuilder xml = new StringBuilder();
		if (!StringUtils.isBlank(text))
			xml.append(StringEscapeUtils.escapeXml11(text));
		return xml;
	}
	
	/**
	 * gets the ChildrenPlugin
	 * 
	 * @param ChildrenPlugin the ChildrenPlugin
	 * @return xml the ChildrenPlugin
	 */
	private StringBuilder getChildrenPlugin(List<Plugin> childrenPlugin) {
		StringBuilder xml = new StringBuilder();
		if (null != childrenPlugin) {
			for (Plugin plugin: childrenPlugin)
				xml.append(getPluginXml(plugin));
		}
		return xml;
	}
	
	/**
	 * gets the EventsXml
	 * 
	 * @param events the Events
	 * @return xml the EventsXml
	 */
	private StringBuilder getEventsXml(List<Event> events) {
		StringBuilder xml = new StringBuilder();
		if (null != events && !events.isEmpty()) {
			if (events.size() > 1) 
				xml.append(getStartTag(ContentWorkflowPipelineParams.events.name()));
			for (Event event: events)
				xml.append(getEventXml(event));
			if (events.size() > 1) 
				xml.append(getEndTag(ContentWorkflowPipelineParams.events.name()));
		}
		return xml;
	}
	
	/**
	 * gets the EventsXml
	 * 
	 * @param event the Event
	 * @return xml the EventXml
	 */
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
	
	/**
	 * gets the ElementXml
	 * 
	 * @param elementData the data
	 * @return xml the ElementXml
	 */
	private StringBuilder getElementXml(Map<String, Object> data) {
		StringBuilder xml = new StringBuilder();
		if (null != data) {
			xml.append(START_TAG_OPENING + data.get(ContentWorkflowPipelineParams.cwp_element_name.name()));
			for (Entry<String, Object> entry: data.entrySet()) {
				if (!ElementMap.isSystemGenerateAttribute(entry.getKey())) {
					xml.append(BLANK_SPACE + entry.getKey() + ATTRIBUTE_KEY_VALUE_SAPERATOR + addQuote(entry.getValue()));
				}
			}
			xml.append(TAG_CLOSING);
		}
		return xml;
	}
	
	/**
	 * gets the ECRFComponentEndTag
	 * 
	 * @param ECRFObject the Object
	 * @return xml the ECRFComponentEndTag
	 */
	private StringBuilder getECRFComponentEndTag(ECRFObject object) {
		StringBuilder xml = new StringBuilder();
		if (null != object && 
				null != object.getData() && 
				null != object.getData().get(ContentWorkflowPipelineParams.cwp_element_name.name())) {
			xml.append(getEndTag(object.getData().get(ContentWorkflowPipelineParams.cwp_element_name.name())));
		}
		return xml;
	}
	
	/**
	 * gets the EndTag
	 * 
	 * @param Object the Object
	 * @return xml the EndTag
	 */
	private StringBuilder getEndTag(Object obj) {
		String elementName = ((null == obj) ? null : obj.toString());
		StringBuilder xml = new StringBuilder();
		if (!StringUtils.isBlank(elementName))
			xml.append(END_TAG_OPENING + elementName + TAG_CLOSING);
		return xml;
	}
	
	/**
	 * gets the StartTag
	 * 
	 * @param elementName the elementName
	 * @return xml the StartTag
	 */
	private StringBuilder getStartTag(String elementName) {
		StringBuilder xml = new StringBuilder();
		if (!StringUtils.isBlank(elementName))
			xml.append(START_TAG_OPENING + elementName + TAG_CLOSING);
		return xml;
	}
	
	/**
	 * adds Quote(double_Quote)
	 * 
	 * @param Object the Object
	 * @return string the QuotedString
	 */
	public static String addQuote(Object obj) {
		String str = (null == obj) ? "" : obj.toString();
		return DOUBLE_QUOTE + StringEscapeUtils.escapeXml11(str) + DOUBLE_QUOTE;
	}

}
