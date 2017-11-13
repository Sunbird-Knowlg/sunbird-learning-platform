/**
 * 
 */
package org.ekstep.tools.loader.destination;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
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
public class ContentDestination implements Destination {

	private static Logger logger = LogManager.getLogger(ContentDestination.class);
	private ExecutionContext context = null;
	private FileWriter outputFile = null;
	private File file = null;

	/**
	 * 
	 */
	public ContentDestination() {
		file = new File("ContentOutput.csv");
		if (!file.exists()) {
			try {
				file.createNewFile();
				outputFile = new FileWriter(file, true);
				outputFile.write("ContentID , ContentName, Status \n");
				outputFile.close();
			} catch (IOException e) {
				logger.debug("Error while creating file");
			}

		}
	}

	/*
	 * (non-Javadoc)
	 */
	@Override
	public void process(List<Record> data, ProgressCallback callback) {
		ContentService service = (ContentService) ServiceProvider.getService("content");
		context = ServiceProvider.getContext();
		int rowNum = 1;
		int totalRows = data.size();
		String contentId = null, name = null;
		JsonArray contentIds = new JsonArray();

		writeOutput("\n------------------- Begin ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
		for (Record record : data) {
			String response = null;
			try {
				JsonObject content = record.getJsonData();
				logger.debug(content.toString());
				if (StringUtils.isNotBlank(JsonUtil.getFromObject(content, "content_id"))) {
					contentId = service.update(content, context);
					if (null != contentId) {
						response = "Success";
					} else {
						response = JsonUtil.getFromObject(content, "response");
					}
					writeOutput(contentId + " , " + "updatedContent" + " , " + response);
				} else {
					name = record.getJsonData().get("name").toString();
					contentId = service.create(record.getJsonData(), context);
					if (null != contentId) {
						response = "Success";
					} else {
						response = JsonUtil.getFromObject(content, "response");
					}
					writeOutput(contentId + " , " + name + " , " + response);
					contentIds.add(contentId);
				}
				rowNum++;

			} catch (Exception e) {
				e.printStackTrace();
				writeOutput(contentId + " , " + name + " , " + e.getMessage());
			}
			callback.progress(totalRows, rowNum++);
		}
		writeOutput("\n------------------- END ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
		/* Needs to be removed. For testing purpose only */
		// System.out.println("Calling retire method to delete the content");
		// service.retire(contentIds, context);

	}

	/**
	 * @param string
	 * @throws IOException
	 */
	private void writeOutput(String output) {
		try {
			outputFile = new FileWriter(file, true);
			outputFile.append(output + "\n");
			outputFile.close();
			logger.debug("Content Output  :: " + output);
		} catch (IOException e) {
			logger.debug("error while wrting to outputfile" + e.getMessage());
		}

	}

}
