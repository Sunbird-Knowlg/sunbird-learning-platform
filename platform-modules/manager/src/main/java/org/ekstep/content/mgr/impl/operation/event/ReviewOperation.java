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
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.stereotype.Component;

@Component
public class ReviewOperation extends DummyBaseContentManager {

    public Response review(String contentId, Request request) throws Exception {
        validateEmptyOrNullContentId(contentId);

        Response response;

        Node node = getNodeForOperation(contentId, "review");

        isNodeUnderProcessing(node, "Review");

        // Fetching body from Content Store.
        String body = getContentBody(node.getIdentifier());
        node.getMetadata().put(ContentAPIParams.body.name(), body);

        node.getMetadata().put(TaxonomyAPIParams.lastSubmittedOn.name(), DateUtils.formatCurrentDate());

        String mimeType = getMimeTypeFrom(node);/*(String) node.getMetadata().get(ContentAPIParams.mimeType.name());*/
        if (StringUtils.isBlank(mimeType)) {
            mimeType = "assets";
        }

        TelemetryManager.log("Mime-Type" + mimeType + " | [Content ID: " + contentId + "]");
        String artifactUrl = getArtifactUrlFrom(node);/*(String) node.getMetadata().get(ContentAPIParams.artifactUrl.name())*/;
        String license = (String) node.getMetadata().get("license");
        if (isYoutubeMimeType(mimeType)/*StringUtils.equals("video/x-youtube", mimeType)*/ && null != artifactUrl && StringUtils.isBlank(license))
            checkYoutubeLicense(artifactUrl, node);
        TelemetryManager.log("Getting Mime-Type Manager Factory. | [Content ID: " + contentId + "]");

        String contentType = getContentTypeFrom(node);
        IMimeTypeManager mimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType);

        response = mimeTypeManager.review(contentId, node, false);

        TelemetryManager.log("Returning 'Response' Object: ", response.getResult());
        return response;
    }

}