package com.ilimi.taxonomy.content.pipeline;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.AWSUploader;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.content.common.ContentConfigurationConstants;
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.util.PropertiesUtil;

public class BasePipeline extends BaseManager {
	
	private static Logger LOGGER = LogManager.getLogger(BasePipeline.class.getName());
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	private static final String DEF_AWS_BUCKET_NAME = "ekstep-public";
    private static final String DEF_AWS_FOLDER_NAME = "content";
    
	protected Response updateContentNode(Node node, String url) {
		Response response = new Response();
		if (null != node) {
			response = updateNode(node);
	        if (StringUtils.isNotBlank(url))
	        	response.put(ContentWorkflowPipelineParams.content_url.name(), url);
		}
        return response;
    }

    protected Response updateNode(Node node) {
    	Response response = new Response();
    	if (null != node) {
	        Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
	        updateReq.put(GraphDACParams.node.name(), node);
	        updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
	        response = getResponse(updateReq, LOGGER);
    	}
        return response;
    }
    
    protected boolean isValidBasePath(String path) {
		boolean isValid = true;
		try {
			LOGGER.info("Validating the Base Path: " + path);
			isValid = isPathExist(Paths.get(path));
		} catch (InvalidPathException |  NullPointerException e) {
            isValid = false;
        }
		return isValid;
	}
	
	protected boolean isPathExist(Path path) {
		boolean exist = true;
		try {
			if (null != path) {
				LOGGER.info("Creating the Base Path: " + path.getFileName());
				if (!Files.exists(path))
					Files.createDirectories(path);
			}
		} catch (FileAlreadyExistsException e) {
			LOGGER.info("Base Path Already Exist: " + path.getFileName());
		} catch (Exception e) {
			exist = false;
			LOGGER.error("Error! Something went wrong while creating the path - " + path.getFileName(), e);
		}
		return exist;
	}
	
	protected String getUploadFolderName() {
		String folderName = DEF_AWS_FOLDER_NAME;
		String env = PropertiesUtil.getProperty(ContentWorkflowPipelineParams.OPERATION_MODE.name());
		if (!StringUtils.isBlank(env)) {
			LOGGER.info("Fetching the Upload Folder (AWS) Name for Environment: " + env);
			//TODO: Write the logic for fetching the environment(DEV, PROD, QA, TEST) aware folder name. 
		}
		return folderName;
	}
	
	protected String getUploadBucketName() {
		String folderName = DEF_AWS_BUCKET_NAME;
		String env = PropertiesUtil.getProperty(ContentWorkflowPipelineParams.OPERATION_MODE.name());
		if (!StringUtils.isBlank(env)) {
			LOGGER.info("Fetching the Upload Bucket (AWS) Name for Environment: " + env);
			//TODO: Write the logic for fetching the environment(DEV, PROD, QA, TEST) aware bucket name. 
		}
		return folderName;
	}
	
	protected String[] uploadToAWS(File uploadedFile, String folder) {
        String[] urlArray = new String[] {};
        try {
            if (StringUtils.isBlank(folder))
                folder = DEF_AWS_FOLDER_NAME;
            urlArray = AWSUploader.uploadFile(DEF_AWS_BUCKET_NAME, folder, uploadedFile);
        } catch (Exception e) {
            throw new ServerException(ContentErrorCodeConstants.UPLOAD_ERROR.name(), 
            		ContentErrorMessageConstants.FILE_UPLOAD_ERROR, e);
        }
        return urlArray;
    }
	
	protected Number getNumericValue(Object obj) {
    	try {
    		return (Number) obj;
    	} catch(Exception e) {
    		return 0;
    	}
    }
	    
	protected Double getDoubleValue(Object obj) {
    	Number n = getNumericValue(obj);
    	if (null == n)
    		return 0.0;
    	return n.doubleValue();
    }
	
	protected Double getS3FileSize(String key) {
        Double bytes = null;
        if (StringUtils.isNotBlank(key)) {
            try {
                return AWSUploader.getObjectSize(ContentConfigurationConstants.BUCKET_NAME, key);
            } catch (IOException e) {
                LOGGER.error("Error! While getting the file size from AWS", e);
            }
        }
        return bytes;
    }
	
	protected static String formatCurrentDate() {
        return format(new Date());
    }
    
	protected static String format(Date date) {
        if (null != date) {
            try {
                return sdf.format(date);
            } catch (Exception e) {
            	LOGGER.error("Error! While Converting the Date Format.", e);
            }
        }
        return null;
    }

}
