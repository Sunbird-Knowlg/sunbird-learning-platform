package com.ilimi.taxonomy.content.util;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.common.ElementMap;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Event;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

public class XMLContentParser {
	
	private static Logger LOGGER = LogManager.getLogger(XMLContentParser.class.getName());

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
			if(document != null){
				document = null;
	        }
		}
		return plugin;
	}
	
	private Plugin processContentDocument(Element root) {
		Plugin plugin = new Plugin();
		if (null != root && root.hasChildNodes()) {
			plugin.setId(getId(root));
			plugin.setData(getDataMap(root));
			plugin.setcData(getCData(root));
			plugin.setManifest(getContentManifest(root.getElementsByTagName(ContentWorkflowPipelineParams.manifest.name())));
			plugin.setControllers(getControllers(root.getElementsByTagName(ContentWorkflowPipelineParams.controller.name())));
			plugin.setChildrenPlugin(getChildrenPlugins(root));
			plugin.setEvents(getEvents(root));
		}
		return plugin;
	}
	
	private Manifest getContentManifest(NodeList manifestNodes) {
		Manifest manifest = new Manifest();
		if (null != manifestNodes && manifestNodes.getLength() > 0) {
			if (manifestNodes.getLength() > 1)
				throw new ClientException(ContentErrorCodeConstants.MULTIPLE_MANIFEST.name(), 
						ContentErrorMessageConstants.MORE_THAN_ONE_MANIFEST_SECTION_ERROR);
			List<Media> medias = new ArrayList<Media>();
			for (int i = 0; i < manifestNodes.getLength(); i++) {
				if (manifestNodes.item(i).hasChildNodes()) {
					NodeList mediaNodes = manifestNodes.item(i).getChildNodes();
					for (int j = 0; j < mediaNodes.getLength(); j++) {
						if (mediaNodes.item(j).getNodeType() == Node.ELEMENT_NODE && 
								StringUtils.equalsIgnoreCase(mediaNodes.item(j).getNodeName(), ContentWorkflowPipelineParams.media.name()))
							medias.add(getContentMedia(mediaNodes.item(j)));
					}
				}
				manifest.setId(getId(manifestNodes.item(i)));
				manifest.setData(getDataMap(manifestNodes.item(i)));
				manifest.setInnerText(getInnerText(manifestNodes.item(i)));
				manifest.setcData(getCData(manifestNodes.item(i)));
				manifest.setMedias(medias);
			}
			
			
		}
		return manifest;
	}
	
	private Media getContentMedia(Node mediaNode) {
		Media media = new Media();
		if (null != mediaNode) {
			String id = getAttributValueByName(mediaNode, ContentWorkflowPipelineParams.id.name());
			String type = getAttributValueByName(mediaNode, ContentWorkflowPipelineParams.src.name());
			String src = getAttributValueByName(mediaNode, ContentWorkflowPipelineParams.type.name());
			//TODO: Error Messages Should Come From Contants Class. 
			if (StringUtils.isBlank(id))
				throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
						"Error! Invalid Media ('id' is required.) in '"+ getNodeString(mediaNode) + "' ...");
			if (StringUtils.isBlank(type))
				throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
						"Error! Invalid Media ('src' is required.) in '"+ getNodeString(mediaNode) + "' ...");
			if (StringUtils.isBlank(src))
				throw new ClientException(ContentErrorCodeConstants.INVALID_MEDIA.name(), 
						"Error! Invalid Media ('type' is required.) in '"+ getNodeString(mediaNode) + "' ...");
			media.setId(id);
			media.setData(getDataMap(mediaNode));
			media.setInnerText(getInnerText(mediaNode));
			media.setcData(getCData(mediaNode));
			media.setSrc(src);
			media.setType(type);
			media.setChildrenPlugin(getChildrenPlugins(mediaNode));
		}
		return media;
	}
	
	private Map<String, String> getAttributeMap(Node node) {
		Map<String, String> attributes = new HashMap<String, String>();
		if (null != node && node.hasAttributes()) {
			NamedNodeMap attribute = node.getAttributes();
			for (int i = 0; i < attribute.getLength(); i++) {
				if (!StringUtils.isBlank(attribute.item(i).getNodeName()) && 
						!StringUtils.isBlank(attribute.item(i).getNodeValue()))
					attributes.put(attribute.item(i).getNodeName(), attribute.item(i).getNodeValue());
			}
		}
		return attributes;
	}
	
//	private List<Map<String, String>> getChildrenData(Node node) {
//		List<Map<String, String>> childrenTags = new ArrayList<Map<String, String>>();
//		NodeList nodeList = ((Element) node).getElementsByTagName("*");
//	    for (int i = 0; i < nodeList.getLength(); i++) {
//	        Node childNode = nodeList.item(i);
//	        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
//	            Map<String, String> map = getDataMap(childNode);
//	            if (!StringUtils.equalsIgnoreCase(node.getNodeName(), childNode.getParentNode().getNodeName()))
//	            	map.put(ContentWorkflowPipelineParams.group_element_name.name(), childNode.getParentNode().getNodeName());
//	            childrenTags.add(map);
//	        }
//	    }
//	    return childrenTags;
//	}
	
	private List<Controller> getControllers(NodeList controllerNodes) {
		List<Controller> controllers = new ArrayList<Controller>();
		if (null != controllerNodes && controllerNodes.getLength() > 0) {
			for (int i = 0; i < controllerNodes.getLength(); i++) {
				Controller controller = new Controller();
				if (controllerNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
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
	
//	private List<Plugin> getPlugins(Node node) {
//		List<Plugin> plugins = new ArrayList<Plugin>();
//		node = getPluginViewOfDocument(node);
//		if (null != node && node.hasChildNodes()) {
//			NodeList pluginNodes = node.getChildNodes();
//			for (int i = 0; i < pluginNodes.getLength(); i++) {
//				if (isPlugin(pluginNodes.item(i).getNodeName()) && pluginNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
//					plugins.add(getPlugin(pluginNodes.item(i)));
//				}
//			}
//		}
//		return plugins;
//	}
	
	private Plugin getPlugin(Node node) {
		Plugin plugin = new Plugin();
		if (null != node) {
			plugin.setId(getId(node));
			plugin.setData(getDataMap(node));
			plugin.setInnerText(getInnerText(node));
			plugin.setcData(getCData(node));
			plugin.setChildrenPlugin(getChildrenPlugins(node));
			plugin.setControllers(getControllers(node.getChildNodes()));
			plugin.setManifest(getContentManifest(((Element)node).getElementsByTagName(ContentWorkflowPipelineParams.manifest.name())));
			plugin.setEvents(getEvents(node));
		}
		return plugin;
	}
	
	private String getInnerText(Node node) {
		String innerText = "";
		if (null != node && node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()) {
			NodeList childrenItems = node.getChildNodes();
			for (int i = 0; i < childrenItems.getLength(); i++)
				if (childrenItems.item(i).getNodeType() == Node.TEXT_NODE) 
						innerText = StringUtils.trimToEmpty(cleanupString(childrenItems.item(i).getTextContent()));
		}
		return innerText;
	}
	
	private String cleanupString(String str) {
		return str.replace("\n", " ").replace("  ", "").replace("\t", " "); 
	}
	
	private List<Plugin> getChildrenPlugins(Node node) {
		List<Plugin> childrenPlugins = new ArrayList<Plugin>();
			if (null != node && node.hasChildNodes()) {
				NodeList childrenItems = node.getChildNodes();
				for (int i = 0; i < childrenItems.getLength(); i++) {
					if (childrenItems.item(i).getNodeType() == Node.ELEMENT_NODE && 
							isPlugin(childrenItems.item(i).getNodeName()) && 
							!isEvent(childrenItems.item(i).getNodeName())) {
						childrenPlugins.add(getPlugin(childrenItems.item(i)));
					}
				}
					
			}
		return childrenPlugins;
	}
	
//	private List<Map<String, String>> getNonPluginChildren(Node node) {
//		List<Map<String, String>> nonPluginChildren = new ArrayList<Map<String, String>>();
//		if (null != node && node.hasChildNodes()) {
//			NodeList nodes = node.getChildNodes();
//			for (int i = 0; i < nodes.getLength(); i++) {
//				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE && 
//						!isPlugin(nodes.item(i).getNodeName()) &&
//						!isEvent(nodes.item(i).getNodeName()) &&
//						!isReservedWrapper(nodes.item(i).getNodeName())) {
//					nonPluginChildren.add(getDataMap(nodes.item(i)));
//				}
//			}
//		}
//		return nonPluginChildren;
//	}
	
	private List<Event> getEvents(Node node) {				
		List<Event> events = new ArrayList<Event>();
		if (null != node && node.hasChildNodes()) {
			NodeList nodes = node.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE && 
						StringUtils.equalsIgnoreCase(nodes.item(i).getNodeName(), ContentWorkflowPipelineParams.events.name())) {
					events.addAll(getEvents(nodes.item(i)));
				}
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE && 
						isEvent(nodes.item(i).getNodeName())) {
					
					events.add(getEvent(nodes.item(i)));
				}
			}
		}
		return events;
	}
	
	private Event getEvent(Node node) {
		Event event = new Event();
		if (null !=  node) {
			event.setId(getId(node));
			event.setData(getDataMap(node));
			event.setInnerText(getInnerText(node));
			event.setcData(getCData(node));
			event.setChildrenPlugin(getChildrenPlugins(node));
		}
		return event;
	}
	
//	private List<Action> getActions(Node node) {
//		List<Action> actions = new ArrayList<Action>();
//		if (null != node && node.hasChildNodes()) {
//			NodeList nodes = node.getChildNodes();
//			for (int i = 0; i < nodes.getLength(); i++) {
//				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE && 
//						isAction(nodes.item(i).getNodeName())) {
//					Action action = new Action();
//					action.setData(getDataMap(nodes.item(i)));
//					actions.add(action);
//				}
//			}
//		}
//		return actions;
//	}
	
//	private Node getPluginViewOfDocument(Node node) {
//		try {
//		if (null != node) {
//			//Remove Manifest From Document
//			NodeList manifestNodeList = ((Element) node).getElementsByTagName(ContentWorkflowPipelineParams.manifest.name());
//			while (manifestNodeList.getLength() > 0) {
//				Node manifestNode = manifestNodeList.item(0);
//				manifestNode.getParentNode().removeChild(manifestNode);
//			}
//			// Remove Controllers From Document
//			NodeList controllerNodeList = ((Element) node).getElementsByTagName(ContentWorkflowPipelineParams.controller.name());
//			while (controllerNodeList.getLength() > 0) {
//				Node controllerNode = controllerNodeList.item(0);
//				controllerNode.getParentNode().removeChild(controllerNode);
//			}
//			// Normalize the DOM tree, puts all text nodes in the
//			// full depth of the sub-tree underneath this node
//			node.normalize();
//		}
//		} catch (ClassCastException e) {
//			throw new ServerException(ContentErrorCodes.ERR_CONTENT_WP_OBJECT_CONVERSION.name(), 
//					ContentErrorMessageConstants.XML_OBJECT_CONVERSION_CASTING_ERROR, e);
//		}
//		return node;
//	}
	
	private String getNodeString(Node node) {
	    try {
	        StringWriter writer = new StringWriter();
	        Transformer transformer = TransformerFactory.newInstance().newTransformer();
	        transformer.transform(new DOMSource(node), new StreamResult(writer));
	        String output = writer.toString();
	        return output.substring(output.indexOf("?>") + 2);	//remove <?xml version="1.0" encoding="UTF-8"?>
	    } catch (TransformerException e) {
	        LOGGER.error(ContentErrorMessageConstants.XML_TRANSFORMATION_ERROR, e);
	    }
	    return node.getTextContent();
	}
	
	private String getId(Node node) {
		return getAttributValueByName(node, ContentWorkflowPipelineParams.id.name());
	}
	
	private String getAttributValueByName(Node node, String attribute) {
		String value = "";
		if (null != node && !StringUtils.isBlank(attribute)) {
			String val = getAttributeMap(node).get(attribute);
			if (!StringUtils.isBlank(val))
				value = val;
		}
		return value;
	}
	
	private Map<String, String> getDataMap(Node node) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != node) {
			map = getAttributeMap(node);
			map.put(ContentWorkflowPipelineParams.element_name.name(), node.getNodeName());
		}
		return map;
	}

	private boolean isPlugin(String elementName) {
		return ElementMap.isPlugin(elementName);
	}

	private boolean isEvent(String elementName) {
		return ElementMap.isEvent(elementName);
	}

//	private boolean isReservedWrapper(String elementName) {
//		return ElementMap.isReservedWrapper(elementName);
//	}

}
