package org.sunbird.assessment.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * This is a utility program to map responses for given set of items from csv
 * @author rashmi
 *
 */
public class BulkItemImport {

	private static ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		BulkItemImport process = new BulkItemImport();
		String csvFile = "src/test/resources/sample.csv";
		try {
			process.readFromCsv(csvFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void readFromCsv(String file) throws IOException {
		FileReader fileReader = null;
		CSVParser csvFileParser = null;
		CSVPrinter csvFilePrinter = new CSVPrinter(new FileWriter("output.csv"), CSVFormat.DEFAULT.withRecordSeparator("\n"));
		try {
			fileReader = new FileReader(file);
			csvFileParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withHeader());
			List<CSVRecord> csvRecords = csvFileParser.getRecords();
			Set<String> headerSet = csvFileParser.getHeaderMap().keySet();
			Set<String> header = new LinkedHashSet<String>(headerSet);
			header.add("request");
			csvFilePrinter.printRecord(header);
			for (int i = 0; i < csvRecords.size(); i++) {
				Map<String, Object> map = new LinkedHashMap<>();
				CSVRecord record = csvRecords.get(i);
				for (String key : headerSet) {
					if (key.startsWith("SelectedOptions")) {
						map.put(key, record.get(key));
					}
					if (key.startsWith("UnselectedOptions")) {
						map.put(key, record.get(key));
					}
					if (key.startsWith("Mmcs")) {
						map.put(key, record.get(key));
					}
					if(key.startsWith("Score")){
						map.put(key, record.get(key));
					}
				}
				
				String request = createRequest(map); 
				List<String> data = new ArrayList<>();
				for(String head : headerSet){
					data.add(record.get(head));
				}
				data.add(request);
				csvFilePrinter.printRecord(data);
			}
			
			csvFilePrinter.flush();
			csvFilePrinter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public static String createRequest(Map<String, Object> csvMap) throws JsonProcessingException {
		List<Object> responseObject = new ArrayList<Object>();
		Map<String,Object> request_map = new HashMap<String,Object>();
		Map<String, Object> value_map = new HashMap<String, Object>();
		int count = 0;
		String requestString = null;
		for (Map.Entry<String, Object> entry : csvMap.entrySet()) {
			if (entry.getKey().startsWith("SelectedOptions")) {
				if(null != entry.getValue()){	
					String[] array = StringUtils.split(entry.getValue().toString(),',');
					for(String str : array){
						value_map.put(str, true);
					}
				}
			}
			if (entry.getKey().startsWith("UnselectedOptions")) {
				if(null != entry.getValue()){
				   String[] array = StringUtils.split(entry.getValue().toString(), ',');
				   for(String str : array){
						value_map.put(str, false);
				   }
				}
			}
			if(entry.getKey().startsWith("Mmcs")){
					if(null != entry.getValue()){
						String[] array = StringUtils.split(entry.getValue().toString(), ',');
						List<String> list = Arrays.asList(array);
						request_map.put("mmc", list);
					}
					else{
						request_map.put("mmc", "[]");
					}
			}
			if(entry.getKey().startsWith("Score")){
				if(null != entry.getValue()){
					request_map.put("score", entry.getValue());
				}
				else{
					request_map.put("score", 0);
				}
			}	
			count++;
			if(count==4){
				request_map.put("value", value_map);
				value_map = new HashMap<>();
				count = 0;
				Map<String,Object> requestMap = new HashMap<String,Object>();
				responseObject.add(request_map);
				requestMap.put("responses", responseObject);
		        requestString = mapper.writeValueAsString(requestMap);
				request_map = new HashMap<>();
			}
		}
		return requestString;
	}
}
