package org.sunbird.graph.reader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.codehaus.jackson.map.ObjectMapper;

public class CSVImportMessageHandler {
	private ObjectMapper mapper;
	private static final String PROPERTY_ID = "identifier";
	CSVFormat csvFileFormat = CSVFormat.DEFAULT;
	List<String> allHeaders;
	Map<String, List<String>> dataRows;
	CSVParser csvReader = null;
	CSVPrinter writer = null;

	public CSVImportMessageHandler(InputStream inputStream) throws Exception {
		// StringWriter writer = new StringWriter();
		// IOUtils.copy(inputStream, writer, "UTF-8");

		try (InputStreamReader isReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			csvReader = new CSVParser(isReader, csvFileFormat);
			List<CSVRecord> recordsList = csvReader.getRecords();
			CSVRecord headerRecord = recordsList.get(0);
			allHeaders = new ArrayList<String>();
			for (int i = 0; i < headerRecord.size(); i++) {
				allHeaders.add(headerRecord.get(i));
			}

			int uniqueIdIndex = allHeaders.indexOf(PROPERTY_ID);
			dataRows = new LinkedHashMap<String, List<String>>();
			for (int i = 1; i < recordsList.size(); i++) {
				CSVRecord record = recordsList.get(i);
				String uniqueId = record.get(uniqueIdIndex);
				List<String> row = new ArrayList<String>();
				for (int j = 0; j < record.size(); j++) {
					row.add(record.get(j));
				}
				dataRows.put(uniqueId, row);
			}
			mapper = new ObjectMapper();
		}
	}

	public OutputStream getOutputStream(Map<String, List<String>> messages) throws Exception {
		try (OutputStream outputStream = new ByteArrayOutputStream()) {
			if (messages == null || dataRows == null) {
				return outputStream;
			} else {
				allHeaders.add("Validation Messages");
				for (String rowIdentifier : dataRows.keySet()) {
					if (messages.containsKey(rowIdentifier)) {
						dataRows.get(rowIdentifier).add(mapper.writeValueAsString(messages.get(rowIdentifier)));
					}
				}
				OutputStreamWriter osWriter = new OutputStreamWriter(outputStream);
				writer = new CSVPrinter(osWriter, csvFileFormat);
				writer.printRecord(allHeaders);
				writer.printRecords(dataRows.values());
				writer.flush();
				osWriter.close();
				writer.close();
				return outputStream;
			}
		}
	}

}
