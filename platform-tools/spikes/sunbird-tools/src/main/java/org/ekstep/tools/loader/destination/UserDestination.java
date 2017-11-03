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
import org.ekstep.tools.loader.service.ExecutionContext;
import org.ekstep.tools.loader.service.ProgressCallback;
import org.ekstep.tools.loader.service.Record;
import org.ekstep.tools.loader.service.UserServiceImpl;
import org.ekstep.tools.loader.shell.ShellContext;
import org.ekstep.tools.loader.utils.JsonUtil;

import com.google.gson.JsonObject;
import com.typesafe.config.Config;

/**
 * @author pradyumna
 *
 */
public class UserDestination implements Destination {

	private Logger logger = LogManager.getLogger(UserDestination.class);
	private Config config = null;
	private String user = null;
	private ExecutionContext context = null;
	private ShellContext shellContext = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.tools.loader.destination.Destination#process(java.util.List,
	 * org.ekstep.tools.loader.service.ProgressCallback)
	 */
	@Override
	public void process(List<Record> data, ProgressCallback callback) {
		shellContext = ShellContext.getInstance();
		config = shellContext.getCurrentConfig().resolve();
		user = shellContext.getCurrentUser();
		context = new ExecutionContext(config, user);
		String userID = null, status = null, name = null;
		int rowNum = 1;
		int totalRows = data.size();

		UserServiceImpl service = new UserServiceImpl();
		service.init(context);

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

				writeOutput(userID + "," + name + "," + status);

			} catch (Exception e) {
				writeOutput(userID + "," + name + "," + e.getMessage());
			}
			callback.progress(totalRows, rowNum++);
		}

	}

	/**
	 * @param string
	 * @throws IOException
	 */
	private void writeOutput(String output) {
		FileWriter outputFile;
		File file = new File("UsersOutput.csv");
		try {
			if (!file.exists()) {
				file.createNewFile();
				outputFile = new FileWriter(file, true);
				outputFile.write("UserID , Name, Status \n");
			} else {
				outputFile = new FileWriter(file, true);
			}

			outputFile.append(output + "\n");
			outputFile.close();
			logger.debug("Users Output  :: " + output);
		} catch (IOException e) {
			logger.debug("error while wrting to outputfile" + e.getMessage());
		}

	}

}
