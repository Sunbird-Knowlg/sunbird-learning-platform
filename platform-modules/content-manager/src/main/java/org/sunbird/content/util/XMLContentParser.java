package org.sunbird.content.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.sunbird.common.exception.ClientException;
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
import org.sunbird.telemetry.logger.TelemetryManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 * The Class XMLContentParser is a utility 
 * used to parse Content to XML
 * holds Util Methods to get ContentMetadata and Properties
 */
public class XMLContentParser {
	
	/** The logger. */
	

	/**
	 * parse the Content(XML)
	 *
	 * @param xml the xml
	 * process the ContentDocument
	 * @return plugin
	 */
	public Plugin parseContent(String xml) {
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document document = null;
		Plugin plugin = new Plugin();
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(xml)));
			document.getDocumentElement().normalize();
			Element root = document.getDocumentElement();
			plugin = processContentDocument(root);
		} catch (ParserConfigurationException e) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_WP_XML_PARSE_CONFIG_ERROR.name(),
					ContentErrorMessageConstants.XML_PARSE_CONFIG_ERROR, e);
		} catch (SAXException e) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_WP_NOT_WELL_FORMED_XML.name(),
					ContentErrorMessageConstants.XML_NOT_WELL_FORMED_ERROR, e);
		} catch (IOException e) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_WP_XML_IO_ERROR.name(),
					ContentErrorMessageConstants.XML_IO_ERROR, e);
		} finally {
			if (document != null) {
				document = null;
			}
		}
		return plugin;
	}
	
	/**
	 * process the ContentDocument
	 *
	 * @param elementroot the root
	 * @return plugin
	 */
	private Plugin processContentDocument(Element root) {
		Plugin plugin = new Plugin();
		if (null != root) {
			plugin.setId(getId(root));
			plugin.setData(getDataMap(root));
			plugin.setcData(getCData(root));
			plugin.setManifest(
					getContentManifest(root, true));
			plugin.setControllers(
					getControllers(root.getElementsByTagName(ContentWorkflowPipelineParams.controller.name())));
			plugin.setChildrenPlugin(getChildrenPlugins(root));
			plugin.setEvents(getEvents(root));
		}
		return plugin;
	}

	/**
	 * gets the ContentManifest
	 *
	 * @param root
	 * @param validateMedia
	 * @return
	 */
	private Manifest getContentManifest(Element root, boolean validateMedia) {
		NodeList childList = root.getChildNodes();
		Node manifestNode = null;
		for (int i = 0; i < childList.getLength(); i++) {
			if (StringUtils.equals(ContentWorkflowPipelineParams.manifest.name(), childList.item(i).getNodeName()))
				manifestNode = childList.item(i);
		}

		List<Media> medias = new ArrayList<>();
		if (null != manifestNode && manifestNode.hasChildNodes()) {
			NodeList mediaNodes = manifestNode.getChildNodes();
			for (int j = 0; j < mediaNodes.getLength(); j++) {
				if (mediaNodes.item(j).getNodeType() == Node.ELEMENT_NODE && StringUtils.equalsIgnoreCase(
						mediaNodes.item(j).getNodeName(), ContentWorkflowPipelineParams.media.name()))
					medias.add(getContentMedia(mediaNodes.item(j), validateMedia));
			}
		}

		Manifest manifest = new Manifest();
		manifest.setId(getId(manifestNode));
		manifest.setData(getDataMap(manifestNode));
		manifest.setInnerText(getInnerText(manifestNode));
		manifest.setcData(getCData(manifestNode));
		manifest.setMedias(medias);
		return manifest;
	}
	
	/**
	 * gets the ContentMedia
	 * 
	 * @param mediaNode the mediaNode
	 * if mediaNode is not null sets all MediaMetadata
	 * else throw ClientException
	 * @return media
	 */
	private Media getContentMedia(Node mediaNode, boolean validateMedia) {
		Media media = new Media();
		if (null != mediaNode) {
			String id = getAttributValueByName(mediaNode, ContentWorkflowPipelineParams.id.name());
			String type = getAttributValueByName(mediaNode, ContentWorkflowPipelineParams.type.name());
			String src = getAttributValueByName(mediaNode, ContentWorkflowPipelineParams.src.name());
			if (validateMedia) {
				if (StringUtils.isBlank(id) && isMediaIdRequiredForMediaType(type))
					throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(),
							"Error! Invalid Media ('id' is required.) in '" + getNodeString(mediaNode) + "' ...");
				if (StringUtils.isBlank(type))
					throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(),
							"Error! Invalid Media ('src' is required.) in '" + getNodeString(mediaNode) + "' ...");
				if (StringUtils.isBlank(src))
					throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(),
							"Error! Invalid Media ('type' is required.) in '" + getNodeString(mediaNode) + "' ...");
			}
			media.setId(id);
			media.setSrc(src);
			media.setType(type);
			media.setData(getDataMap(mediaNode));
			media.setInnerText(getInnerText(mediaNode));
			media.setcData(getCData(mediaNode));
			media.setChildrenPlugin(getChildrenPlugins(mediaNode));
		}
		return media;
	}
	
	/**
	 * gets the AttributeMap
	 * 
	 * @param node the Node
	 * if Node is not null and node has Attributes
	 * gets all AttributeProperties
	 * @return AttributesMap
	 */
	private Map<String, Object> getAttributeMap(Node node) {
		Map<String, Object> attributes = new HashMap<String, Object>();
		if (null != node && node.hasAttributes()) {
			NamedNodeMap attribute = node.getAttributes();
			for (int i = 0; i < attribute.getLength(); i++) {
				if (!StringUtils.isBlank(attribute.item(i).getNodeName())
						&& !StringUtils.isBlank(attribute.item(i).getNodeValue()))
					attributes.put(attribute.item(i).getNodeName(), attribute.item(i).getNodeValue());
			}
		}
		return attributes;
	}
	
	/**
	 * gets the List of Controllers
	 *
	 * @param controllerNodes the controllerNodes 
	 * if controllerNode is not null and controllerNodeLenghth > 0
	 * sets all controllerMetadata
	 * @return ControllersList
	 */
	private List<Controller> getControllers(NodeList controllerNodes) {
		List<Controller> controllers = new ArrayList<Controller>();
		if (null != controllerNodes && controllerNodes.getLength() > 0) {
			for (int i = 0; i < controllerNodes.getLength(); i++) {
				Controller controller = new Controller();
				if (controllerNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					String id = getAttributValueByName(controllerNodes.item(i),
							ContentWorkflowPipelineParams.id.name());
					String type = getAttributValueByName(controllerNodes.item(i),
							ContentWorkflowPipelineParams.type.name());
					if (StringUtils.isBlank(id))
						throw new ClientException(ContentErrorCodeConstants.INVALID_CONTROLLER.name(),
								"Error! Invalid Controller ('id' is required.) in '"
										+ getNodeString(controllerNodes.item(i)) + "' ...");
					if (StringUtils.isBlank(type))
						throw new ClientException(ContentErrorCodeConstants.INVALID_CONTROLLER.name(),
								"Error! Invalid Controller ('type' is required.) in '"
										+ getNodeString(controllerNodes.item(i)) + "' ...");
					if (!StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.items.name(), type)
							&& !StringUtils.equalsIgnoreCase(ContentWorkflowPipelineParams.data.name(), type))
						throw new ClientException(ContentErrorCodeConstants.INVALID_CONTROLLER.name(),
								"Error! Invalid Controller ('type' should be either 'items' or 'data') in '"
										+ getNodeString(controllerNodes.item(i)) + "' ...");
					controller.setId(getId(controllerNodes.item(i)));
					controller.setData(getDataMap(controllerNodes.item(i)));
					controller.setInnerText(getInnerText(controllerNodes.item(i)));
					controller.setcData(getCData(controllerNodes.item(i)));
				}
				controllers.add(controller);
			}
		}
		return controllers;
	}
	
	/**
	 * gets the Cdata
	 *
	 * @param node the Node
	 * @return CData
	 */
	private String getCData(Node node) {
		String cData = "";
		if (null != node && node.hasChildNodes()) {
			NodeList childrenNodes = node.getChildNodes();
			for (int i = 0; i < childrenNodes.getLength(); i++) {
				if (childrenNodes.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
					cData = childrenNodes.item(i).getNodeValue();
				}
			}
		}
		return cData;
	}
	
	/**
	 * gets the plugin
	 *
	 * @param node the Node
	 * if node is not null set all PluginProperties
	 * @return plugin
	 */
	private Plugin getPlugin(Node node) {
		Plugin plugin = new Plugin();
		if (null != node) {
			plugin.setId(getId(node));
			plugin.setData(getDataMap(node));
			plugin.setInnerText(getInnerText(node));
			plugin.setcData(getCData(node));
			plugin.setChildrenPlugin(getChildrenPlugins(node));
			plugin.setControllers(getControllers(
					((Element) node).getElementsByTagName(ContentWorkflowPipelineParams.controller.name())));
			plugin.setManifest(getContentManifest( (Element) node, false));
			plugin.setEvents(getEvents(node));
		}
		return plugin;
	}

	/**
	 * gets the InnerText
	 *
	 * @param node the Node
	 * @return InnerText
	 */
	private String getInnerText(Node node) {
		String innerText = "";
		if (null != node && node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()) {
			NodeList childrenItems = node.getChildNodes();
			for (int i = 0; i < childrenItems.getLength(); i++)
				if (childrenItems.item(i).getNodeType() == Node.TEXT_NODE)
					innerText = childrenItems.item(i).getTextContent();
		}
		return innerText;
	}
	
	/**
	 * gets the list of ChildrenPlugins
	 *
	 * @param Node the node
	 * @return childrenPluginList
	 */
	private List<Plugin> getChildrenPlugins(Node node) {
		List<Plugin> childrenPlugins = new ArrayList<Plugin>();
		if (null != node && node.hasChildNodes()) {
			NodeList childrenItems = node.getChildNodes();
			for (int i = 0; i < childrenItems.getLength(); i++) {
				if (childrenItems.item(i).getNodeType() == Node.ELEMENT_NODE
						&& isPlugin(childrenItems.item(i).getNodeName())
						&& !isEvent(childrenItems.item(i).getNodeName())) {
					childrenPlugins.add(getPlugin(childrenItems.item(i)));
				}
			}

		}
		return childrenPlugins;
	}
	
	/**
	 * gets the Events
	 *
	 * @param node the Node
	 * @return EventList
	 */
	private List<Event> getEvents(Node node) {				
		List<Event> events = new ArrayList<Event>();
		if (null != node && node.hasChildNodes()) {
			NodeList nodes = node.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE && StringUtils
						.equalsIgnoreCase(nodes.item(i).getNodeName(), ContentWorkflowPipelineParams.events.name())) {
					events.addAll(getEvents(nodes.item(i)));
				}
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE && isEvent(nodes.item(i).getNodeName())) {

					events.add(getEvent(nodes.item(i)));
				}
			}
		}
		return events;
	}
	
	/**
	 * gets the Event
	 *
	 * @param node the Node
	 * @return Event
	 */
	private Event getEvent(Node node) {
		Event event = new Event();
		if (null != node) {
			event.setId(getId(node));
			event.setData(getDataMap(node));
			event.setInnerText(getInnerText(node));
			event.setcData(getCData(node));
			event.setChildrenPlugin(getChildrenPlugins(node));
		}
		return event;
	}
	
	/**
	 * gets the NodesString
	 *
	 * @param node the Node
	 * @return nodeString
	 */
	private String getNodeString(Node node) {
		try {
			try (StringWriter writer = new StringWriter()) {
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.transform(new DOMSource(node), new StreamResult(writer));
				String output = writer.toString();
				return output.substring(output.indexOf("?>") + 2); // remove <?xml version="1.0" encoding="UTF-8"?>
			}
		} catch (TransformerException e) {
			TelemetryManager.error(ContentErrorMessageConstants.XML_TRANSFORMATION_ERROR, e);
		} catch (IOException e) {
			TelemetryManager.error(ContentErrorMessageConstants.STRING_WRITER_AUTO_CLOSE_ERROR, e);
		}
		return node.getTextContent();
	}
	
	/**
	 * gets the Id(identifier)as String 
	 *
	 * @param node the Node
	 * @return Id
	 */
	private String getId(Node node) {
		return getAttributValueByName(node, ContentWorkflowPipelineParams.id.name());
	}

	private String getAttributValueByName(Node node, String attribute) {
		String value = "";
		if (null != node && !StringUtils.isBlank(attribute)) {
			Object obj = getAttributeMap(node).get(attribute);
			String val = (null == obj ? null : obj.toString());
			if (!StringUtils.isBlank(val))
				value = val;
		}
		return value;
	}
	
	/**
	 * gets the dataMap
	 *
	 * @param node the Node
	 * gets attributeMap
	 * @return dataMap
	 */
	private Map<String, Object> getDataMap(Node node) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != node) {
			map = getAttributeMap(node);
			map.put(ContentWorkflowPipelineParams.cwp_element_name.name(), node.getNodeName());
		}
		return map;
	}

	/**
	 * checks isPlugin
	 *
	 * @param elementName the elementName
	 * @return true/false
	 */
	private boolean isPlugin(String elementName) {
		return ElementMap.isPlugin(elementName);
	}

	/**
	 * checks isEvent
	 *
	 * @param elementName the elementName
	 * @return true/false
	 */
	private boolean isEvent(String elementName) {
		return ElementMap.isEvent(elementName);
	}
	
	/**
	 * Checks if is media id required for given media type.
	 *
	 * @param type the type
	 * @return true, if is media id required for media type
	 */
	private boolean isMediaIdRequiredForMediaType(String type) {
		boolean isMediaIdRequired = true;
		if (StringUtils.isNotBlank(type) 
				&& (StringUtils.equalsIgnoreCase(type, ContentWorkflowPipelineParams.js.name()) 
						|| StringUtils.equalsIgnoreCase(type, ContentWorkflowPipelineParams.css.name())))
			isMediaIdRequired = false;
			
		return isMediaIdRequired;
	}

}
