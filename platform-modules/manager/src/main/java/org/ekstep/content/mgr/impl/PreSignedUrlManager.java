package org.ekstep.content.mgr.impl;

import org.ekstep.common.Slug;
import org.ekstep.common.dto.Response;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.util.cloud.CloudStore;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.springframework.stereotype.Component;
import scala.Option;

@Component
public class PreSignedUrlManager extends BaseContentManager {

    public Response preSignedUrl(String contentId, String fileName, String type) {
        Response contentResp = getDataNode(TAXONOMY_ID, contentId);
        if (checkError(contentResp))
            return contentResp;
        Response response = new Response();
        String objectKey = "content/" + type +"/"+contentId+"/"+ Slug.makeSlug(fileName);
        String expiry = S3PropertyReader.getProperty("cloud_storage.upload.url.ttl");
        String preSignedURL = CloudStore.getCloudStoreService().getSignedURL(CloudStore.getContainerName(), objectKey, Option.apply(Integer.parseInt(expiry)), Option.apply("w"));
        response.put(ContentAPIParams.content_id.name(), contentId);
        response.put(ContentAPIParams.pre_signed_url.name(), preSignedURL);
        response.put(ContentAPIParams.url_expiry.name(), expiry);
        return response;
    }

}
