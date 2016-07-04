package org.ekstep.config.controller;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.plaf.synth.SynthSpinnerUI;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ContentDefinitionCsv {
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String SPACE_SEPERATOR = "";
	static Set<String> propertySet = new HashSet<String>();

	public static void main(String[] args) {

		try {
			String fileName = "src/main/resources/content_definition.json";
			Map<String, Object> definitionMap = new HashMap<String, Object>();
			JSONObject getDefinitions = readJsonFile(fileName);
			if(!getDefinitions.isEmpty()){
			 definitionMap = getProperties(getDefinitions);
			 System.out.println(definitionMap);
			}
			
			csvWriter(definitionMap);
		} catch (IOException e) {
			System.out.println("please enter proper file location");
		}
	}

	// reading Json file
	public static JSONObject readJsonFile(String filename) {
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader(filename));
		} catch (FileNotFoundException e) {
			System.out.println("file not found in the given location");
		} catch (IOException e) {
			System.out.println("file could not be read due to IO exception");
		} catch (ParseException e) {
			System.out.println("file could not be parsed due to ParseException");
		}
		JSONObject definitionObj = (JSONObject) obj;
		return definitionObj;
	}

	// all properties
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, Object> getProperties(JSONObject definitionObj) {
      
		Object getProperties = definitionObj.get("definitionNodes");
		Map<String, Object> definitionMap = (Map<String, Object>) ((List) getProperties).get(0);
		return definitionMap;
	}

	// all properties details
	@SuppressWarnings("unchecked")
	public static void getPropertiesDetails(Map<String, Object> definitionMap, CSVPrinter csvFilePrinter) {
		for (Object properties : (List<String>) definitionMap.get("properties")) {

			String property = (String) ((HashMap<String, Object>) properties).get("propertyName");
			String description = (String) ((HashMap<String, Object>) properties).get("description");
			String title = (String) ((HashMap<String, Object>) properties).get("title");
			List<String> csvData = new ArrayList<String>();

			if (!propertySet.contains(property)) {
				csvData.add(property);
				csvData.add(description);
				csvData.add(title);
				try {
					csvFilePrinter.printRecord(csvData);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			propertySet.add(property);
		}
	}

	// all ranges
	@SuppressWarnings("unchecked")
	public static void getAllRange(Map<String, Object> definitionMap, CSVPrinter csvFilePrinter) {
		for (Object properties : (List<String>) definitionMap.get("properties")) {
			List<String> range = new ArrayList<String>();
			range = (List<String>) ((HashMap<String, Object>) properties).get("range");
			
			if (range != null) {
				System.out.println(range.isEmpty());
				for (Object rangeValue : range) {
				
					List<String> csvData = new ArrayList<String>();
					if (!propertySet.contains(rangeValue)) {
						csvData.add((String) rangeValue);
						csvData.add(SPACE_SEPERATOR);
						csvData.add((String) rangeValue);
						try {
							csvFilePrinter.printRecord(csvData);

						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					propertySet.add((String) rangeValue);
				}

			}
		}
	}

	// inRelations
	@SuppressWarnings("unchecked")
	public static void getAllRelations(Map<String, Object> definitionMap, CSVPrinter csvFilePrinter) {
		String title = "";
		String description = "";
		for (Object inrelations : (List<String>) definitionMap.get("inRelations")) {
			System.out.println(definitionMap.get("inRelations"));
			if (!propertySet.contains(inrelations)) {
				List<String> csvData = new ArrayList<String>();
				title = (String) ((HashMap<String, Object>) inrelations).get("title");
				description = (String) ((HashMap<String, Object>) inrelations).get("description");
				if (!propertySet.contains(title) && !propertySet.contains(description)) {
					csvData.add(title);
					csvData.add(description);
					try {
						csvFilePrinter.printRecord(csvData);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			propertySet.add(title);
			propertySet.add(description);
		}
		for (Object outrelations : (List<String>) definitionMap.get("outRelations")) {
			List<String> csvData = new ArrayList<String>();
			title = (String) ((HashMap<String, Object>) outrelations).get("title");
			description = (String) ((HashMap<String, Object>) outrelations).get("description");
			if (!propertySet.contains(title) && !propertySet.contains(description)) {
				csvData.add(title);
				csvData.add(description);
				try {
					csvFilePrinter.printRecord(csvData);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			propertySet.add(title);
			propertySet.add(description);
		}
	}

	// system tags
	public static void getSystemTags(CSVPrinter csvFilePrinter) {
		List<String> csvData = new ArrayList<String>();
		csvData.add("tags");
		csvData.add(SPACE_SEPERATOR);
		csvData.add("tags");
		try {
			csvFilePrinter.printRecord(csvData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// write to csv file
	public static void csvWriter(Map<String, Object> definitionMap) throws IOException {

		final Object[] FILE_HEADER = { "KEY", "DESCRIPTION", "EN", "KA", "HI", "TE", "TA" };
		FileWriter fileWriter = null;
		CSVPrinter csvFilePrinter = null;
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

		fileWriter = new FileWriter("src/main/resources/contentDef.csv");
		csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
		csvFilePrinter.printRecord(FILE_HEADER);

		getPropertiesDetails(definitionMap, csvFilePrinter);
		getAllRange(definitionMap, csvFilePrinter);
		getAllRelations(definitionMap, csvFilePrinter);

		fileWriter.flush();
		fileWriter.close();
		csvFilePrinter.close();
	}
}
