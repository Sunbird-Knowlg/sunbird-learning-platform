package com.ilimi.taxonomy.content.initializer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.UnzipUtility;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.content.client.PipelineRequestorClient;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.entity.Plugin;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.finalizer.FinalizePipeline;
import com.ilimi.taxonomy.content.pipeline.BasePipeline;
import com.ilimi.taxonomy.content.processor.AbstractProcessor;
import com.ilimi.taxonomy.content.util.JSONContentParser;
import com.ilimi.taxonomy.content.util.XMLContentParser;
import com.ilimi.taxonomy.content.validator.ContentValidator;

public class InitializePipeline extends BasePipeline {
	
	private static Logger LOGGER = LogManager.getLogger(InitializePipeline.class.getName());
	
	private static final String JSON_ECML_FILE_NAME = "index.json";
	private static final String XML_ECML_FILE_NAME = "index.ecml";
	
	protected String basePath;
	protected String contentId;
	
	public InitializePipeline (String basePath, String contentId) {
		if (!isValidBasePath(basePath))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Path does not Exist.]");
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
					ContentErrorMessageConstants.INVALID_CWP_CONST_PARAM + " | [Invalid Content Id.]");
		this.basePath = basePath;
		this.contentId = contentId;
	}
	
	public Response init(String operation, Map<String, Object> parameterMap) {
		Response response = new Response();
		if (StringUtils.isBlank(operation))
			throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
					ContentErrorMessageConstants.INVALID_CWP_INIT_PARAM + " | [Invalid Operation.]");
		if (null != parameterMap && StringUtils.isNotBlank(operation)) {
			switch (operation) {
			case "upload":
			case "UPLOAD":
				File file = (File) parameterMap.get(ContentWorkflowPipelineParams.file.name());
				Node node = (Node) parameterMap.get(ContentWorkflowPipelineParams.node.name());
				if (null == file || !file.exists()) 
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_INIT_PARAM + " | [File does not Exist.]");
				if (null == node) 
					throw new ClientException(ContentErrorCodeConstants.INVALID_PARAMETER.name(), 
							ContentErrorMessageConstants.INVALID_CWP_INIT_PARAM + " | [Invalid or null Node.]");
				ContentValidator validator = new ContentValidator();
				if (validator.isValidContentPackage(file)) {
					//Extract the ZIP File
					extractContentPackage(file);
					
					// Get ECRF Object
					Plugin ecrf = getECRFObject();
					
					// Get Pipeline Object
					AbstractProcessor pipeline = PipelineRequestorClient
							.getPipeline(ContentWorkflowPipelineParams.extract.name(), basePath, contentId);
					
					// Start Pipeline Operation
					ecrf = pipeline.execute(ecrf);
					
					// Call Finalyzer
					FinalizePipeline finalize = new FinalizePipeline(operation, contentId);
					Map<String, Object> finalizeParamMap = new HashMap<String, Object>();
					finalizeParamMap.put(ContentWorkflowPipelineParams.ecrf.name(), ecrf);
					finalizeParamMap.put(ContentWorkflowPipelineParams.file.name(), file);
					finalizeParamMap.put(ContentWorkflowPipelineParams.ecmlType.name(), getECMLType());
					finalizeParamMap.put(ContentWorkflowPipelineParams.node.name(), node);
					response = finalize.finalyze(operation, finalizeParamMap);
				}
				break;
			case "publish":
			case "PUBLISH":
				
				break;

			default:
				break;
			}
			
		}
		return response;
	}
	
	private Plugin getECRFObject() {
		Plugin plugin = new Plugin();
		String ecml = getFileString();
		String ecmlType = getECMLType(); 
		if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.xml.name())) {
			XMLContentParser parser = new XMLContentParser();
			plugin = parser.parseContent(ecml);
		} else if (StringUtils.equalsIgnoreCase(ecmlType, ContentWorkflowPipelineParams.json.name())) {
			JSONContentParser parser = new JSONContentParser();
			plugin = parser.parseContent(ecml);
		}
		return plugin;
	}
	
	private String getECMLType() {
		String type = "";
		if (new File(basePath + File.separator + JSON_ECML_FILE_NAME).exists())
			type = ContentWorkflowPipelineParams.json.name();
		else if (new File(basePath + File.separator + XML_ECML_FILE_NAME).exists())
			type = ContentWorkflowPipelineParams.xml.name();
		return type;
	}
	
	private void extractContentPackage(File file) {
		try {
			UnzipUtility util = new UnzipUtility();
			util.unzip(file.getAbsolutePath(), basePath);
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.ZIP_EXTRACTION.name(), 
					ContentErrorMessageConstants.ZIP_EXTRACTION_ERROR + " | [ZIP Extraction Failed.]");
		}
	}
	
	public String getFileString() {
		String fileString = "";
		File jsonECMLFile = new File(basePath + File.separator + JSON_ECML_FILE_NAME);
		File xmlECMLFilePath = new File(basePath + File.separator + XML_ECML_FILE_NAME);
		if (jsonECMLFile.exists() && xmlECMLFilePath.exists())
			throw new ClientException(ContentErrorCodeConstants.MULTIPLE_ECML.name(), 
					ContentErrorMessageConstants.MULTIPLE_ECML_FILES_FOUND + " | [index.json and index.ecml]");
		
		try {
			LOGGER.info("Reading ECML File.");
			if (jsonECMLFile.exists())
				fileString = FileUtils.readFileToString(jsonECMLFile);
			else if (xmlECMLFilePath.exists())
				fileString = FileUtils.readFileToString(xmlECMLFilePath);
			
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.ECML_FILE_READ.name(), 
					ContentErrorMessageConstants.ECML_FILE_READ_ERROR, e);
		}
		return fileString;
	}

}
