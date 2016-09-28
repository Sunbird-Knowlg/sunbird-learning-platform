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

/**
 * The Class BaseInitializer is a BaseClass for all Initializers, extends BasePipeline which
 * mainly holds Common Methods and operations of a ContentNode.
 * BaseInitializer holds methods to get ECML and ECRFtype from the ContentBody
 */
public class BaseInitializer extends BasePipeline {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(BaseInitializer.class.getName());

	/** isCompressRequired()
	 * 
	 *  @param Node the node
	 *  checks if contentBody and artifactUrl are not present in node's metadata
	 *  throws ClientException
	 *  return true if validation is successful else false
	 *  */
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

	/** gets the ECRFObject(Ekstep Common Representation Format) from  ContentBody.
	 * 
	 *  @param ContentBody the contentBody
	 *  gets the EcmlType from the ContentBody, if type is JSON calls JSONContentParser
	 *  else XMLContentParser
	 *  return ECRFObject 
	 *  */
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
	
	/** gets the ECMLtype of the ContentBody.
	 * 
	 *  @param contentBody the contentBody
	 *  checks if contentBody isValidJSON else isValidXML else throws ClientException
	 *  return EcmlType of the given ContentBody
	 *  */
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
