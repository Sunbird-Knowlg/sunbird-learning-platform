/**
 * 
 */
package org.ekstep.tools.loader.shell;

import java.io.File;

import org.ekstep.tools.loader.service.BulkLoaderService;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 * @author pradyumna
 *
 */
@Component
public class UpdateCommand implements CommandMarker {

	@CliCommand(value = "add member", help = "Add Users to organisations")
	public String addOrgMember(
			@CliOption(key = { "input" }, mandatory = true, help = "Input CSV file") final File csvFile,
			@CliOption(key = { "mapping" }, mandatory = true, help = "Mapping file") final File tfmFile,
			@CliOption(key = {
					"key-column" }, mandatory = true, help = "Name of the key column in csv") final String keyColumn,
			@CliOption(key = { "dry-run" }, mandatory = false, help = "Dry-run only") final boolean dryRun)
			throws Exception {

		return callService(csvFile, tfmFile, keyColumn, "addMember");

	}

	@CliCommand(value = "upload artfct", help = "Upload content artifact")
	public String uploadArtifact(
			@CliOption(key = { "input" }, mandatory = true, help = "Input CSV file") final File csvFile,
			@CliOption(key = { "mapping" }, mandatory = true, help = "Mapping file") final File tfmFile,
			@CliOption(key = {
					"key-column" }, mandatory = true, help = "Name of the key column in csv") final String keyColumn,
			@CliOption(key = { "dry-run" }, mandatory = false, help = "Dry-run only") final boolean dryRun)
			throws Exception {

		return callService(csvFile, tfmFile, keyColumn, "artifact");

	}

	@CliCommand(value = "submit review", help = "Submit content for review")
	public String submitReview(
			@CliOption(key = { "input" }, mandatory = true, help = "Input CSV file") final File csvFile,
			@CliOption(key = { "mapping" }, mandatory = true, help = "Mapping file") final File tfmFile,
			@CliOption(key = {
					"key-column" }, mandatory = true, help = "Name of the key column in csv") final String keyColumn,
			@CliOption(key = { "dry-run" }, mandatory = false, help = "Dry-run only") final boolean dryRun)
			throws Exception {

		return callService(csvFile, tfmFile, keyColumn, "review");

	}

	@CliCommand(value = "publish", help = "Publish the content")
	public String publish(@CliOption(key = { "input" }, mandatory = true, help = "Input CSV file") final File csvFile,
			@CliOption(key = { "mapping" }, mandatory = true, help = "Mapping file") final File tfmFile,
			@CliOption(key = {
					"key-column" }, mandatory = true, help = "Name of the key column in csv") final String keyColumn,
			@CliOption(key = { "dry-run" }, mandatory = false, help = "Dry-run only") final boolean dryRun)
			throws Exception {

		return callService(csvFile, tfmFile, keyColumn, "publish");

	}

	@CliCommand(value = "update orgsts", help = "Update organisation status")
	public String updateOrgStatus(
			@CliOption(key = { "input" }, mandatory = true, help = "Input CSV file") final File csvFile,
			@CliOption(key = { "mapping" }, mandatory = true, help = "Mapping file") final File tfmFile,
			@CliOption(key = {
					"key-column" }, mandatory = true, help = "Name of the key column in csv") final String keyColumn,
			@CliOption(key = { "dry-run" }, mandatory = false, help = "Dry-run only") final boolean dryRun)
			throws Exception {

		return callService(csvFile, tfmFile, keyColumn, "orgStatus");

	}

	@CliCommand(value = "retire content", help = "Retire contents")
	public String retireContent(
			@CliOption(key = { "input" }, mandatory = true, help = "Input CSV file") final File csvFile,
			@CliOption(key = { "mapping" }, mandatory = true, help = "Mapping file") final File tfmFile,
			@CliOption(key = {
					"key-column" }, mandatory = true, help = "Name of the key column in csv") final String keyColumn,
			@CliOption(key = { "dry-run" }, mandatory = false, help = "Dry-run only") final boolean dryRun)
			throws Exception {

		return callService(csvFile, tfmFile, keyColumn, "retireContent");

	}

	@CliCommand(value = "retire concept", help = "Retire concepts")
	public String retireConcept(
			@CliOption(key = { "input" }, mandatory = true, help = "Input CSV file") final File csvFile,
			@CliOption(key = { "mapping" }, mandatory = true, help = "Mapping file") final File tfmFile,
			@CliOption(key = {
					"key-column" }, mandatory = true, help = "Name of the key column in csv") final String keyColumn,
			@CliOption(key = { "dry-run" }, mandatory = false, help = "Dry-run only") final boolean dryRun)
			throws Exception {

		return callService(csvFile, tfmFile, keyColumn, "retireConcept");

	}

	private String callService(File inputFile, File tfmFile, String keyColumn, String command) throws Exception {
		ShellContext context = ShellContext.getInstance();
		if (context.getCurrentConfig() == null)
			return "Not logged in.";

		BulkLoaderService service = new BulkLoaderService();
		service.setCsvFile(inputFile);
		service.setTfmFile(tfmFile);
		service.setKeyColumn(keyColumn);
		service.setUserID(keyColumn);
		service.setContext(command);

		System.out.println("Starting the bulk load process...");
		long begin = System.currentTimeMillis();
		service.execute(service);
		long end = System.currentTimeMillis();

		return "Completed the operation in " + (end - begin) + " ms.";
	}

	@CliCommand(value = "cmdlist", help = "List of commands and their definitions")
	public String cmdList()
			throws Exception {

		StringBuilder strb = new StringBuilder();
		strb.append(
				"login         ===> User has to login using the credentials and config file before executing belo commands \n");
		strb.append(
				"load content  ===> Create Content(if content-Id is not present in the inputfile AND update content if content_id is present in the input file \n");
		strb.append("load concept  ===> Create Concept \n");
		strb.append("load org      ===> Create Organisation \n");
		strb.append(
				"load user     ===> Create Users(if userId is not present in the inputfile AND update users if userId is present in the input file \n");
		strb.append(
				"load content  ===> Create Content(if content-Id is not present in the inputfile AND update content if content_id is present in the input file \n");
		strb.append(
				"load content  ===> Create Content(if content-Id is not present in the inputfile AND update content if content_id is present in the input file \n");
		strb.append("add member    ===> Adds user to organisation \n");
		strb.append("upload artfct ===> Uploads artifact for a content \n");
		strb.append("submit review ===> Submit content for review \n");
		strb.append("publish       ===> Publish the contents \n");
		strb.append("update orgsts ===> Update Organisation status \n");

		return strb.toString();

	}

}
