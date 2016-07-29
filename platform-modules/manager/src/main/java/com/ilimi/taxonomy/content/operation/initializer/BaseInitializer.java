package com.ilimi.taxonomy.content.operation.initializer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.pipeline.BasePipeline;
import com.ilimi.taxonomy.content.util.JSONContentParser;
import com.ilimi.taxonomy.content.util.XMLContentParser;

public class BaseInitializer extends BasePipeline {

	private static Logger LOGGER = LogManager.getLogger(BaseInitializer.class.getName());

	protected boolean isCompressRequired(Node node) {
		boolean required = false;
		if (null != node && null != node.getMetadata()) {
			LOGGER.info("Compression Required Check For Content Id: " + node.getIdentifier());
			String contentBody = (String) node.getMetadata().get(ContentWorkflowPipelineParams.body.name());
			String artifactUrl = (String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
			if (StringUtils.isBlank(artifactUrl) && StringUtils.isBlank(contentBody))
				throw new ClientException(ContentErrorCodeConstants.OPERATION_DENIED.name(),
						ContentErrorMessageConstants.UNABLE_TO_PUBLISH_OR_BUNDLE_CONTENT
								+ " | [Either Content 'body' or 'artifactUrl' is needed for the operation.]");
			if (StringUtils.isBlank(artifactUrl) && StringUtils.isNotBlank(contentBody))
				required = true;
		}
		return required;
	}

	protected Plugin getECRFObject(String contentBody) {
		Plugin plugin = new Plugin();
		String ecml = contentBody;
		String ecmlType = getECMLType(contentBody);
		if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.ecml.name())) {
			XMLContentParser parser = new XMLContentParser();
			plugin = parser.parseContent(ecml);
		} else if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.json.name())) {
			JSONContentParser parser = new JSONContentParser();
			plugin = parser.parseContent(ecml);
		}
		return plugin;
	}

	protected String getECMLType(String contentBody) {
		String type = "";
		if (!StringUtils.isBlank(contentBody)) {
			if (isValidJSON(contentBody))
				type = ContentWorkflowPipelineParams.json.name();
			else if (isValidXML(contentBody))
				type = ContentWorkflowPipelineParams.ecml.name();
			else
				throw new ClientException(ContentErrorCodeConstants.INVALID_BODY.name(),
						ContentErrorMessageConstants.INVALID_CONTENT_BODY);
			LOGGER.info("ECML Type: " + type);
		}
		return type;
	}

}
