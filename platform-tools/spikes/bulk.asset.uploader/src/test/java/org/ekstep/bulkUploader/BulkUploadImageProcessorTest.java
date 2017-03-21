package org.ekstep.bulkUploader;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.ekstep.bulkUpload.BulkUploadImageProcessor;
import org.junit.After;
import org.junit.Test;

public class BulkUploadImageProcessorTest {

	public BulkUploadImageProcessor processor = new BulkUploadImageProcessor();
	public static File file;

	@Test
	public void testBulkUpload() {
		String csvfileName = "src/test/resources/Test.csv";
		String zipFiles =  "src/test/resources/test.zip" ;
		String env = "DEV";
		FileReader fileReader = null;
		CSVParser csvFileParser = null;
		try {
			fileReader = new FileReader(csvfileName);
			csvFileParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withHeader());
			List<CSVRecord> csvInputRecords = csvFileParser.getRecords();
			processor.loadEnvironment(env);
			processor.updateCSV(csvfileName, zipFiles);
			String opfileName = csvfileName.replace(".csv", "-Output.csv");
			file = new File(opfileName);
			assertTrue(file.exists());
			fileReader = new FileReader(opfileName);
			csvFileParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withHeader());
			List<CSVRecord> csvOutputRecords = csvFileParser.getRecords();
			assertTrue(csvFileParser.getHeaderMap().containsKey("identifier"));
			assertTrue(csvFileParser.getHeaderMap().containsKey("name"));
			assertTrue(csvFileParser.getHeaderMap().containsKey("code"));
			assertTrue(csvFileParser.getHeaderMap().containsKey("keywords"));
			assertTrue(csvFileParser.getHeaderMap().containsKey("status"));
			assertTrue(csvFileParser.getHeaderMap().containsKey("flag"));
			assertTrue(csvFileParser.getHeaderMap().containsKey("downloadUrl"));
			assertTrue(csvInputRecords.size() == csvOutputRecords.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void deleteFileAfterTest() {
		try {
			if (file.exists()) {
				processor.delete(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
