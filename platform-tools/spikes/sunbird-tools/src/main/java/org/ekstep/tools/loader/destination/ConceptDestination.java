/**
 * 
 */
package org.ekstep.tools.loader.destination;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.tools.loader.service.ConceptServiceImpl;
import org.ekstep.tools.loader.service.ExecutionContext;
import org.ekstep.tools.loader.service.ProgressCallback;
import org.ekstep.tools.loader.service.Record;
import org.ekstep.tools.loader.shell.ShellContext;
import org.ekstep.tools.loader.utils.JsonUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;

/**
 * @author pradyumna
 *
 */
public class ConceptDestination implements Destination {

	private static Logger logger = LogManager.getLogger(ContentDestination.class);
	private Config config = null;
	private String user = null;
	private ExecutionContext context = null;

	/* (non-Javadoc)
	 * @see org.ekstep.tools.loader.service.Destination#process(java.util.List, org.ekstep.tools.loader.service.ProgressCallback)
	 */
	@Override
	public void process(List<Record> data, ProgressCallback callback) {
		ShellContext shellContext = ShellContext.getInstance();
		config = shellContext.getCurrentConfig().resolve();
		user = shellContext.getCurrentUser();
		ConceptServiceImpl service = new ConceptServiceImpl();
		context = new ExecutionContext(config, user);
		int rowNum = 1;
		int totalRows = data.size();
		JsonArray conceptIds = new JsonArray();
		String name =null, conceptId = null, status = null;
		
		service.init(context);

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
					conceptIds.add(conceptId);
				}

			} catch (Exception e) {
				writeOutput(conceptId + " , " + name + " , " + e.getMessage());
			}
			callback.progress(totalRows, rowNum++);
		}

		/* Needs to be removed. For testing purpose only */

		// System.out.println("Calling retire method to delete the concepts");
		//
		// service.retire(conceptIds, context);
	}

	/**
	 * @param string
	 * @throws IOException
	 */
	private void writeOutput(String output) {
		FileWriter outputFile;
		File file = new File("ConceptOutput.csv");
		try {
			if (!file.exists()) {
				file.createNewFile();
				outputFile = new FileWriter(file, true);
				outputFile.write("ConceptID , ConceptName, Status \n");
			} else {
				outputFile = new FileWriter(file, true);
			}

			outputFile.append(output + "\n");
			outputFile.close();
			logger.debug("Concept Output  :: " + output);
		} catch (IOException e) {
			logger.debug("error while wrting to outputfile" + e.getMessage());
		}

	}
}
