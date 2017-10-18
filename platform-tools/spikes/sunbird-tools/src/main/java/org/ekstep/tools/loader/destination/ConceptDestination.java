/**
 * 
 */
package org.ekstep.tools.loader.destination;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.tools.loader.service.ConceptServiceImpl;
import org.ekstep.tools.loader.service.ExecutionContext;
import org.ekstep.tools.loader.service.ProgressCallback;
import org.ekstep.tools.loader.service.Record;
import org.ekstep.tools.loader.shell.ShellContext;

import com.google.gson.JsonArray;
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
	public void process(List<Record> data, ProgressCallback callback) throws Exception {
		ShellContext shellContext = ShellContext.getInstance();
		config = shellContext.getCurrentConfig().resolve();
		user = shellContext.getCurrentUser();
		ConceptServiceImpl service = new ConceptServiceImpl();
		context = new ExecutionContext(config, user);
		int rowNum = 0;
		int totalRows = data.size();
		JsonArray conceptIds = new JsonArray();

		service.init(context);

		for (Record record : data) {
			String name = record.getCsvData().get("name");
			String conceptId = service.create(record.getJsonData(), context);
			System.out.println("Concept Name : " + name + "\t" + "ConceptId : " + conceptId);
			rowNum++;
			conceptIds.add(conceptId);
			callback.progress(totalRows, rowNum);

		}

		/* Needs to be removed. For testing purpose only */

		// System.out.println("Calling retire method to delete the concepts");
		//
		// service.retire(conceptIds, context);
	}

}
