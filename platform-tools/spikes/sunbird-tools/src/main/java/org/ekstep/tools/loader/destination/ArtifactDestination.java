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
import org.ekstep.tools.loader.service.ContentServiceImpl;
import org.ekstep.tools.loader.service.ExecutionContext;
import org.ekstep.tools.loader.service.ProgressCallback;
import org.ekstep.tools.loader.service.Record;
import org.ekstep.tools.loader.shell.ShellContext;
import org.ekstep.tools.loader.utils.JsonUtil;

import com.google.gson.JsonObject;
import com.typesafe.config.Config;

/**
 * @author pradyumna
 *
 */
public class ArtifactDestination implements Destination {

	private static Logger logger = LogManager.getLogger(ArtifactDestination.class);
	private Config config = null;
	private String user = null;
	private ExecutionContext context = null;
	private ShellContext shellContext = null;
	private FileWriter outputFile = null;
	private File file = null;

	/**
	 * 
	 */
	public ArtifactDestination() {
		file = new File("ArtifactOutput.csv");
		if (!file.exists()) {
			try {
				file.createNewFile();
				outputFile = new FileWriter(file, true);
				outputFile.write("ContentID , Status \n");
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
		shellContext = ShellContext.getInstance();
		config = shellContext.getCurrentConfig().resolve();
		user = shellContext.getCurrentUser();
		context = new ExecutionContext(config, user);
		ContentService service = new ContentServiceImpl(context);
		int rowNum = 1;
		int totalRows = data.size();
		String contentId = null, artifactUrl = null, status = null;


		writeOutput("\n------------------- Begin ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");

		for (Record record : data) {
			try {
				JsonObject content = record.getJsonData();
				contentId = JsonUtil.getFromObject(content, "content_id");

				artifactUrl = service.uploadArtifact(content, context);
				if (null != artifactUrl) {
					status = "Success";

				} else {
					status = JsonUtil.getFromObject(content, "response");
				}
				writeOutput(contentId + "," + status);

			} catch (Exception e) {
				logger.error("Could not upload URL : ", e);
				writeOutput(contentId + "," + e.getMessage());
			}
			callback.progress(totalRows, rowNum++);
		}
		writeOutput("\n------------------- End ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");

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
			logger.debug("Content Upload Output  :: " + output);
		} catch (IOException e) {
			logger.debug("error while wrting to outputfile" + e.getMessage());
		}

	}

}
