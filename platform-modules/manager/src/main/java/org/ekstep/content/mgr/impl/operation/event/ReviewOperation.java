package org.ekstep.content.mgr.impl.operation.event;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.util.MimeTypeManagerFactory;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.taxonomy.mgr.impl.BaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

public class ReviewOperation extends BaseContentManager {

    public Response review(String contentId, Request request) throws Exception {
        validateEmptyOrNullContentId(contentId);

        Response response;

        Node node = getNodeForOperation(contentId, "review");

        isNodeUnderProcessing(node, "Review");

        // Fetching body from Content Store.
        String body = getContentBody(node.getIdentifier());
        node.getMetadata().put(ContentAPIParams.body.name(), body);

        node.getMetadata().put(TaxonomyAPIParams.lastSubmittedOn.name(), DateUtils.formatCurrentDate());

        String mimeType = getMimeTypeFrom(node);
        if (StringUtils.isBlank(mimeType)) {
            mimeType = "assets";
        }

        TelemetryManager.log("Mime-Type" + mimeType + " | [Content ID: " + contentId + "]");
        String artifactUrl = getArtifactUrlFrom(node);
        String license = (String) node.getMetadata().get("license");
        if (isYoutubeMimeType(mimeType) && null != artifactUrl && StringUtils.isBlank(license))
            checkYoutubeLicense(artifactUrl, node);
        TelemetryManager.log("Getting Mime-Type Manager Factory. | [Content ID: " + contentId + "]");

        String contentType = getContentTypeFrom(node);
        IMimeTypeManager mimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType);

        response = mimeTypeManager.review(contentId, node, false);

        TelemetryManager.log("Returning 'Response' Object: ", response.getResult());
        return response;
    }

}