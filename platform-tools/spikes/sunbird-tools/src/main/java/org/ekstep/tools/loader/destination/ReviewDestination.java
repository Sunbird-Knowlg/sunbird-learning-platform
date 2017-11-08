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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.tools.loader.service.ContentService;
import org.ekstep.tools.loader.service.ExecutionContext;
import org.ekstep.tools.loader.service.ProgressCallback;
import org.ekstep.tools.loader.service.Record;
import org.ekstep.tools.loader.utils.JsonUtil;

import com.google.gson.JsonObject;

/**
 * @author pradyumna
 *
 */
public class ReviewDestination implements Destination {

	private Logger logger = LogManager.getLogger(ReviewDestination.class);

	private ExecutionContext context = null;
	private FileWriter outputFile;
	private File file = null;
	private static final String OUTPUT_FILE = "ReviewContentOutput.csv";

	/**
	 * 
	 */
	public ReviewDestination() {
		file = new File(OUTPUT_FILE);
		if (!file.exists()) {
			try {
				file.createNewFile();
				outputFile = new FileWriter(file, true);
				outputFile.write("ContentId , Status \n");
				outputFile.close();
			} catch (IOException e) {
				logger.debug("Error while creating file");
			}

		}
	}

	/* (non-Javadoc)
	 * @see org.ekstep.tools.loader.destination.Destination#process(java.util.List, org.ekstep.tools.loader.service.ProgressCallback)
	 */
	@Override
	public void process(List<Record> data, ProgressCallback callback) {
		ContentService service = (ContentService) ServiceProvider.getService("content");
		context = ServiceProvider.getContext();
		String contentId = null, status = null;
		int rowNum = 1;
		int totalRows = data.size();

		ServiceProvider.writeOutput(file,
				"\n------------------- Begin ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
		for (Record record : data) {

			try {
				JsonObject content = record.getJsonData();
				contentId = JsonUtil.getFromObject(content, "content_id");
				status = service.submitForReview(content, context);
				if (status.equalsIgnoreCase("OK")) {
					status = "Success";
				}
				ServiceProvider.writeOutput(file, contentId + "," + status);
			} catch (Exception e) {
				ServiceProvider.writeOutput(file, contentId + "," + e.getMessage());
			}
			callback.progress(totalRows, rowNum++);
		}
		ServiceProvider.writeOutput(file,
				"\n------------------- END ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
	}

}
