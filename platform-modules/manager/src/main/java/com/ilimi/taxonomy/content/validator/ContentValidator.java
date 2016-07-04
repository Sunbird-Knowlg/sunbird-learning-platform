package com.ilimi.taxonomy.content.validator;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.util.PropertiesUtil;

public class ContentValidator {
	
	private static Logger LOGGER = LogManager.getLogger(ContentValidator.class.getName());
	
	private static final String DEF_CONTENT_PACKAGE_MIME_TYPE = "application/zip";
	
	public boolean isValidContentPackage(File file) {
		boolean isValidContentPackage = false;
		try {
			if (file.exists()) {
				LOGGER.info("Validating File : " + file.getName());
				if (!isValidContentMimeType(file))
					throw new ClientException(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_FILE_MIME_TYPE_ERROR, "The uploaded package is invalid.");
				if (!isValidContentPackageStructure(file))
					throw new ClientException(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_STRUCTURE_ERROR, "The uploaded package has invalid folder structure.");
				if (!isValidContentSize(file))
					throw new ClientException(ContentErrorMessageConstants.INVALID_CONTENT_PACKAGE_SIZE_ERROR, "The uploaded package has exceeds the file package size limit.");
				
				isValidContentPackage = true;
			}
		} catch(ClientException ce) {
			throw ce;
		} catch(IOException e) {
			e.printStackTrace();
			throw new ServerException(ContentErrorMessageConstants.CONTENT_PACKAGE_FILE_OPERATION_ERROR, "Something went wrong while processing the Package file.");
		} catch(Exception e) {
			e.printStackTrace();
			throw new ClientException(ContentErrorMessageConstants.CONTENT_PACKAGE_VALIDATOR_ERROR, "Something went wrong while validating the Package file.");
		}
		return isValidContentPackage;
	}
	
	private boolean isValidContentMimeType(File file) throws IOException {
		boolean isValidMimeType = false;
		if (file.exists()) {
			LOGGER.info("Validating File For MimeType: " + file.getName());
			Tika tika = new Tika();
			String mimeType = tika.detect(file);
			isValidMimeType = StringUtils.equalsIgnoreCase(DEF_CONTENT_PACKAGE_MIME_TYPE, mimeType);
		}
		return isValidMimeType;
	}
	
	private boolean isValidContentSize(File file) {
		boolean isValidSize = false;
		if (file.exists()) {
			LOGGER.info("Validating File For Size: " + file.getName());
			if (file.length() <= getContentPackageFileSizeLimit())
				isValidSize = true;
		}
		return isValidSize;
	}
	
	private double getContentPackageFileSizeLimit() {
		double size = 52428800;			// In Bytes, Default is 50MB
		String limit = PropertiesUtil.getProperty(ContentWorkflowPipelineParams.MAX_CONTENT_PACKAGE_FILE_SIZE_LIMIT.name());
		if (!StringUtils.isBlank(limit)) {
			try {
				size = Double.parseDouble(limit);
			} catch (Exception e) {
			}
		}
		return size;
	}
	
	@SuppressWarnings("resource")
	private boolean isValidContentPackageStructure(File file) throws IOException {
		final String JSON_ECML_FILE_NAME = "index.json";
		final String XML_ECML_FILE_NAME = "index.ecml";
		boolean isValidPackage = false;
		if (file.exists()) {
			LOGGER.info("Validating File For Folder Structure: " + file.getName());
			ZipFile zipFile = new ZipFile(file);
		    Enumeration<? extends ZipEntry> entries = zipFile.entries();
		    while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        if (StringUtils.equalsIgnoreCase(entry.getName(), JSON_ECML_FILE_NAME) || 
		        		StringUtils.equalsIgnoreCase(entry.getName(), XML_ECML_FILE_NAME)) {
		        	isValidPackage = true;
		        	break;
		        }
		    }
		}
		return isValidPackage;
	}

}
