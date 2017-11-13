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
import org.ekstep.tools.loader.service.ProgressCallback;
import org.ekstep.tools.loader.service.Record;
import org.ekstep.tools.loader.service.UserService;
import org.ekstep.tools.loader.utils.JsonUtil;

import com.google.gson.JsonObject;

/**
 * @author pradyumna
 *
 */
public class UserDestination implements Destination {

	private Logger logger = LogManager.getLogger(UserDestination.class);
	private ExecutionContext context = null;
	private FileWriter outputFile = null;
	private File file = null;

	private static final String OUTPUT_FILE = "UsersOutput.csv";

	/**
	 * 
	 */
	public UserDestination() {
		file = new File(OUTPUT_FILE);
		if (!file.exists()) {
			try {
				file.createNewFile();
				outputFile = new FileWriter(file, true);
				outputFile.write("UserID , Name, Status \n");
				outputFile.close();
			} catch (IOException e) {
				logger.debug("Error while creating file");
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.tools.loader.destination.Destination#process(java.util.List,
	 * org.ekstep.tools.loader.service.ProgressCallback)
	 */
	@Override
	public void process(List<Record> data, ProgressCallback callback) {
		String userID = null, status = null, name = null;
		int rowNum = 1;
		int totalRows = data.size();

		UserService service = (UserService) ServiceProvider.getService("user");
		context = ServiceProvider.getContext();
		ServiceProvider.writeOutput(file,
				"\n------------------- Begin ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
		for (Record record : data) {
			try {
				JsonObject userRec = record.getJsonData();
				if (StringUtils.isNotBlank(JsonUtil.getFromObject(userRec, "userId"))) {
					userID = service.updateUser(userRec, context);
					name = "updatedUser";
				} else {
					name = JsonUtil.getFromObject(userRec, "firstName");
					userID = service.createUser(userRec, context);
					if (StringUtils.isNotEmpty(userID)) {
						status = "Success";
					} else {
						status = JsonUtil.getFromObject(userRec, "response");
					}
				}
				logger.debug("User Output : " + userID + " , " + name + " , " + status);
				ServiceProvider.writeOutput(file, userID + "," + name + "," + status);

			} catch (Exception e) {
				logger.error(e);
				ServiceProvider.writeOutput(file, userID + "," + name + "," + e.getMessage());
			}
			callback.progress(totalRows, rowNum++);
		}
		ServiceProvider.writeOutput(file,
				"\n------------------- End ::" + LocalDate.now() + " " + LocalTime.now() + "------------\n");
	}
}
