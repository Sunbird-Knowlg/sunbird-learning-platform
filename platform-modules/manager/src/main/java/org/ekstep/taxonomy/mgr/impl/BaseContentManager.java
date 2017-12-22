package org.ekstep.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentErrorCodes;

import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;

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
		List<String> status = new ArrayList<>();
		status.add(TaxonomyAPIParams.Processing.name());
		status.add(TaxonomyAPIParams.Pending.name());
		boolean isProccssing = checkNodeStatus(node, status);
		if (BooleanUtils.isTrue(isProccssing)) {
			PlatformLogger.log("Given Content is in Processing Status.");
			throw new ClientException(TaxonomyErrorCodes.ERR_NODE_ACCESS_DENIED.name(),
					"Operation Denied! | [Cannot Apply '"+ operation +"' Operation on the Content in '" + 
							(String)node.getMetadata().get(TaxonomyAPIParams.status.name()) + "' Status.] ");
		} else {
			PlatformLogger.log("Given Content is not in " + (String)node.getMetadata().get(TaxonomyAPIParams.status.name()) + " Status.");
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
	private boolean checkNodeStatus(Node node, List<String> status) {
		boolean inGivenStatus = false;
		try {
			if (null != node && null != node.getMetadata()) {
				for(String st : status) {
					if(StringUtils.equalsIgnoreCase((String) node.getMetadata().get(TaxonomyAPIParams.status.name()),
							st)) {
						inGivenStatus = true;
					}
				}
			}
		} catch (Exception e) {
			PlatformLogger.log("Something Went Wrong While Checking the is Under Processing Status.", null, e);
		}
		return inGivenStatus;
	}
}
