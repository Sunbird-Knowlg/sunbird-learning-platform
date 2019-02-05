package org.ekstep.common.dto;

import java.io.Serializable;
import java.util.Map;

import org.ekstep.graph.dac.enums.SystemProperties;

public class NodeDTO implements Serializable {

	private static final long serialVersionUID = -3083582629330476187L;
	private String identifier;
	private String name;
	private String objectType;
	private String relation;
	private String description;
	private Integer index;
	private String status;
	private Integer depth;
	private String mimeType;
	private String visibility;
	private Integer compatibilityLevel;
	private Double pkgVersion;
	private String channel;
	private String lastPublishedBy;
	private String versionKey;
	private String contentType;

	public NodeDTO() {

	}
	
	public NodeDTO(String identifier, String name, String mimeType, Double pkgVersion, String channel, String lastPublishedBy, String versionKey, String contentType) {
		super();
		this.identifier = identifier;
		this.name = name;
		this.mimeType = mimeType;
		this.pkgVersion = pkgVersion;
		this.channel = channel;
		this.lastPublishedBy = lastPublishedBy;
		this.contentType = contentType;
	}
	
	public NodeDTO(String identifier, String name, Integer depth,  String status, String mimeType, String visibility) {
		super();
		this.identifier = identifier;
		this.name = name;
		this.status = status;
		this.depth = depth;
		this.mimeType = mimeType;
		this.visibility = visibility;
	}



	public NodeDTO(String identifier, String name, String objectType) {
		this.identifier = identifier;
		this.name = name;
		this.objectType = objectType;
	}

	public NodeDTO(String identifier, String name, String objectType, String relation) {
		this(identifier, name, null, objectType, relation);
	}

	public NodeDTO(String identifier, String name, String description, String objectType, String relation) {
		this.identifier = identifier;
		this.name = name;
		this.description = description;
		this.objectType = objectType;
		this.relation = relation;
	}

	public NodeDTO(String identifier, String name, String objectType, String relation, Map<String, Object> metadata) {
		this(identifier, name, null, objectType, relation, metadata);
	}

	public NodeDTO(String identifier, String name, String description, String objectType, String relation, Map<String, Object> metadata) {
		this.identifier = identifier;
		this.name = name;
		this.description = description;
		this.objectType = objectType;
		this.relation = relation;
		if (null != metadata && !metadata.isEmpty()) {
			if (metadata.containsKey(SystemProperties.IL_SEQUENCE_INDEX.name())
					&& null != metadata.get(SystemProperties.IL_SEQUENCE_INDEX.name())) {
				try {
					this.index = Integer.parseInt(metadata.get(SystemProperties.IL_SEQUENCE_INDEX.name()).toString());
				} catch (Exception e) {
				}
			}
		}
	}
	public NodeDTO(String identifier, String name, String description, String visibility, String objectType, String relation, Map<String, Object> metadata) {
		this(identifier, name, description, objectType, relation, metadata);
		this.visibility = visibility;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getDepth() {
		return depth;
	}

	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public Integer getCompatibilityLevel() {
		return compatibilityLevel;
	}

	public void setCompatibilityLevel(int compatibilityLevel) {
		this.compatibilityLevel = compatibilityLevel;
	}

	public void setCompatibilityLevel(Integer compatibilityLevel) {
		this.compatibilityLevel = compatibilityLevel;
	}
	public Double getPkgVersion() {
		return pkgVersion;
	}

	public void setPkgVersion(Double pkgVersion) {
		this.pkgVersion = pkgVersion;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getLastPublishedBy() {
		return lastPublishedBy;
	}

	public void setLastPublishedBy(String lastPublishedBy) {
		this.lastPublishedBy = lastPublishedBy;
	}

	public String getVersionKey() {
		return versionKey;
	}

	public void setVersionKey(String versionKey) {
		this.versionKey = versionKey;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
}
