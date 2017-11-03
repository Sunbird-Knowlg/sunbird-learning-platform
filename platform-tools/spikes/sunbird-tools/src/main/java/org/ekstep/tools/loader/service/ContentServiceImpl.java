/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.tools.loader.utils.Constants;
import org.ekstep.tools.loader.utils.FileUtil;
import org.ekstep.tools.loader.utils.JsonUtil;
import org.ekstep.tools.loader.utils.RestUtil;
import org.ekstep.tools.loader.utils.ValidationUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;

/**
 *
 * @author feroz
 */
public class ContentServiceImpl implements ContentService {
    
    static Logger logger = LogManager.getLogger(ContentServiceImpl.class);
    
	/**
	 * 
	 */
	public ContentServiceImpl(ExecutionContext context) {
		init(context);
	}

	private void init(ExecutionContext context) {
		RestUtil.init(context, Constants.EKSTEP_API_TOKEN);
    }
    
    @Override
    public String create(JsonObject content, ExecutionContext context) throws Exception {
        
        String contentId = null;
        
        // Step 0 - (TODO) Validate the object to make sure it is clean to upload
		if (ValidationUtil.validateCreateContent(content)) {
			// Step 1 - If the content has a thumbnail, upload it
			uploadAppIcon(content, context);

			// Step 2 - Create the content object
			String createUrl = context.getString(Constants.API_CONTENT_CREATE);
			String body = JsonUtil.wrap(content, "content").toString();
			BaseRequest request = Unirest.post(createUrl).body(body);

			HttpResponse<JsonNode> createResponse = RestUtil.execute(request);

			if (RestUtil.isSuccessful(createResponse)) {

				contentId = RestUtil.getFromResponse(createResponse, "result.content_id");
				String versionKey = RestUtil.getFromResponse(createResponse, "result.versionKey");
				content.addProperty("content_id", contentId);
				content.addProperty("versionKey", versionKey);
				content.addProperty("response", "OK");

				logger.debug("Created Content " + contentId);

				// Step 3 - Upload the artifact (if local, will be uploaded, else sent as is)
				uploadArtifact(content, context);
			} else {

				String error = RestUtil.getFromResponse(createResponse, "params.errmsg");
				content.addProperty("response", error);
				logger.debug("Create Content Failed : " + error);
			}
		} else {
			content.addProperty("response", "Missing mandatory fields for Content creation");
		}
        
        return contentId;
    }

    @Override
    public String update(JsonObject content, ExecutionContext context) throws Exception {
        
        String content_id = JsonUtil.getFromObject(content,"content_id");
        if (StringUtils.isEmpty(content_id)) throw new IllegalArgumentException("Content ID is mandatory for update");

        // Step 1 - Get versionKey, otherwise update will fail
		String getUrl = context.getString(Constants.API_CONTENT_GET);
        BaseRequest getRequest = Unirest.get(getUrl).routeParam("content_id", content_id);
        HttpResponse<JsonNode> getResponse = RestUtil.execute(getRequest);

		String versionKey = RestUtil.getFromResponse(getResponse, "result.content.versionKey");
        content.addProperty("versionKey", versionKey);
        logger.debug("Got content version : " + versionKey);
        
        // Step 2 - Update content using the versionKey
		String updateUrl = context.getString(Constants.API_CONTENT_UPDATE);
		content.remove("content_id");
        String body = JsonUtil.wrap(content, "content").toString();
        BaseRequest updateRequest = Unirest.patch(updateUrl).routeParam("content_id", content_id).body(body);
        HttpResponse<JsonNode> updateResponse = RestUtil.execute(updateRequest);

		versionKey = RestUtil.getFromResponse(updateResponse, "result.content.versionKey");
		content.addProperty("content_id", content_id);
        content.addProperty("versionKey", versionKey);
        logger.debug("Updated content successfully. New version key " + versionKey);
        
        return content_id;
    }

    @Override
    public String retire(JsonArray contentIds, ExecutionContext context) throws Exception {
		String retireUrl = context.getString(Constants.API_CONTENT_RETIRE);
        
        String body = JsonUtil.wrap(contentIds, "contentIds").toString();
        BaseRequest retireRequest = Unirest.delete(retireUrl).body(body);
        HttpResponse<JsonNode> retireResponse = RestUtil.execute(retireRequest);
        
        String response = "OK";
        if (RestUtil.isSuccessful(retireResponse)) {
            logger.debug("Retired " + contentIds);
            response = "OK";
        }
        else {
            response = RestUtil.getFromResponse(retireResponse, "params.errmsg");
            logger.debug("Failed to retire content : " + response);
        }
        
        return response;
    }

    @Override
    public String uploadArtifact(JsonObject content, ExecutionContext context) throws Exception {
        String artifactUrl = JsonUtil.getFromObject(content,"artifactUrl");
        String contentId = JsonUtil.getFromObject(content,"content_id");
        
        // If the URL is local, then upload the file
        if (StringUtils.isNotBlank(artifactUrl)) {
            
            // Upload only if it is a local file and exists
			
			if (FileUtil.existsLocally(artifactUrl)) {
				// Step1 - get the signed url to upload the file to
				String uploadSignUrl = context.getString(Constants.API_CONTENT_UPLOAD_URL);
				JsonObject payload = new JsonObject();
				payload.addProperty("fileName", FileUtil.getFileName(artifactUrl));
			
				String body = JsonUtil.wrap(payload, "content").toString();
				BaseRequest signRequest = Unirest.post(uploadSignUrl).routeParam("content_id", contentId).body(body);
				HttpResponse<JsonNode> signResponse = RestUtil.execute(signRequest);
				String signedUrl = RestUtil.getFromResponse(signResponse, "result.pre_signed_url");
				logger.debug("Got signed URL to upload local artifact - " + signedUrl);
			
				// Step 2 - actually upload the file to the signed url artifactUrl =
				artifactUrl = FileUtil.upload(artifactUrl, signedUrl);
				logger.debug("Uploaded artifact to signed url. Final URL = " + artifactUrl);
			}
			Unirest.clearDefaultHeaders();
			Unirest.setDefaultHeader("Content-Type", "multipart/form-data");
			Unirest.setDefaultHeader("user-id", context.getCurrentUser());
			Unirest.setDefaultHeader("Authorization", "Bearer " + context.getString(Constants.EKSTEP_API_TOKEN));
            // Step 3 - When the file is available remote
			String uploadUrl = context.getString(Constants.API_CONTENT_UPLOAD);
			BaseRequest artifactRequest = Unirest.post(uploadUrl).routeParam("content_id", contentId)
					.header("content-type", "multipart/form-data; boundary=---bound")
					.body("-----bound\r\nContent-Disposition: form-data; name=\"fileUrl\"\r\n\r\n" + artifactUrl
							+ "\r\n-----bound--\r\n");

			/*artifactRequest.getHttpRequest().getBody().*/

			HttpResponse<JsonNode> artifactResponse = RestUtil.execute(artifactRequest);
			if (RestUtil.isSuccessful(artifactResponse)) {
				artifactUrl = RestUtil.getFromResponse(artifactResponse, "result.content_url");
			    content.addProperty("artifactUrl", artifactUrl);
			}else {
				artifactUrl = null;
				content.addProperty("response", RestUtil.getFromResponse(artifactResponse, "params.errmsg"));
			}
            logger.debug("Updated the content with artifact URL");
            
        }
        else {
            logger.debug("No artifact to upload");
        }

        return artifactUrl;
    }

    @Override
    public String uploadAppIcon(JsonObject content, ExecutionContext context) throws Exception {
        String appIcon = JsonUtil.getFromObject(content,"appIcon");
        
        // AppIcon property is specified in the content
        if (StringUtils.isNotBlank(appIcon)) {
            
            // Upload only if it is a local file and exists
            if (FileUtil.existsLocally(appIcon)) {
                
				String mediaUploadApi = context.getString(Constants.API_MEDIA_UPLOAD);
                appIcon = FileUtil.upload(appIcon, mediaUploadApi);
                content.addProperty("appIcon", appIcon);
                logger.debug("Uploaded appIcon to = " + appIcon);
                return appIcon;
            }
        }
        
        return null;
    }

    @Override
    public String submitForReview(JsonObject content, ExecutionContext context) throws Exception {
        String content_id = JsonUtil.getFromObject(content,"content_id");
        if (StringUtils.isEmpty(content_id)) throw new IllegalArgumentException("Content ID is mandatory for submitting for review.");
        
		String reviewUrl = context.getString(Constants.API_CONTENT_REVIEW);
        BaseRequest request = Unirest.post(reviewUrl).routeParam("content_id", content_id).body("{}");
        HttpResponse<JsonNode> response = RestUtil.execute(request);
        
        String code = "OK";
        if (RestUtil.isSuccessful(response)) {
            logger.debug("Submitted successfully for review.");
            code = "OK";
        }
        else {
            code = RestUtil.getFromResponse(response, "params.errmsg");
            logger.debug("Failed to submit for review : " + code);
        }
        
        return code;
    }

    @Override
    public String publish(JsonObject content, ExecutionContext context) throws Exception {
        String content_id = JsonUtil.getFromObject(content,"content_id");
        if (StringUtils.isEmpty(content_id)) throw new IllegalArgumentException("Content ID is mandatory for pubishing.");
        
        JsonObject publishedBy = new JsonObject();
        publishedBy.addProperty("lastPublishedBy", context.getCurrentUser());

		String publishUrl = context.getString(Constants.API_CONTENT_PUBLISH);
        String body = JsonUtil.wrap(publishedBy, "content").toString();
		BaseRequest request = Unirest.post(publishUrl).routeParam("content_id", content_id).body(body);
        HttpResponse<JsonNode> response = RestUtil.execute(request);
        
        String code = "OK";
        if (RestUtil.isSuccessful(response)) {
            logger.debug("Submitted successfully for publish.");
            code = "OK";
        }
        else {
            code = RestUtil.getFromResponse(response, "params.errmsg");
            logger.debug("Failed to submit for publish : " + code);
        }
        
        return code;
    }
}
