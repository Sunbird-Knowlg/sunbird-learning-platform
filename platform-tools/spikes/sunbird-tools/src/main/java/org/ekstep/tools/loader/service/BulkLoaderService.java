/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.tools.loader.destination.ArtifactDestination;
import org.ekstep.tools.loader.destination.ConceptDestination;
import org.ekstep.tools.loader.destination.ContentDestination;
import org.ekstep.tools.loader.destination.Destination;
import org.ekstep.tools.loader.destination.OrgDestination;
import org.ekstep.tools.loader.destination.OrgMemberDestination;
import org.ekstep.tools.loader.destination.PublishDestination;
import org.ekstep.tools.loader.destination.RetireConceptDestination;
import org.ekstep.tools.loader.destination.RetireContentDestination;
import org.ekstep.tools.loader.destination.ReviewDestination;
import org.ekstep.tools.loader.destination.SysoutDestination;
import org.ekstep.tools.loader.destination.UserDestination;

/**
 *
 * @author feroz
 */
public class BulkLoaderService implements ProgressCallback {
	static Logger logger = LogManager.getLogger(BulkLoaderService.class);

	private File csvFile;
	private File tfmFile;
	private String keyColumn;
	private List<String> lookupFiles;
	private String userID;
	private String context;

	/**
	 * @return the csvFile
	 */
	public File getCsvFile() {
		return csvFile;
	}

	/**
	 * @param csvFile
	 *            the csvFile to set
	 */
	public void setCsvFile(File csvFile) {
		this.csvFile = csvFile;
	}

	/**
	 * @return the tfmFile
	 */
	public File getTfmFile() {
		return tfmFile;
	}

	/**
	 * @param tfmFile
	 *            the tfmFile to set
	 */
	public void setTfmFile(File tfmFile) {
		this.tfmFile = tfmFile;
	}

	/**
	 * @return the keyColumn
	 */
	public String getKeyColumn() {
		return keyColumn;
	}

	/**
	 * @param keyColumn
	 *            the keyColumn to set
	 */
	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn;
	}

	/**
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * @param userID
	 *            the userID to set
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}

	/**
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(String context) {
		this.context = context;
	}

	public void execute(ProgressCallback callback) throws Exception {
		Source source = new CsvParserSource(csvFile, keyColumn);
		Processor mapper = new JsonMapperProcessor(getTfmFile());
		Processor merger = new JsonMergeProcessor();
		Destination destination = new SysoutDestination();

		logger.info("Step 1 of 4 - Reading input data");
		List<Record> data = source.process(callback);

		logger.info("Step 2 of 4 - Transforming input data");
		data = mapper.process(data, callback);

		logger.info("Step 3 of 4 - Merging input data rows");
		data = merger.process(data, callback);

		logger.info("Step 4 of 4 - Loading the transformed data");
		switch (context) {
		case "content":
			destination = new ContentDestination();
			break;

		case "concept":
			destination = new ConceptDestination();
			break;

		case "organisation":
			destination = new OrgDestination();
			break;

		case "user":
			destination = new UserDestination();
			break;

		case "addMember":
			destination = new OrgMemberDestination();
			break;

		case "artifact":
			destination = new ArtifactDestination();
			break;

		case "review":
			destination = new ReviewDestination();
			break;

		case "publish":
			destination = new PublishDestination();
			break;

		case "retireContent":
			destination = new RetireContentDestination();
			break;

		case "retireConcept":
			destination = new RetireConceptDestination();
			break;

		default:
			break;
		}

		destination.process(data, callback);
	}

	@Override
	public void progress(int totalSteps, int completedSteps) {
		if (completedSteps % 2 == 0) {
			if (completedSteps > totalSteps)
				completedSteps = totalSteps;
			double progress = (double) completedSteps / (double) totalSteps * 100d;
			String progStr = String.format("%.2f", progress);
			logger.info("      Completed " + progStr + " %");
		}
	}
}
