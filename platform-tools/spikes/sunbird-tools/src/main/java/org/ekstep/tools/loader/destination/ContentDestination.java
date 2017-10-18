/**
 * 
 */
package org.ekstep.tools.loader.destination;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.tools.loader.service.ContentServiceImpl;
import org.ekstep.tools.loader.service.ExecutionContext;
import org.ekstep.tools.loader.service.ProgressCallback;
import org.ekstep.tools.loader.service.Record;
import org.ekstep.tools.loader.shell.ShellContext;

import com.google.gson.JsonArray;
import com.typesafe.config.Config;

/**
 * @author pradyumna
 *
 */
public class ContentDestination implements Destination {

	private static Logger logger = LogManager.getLogger(ContentDestination.class);
	private Config config = null;
	private String user = null;
	private ExecutionContext context = null;

	/*
	 * (non-Javadoc)
	 */
	@Override
	public void process(List<Record> data, ProgressCallback callback) throws Exception {
		ShellContext shellContext = ShellContext.getInstance();
		config = shellContext.getCurrentConfig().resolve();
		user = shellContext.getCurrentUser();
		ContentServiceImpl service = new ContentServiceImpl();
		context = new ExecutionContext(config, user);
		int rowNum = 0;
		int totalRows = data.size();
		JsonArray contentIds = new JsonArray();
		
		service.init(context);

		for (Record record : data) {
			String contentId = record.getCsvData().get("contentId");
			if (StringUtils.isNotEmpty(contentId)) {
				service.update(record.getJsonData(), context);
			}
			String name = record.getCsvData().get("name");
			contentId = service.create(record.getJsonData(), context);
			System.out.println("Content Name : " + name + "\t" + "ContentId : " + contentId);
			rowNum++;
			contentIds.add(contentId);

			callback.progress(totalRows, rowNum);
		}

		/* Needs to be removed. For testing purpose only */
		// System.out.println("Calling retire method to delete the content");
		// service.retire(contentIds, context);

	}

}
