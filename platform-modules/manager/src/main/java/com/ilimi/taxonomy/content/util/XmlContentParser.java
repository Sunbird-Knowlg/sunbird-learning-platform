package com.ilimi.taxonomy.content.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.common.TagMap;
import com.ilimi.taxonomy.content.entity.Action;
import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.entity.Controller;
import com.ilimi.taxonomy.content.entity.Event;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

public class XmlContentParser {

	public Content parseContent(String xml) {
		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document document = null;
		Content content = new Content();
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(xml)));
			document.getDocumentElement().normalize();
			Element root = document.getDocumentElement();
			content = processContentDocument(root);
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
		return content;
	}
	
	private Content processContentDocument(Element root) {
		Content content = new Content();
		if (null != root && root.hasChildNodes()) {
			content.setManifest(getContentManifest(root.getElementsByTagName(ContentWorkflowPipelineParams.manifest.name())));
			content.setControllers(getControllers(root.getElementsByTagName(ContentWorkflowPipelineParams.controller.name())));
			content.setPlugins(getPlugins(root));
		}
		return content;
	}
	
	private Manifest getContentManifest(NodeList manifestNodes) {
		Manifest manifest = new Manifest();
		if (null != manifestNodes && manifestNodes.getLength() > 0) {
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
			}
			manifest.setMedias(medias);
		}
		return manifest;
	}
	
	private Media getContentMedia(Node mediaNode) {
		Media media = new Media();
		if (null != mediaNode) {
			media.setData(getDataMap(mediaNode));
			media.setChildrenData(getChildrenData(mediaNode));
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
	
	public List<Map<String, String>> getChildrenData(Node node) {
		List<Map<String, String>> childrenTags = new ArrayList<Map<String, String>>();
		NodeList nodeList = ((Element) node).getElementsByTagName("*");
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        Node childNode = nodeList.item(i);
	        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
	            Map<String, String> map = getDataMap(childNode);
	            map.put(ContentWorkflowPipelineParams.group_tag_name.name(), childNode.getParentNode().getNodeName());
	            childrenTags.add(map);
	        }
	    }
	    return childrenTags;
	}
	
	private List<Controller> getControllers(NodeList controllerNodes) {
		List<Controller> controllers = new ArrayList<Controller>();
		if (null != controllerNodes && controllerNodes.getLength() > 0) {
			for (int i = 0; i < controllerNodes.getLength(); i++) {
				Controller controller = new Controller();
				if (controllerNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					controller.setData(getDataMap(controllerNodes.item(i)));
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
	
	private List<Plugin> getPlugins(Node node) {
		List<Plugin> plugins = new ArrayList<Plugin>();
		node = getPluginViewOfDocument(node);
		if (null != node && node.hasChildNodes()) {
			NodeList pluginNodes = node.getChildNodes();
			for (int i = 0; i < pluginNodes.getLength(); i++) {
				if (isPlugin(pluginNodes.item(i).getNodeName()) && pluginNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					plugins.add(getPlugin(pluginNodes.item(i)));
				}
			}
		}
		return plugins;
	}
	
	private Plugin getPlugin(Node node) {
		Plugin plugin = new Plugin();
		if (null != node) {
			plugin.setData(getDataMap(node));
			plugin.setChildrenData(getNonPluginChildren(node));
			plugin.setEvents(getEvents(node));
			plugin.setChildrenPlugin(getChildrenPlugins(node));
			plugin.setInnerText(getInnerText(node));
		}
		return plugin;
	}
	
	private String getInnerText(Node node) {
		String innerText = "";
		if (null != node && node.getNodeType() == Node.ELEMENT_NODE)
			innerText = cleanupString(node.getTextContent());
		return innerText;
	}
	
	private String cleanupString(String str) {
		return str.replace("/n", "").replace("  ", "").replace("/t", ""); 
	}
	
	private List<Plugin> getChildrenPlugins(Node node) {
		List<Plugin> childrenPlugins = new ArrayList<Plugin>();
			if (null != node && node.hasChildNodes()) {
				NodeList childrenItems = node.getChildNodes();
				for (int i = 0; i < childrenItems.getLength(); i++) {
					if (childrenItems.item(i).getNodeType() == Node.ELEMENT_NODE && 
							isPlugin(childrenItems.item(i).getNodeName()) && 
							!isEvent(childrenItems.item(i).getNodeName()) &&
							!isAction(childrenItems.item(i).getNodeName())) {
						Plugin plugin = new Plugin();
						plugin.setData(getDataMap(childrenItems.item(i)));
						plugin.setEvents(getEvents(childrenItems.item(i)));
						plugin.setChildrenData(getNonPluginChildren(childrenItems.item(i)));
						plugin.setChildrenPlugin(getChildrenPlugins(childrenItems.item(i)));
						plugin.setInnerText(getInnerText(childrenItems.item(i)));
						childrenPlugins.add(plugin);
					}
				}
					
			}
		return childrenPlugins;
	}
	
	private List<Map<String, String>> getNonPluginChildren(Node node) {
		List<Map<String, String>> nonPluginChildren = new ArrayList<Map<String, String>>();
		if (null != node && node.hasChildNodes()) {
			NodeList nodes = node.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE && !isPlugin(nodes.item(i).getNodeName())) {
					nonPluginChildren.add(getDataMap(nodes.item(i)));
				}
			}
		}
		return nonPluginChildren;
	}
	
	private List<Event> getEvents(Node node) {				//'<events>' tag is not handled
		List<Event> events = new ArrayList<Event>();
		if (null != node && node.hasChildNodes()) {
			NodeList nodes = node.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE && 
						isEvent(nodes.item(i).getNodeName())) {
					Event event = new Event();
					event.setData(getDataMap(nodes.item(i)));
					event.setActions(getActions(nodes.item(i)));
					events.add(event);
				}
			}
		}
		return events;
	}
	
	private List<Action> getActions(Node node) {
		List<Action> actions = new ArrayList<Action>();
		if (null != node && node.hasChildNodes()) {
			NodeList nodes = node.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE && 
						isAction(nodes.item(i).getNodeName())) {
					Action action = new Action();
					action.setData(getDataMap(nodes.item(i)));
					actions.add(action);
				}
			}
		}
		return actions;
	}
	
	private Node getPluginViewOfDocument(Node node) {
		try {
		if (null != node) {
			//Remove Manifest From Document
			NodeList manifestNodeList = ((Element) node).getElementsByTagName(ContentWorkflowPipelineParams.manifest.name());
			while (manifestNodeList.getLength() > 0) {
				Node manifestNode = manifestNodeList.item(0);
				manifestNode.getParentNode().removeChild(manifestNode);
			}
			// Remove Controllers From Document
			NodeList controllerNodeList = ((Element) node).getElementsByTagName(ContentWorkflowPipelineParams.controller.name());
			while (controllerNodeList.getLength() > 0) {
				Node controllerNode = controllerNodeList.item(0);
				controllerNode.getParentNode().removeChild(controllerNode);
			}
			// Normalize the DOM tree, puts all text nodes in the
			// full depth of the sub-tree underneath this node
			node.normalize();
		}
		} catch (ClassCastException e) {
			throw new ServerException(ContentErrorCodes.ERR_CONTENT_WP_OBJECT_CONVERSION.name(), 
					ContentErrorMessageConstants.XML_OBJECT_CONVERSION_CASTING_ERROR, e);
		}
		return node;
	} 
	
	private Map<String, String> getDataMap(Node node) {
		Map<String, String> map = new HashMap<String, String>();
		if (null != node) {
			map = getAttributeMap(node);
			map.put(ContentWorkflowPipelineParams.tag_name.name(), node.getNodeName());
		}
		return map;
	}

	private boolean isPlugin(String elementName) {
		return TagMap.isPlugin(elementName);
	}

	private boolean isEvent(String elementName) {
		return TagMap.isEvent(elementName);
	}

	private boolean isAction(String elementName) {
		return TagMap.isAction(elementName);
	}

}
