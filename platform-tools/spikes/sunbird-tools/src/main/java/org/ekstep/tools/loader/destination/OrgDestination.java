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
import org.ekstep.tools.loader.service.ExecutionContext;
import org.ekstep.tools.loader.service.OrganisationServiceImpl;
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
public class OrgDestination implements Destination {
	private static Logger logger = LogManager.getLogger(OrgDestination.class);
	private Config config = null;
	private String user = null;
	private ExecutionContext context = null;
	private ShellContext shellContext = null;
	private FileWriter outputFile = null;
	private File file = null;

	/**
	 * 
	 */
	public OrgDestination() {
		file = new File("OrgOutput.csvs");
		if (!file.exists()) {
			try {
				file.createNewFile();
				outputFile = new FileWriter(file, true);
				outputFile.write("OrganisationID , OrganisationName, Status \n");
				outputFile.close();
			} catch (IOException e) {
				logger.debug("Error while creating file");
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 */
	@Override
	public void process(List<Record> data, ProgressCallback callback) {
		shellContext = ShellContext.getInstance();
		config = shellContext.getCurrentConfig().resolve();
		user = shellContext.getCurrentUser();
		context = new ExecutionContext(config, user);
		String orgID = null, status = null, orgName = null;
		int rowNum = 1;
		int totalRows = data.size();

		OrganisationServiceImpl service = new OrganisationServiceImpl();

		service.init(context);
		writeOutput("\n------------------- Begin ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
		for (Record record : data) {
			try {
				JsonObject org = record.getJsonData();
				if (checkForOrgStatus(org)) {
					orgID = service.updateOrgStatus(org, context);
					status = JsonUtil.getFromObject(org, "response");
					writeOutput(orgID + "," + "StatusUpdate" + "," + status);
				} else {
					orgName = JsonUtil.getFromObject(org, "orgName");
					orgID = service.createOrg(org, context);
					if (StringUtils.isNotEmpty(orgID)) {
						status = "Success";
					} else {
						status = JsonUtil.getFromObject(org, "response");
					}

					writeOutput(orgID + "," + orgName + "," + status);
				}

			} catch (Exception e) {
				writeOutput(orgID + "," + orgName + "," + e.getMessage());
			}
			callback.progress(totalRows, rowNum++);
		}
		writeOutput("\n------------------- End ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
	}

	/**
	 * @param org
	 * @return
	 */
	private boolean checkForOrgStatus(JsonObject org) {
		if (StringUtils.isNotBlank(JsonUtil.getFromObject(org, "orgId"))
				&& null != JsonUtil.getFromObject(org, "status") && org.keySet().size() == 2)
			return true;
		return false;
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
			logger.debug("Organisation Output  :: " + output);
		} catch (IOException e) {
			logger.debug("error while wrting to outputfile" + e.getMessage());
		}

	}

}
