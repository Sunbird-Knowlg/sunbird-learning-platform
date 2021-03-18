package org.sunbird.content.operation.initializer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.common.util.UnzipUtility;
import org.sunbird.content.client.PipelineRequestorClient;
import org.sunbird.content.common.ContentErrorMessageConstants;
import org.sunbird.content.entity.Plugin;
import org.sunbird.content.enums.ContentErrorCodeConstants;
import org.sunbird.content.enums.ContentWorkflowPipelineParams;
import org.sunbird.content.pipeline.finalizer.FinalizePipeline;
import org.sunbird.content.processor.AbstractProcessor;
import org.sunbird.content.util.JSONContentParser;
import org.sunbird.content.util.XMLContentParser;
import org.sunbird.content.validator.ContentValidator;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.telemetry.logger.TelemetryManager;


/**
 * The Class UploadInitializer, extends BaseInitializer which
 * mainly holds methods to get ECML and ECRFtype from the ContentBody.
 * UploadInitializer holds methods which perform ContentUploadPipeline operations
 */
public class UploadInitializer extends BaseInitializer {
	
	/** The logger. */
	
	
	/** The Constant JSON_ECML_FILE_NAME. */
	private static final String JSON_ECML_FILE_NAME = "index.json";
	
	/** The Constant XML_ECML_FILE_NAME. */
	private static final String XML_ECML_FILE_NAME = "index.ecml";
	
	/** The BasePath. */
	protected String basePath;
	
	/** The ContentId. */
	protected String contentId;

	/**
	 * Instantiates a new UploadInitializer and sets the base
	 * path and current content id for further processing.
	 *
	 * @param basePath
	 *            the base path is the location for content package file handling and all manipulations. 
	 * @param contentId
	 *            the content id is the identifier of content for which the Processor is being processed currently.
	 */
	public UploadInitializer(String basePath, String contentId) {
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
	 * initialize()
	 *
	 * @param Map the parameterMap
	 * 
	 * checks if UploadFile and Node exists in the parameterMap else throws ClientException
	 * validates the ContentPackage
	 * extracts the ZIP file
	 * gets ECRF object
	 * Gets Pipeline Object
	 * Starts Pipeline Operation
	 * Calls Finalizer
	 * 
	 * @return the response
	 */
	public Response initialize(Map<String, Object> parameterMap) {
		Response response = new Response();
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
			//  Extract the ZIP File 
			extractContentPackage(file);

			//  Get ECRF Object 
			Plugin ecrf = getECRFObject();

			// Get Pipeline Object 
			AbstractProcessor pipeline = PipelineRequestorClient
					.getPipeline(ContentWorkflowPipelineParams.extract.name(), basePath, contentId);

			//  Start Pipeline Operation 
			ecrf = pipeline.execute(ecrf);

			//  Call Finalyzer 
			FinalizePipeline finalize = new FinalizePipeline(basePath, contentId);
			Map<String, Object> finalizeParamMap = new HashMap<String, Object>();
			finalizeParamMap.put(ContentWorkflowPipelineParams.ecrf.name(), ecrf);
			finalizeParamMap.put(ContentWorkflowPipelineParams.file.name(), file);
			finalizeParamMap.put(ContentWorkflowPipelineParams.ecmlType.name(), getECMLType());
			finalizeParamMap.put(ContentWorkflowPipelineParams.node.name(), node);
			response = finalize.finalyze(ContentWorkflowPipelineParams.upload.name(), finalizeParamMap);
		}
		return response;
	}
	
	/** gets the ECRFObject(Ekstep Common Representation Format).
	 * 
	 *  gets the EcmlType, if type is JSON calls JSONContentParser
	 *  else XMLContentParser
	 *  return ECRFObject 
	 *  */
	private Plugin getECRFObject() {
		Plugin plugin = new Plugin();
		String ecml = getFileString();
		String ecmlType = getECMLType();
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
	 * extractContentPackage() 
	 * extracts the ContentPackageZip file
	 * 
	 * @param file the ContentPackageFile
	 */
	private void extractContentPackage(File file) {
		try {
			UnzipUtility util = new UnzipUtility();
			util.unzip(file.getAbsolutePath(), basePath);
		} catch (IOException e) {
			throw new ServerException(ContentErrorCodeConstants.ZIP_EXTRACTION.name(),
					ContentErrorMessageConstants.ZIP_EXTRACTION_ERROR + " | [ZIP Extraction Failed.]");
		}
	}

	/**
	 * getFileString() 
	 * extracts the ContentPackageZip file
	 * 
	 * checks if the package contains index.js or index.emcl
	 * if it contains both throws ClientException
	 * else read the index.js or index.ecml and
	 * @returns the fileString
	 */
	private String getFileString() {
		String fileString = "";
		File jsonECMLFile = new File(basePath + File.separator + JSON_ECML_FILE_NAME);
		File xmlECMLFilePath = new File(basePath + File.separator + XML_ECML_FILE_NAME);
		if (jsonECMLFile.exists() && xmlECMLFilePath.exists())
			throw new ClientException(ContentErrorCodeConstants.MULTIPLE_ECML.name(),
					ContentErrorMessageConstants.MULTIPLE_ECML_FILES_FOUND + " | [index.json and index.ecml]");

		try {
			TelemetryManager.log("Reading ECML File.");
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
	
	/** gets the ECMLtype.
	 * 
	 *  checks if JsonFile or EcmlFile exists and
	 *  return EcmlType
	 *  */
	private String getECMLType() {
		String type = "";
		if (new File(basePath + File.separator + JSON_ECML_FILE_NAME).exists())
			type = ContentWorkflowPipelineParams.json.name();
		else if (new File(basePath + File.separator + XML_ECML_FILE_NAME).exists())
			type = ContentWorkflowPipelineParams.ecml.name();
		TelemetryManager.log("ECML Type: " + type);
		return type;
	}
}
