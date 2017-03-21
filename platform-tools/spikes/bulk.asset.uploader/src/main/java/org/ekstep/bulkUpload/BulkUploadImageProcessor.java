package org.ekstep.bulkUpload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class BulkUploadImageProcessor {

	private static String PLATFORM_API_URL;
	private static final String PLATFORM_API_URL_DEV = "https://dev.ekstep.in/api/learning";
	private static final String PLATFORM_API_URL_QA = "https://qa.ekstep.in/api/learning";
	private static final String PLATFORM_API_URL_PROD = "https://api.ekstep.in/api/learning";
	private static Object[] FILE_HEADER_MAPPING_OUTPUT;
	private static Set<String> MANDATORY_INPUT_HEADERS;
	private static Set<String> MANDATORY_OUTPUT_HEADERS;
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String SUB_FIELD_SEPARATOR = "::";
	private static final String output = "/data/contentBundle/output";
	private static Logger LOGGER = LogManager.getLogger(BulkUploadImageProcessor.class.getName());
	private ObjectMapper mapper = new ObjectMapper();

	static {
		MANDATORY_INPUT_HEADERS = new HashSet<String>();
		MANDATORY_INPUT_HEADERS.add("name");
		MANDATORY_INPUT_HEADERS.add("code");
		MANDATORY_INPUT_HEADERS.add("contentType");
		MANDATORY_INPUT_HEADERS.add("mimeType");
		MANDATORY_INPUT_HEADERS.add("mediaType");
		MANDATORY_INPUT_HEADERS.add("objectType");

		MANDATORY_OUTPUT_HEADERS = new HashSet<String>();
		MANDATORY_OUTPUT_HEADERS.add("identifier");
		MANDATORY_OUTPUT_HEADERS.add("name");
		MANDATORY_OUTPUT_HEADERS.add("code");
		MANDATORY_OUTPUT_HEADERS.add("keywords");
		MANDATORY_OUTPUT_HEADERS.add("status");
		MANDATORY_OUTPUT_HEADERS.add("flag");
		MANDATORY_OUTPUT_HEADERS.add("downloadUrl");
	}

	/**
	 * Method to get the content node data for all images in the csv file using
	 * content api's
	 * 
	 * @param csvfileName
	 * @param zipFiles
	 */
	@SuppressWarnings("unchecked")
	public void updateCSV(String fileName, String zipFile) {
		LOGGER.info("In update CSV ");
		String opfileName = fileName.replace(".csv", "-Output.csv");
		FileReader fileReader = null;
		FileWriter fileWriter = null;
		CSVParser csvFileParser = null;
		CSVPrinter csvFilePrinter = null;
		CSVFormat csvFileFormat = null;
		try {
			fileReader = new FileReader(fileName);
			csvFileParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withHeader());
			List<CSVRecord> csvRecords = csvFileParser.getRecords();
			Set<String> headerSet = csvFileParser.getHeaderMap().keySet();
			Set<String> headerList = new HashSet<String>(headerSet);
			headerList.addAll(MANDATORY_OUTPUT_HEADERS);
			fileWriter = new FileWriter(opfileName);
			csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
			FILE_HEADER_MAPPING_OUTPUT = headerList.toArray();
			csvFilePrinter.printRecord(FILE_HEADER_MAPPING_OUTPUT);
			String folderLocation = unzip(zipFile);
			for (CSVRecord record : csvRecords) {
				Map<String, Object> outputData = callContentAPIs(folderLocation, record.toMap());
				List<Object> updatedData = new ArrayList<Object>();
				for (Object header : FILE_HEADER_MAPPING_OUTPUT) {
					if (null != outputData.get(header)) {
						if (outputData.get(header) instanceof List) {
							List<String> arr = (List<String>) outputData.get(header);
							updatedData.add(String.join(SUB_FIELD_SEPARATOR, arr));
						} else {
							updatedData.add(outputData.get(header));
						}
					} else {
						updatedData.add(record.toMap().get(header));
					}
				}
				csvFilePrinter.printRecord(updatedData);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				deleteFile();
				fileReader.close();
				csvFileParser.close();
				fileWriter.flush();
				fileWriter.close();
				csvFilePrinter.close();
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Deletes the files/folder extracted while upload process
	 */
	private void deleteFile() {
		File file = new File(output);
		try {
			if (file.exists()) {
				delete(file);
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void delete(File file) throws IOException {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					delete(fileDelete);
				}
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			file.delete();
		}
	}

	/**
	 * extracts the zipfile and process each file
	 * 
	 * @param zipFilePaths
	 * @return folderName where extracted
	 */
	private String unzip(String zipFilePath) {
		String finalOutput = output;
		byte[] bytesIn = new byte[4096];
		File destDir = new File(output);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		try {
			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
			ZipEntry entry = zipIn.getNextEntry();
			while (entry != null) {
				String filePath = output + File.separator + entry.getName();
				if (!entry.isDirectory()) {
					if (entry.getName().contains(File.separator)) {
						String path = entry.getName();
						filePath = output + File.separator
								+ path.substring((path.lastIndexOf(File.separator) + 1), path.length());
					}
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
					int read = 0;
					while ((read = zipIn.read(bytesIn)) != -1) {
						bos.write(bytesIn, 0, read);
					}
					bos.close();
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
			zipIn.close();
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
		return finalOutput;
	}

	private Map<String, Object> callContentAPIs(String folderLocation, Map<String, String> data) {
		Map<String, Object> output = new HashMap<String, Object>();
		try {
			String fileName = data.get("name");
			File file = new File(folderLocation + File.separator + fileName);
			String identifier = createContent(data);
			uploadContent(identifier, file);
			output = getContent(identifier);
		} catch (NullPointerException e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
		return output;
	}

	@SuppressWarnings("unchecked")
	private String createContent(Map<String, String> data) throws Exception {
		String url = PLATFORM_API_URL + "/v2/content";
		Map<String, Object> requestBodyMap = new HashMap<String, Object>();
		Map<String, Object> requestMap = new HashMap<String, Object>();
		Map<String, Object> contentMap = new HashMap<String, Object>();
		for (String field : MANDATORY_INPUT_HEADERS) {
			if (null != data.get(field) && !StringUtils.isBlank(data.get(field))) {
				contentMap.put(field, data.get(field));
			} else {
				throw new Exception("Please provide " + field);
			}
		}
		contentMap.put("description", data.get("description"));
		contentMap.put("owner", data.get("owner"));
		contentMap.put("identifier", StringUtils.isBlank(data.get("identifier")) ? null : data.get("identifier"));
		contentMap.put("subject", data.get("subject"));
		String[] language = StringUtils.split(data.get("language"), SUB_FIELD_SEPARATOR);
		contentMap.put("language", language);
		String[] keywords = StringUtils.split(data.get("keywords"), SUB_FIELD_SEPARATOR);
		contentMap.put("keywords", keywords);

		requestMap.put("content", contentMap);
		requestBodyMap.put("request", requestMap);
		String result = null;
		try {
			String requestBody = mapper.writeValueAsString(requestBodyMap);
			result = HTTPUtil.makePostRequest(url, requestBody);
			Map<String, Object> response = mapper.readValue(result, Map.class);
			if (null != response && null != response.get("result")) {
				Map<String, Object> content = (Map<String, Object>) response.get("result");
				result = (String) content.get("node_id");
			}
		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private String uploadContent(String identifier, File file) {
		String url = PLATFORM_API_URL + "/v2/content/upload/" + identifier;
		String result = null;
		try {
			result = HTTPUtil.makePostRequestUploadFile(url, file);
			Map<String, Object> response = mapper.readValue(result, Map.class);
			if (null != response && null != response.get("result")) {
				Map<String, Object> content = (Map<String, Object>) response.get("result");
				result = (String) content.get("content_url");
			}
		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getContent(String identifier) {
		String url = PLATFORM_API_URL + "/v2/content/" + identifier;
		String result = null;
		Map<String, Object> nodeData = new HashMap<String, Object>();
		try {
			result = HTTPUtil.makeGetRequest(url);
			Map<String, Object> response = mapper.readValue(result, Map.class);
			if (null != response && null != response.get("result")) {
				Map<String, Object> content = (Map<String, Object>) response.get("result");
				nodeData = (Map<String, Object>) content.get("content");
			}
		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
		return nodeData;
	}

	public void loadEnvironment(String env) {
		switch (env.toLowerCase()) {
		case "dev": {
			PLATFORM_API_URL = PLATFORM_API_URL_DEV;
			break;
		}
		case "qa": {
			PLATFORM_API_URL = PLATFORM_API_URL_QA;
			break;
		}
		case "prod": {
			PLATFORM_API_URL = PLATFORM_API_URL_PROD;
			break;
		}
		}
	}

	public static void main(String[] args) {
		BulkUploadImageProcessor process = new BulkUploadImageProcessor();
		String csvFile = args[0];
		String zipFile = args[1];
		String env = args[2];
		process.loadEnvironment(env);
		process.updateCSV(csvFile, zipFile);
	}
}
