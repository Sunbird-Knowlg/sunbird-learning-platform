package org.sunbird.content.mgr.impl.operation.plugin;

import org.sunbird.common.Slug;
import org.sunbird.common.dto.Response;
import org.sunbird.common.util.S3PropertyReader;
import org.sunbird.learning.common.enums.ContentAPIParams;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.taxonomy.mgr.impl.BaseContentManager;
import scala.Option;

public class PreSignedUrlOperation extends BaseContentManager {

    public Response preSignedUrl(String contentId, String fileName, String type) {
        Response contentResp = getDataNode(TAXONOMY_ID, contentId);
        if (checkError(contentResp))
            return contentResp;
        Response response = OK();
        String objectKey = "content/" + type +"/"+contentId+"/"+ Slug.makeSlug(fileName, true);
        String expiry = S3PropertyReader.getProperty("cloud_storage.upload.url.ttl");
        String preSignedURL = CloudStore.getCloudStoreService().getSignedURL(CloudStore.getContainerName(), objectKey, Option.apply(Integer.parseInt(expiry)), Option.apply("w"));
        response.put(ContentAPIParams.content_id.name(), contentId);
        response.put(ContentAPIParams.pre_signed_url.name(), preSignedURL);
        response.put(ContentAPIParams.url_expiry.name(), expiry);
        return response;
    }

}
