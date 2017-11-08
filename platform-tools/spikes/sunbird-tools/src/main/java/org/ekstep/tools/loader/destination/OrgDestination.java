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
import org.ekstep.tools.loader.service.OrganisationService;
import org.ekstep.tools.loader.service.ProgressCallback;
import org.ekstep.tools.loader.service.Record;
import org.ekstep.tools.loader.utils.JsonUtil;

import com.google.gson.JsonObject;

/**
 * @author pradyumna
 *
 */
public class OrgDestination implements Destination {
	private static Logger logger = LogManager.getLogger(OrgDestination.class);
	private ExecutionContext context = null;
	private FileWriter outputFile = null;
	private File file = null;
	private static final String OUTPUT_FILE = "OrgOutput.csv";
	/**
	 * 
	 */
	public OrgDestination() {
		file = new File(OUTPUT_FILE);
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
		String orgID = null, status = null, orgName = null;
		int rowNum = 1;
		int totalRows = data.size();

		OrganisationService service = (OrganisationService) ServiceProvider.getService("org");
		context = ServiceProvider.getContext();

		ServiceProvider.writeOutput(file,
				"\n------------------- Begin ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
		for (Record record : data) {
			try {
				JsonObject org = record.getJsonData();
				if (checkForOrgStatus(org)) {
					orgID = service.updateOrgStatus(org, context);
					status = JsonUtil.getFromObject(org, "response");
					ServiceProvider.writeOutput(file, orgID + "," + "StatusUpdate" + "," + status);
				} else {
					System.out.println("calling Org Service");
					orgName = JsonUtil.getFromObject(org, "orgName");
					orgID = service.createOrg(org, context);
					if (StringUtils.isNotEmpty(orgID)) {
						status = "Success";
					} else {
						status = JsonUtil.getFromObject(org, "response");
					}
					logger.debug("Org Output : " + orgID + " , " + orgName + " , " + status);
					ServiceProvider.writeOutput(file, orgID + "," + orgName + "," + status);
				}

			} catch (Exception e) {
				logger.error(e);
				ServiceProvider.writeOutput(file, orgID + "," + orgName + "," + e.getMessage());
			}
			callback.progress(totalRows, rowNum++);
		}
		ServiceProvider.writeOutput(file,
				"\n------------------- End ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
	}

	/**
	 * @param org
	 * @return
	 */
	private boolean checkForOrgStatus(JsonObject org) {
		if (StringUtils.isNotBlank(JsonUtil.getFromObject(org, "organisationId"))
				&& null != JsonUtil.getFromObject(org, "status") && org.keySet().size() == 2)
			return true;
		return false;
	}

}
