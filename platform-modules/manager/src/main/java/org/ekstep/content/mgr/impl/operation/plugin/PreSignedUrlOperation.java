package org.ekstep.content.mgr.impl.operation.plugin;

import org.ekstep.common.Slug;
import org.ekstep.common.dto.Response;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import scala.Option;

public class PreSignedUrlOperation extends BaseContentManager {

    public Response preSignedUrl(String contentId, String fileName, String type, Boolean idValReq) {
        Response contentResp = getDataNode(TAXONOMY_ID, contentId);
        if (checkError(contentResp) && idValReq)
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
