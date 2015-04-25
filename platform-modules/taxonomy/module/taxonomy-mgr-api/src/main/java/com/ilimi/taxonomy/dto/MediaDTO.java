package com.ilimi.taxonomy.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.graph.common.dto.BaseValueObject;
import com.ilimi.graph.dac.model.Node;

public class MediaDTO extends BaseValueObject {

    private static final long serialVersionUID = 6270427667834578523L;
    private String identifier;
    private String mediaUrl;
    private String mediaType;
    private String mimeType;
    private String posterImage;
    private String title;
    private String description;
    private Map<String, Object> metadata;

    public MediaDTO() {

    }

    public MediaDTO(Node node) {
        if (null != node) {
            this.identifier = node.getIdentifier();
            Map<String, Object> metadata = node.getMetadata();
            if (null != metadata && !metadata.isEmpty()) {
                Map<String, Object> otherMetadata = new HashMap<String, Object>();
                for (Entry<String, Object> entry : metadata.entrySet()) {
                    if (StringUtils.equalsIgnoreCase("mediaUrl", entry.getKey())) {
                        this.mediaUrl = (String) entry.getValue();
                    } else if (StringUtils.equalsIgnoreCase("mediaType", entry.getKey())) {
                        this.mediaType = (String) entry.getValue();
                    } else if (StringUtils.equalsIgnoreCase("mimeType", entry.getKey())) {
                        this.mimeType = (String) entry.getValue();
                    } else if (StringUtils.equalsIgnoreCase("posterImage", entry.getKey())) {
                        this.posterImage = (String) entry.getValue();
                    } else if (StringUtils.equalsIgnoreCase("title", entry.getKey())) {
                        this.title = (String) entry.getValue();
                    } else if (StringUtils.equalsIgnoreCase("description", entry.getKey())) {
                        this.description = (String) entry.getValue();
                    } else {
                        otherMetadata.put(entry.getKey(), entry.getValue());
                    }
                }
                this.metadata = otherMetadata;
            }
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPosterImage() {
        return posterImage;
    }

    public void setPosterImage(String posterImage) {
        this.posterImage = posterImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
