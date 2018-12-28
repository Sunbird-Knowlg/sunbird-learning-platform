package org.ekstep.content.mgr.impl;

import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.upload.UploadFileManager;
import org.ekstep.content.mgr.impl.upload.UploadUrlManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class UploadManager {

    @Autowired private UploadFileManager uploadFileManager;

    @Autowired private UploadUrlManager  uploadUrlManager;

    public Response upload(String contentId, File uploadedFile, String mimeType) {
        return this.uploadFileManager.upload(contentId, uploadedFile, mimeType);
    }

    public Response upload(String contentId, String fileUrl, String mimeType) {
        return this.uploadUrlManager.upload(contentId, fileUrl, mimeType);
    }

}