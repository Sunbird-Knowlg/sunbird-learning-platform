package com.ilimi.taxonomy.content.pipeline;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import com.ilimi.taxonomy.content.common.ContentErrorMessageConstants;
import com.ilimi.taxonomy.content.enums.ContentErrorCodeConstants;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.content.util.PropertiesUtil;

public class BasePipeline extends BaseManager {
	
	private static Logger LOGGER = LogManager.getLogger(BasePipeline.class.getName());
	
	private static final String DEF_AWS_BUCKET_NAME = "ekstep-public";
    private static final String DEF_AWS_FOLDER_NAME = "content";
    
	protected Response updateContentNode(Node node, String url) {
        Response updateRes = updateNode(node);
        if (StringUtils.isNotBlank(url))
            updateRes.put(ContentWorkflowPipelineParams.content_url.name(), url);
        return updateRes;
    }

    protected Response updateNode(Node node) {
        Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
        updateReq.put(GraphDACParams.node.name(), node);
        updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
        Response updateRes = getResponse(updateReq, LOGGER);
        return updateRes;
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
	
	public String[] uploadToAWS(File uploadedFile, String folder) {
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

}
