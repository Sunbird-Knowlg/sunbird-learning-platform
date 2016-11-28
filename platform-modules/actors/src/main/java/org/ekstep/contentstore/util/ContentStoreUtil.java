package org.ekstep.contentstore.util;

import org.ekstep.contentstore.model.ContentData;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.ilimi.common.exception.ResourceNotFoundException;

public class ContentStoreUtil {

	public static void updateContentBody(String contentId, String body) {
		Session session = CassandraConnector.getSession();
		ContentData data = new ContentData(contentId, body);
		Mapper<ContentData> mapper = ContentData.getMapper(session);
		mapper.save(data);
	}

	public static String getContentBody(String contentId) {
		Session session = CassandraConnector.getSession();
		Mapper<ContentData> mapper = ContentData.getMapper(session);
		ContentData data = mapper.get(contentId);
		if (null == data || null == data.getBody())
			throw new ResourceNotFoundException(ContentStoreParams.ERR_CONTENT_DATA_NOT_FOUND.name(),
					"Content body not found for " + contentId);
		return new String(data.getBody().array());
	}
}
