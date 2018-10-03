package org.ekstep.taxonomy.controller;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/media")
public class MediaController extends BaseController {
    
    
    
    private static final String MEDIA_FOLDER = "cloud_storage.media.folder";

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> upload(@RequestParam(value = "file", required = true) MultipartFile file) {
        String apiId = "media.upload";
        TelemetryManager.log("Upload | File: " + file);
        try {
            String name = FilenameUtils.getBaseName(file.getOriginalFilename()) + "_" + System.currentTimeMillis() + "."
                    + FilenameUtils.getExtension(file.getOriginalFilename());
            File uploadedFile = new File(name);
            file.transferTo(uploadedFile);
            String[] urlArray = new String[] {};
            try {
            	String folder = S3PropertyReader.getProperty(MEDIA_FOLDER);
            	urlArray = CloudStore.uploadFile(folder, uploadedFile, true);
            } catch (Exception e) {
                TelemetryManager.error("Error while uploading the file.", e);
                throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(),
                        "Error while uploading the File.", e);
            }
            String url = urlArray[1];
            Response response = new Response();
            response.put("url", url);
            ResponseParams params = new ResponseParams();
            params.setErr("0");
            params.setStatus(StatusType.successful.name());
            params.setErrmsg("Operation successful");
            response.setParams(params);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Upload | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}
