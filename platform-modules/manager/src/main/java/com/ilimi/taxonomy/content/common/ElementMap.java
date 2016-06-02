package com.ilimi.taxonomy.content.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;

public class ElementMap {
	
	private static final Map<String, String> nonPluginElements = new HashMap<String, String>();

	private static final Map<String, String> eventElements = new HashMap<String, String>();

	private static final Map<String, String> actionElements = new HashMap<String, String>();
	
	private static final Map<String, String> reservedWrapperElements = new HashMap<String, String>();
	
	private static final List<String> systemGeneratedAttribute = new ArrayList<String>() {
		private static final long serialVersionUID = 7315113992066657012L;
		{
			add(ContentWorkflowPipelineParams.element_name.name());
			add(ContentWorkflowPipelineParams.group_element_name.name());
		}
	};

	static {
		nonPluginElements.put(ContentWorkflowPipelineParams.param.name(), ContentWorkflowPipelineParams.param.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.data.name(), ContentWorkflowPipelineParams.data.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.audioSprite.name(),ContentWorkflowPipelineParams.audioSprite.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.action.name(), ContentWorkflowPipelineParams.action.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.event.name(), ContentWorkflowPipelineParams.event.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.manifest.name(), ContentWorkflowPipelineParams.manifest.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.media.name(), ContentWorkflowPipelineParams.media.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.theme.name(), ContentWorkflowPipelineParams.theme.name());
		nonPluginElements.put(ContentWorkflowPipelineParams.events.name(), ContentWorkflowPipelineParams.events.name());

		eventElements.put(ContentWorkflowPipelineParams.event.name(), ContentWorkflowPipelineParams.event.name());
		actionElements.put(ContentWorkflowPipelineParams.action.name(), ContentWorkflowPipelineParams.action.name());
		
		reservedWrapperElements.put(ContentWorkflowPipelineParams.events.name(), ContentWorkflowPipelineParams.events.name());
	}
	
	public static boolean isPlugin(String elementName) {
		return !nonPluginElements.containsKey(elementName);
	}
	
	public static boolean isEvent(String elementName) {
		return eventElements.containsKey(elementName);
	}
	
	public static boolean isAction(String elementName) {
		return actionElements.containsKey(elementName);
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
