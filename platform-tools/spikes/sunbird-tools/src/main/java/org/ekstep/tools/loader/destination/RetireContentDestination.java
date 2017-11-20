/**
 * 
 */
package org.ekstep.tools.loader.destination;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.tools.loader.service.ContentService;
import org.ekstep.tools.loader.service.ExecutionContext;
import org.ekstep.tools.loader.service.ProgressCallback;
import org.ekstep.tools.loader.service.Record;
import org.ekstep.tools.loader.utils.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author pradyumna
 *
 */
public class RetireContentDestination implements Destination {
	private static Logger logger = LogManager.getLogger(RetireContentDestination.class);
	private ExecutionContext context = null;

	@Override
	public void process(List<Record> data, ProgressCallback callback) {
		ContentService service = (ContentService) ServiceProvider.getService("content");
		context = ServiceProvider.getContext();
		int rowNum = 1;
		int totalRows = data.size();
		JsonArray contentIds = new JsonArray();
		try {
			for (Record record : data) {
				JsonObject content = record.getJsonData();
				String contentId = JsonUtil.getFromObject(content, "content_id");
				if (StringUtils.isNotBlank(contentId)) {
					contentIds.add(contentId);
				}
			}
			if (contentIds.size() > 0) {
				String response = service.retire(contentIds, context);
				if (response.equalsIgnoreCase("OK")) {
					logger.info("Successfully retired all contentIds");
				} else {
					logger.info(response);
				}
			} else {
				logger.info("No ContentIds to retire");
			}
		} catch (Exception e) {
			logger.error("Error while retiring contentIds - ", e);
		}
		callback.progress(totalRows, rowNum++);

	}

}
