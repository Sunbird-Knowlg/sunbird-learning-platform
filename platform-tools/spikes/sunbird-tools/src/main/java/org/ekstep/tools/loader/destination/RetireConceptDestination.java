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
import org.ekstep.tools.loader.service.ConceptService;
import org.ekstep.tools.loader.service.ExecutionContext;
import org.ekstep.tools.loader.service.ProgressCallback;
import org.ekstep.tools.loader.service.Record;
import org.ekstep.tools.loader.utils.JsonUtil;

import com.google.gson.JsonObject;

/**
 * @author pradyumna
 *
 */
public class RetireConceptDestination implements Destination {
	private static Logger logger = LogManager.getLogger(RetireConceptDestination.class);
	private ExecutionContext context = null;
	private FileWriter outputFile;
	private File file = null;
	private static final String OUTPUT_FILE = "RetireConceptOutput.csv";

	/**
	 * 
	 */
	public RetireConceptDestination() {
		file = new File(OUTPUT_FILE);
		if (!file.exists()) {
			try {
				file.createNewFile();
				outputFile = new FileWriter(file, true);
				outputFile.write("ConceptId , Status \n");
				outputFile.close();
			} catch (IOException e) {
				logger.debug("Error while creating file");
			}

		}
	}

	@Override
	public void process(List<Record> data, ProgressCallback callback) {
		ConceptService service = (ConceptService) ServiceProvider.getService("concept");
		context = ServiceProvider.getContext();

		int rowNum = 1;
		int totalRows = data.size();

		ServiceProvider.writeOutput(file,
				"\n------------------- Begin ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");

		for (Record record : data) {
			String conceptId = null;
			try {
				JsonObject content = record.getJsonData();
				conceptId = JsonUtil.getFromObject(content, "conceptId");
				String framework = JsonUtil.getFromObject(content, "framework");
				if (StringUtils.isNotBlank(conceptId)) {
					String response = service.retire(conceptId, context, framework);
					if (response.equalsIgnoreCase("OK")) {
						ServiceProvider.writeOutput(file, conceptId + "," + "Success");
					} else {
						ServiceProvider.writeOutput(file, conceptId + "," + response);
					}
				} else {
					logger.info("No ConceptIds to retire");
				}
			} catch (Exception e) {
				ServiceProvider.writeOutput(file, conceptId + "," + e.getMessage());
				logger.error("Error while retiring conceptIds - ", e);
			}
			callback.progress(totalRows, rowNum++);
		}
		ServiceProvider.writeOutput(file,
				"\n------------------- End ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
	}

}
