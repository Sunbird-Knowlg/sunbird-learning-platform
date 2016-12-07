package org.ekstep.contentstore.model;

import java.nio.ByteBuffer;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.contentstore.util.ContentStoreParams;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.ilimi.common.exception.ClientException;

@Table(keyspace="content_store", name="content_data")
public class ContentData {

	@Column(name="content_id")
	@PartitionKey
	private String contentId;
	
	@Column(name="last_updated_on")
	private Date lastUpdatedOn;
	
	@Column(name="body")
	private ByteBuffer body;
	
	public ContentData() {
	}
	
	public ContentData(String contentId, String body) {
		this(contentId, body, null);
	}
	
	public ContentData(String contentId, String body, Date lastUpdatedOn) {
		if (StringUtils.isBlank(contentId))
			throw new ClientException(ContentStoreParams.ERR_BLANK_CONTENT_ID.name(), "Content data cannot be created/updated without contentId");
		if (StringUtils.isBlank(body))
			throw new ClientException(ContentStoreParams.ERR_BLANK_CONTENT_BODY.name(), "Body must be provided for creating/updating content data");
		if (null == lastUpdatedOn)
			lastUpdatedOn = new Date();
		this.contentId = contentId;
		this.lastUpdatedOn = lastUpdatedOn;
		ByteBuffer buffer = ByteBuffer.wrap(body.getBytes());
		this.body = buffer;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public Date getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(Date lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public ByteBuffer getBody() {
		return body;
	}

	public void setBody(ByteBuffer body) {
		this.body = body;
	}
	
	public static Mapper<ContentData> getMapper(Session session) {
		MappingManager manager = new MappingManager(session);
		Mapper<ContentData> mapper = manager.mapper(ContentData.class);
		return mapper;
	}
	
}
