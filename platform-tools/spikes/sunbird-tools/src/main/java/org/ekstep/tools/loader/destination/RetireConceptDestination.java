/**
 * 
 */
package org.ekstep.tools.loader.destination;

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
public class RetireConceptDestination implements Destination {
	private static Logger logger = LogManager.getLogger(RetireConceptDestination.class);
	private Config config = null;
	private String user = null;
	private ExecutionContext context = null;
	private ShellContext shellContext = null;

	@Override
	public void process(List<Record> data, ProgressCallback callback) {
		shellContext = ShellContext.getInstance();
		config = shellContext.getCurrentConfig().resolve();
		user = shellContext.getCurrentUser();
		context = new ExecutionContext(config, user);
		ConceptServiceImpl service = new ConceptServiceImpl();
		int rowNum = 1;
		int totalRows = data.size();
		JsonArray conceptIds = new JsonArray();

		for (Record record : data) {
			try {
				JsonObject content = record.getJsonData();
				String conceptId = JsonUtil.getFromObject(content, "conceptId");
				if (StringUtils.isNotBlank(conceptId)) {
					conceptIds.add(conceptId);
				}
				if (conceptIds.size() > 0) {
					String response = service.retire(conceptIds, context);
					if (response.equalsIgnoreCase("OK")) {
						logger.info("Successfully retired all conceptIds");
					} else {
						logger.info(response);
					}
				} else {
					logger.info("No ConceptIds to retire");
				}
			} catch (Exception e) {
				logger.error("Error while retiring conceptIds - ", e);
			}
			callback.progress(totalRows, rowNum++);
		}

	}

}
