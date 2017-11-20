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
public class OrgMemberDestination implements Destination {

	private Logger logger = LogManager.getLogger(OrgMemberDestination.class);

	private ExecutionContext context = null;
	private FileWriter outputFile;
	private File file = null;
	private static final String OUTPUT_FILE = "OrgMemberOutput.csv";

	/**
	 * 
	 */
	public OrgMemberDestination() {
		file = new File(OUTPUT_FILE);
		if (!file.exists()) {
			try {
				file.createNewFile();
				outputFile = new FileWriter(file, true);
				outputFile.write("OrganisationID , UserId, Status \n");
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
		String orgID = null, userId = null;
		int rowNum = 1;
		int totalRows = data.size();

		OrganisationService service = (OrganisationService) ServiceProvider.getService("org");
		context = ServiceProvider.getContext();

		ServiceProvider.writeOutput(file,
				"\n------------------- Begin ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
		for (Record record : data) {
			try {
				JsonObject member = record.getJsonData();
				orgID = JsonUtil.getFromObject(member, "organisationId");
				userId = JsonUtil.getFromObject(member, "userId");
				String response = service.addOrgMember(member, context);
				ServiceProvider.writeOutput(file, orgID + "," + userId + "," + response);
			} catch (Exception e) {
				ServiceProvider.writeOutput(file, orgID + "," + userId + "," + e.getMessage());
			}
			callback.progress(totalRows, rowNum++);

		}
		ServiceProvider.writeOutput(file,
				"\n------------------- END ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
	}

}
