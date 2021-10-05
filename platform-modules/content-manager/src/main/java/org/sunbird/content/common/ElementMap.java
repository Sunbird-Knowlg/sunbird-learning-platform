package org.sunbird.content.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sunbird.content.enums.ContentWorkflowPipelineParams;

public class ElementMap {
	
	private static final Map<String, String> nonPluginElements = new HashMap<String, String>();

	private static final Map<String, String> eventElements = new HashMap<String, String>();

	private static final Map<String, String> reservedWrapperElements = new HashMap<String, String>();
	
	private static final List<String> systemGeneratedAttribute = new ArrayList<String>() {
		private static final long serialVersionUID = 7315113992066657012L;
		{
			add(ContentWorkflowPipelineParams.cwp_element_name.name());
			add(ContentWorkflowPipelineParams.cwp_group_element_name.name());
		}
	};

	static {
		nonPluginElements.put(ContentWorkflowPipelineParams.manifest.name(), ContentWorkflowPipelineParams.manifest.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.controller.name(), ContentWorkflowPipelineParams.controller.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.media.name(), ContentWorkflowPipelineParams.media.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.events.name(), ContentWorkflowPipelineParams.events.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.event.name(), ContentWorkflowPipelineParams.event.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.__cdata.name(), ContentWorkflowPipelineParams.__cdata.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.__text.name(), ContentWorkflowPipelineParams.__text.name());

		eventElements.put(ContentWorkflowPipelineParams.event.name(), ContentWorkflowPipelineParams.event.name());
		
		reservedWrapperElements.put(ContentWorkflowPipelineParams.events.name(), ContentWorkflowPipelineParams.events.name());
	}
	
	public static boolean isPlugin(String elementName) {
		return !nonPluginElements.containsKey(elementName);
	}
	
	public static boolean isEvent(String elementName) {
		return eventElements.containsKey(elementName);
	}
	
	public static boolean isReservedWrapper(String elementName) {
		return reservedWrapperElements.containsKey(elementName);
	}
	
	public static boolean isSystemGenerateAttribute(String attributeName)  {
		return systemGeneratedAttribute.contains(attributeName);
	}
	
	public static List<String> getSystemGenerateAttribute() {
		return systemGeneratedAttribute;
	}

}
