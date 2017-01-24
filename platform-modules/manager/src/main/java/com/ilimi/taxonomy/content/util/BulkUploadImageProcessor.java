package com.ilimi.taxonomy.content.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.ArrayUtils;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.visionApi.TestVisionApi;

public class BulkUploadImageProcessor {

	private static String[] FILE_HEADER_MAPPING_INPUT;
	private static Object[] FILE_HEADER_MAPPING_OUTPUT;
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String s3AssetsFolder = "s3.asset.folder";
	private static final String output = "src/main/resources/output";

	@SuppressWarnings("unchecked")
	public void updateCSV(String fileName, String[] zipFile) {
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
			FILE_HEADER_MAPPING_INPUT = headerSet.toArray(new String[headerSet.size()]);
			List<String> headerList = new ArrayList<>(headerSet);
			headerList.add("DownloadUrl");
			headerList.add("Flags");
			fileWriter = new FileWriter(opfileName);
			csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
			FILE_HEADER_MAPPING_OUTPUT = headerList.toArray();
			csvFilePrinter.printRecord(FILE_HEADER_MAPPING_OUTPUT);
			String folderLocation = unzip(zipFile);
			for (CSVRecord record : csvRecords) {
				Map<String, Object> outputData = callVisionAPIandAWSUpload(folderLocation,
						record.get(FILE_HEADER_MAPPING_INPUT[0]));
				List<String> updatedData = new ArrayList<>();
				for (Object header : FILE_HEADER_MAPPING_OUTPUT) {
					if ("Tags".equalsIgnoreCase(header.toString()) || "Keywords".equalsIgnoreCase(header.toString())) {
						Map<String, Object> data = (Map<String, Object>) outputData.get("Tags");
						List<String> tagList = new ArrayList<String>();
						List<String> rec = Arrays.asList(record.get(header.toString()).split(","));
						tagList.addAll(rec);
						if (null != data.get("80-90")) {
							List<String> value = (List<String>) data.get("80-90");
							for (String valueFinal : value) {
								if (!containsIgnoreCase(tagList, valueFinal)) {
									tagList.add(valueFinal);
								}
							}
						}
						if (null != data.get("90-100")) {
							List<String> value = (List<String>) data.get("90-100");
							for (String valueFinal : value) {
								if (!containsIgnoreCase(tagList, valueFinal)) {
									tagList.add(valueFinal);
								}
							}
						}
						updatedData.add(String.join(",", tagList));
					} else if ("Flags".equalsIgnoreCase(header.toString())) {
						Map<String, Object> data = (Map<String, Object>) outputData.get(header.toString());
						List<String> flagList = new ArrayList<String>();
						if (null != data.get("VERY_LIKELY")) {
							List<String> value = (List<String>) data.get("VERY_LIKELY");
							for (String valueFinal : value) {
								if (!containsIgnoreCase(flagList, valueFinal)) {
									flagList.add(valueFinal);
								}
							}
						}
						if (null != data.get("LIKELY")) {
							List<String> value = (List<String>) data.get("LIKELY");
							for (String valueFinal : value) {
								if (!containsIgnoreCase(flagList, valueFinal)) {
									flagList.add(valueFinal);
								}
							}
						}
						updatedData.add(String.join(",", flagList));
					} else if (Arrays.asList(FILE_HEADER_MAPPING_INPUT).contains(header)) {
						updatedData.add(record.get(header.toString()));
					} else {
						updatedData.add(outputData.get(header).toString());
					}
				}
				csvFilePrinter.printRecord(updatedData);
			}
		} catch (Exception e) {
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
				e.printStackTrace();
			}
		}
	}

	private void deleteFile() {
		File file = new File(output);
		try {
			if (file.exists()) {
				delete(file);
			}
		} catch (IOException e) {
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

	public String unzip(String[] zipFilePaths) {
		String finalOutput = output;
		byte[] bytesIn = new byte[4096];
		File destDir = new File(output);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		try {
			for (String zipFilePath : zipFilePaths) {
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
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalOutput;
	}

	public Map<String, Object> callVisionAPIandAWSUpload(String folder, String fileName) {
		Map<String, Object> result = new HashMap<>();
		File file = new File(folder + "/" + fileName);
		File newFile = new File(folder + "/" + fileName.substring(0, fileName.lastIndexOf(".")) + "_"
				+ System.currentTimeMillis() + fileName.substring(fileName.lastIndexOf("."), fileName.length()));
		try {
			if (file.renameTo(newFile)) {
				file = newFile;
				TestVisionApi api = new TestVisionApi(TestVisionApi.getVisionService());
				result.put("Tags", api.labelImage(file.toPath()));
				result.put("Flags", api.safeSearch(file.toPath()));
				result.put("DownloadUrl", callAWSUploader(file));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		return result;
	}

	public String callAWSUploader(File file) {
		String url = "";
		try {
			String folder = S3PropertyReader.getProperty(s3AssetsFolder);
			String[] result = AWSUploader.uploadFile(folder, file);
			if (null != result && result.length == 2) {
				url = result[1];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}

	public static void main(String[] args) {
		BulkUploadImageProcessor process = new BulkUploadImageProcessor();
		String csvFile = args[0];
		String[] zipFile = (String[]) ArrayUtils.removeElement(args, args[0]);
		process.updateCSV(csvFile, zipFile);
	}

	public boolean containsIgnoreCase(List<String> l, String s) {
		for (String d : l) {
			if (d.trim().equalsIgnoreCase(s))
				return true;
		}
		return false;
	}

}
