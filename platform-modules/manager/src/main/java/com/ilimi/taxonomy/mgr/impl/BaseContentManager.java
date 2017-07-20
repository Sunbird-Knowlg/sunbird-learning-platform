package com.ilimi.taxonomy.mgr.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.learning.common.enums.ContentErrorCodes;

import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;

public abstract class BaseContentManager extends BaseManager {

	/**
	 * The Default 'ContentImage' Object Suffix (Content_Object_Identifier +
	 * ".img")
	 */
	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
	
	private static final String DEFAULT_MIME_TYPE = "assets";
	
	protected String getActualIdentifier(String identifier) {
		if (StringUtils.endsWith(identifier, ".img")) {
			return identifier.replace(".img", "");
		}
		return identifier;
	}
	
	protected void isImageContentId(String identifier) {
		if (StringUtils.endsWithIgnoreCase(identifier, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX))
			throw new ClientException(ContentErrorCodes.OPERATION_DENIED.name(),
					"Invalid Content Identifier. | [Content Identifier does not Exists.]");
	}
	
	protected void isNodeUnderProcessing(Node node, String operation) {
		boolean isProccssing = checkNodeStatus(node, TaxonomyAPIParams.Processing.name());
		if (BooleanUtils.isTrue(isProccssing)) {
			PlatformLogger.log("Given Content is in Processing Status.");
			throw new ClientException(TaxonomyErrorCodes.ERR_NODE_ACCESS_DENIED.name(),
					"Operation Denied! | [Cannot Apply '"+ operation +"' Operation on the Content in 'Processing' Status.] ");
		} else {
			PlatformLogger.log("Given Content is not in Processing Status.");
		}
	}
	
	protected String getMimeType(Node node) {
		String mimeType = (String) node.getMetadata().get("mimeType");
		if (StringUtils.isBlank(mimeType)) {
			mimeType = DEFAULT_MIME_TYPE;
		}
		return mimeType;
	}
	
	// TODO: if exception occurs it return false. It is invalid. Check.
	private boolean checkNodeStatus(Node node, String status) {
		boolean inGivenStatus = false;
		try {
			if (null != node && null != node.getMetadata()
					&& StringUtils.equalsIgnoreCase((String) node.getMetadata().get(TaxonomyAPIParams.status.name()),
							status))
				inGivenStatus = true;
		} catch (Exception e) {
			PlatformLogger.log("Something Went Wrong While Checking the is Under Processing Status.", null, e);
		}
		return inGivenStatus;
	}
}
