package org.ekstep.content.operation.finalizer;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.YouTubeUrlUtil;
import org.ekstep.content.util.AssetEnrichmentEnums;
import org.ekstep.content.util.OptimizerUtil;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetEnrichmentFinalizer extends BaseFinalizer {
    private static final String tempFileLocation = Platform.config.hasPath("asset.tmp.file.location") ? Platform.config.getString("asset.tmp.file.location") : "data/tmp/upload";


    public void enrichAssets(Node node, File file) {
        if(StringUtils.contains(node.getIdentifier(),".img")) {
            Response response = getDataNode("domain", node.getIdentifier().replace(".img", ""));
            if (checkError(response))
                throw new ResourceNotFoundException("ERR_FRAMEWORK_NOT_FOUND",
                        "Framework Not Found With Id : " + node.getIdentifier().replace(".img", ""));
            node = (Node) response.get(GraphDACParams.node.name());
        }
        if ((null != node) && (node.getObjectType().equalsIgnoreCase(AssetEnrichmentEnums.content.name()))) {
            getMediaEnrichmentMap(node, file).get(((String) node.getMetadata().get("mediaType")).toLowerCase()).run();
        } else
            throw new ClientException(AssetEnrichmentEnums.ERR_NODE_CANT_BE_NULL.name(), "The node is null for identifier" + node.getIdentifier());
    }

    public Map<String, Runnable> getMediaEnrichmentMap(Node node, File file) {
        return new HashMap<String, Runnable>() {{
            put("image", () -> imageEnrichment(node));
            put("video", () -> videoEnrichment(node));
        }};
    }

    private void imageEnrichment(Node node) {
        String identifier = node.getIdentifier();
        try {
            TelemetryManager.info("Processing image enrichment for node:" + identifier);
            Map<String, String> variantsMap = OptimizerUtil.optimizeImage(identifier, tempFileLocation, node);
            if (null == variantsMap)
                variantsMap = new HashMap<String, String>();
            if (StringUtils.isBlank(variantsMap.get(AssetEnrichmentEnums.medium.name()))) {
                String downloadUrl = (String) node.getMetadata().get(AssetEnrichmentEnums.downloadUrl.name());
                if (StringUtils.isNotBlank(downloadUrl))
                    variantsMap.put(AssetEnrichmentEnums.medium.name(), downloadUrl);
            }
            processImage(variantsMap, node);
        } catch (Exception e) {
            TelemetryManager.error("Something Went Wrong While Performing Asset Enrichment operation. | [Content Id: "
                            + identifier + "]", e);
            node.getMetadata().put(AssetEnrichmentEnums.processingError.name(), e.getMessage());
            node.getMetadata().put(AssetEnrichmentEnums.status.name(), AssetEnrichmentEnums.Failed.name());
            Response res = updateNode(node);
            if (checkError(res))
                throw new ServerException(AssetEnrichmentEnums.PROCESSING_ERROR.name(), "Error! While Updating the Metadata | [Content Id: " +
                        identifier + "] :: " + res.getParams().getErr() + " :: " + res.getParams().getErrmsg());
            e.printStackTrace();
            throw new ServerException(AssetEnrichmentEnums.PROCESSING_ERROR.name(), "Error! While Updating the Metadata | [Content Id: " +
                    identifier + "] :: " + res.getParams().getErr() + " :: " + res.getParams().getErrmsg());
        }
    }

    private void processImage(Map<String, String> variantsMap, Node node) {
        String identifier = node.getIdentifier();
        TelemetryManager.info("Image " + identifier + " channel:" + node.getMetadata().get("channel"));
        TelemetryManager.info("Image " + identifier + " appId:" + node.getMetadata().get("appId"));
        TelemetryManager.info("Image " + identifier + " consumerId:" + node.getMetadata().get("consumerId"));
        node.getMetadata().put(AssetEnrichmentEnums.status.name(), AssetEnrichmentEnums.Live.name());
        node.getMetadata().put(AssetEnrichmentEnums.variants.name(), variantsMap);
        Response res = updateNode(node);
        if (checkError(res)) {
            throw new ServerException(AssetEnrichmentEnums.PROCESSING_ERROR.name(), "Error! While Updating the Metadata | [Content Id: " +
                    identifier + "] :: " + res.getParams().getErr() + " :: " + res.getParams().getErrmsg());
        }
    }


    private void videoEnrichment(Node node) {
        String identifier = node.getIdentifier();
        String videoUrl = (String) node.getMetadata().get(AssetEnrichmentEnums.artifactUrl.name());
        try {
            if (StringUtils.isBlank(videoUrl)) {
                TelemetryManager.info("Content artifactUrl is blank.");
                throw new ClientException(AssetEnrichmentEnums.PROCESSING_ERROR.name(), "Content artifactUrl is blank.");
            }
            processVideo(node, videoUrl);
            pushStreamingUrlRequest(node, videoUrl);
        } catch (Exception e) {
            TelemetryManager.error("Something Went Wrong While Performing Asset Enrichment operation. | [Content Id: " +
                    identifier + "]", e);
            node.getMetadata().put(AssetEnrichmentEnums.processingError.name(), e.getMessage());
            node.getMetadata().put(AssetEnrichmentEnums.status.name(), AssetEnrichmentEnums.Failed.name());
            Response res = updateNode(node);
            if (checkError(res))
                throw new ServerException(AssetEnrichmentEnums.PROCESSING_ERROR.name(), "Error! While Updating the Metadata | [Content Id: " +
                        identifier + "] :: " + res.getParams().getErr() + " :: " + res.getParams().getErrmsg());
            e.printStackTrace();
        }
    }

    private void processVideo(Node node, String videoUrl) throws Exception {
        String identifier = node.getIdentifier();
        if (StringUtils.equalsIgnoreCase("video/x-youtube", (String) node.getMetadata().get(AssetEnrichmentEnums.mimeType.name()))) {
            Map<String, Object> data = YouTubeUrlUtil.getVideoInfo(videoUrl, "snippet,contentDetails", "thumbnail", "duration");
            if (MapUtils.isNotEmpty(data)) {
                if (data.containsKey("thumbnail"))
                    node.getMetadata().put("thumbnail", (String) data.get("thumbnail"));
                if (data.containsKey("duration"))
                    node.getMetadata().put("duration", data.get("duration"));
            }
        } else {
            File videoFile = org.apache.commons.io.FileUtils.toFile(new URL(videoUrl));
            OptimizerUtil.videoEnrichment(node, tempFileLocation, videoFile);
        }
        node.getMetadata().put(AssetEnrichmentEnums.status.name(), AssetEnrichmentEnums.Live.name());
        Response res = updateNode(node);
        if (checkError(res)) {
            TelemetryManager.info("Error response during asset update to Live status : " + res.getParams().getErr() + " :: " + res.getParams().getErrmsg() + " :: " + res.getResult());
            throw new ServerException(AssetEnrichmentEnums.PROCESSING_ERROR.name(),
                    "Error! While Updating the Metadata | [Content Id: " + identifier + "]");
        }
    }

    public void pushStreamingUrlRequest(Node node, String videoUrl) {
        List<String> streamableMimeType = Platform.config.hasPath("stream.mime.type") ?
                Arrays.asList(Platform.config.getString("stream.mime.type").split(",")) : Arrays.asList("video/mp4");
        if (streamableMimeType.contains((String) node.getMetadata().get(AssetEnrichmentEnums.mimeType.name()))) {
            node.getMetadata().put(AssetEnrichmentEnums.streamingUrl.name(), videoUrl);
            updateNode(node);
        }
    }

}
