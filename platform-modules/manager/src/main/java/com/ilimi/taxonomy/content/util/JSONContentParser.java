package com.ilimi.taxonomy.content.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ilimi.common.exception.ClientException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Content;
import com.ilimi.taxonomy.content.entity.Manifest;
import com.ilimi.taxonomy.content.entity.Media;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

public class JSONContentParser {
	
	private static final Map<String, String> nonPluginTags = new HashMap<String, String>();

	private static final Map<String, String> eventTags = new HashMap<String, String>();

	private static final Map<String, String> actionTags = new HashMap<String, String>();

	{
		nonPluginTags.put(ContentWorkflowPipelineParams.param.name(), ContentWorkflowPipelineParams.param.name());
		nonPluginTags.put(ContentWorkflowPipelineParams.data.name(), ContentWorkflowPipelineParams.data.name());
		nonPluginTags.put(ContentWorkflowPipelineParams.audioSprite.name(),ContentWorkflowPipelineParams.audioSprite.name());
		nonPluginTags.put(ContentWorkflowPipelineParams.action.name(), ContentWorkflowPipelineParams.action.name());
		nonPluginTags.put(ContentWorkflowPipelineParams.event.name(), ContentWorkflowPipelineParams.event.name());
		nonPluginTags.put(ContentWorkflowPipelineParams.manifest.name(), ContentWorkflowPipelineParams.manifest.name());
		nonPluginTags.put(ContentWorkflowPipelineParams.media.name(), ContentWorkflowPipelineParams.media.name());
		nonPluginTags.put(ContentWorkflowPipelineParams.theme.name(), ContentWorkflowPipelineParams.theme.name());
		nonPluginTags.put(ContentWorkflowPipelineParams.events.name(), ContentWorkflowPipelineParams.events.name());

		eventTags.put(ContentWorkflowPipelineParams.event.name(), ContentWorkflowPipelineParams.event.name());
		actionTags.put(ContentWorkflowPipelineParams.action.name(), ContentWorkflowPipelineParams.action.name());
	}
	
	public Content parseContent(String json) {
		Content content = new Content();
		try {
			Map<String, Object> contentMap = ConversionUtil.jsonString2Map(json);
		} catch (JSONException ex) {
			throw new ClientException(ContentErrorCodes.ERR_CONTENT_WP_JSON_PARSE_ERROR.name(),
					ContentErrorMessageConstants.XML_PARSE_CONFIG_ERROR);
		}
		return content;
	}
	
	private Content processContentDocument(Element root) {
		Content content = new Content();
		return content;
	}
	
	private Manifest getContentManifest(NodeList manifestNodes) {
		Manifest manifest = new Manifest();
		return manifest;
	}
	
	private Media getContentMedia(Node mediaNode) {
		Media media = new Media();
		return media;
	}

}
