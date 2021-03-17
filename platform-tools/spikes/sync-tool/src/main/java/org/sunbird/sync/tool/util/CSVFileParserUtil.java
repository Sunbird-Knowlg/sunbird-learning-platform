package org.sunbird.sync.tool.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

public class CSVFileParserUtil {

	public static final String PROPERTY_ID = "identifier";
	public static final String PROPERTY_OBJECT_TYPE = "objectType";

	public static List<String> getIdentifiers(String filePath, String objectType) throws Exception {
		List<String> identifiers = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
				CSVParser csvReader = new CSVParser(reader, CSVFormat.DEFAULT)) {
			List<CSVRecord> recordsList = csvReader.getRecords();
			CSVRecord headerRecord = recordsList.get(0);
			List<String> allHeaders = new ArrayList<String>();
			for (int i = 0; i < headerRecord.size(); i++) {
				allHeaders.add(headerRecord.get(i).trim());
			}
			int uniqueIdIndex = allHeaders.indexOf(PROPERTY_ID);
			int objectTypeIndex = allHeaders.indexOf(PROPERTY_OBJECT_TYPE);
			for (int i = 1; i < recordsList.size(); i++) {
				CSVRecord record = recordsList.get(i);
				String uniqueId = record.get(uniqueIdIndex).trim();
				String inputObjectType = objectTypeIndex > -1 ? record.get(objectTypeIndex).trim() : "";
				if (StringUtils.isNotBlank(objectType) && StringUtils.isNotBlank(inputObjectType)) {
					if (objectType.equalsIgnoreCase(inputObjectType))
						identifiers.add(uniqueId);
				} else {
					identifiers.add(uniqueId);
				}
			}
		}
		return identifiers;
	}
}