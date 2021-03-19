package org.sunbird.content.operation.initializer;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.ClientException;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.pipeline.BasePipeline;
import org.sunbird.content.util.JSONContentParser;
import org.sunbird.content.util.XMLContentParser;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.telemetry.logger.TelemetryManager;

/**
 * The Class BaseInitializer is a BaseClass for all Initializers, extends
 * BasePipeline which mainly holds Common Methods and operations of a
 * ContentNode. BaseInitializer holds methods to get ECML and ECRFtype from the
 * ContentBody
 */
public class BaseInitializer extends BasePipeline {

    /**
     *
     * @param node this is the input of the method.
     * @return boolean
     *
     * the node checks if contentBody and artifactUrl are not present
     * in node's metadata throws ClientException return true if
     * validation is successful else false
     */
	protected boolean isCompressRequired(Node node) {
		boolean required = false;
		if (null != node && null != node.getMetadata()) {
			TelemetryManager.log("Compression Required Check For Content Id: " + node.getIdentifier());
			String contentBody = (String) node.getMetadata().get(ContentWorkflowPipelineParams.body.name());
			String artifactUrl = (String) node.getMetadata().get(ContentWorkflowPipelineParams.artifactUrl.name());
			if (StringUtils.isBlank(artifactUrl) && StringUtils.isBlank(contentBody))
				throw new ClientException(ContentErrorCodeConstants.OPERATION_DENIED.name(),
						ContentErrorMessageConstants.UNABLE_TO_PUBLISH_OR_BUNDLE_CONTENT
								+ " | [Either Content 'body' or 'artifactUrl' is needed for the operation.]");

			/** Checking for the required conditions for applying 'Compression'.
			* Below if Condition is the basic difference between this method
			* and Content Validator's 'isAllRequiredFieldsAvailable' method's
			* 'application/vnd.ekstep.ecml-archive' Case block.
			*/
			if (StringUtils.isBlank(artifactUrl) && StringUtils.isNotBlank(contentBody))
				required = true;
		}
		return required;
	}

	/**
	 * gets the ECRFObject(Ekstep Common Representation Format) from
	 * ContentBody.
	 *
     * @param contentBody this is the input of the method.
     * @return Plugin
	 *            the contentBody gets the EcmlType from the ContentBody, if
	 *            type is JSON calls JSONContentParser else XMLContentParser
	 *            return ECRFObject
	 */
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

	/**
	 * gets the ECMLtype of the ContentBody.
	 * 
	 * @param contentBody this is the input of the method.
     * @return String
	 *            the contentBody checks if contentBody isValidJSON else
	 *            isValidXML else throws ClientException return EcmlType of the
	 *            given ContentBody
	 */
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
			TelemetryManager.log("ECML Type: " + type);
		}
		return type;
	}
	
	public Plugin getPlugin(String contentBody) {
		return getECRFObject(contentBody);
	}

}
