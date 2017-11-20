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
public class ConceptDestination implements Destination {

	private static Logger logger = LogManager.getLogger(ContentDestination.class);
	private ExecutionContext context = null;
	private FileWriter outputFile;
	private File file = null;

	public ConceptDestination() {
		file = new File("ConceptOutput.csv");
		if (!file.exists()) {
			try {
				file.createNewFile();
				outputFile = new FileWriter(file, true);
				outputFile.write("ConceptID , ConceptName, Status \n");
				outputFile.close();
			} catch (IOException e) {
				logger.debug("Error while creating file");
			}

		}
	}
	/* (non-Javadoc)
	 * @see org.ekstep.tools.loader.service.Destination#process(java.util.List, org.ekstep.tools.loader.service.ProgressCallback)
	 */
	@Override
	public void process(List<Record> data, ProgressCallback callback) {
		ConceptService service = (ConceptService) ServiceProvider.getService("concept");
		context = ServiceProvider.getContext();
		int rowNum = 1;
		int totalRows = data.size();
		String name = null, conceptId = null, status = null;
		
		writeOutput("\n------------------- Begin ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");

		for (Record record : data) {
			try {
				JsonObject concept = record.getJsonData();

				if (StringUtils.isNotBlank(JsonUtil.getFromObject(concept, "identifier"))) {
					conceptId = service.update(concept, context);
					if (null != conceptId) {
						status = "Success";
					} else {
						status = JsonUtil.getFromObject(concept, "response");
					}
					writeOutput(conceptId + " , " + "updatedConcept" + " , " + status);
				} else {
					name = record.getCsvData().get("name");
					conceptId = service.create(concept, context);
					if (StringUtils.isNotEmpty(conceptId)) {
						status = "Success";
					} else {
						status = JsonUtil.getFromObject(concept, "response");
					}
					writeOutput(conceptId + " , " + name + " , " + status);
					System.out.println("Concept Name : " + name + "\t" + "ConceptId : " + conceptId);
				}

			} catch (Exception e) {
				writeOutput(conceptId + " , " + name + " , " + e.getMessage());
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
			logger.debug("Concept Output  :: " + output);
		} catch (IOException e) {
			logger.debug("error while wrting to outputfile" + e.getMessage());
		}

	}
}
