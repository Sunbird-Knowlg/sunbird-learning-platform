package com.ilimi.taxonomy.content.operation.finalizer;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;

public class ReviewFinalizer extends BaseFinalizer {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ReviewFinalizer.class.getName());

	/** The BasePath. */
	protected String basePath;

	/** The ContentId. */
	protected String contentId;

	/**
	 * Instantiates a new ReviewFinalizer and sets the base path and current
	 * content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file
	 *            handling and all manipulations.
	 * @param contentId
	 *            the content id is the identifier of content for which the
	 *            Processor is being processed currently.
	 */
	public ReviewFinalizer(String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}

	/**
	 * finalize()
	 *
	 * @param Map
	 *            the parameterMap
	 * 
	 *            checks if Node, ecrfType,ecmlType exists in the parameterMap
	 *            else throws ClientException Output only ECML format create
	 *            'artifactUrl' Get Content String write ECML File Create 'ZIP'
	 *            Package Upload Package Upload to S3 Set artifact file For Node
	 *            Download App Icon and create thumbnail Set Package Version
	 *            Create ECAR Bundle Delete local compressed artifactFile
	 *            Populate Fields and Update Node
	 * @return the response
	 */
	public Response finalize(Map<String, Object> parameterMap) {
		LOGGER.debug("Parameter Map: ", parameterMap);
		if (null == parameterMap)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_PARAMETER_MAP + " | [Parameter Map Cannot be 'null']");
		
		Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(),
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null Node.]");

		boolean isPublishOperation = (boolean) parameterMap
				.get(ContentWorkflowPipelineParams.isPublishOperation.name());
		if (BooleanUtils.isTrue(isPublishOperation)) {
			LOGGER.info("Changing the Content Status to 'Processing'.");
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
					ContentWorkflowPipelineParams.Processing.name());
		} else {
			LOGGER.info("Changing the Content Status to 'Review'.");
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(),
					ContentWorkflowPipelineParams.Review.name());
		}
		Node newNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
		newNode.setGraphId(node.getGraphId());
		newNode.setMetadata(node.getMetadata());

		LOGGER.debug("Updating the Node: ", node);
		return updateNode(newNode);
	}

}
