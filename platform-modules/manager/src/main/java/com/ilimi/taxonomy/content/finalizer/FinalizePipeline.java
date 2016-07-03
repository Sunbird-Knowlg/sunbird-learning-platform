package com.ilimi.taxonomy.content.finalizer;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.pipeline.BasePipeline;
import com.ilimi.taxonomy.content.util.ECRFToJSONConvertor;
import com.ilimi.taxonomy.content.util.ECRFToXMLConvertor;

public class FinalizePipeline extends BasePipeline{
	
	private static Logger LOGGER = LogManager.getLogger(FinalizePipeline.class.getName());
	
	private static final int IDX_S3_KEY = 0;
	private static final int IDX_S3_URL = 1;
	
	protected String basePath;
	protected String contentId;
	
	public FinalizePipeline (String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}
	
	public Response finalyze(String operation, Map<String, Object> parameterMap) {
		Response response = new Response();
		if (StringUtils.isBlank(operation))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
					ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid Operation.]");
		if (null != parameterMap && !StringUtils.isBlank(operation)) {
			switch (operation) {
			case "upload":
			case "UPLOAD":
				File file = (File) parameterMap.get(ContentWorkflowPipelineParams.file.name());
				Plugin ecrf = (Plugin) parameterMap.get(ContentWorkflowPipelineParams.ecrf.name());
				String ecmlType = (String) parameterMap.get(ContentWorkflowPipelineParams.ecmlType.name());
				Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
				if (null == file || !file.exists()) 
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [File does not Exist.]");
				if (null == ecrf)
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null ECRF Object.]");
				if (StringUtils.isBlank(ecmlType))
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid ECML Type.]");
				if (null == node) 
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_FINALIZE_PARAM + " | [Invalid or null Node.]");
				
				// Get Content String
				String ecml = getECMLString(ecrf, ecmlType);
				
				// Upload Package
				String[] urlArray = uploadToAWS(file, getUploadFolderName());
				
				// Update Body, Reset Editor State and Update Content Node
				node.getMetadata().put(ContentWorkflowPipelineParams.s3Key.name(), urlArray[IDX_S3_KEY]);
		        node.getMetadata().put(ContentWorkflowPipelineParams.artifactUrl.name(), urlArray[IDX_S3_URL]);
		        node.getMetadata().put(ContentWorkflowPipelineParams.body.name(), ecml);
		        node.getMetadata().put(ContentWorkflowPipelineParams.editorState.name(), null);
				
				// Update Node
		        response = updateContentNode(node, urlArray[IDX_S3_URL]);
				break;

			default:
				break;
			}
		}
		return response;
	}
	
	private String getECMLString(Plugin ecrf, String ecmlType) {
		String ecml = "";
		if (null != ecrf) {
			LOGGER.info("Converting ECML From ECRF Object.");
			if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.xml.name())) {
				ECRFToXMLConvertor convertor = new ECRFToXMLConvertor();
				ecml = convertor.getContentXmlString(ecrf);
			} else if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.json.name())) {
				ECRFToJSONConvertor convertor = new ECRFToJSONConvertor();
				ecml = convertor.getContentJsonString(ecrf);
			}
		}
		return ecml;
	}
	
	

}
