package org.ekstep.content.mgr.impl;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.content.mimetype.mgr.IMimeTypeManager;
import org.ekstep.content.util.MimeTypeManagerFactory;
import org.ekstep.graph.common.DateUtils;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.ekstep.telemetry.logger.TelemetryManager;

public class ReviewManager extends DummyBaseContentManager {

    public Response review(String contentId, Request request) throws Exception {
        if (StringUtils.isBlank(contentId))
            throw new ClientException(ContentErrorCodes.ERR_CONTENT_BLANK_ID.name(), "Content Id is blank");

        Response response = new Response();

        Node node = getNodeForOperation(contentId, "review");

        isNodeUnderProcessing(node, "Review");

        // Fetching body from Content Store.
        String body = getContentBody(node.getIdentifier());
        node.getMetadata().put(ContentAPIParams.body.name(), body);

        node.getMetadata().put(TaxonomyAPIParams.lastSubmittedOn.name(), DateUtils.formatCurrentDate());

        String mimeType = (String) node.getMetadata().get(ContentAPIParams.mimeType.name());
        if (StringUtils.isBlank(mimeType)) {
            mimeType = "assets";
        }

        TelemetryManager.log("Mime-Type" + mimeType + " | [Content ID: " + contentId + "]");
        String artifactUrl = (String) node.getMetadata().get(ContentAPIParams.artifactUrl.name());
        String license = (String) node.getMetadata().get("license");
        if (StringUtils.equals("video/x-youtube", mimeType) && null != artifactUrl && StringUtils.isBlank(license))
            checkYoutubeLicense(artifactUrl, node);
        TelemetryManager.log("Getting Mime-Type Manager Factory. | [Content ID: " + contentId + "]");

        String contentType = (String) node.getMetadata().get("contentType");
        IMimeTypeManager mimeTypeManager = MimeTypeManagerFactory.getManager(contentType, mimeType);

        response = mimeTypeManager.review(contentId, node, false);

        TelemetryManager.log("Returning 'Response' Object: ", response.getResult());
        return response;
    }

}
