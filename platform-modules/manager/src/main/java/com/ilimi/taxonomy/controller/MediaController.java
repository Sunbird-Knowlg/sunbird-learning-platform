package com.ilimi.taxonomy.controller;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.S3PropertyReader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.taxonomy.enums.ContentErrorCodes;

@Controller
@RequestMapping("/media")
public class MediaController extends BaseController {
    
    private static LogHelper LOGGER = LogHelper.getInstance(MediaController.class.getName());
    
    private static final String s3Media = "s3.media.folder";

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> upload(@RequestParam(value = "file", required = true) MultipartFile file) {
        String apiId = "media.upload";
        LOGGER.info("Upload | File: " + file);
        try {
            String name = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_" + System.currentTimeMillis() + "."
                    + FilenameUtils.getExtension(file.getOriginalFilename());
            File uploadedFile = new File(name);
            file.transferTo(uploadedFile);
            /*String bucketName = "ekstep-public";
            String folder = "content";*/
            String[] urlArray = new String[] {};
            try {
            	String folder = S3PropertyReader.getProperty(s3Media);
                urlArray = AWSUploader.uploadFile(folder, uploadedFile);
            } catch (Exception e) {
                throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(),
                        "Error wihile uploading the File.", e);
            }
            String url = urlArray[1];
            Response response = new Response();
            response.put("url", url);
            ResponseParams params = new ResponseParams();
            params.setErr("0");
            params.setStatus(StatusType.successful.name());
            params.setErrmsg("Operation successful");
            response.setParams(params);
            LOGGER.info("Upload | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("Upload | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}
