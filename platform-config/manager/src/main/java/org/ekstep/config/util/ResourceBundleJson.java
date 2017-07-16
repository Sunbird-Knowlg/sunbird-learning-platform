package org.ekstep.config.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ekstep.common.util.AWSUploader;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.ilimi.common.dto.CoverageIgnore;
import com.opencsv.CSVReader;

public class ResourceBundleJson {
	static Map<String, Integer> langHdr = new HashMap<String, Integer>();

	@CoverageIgnore
	public static void main(String[] args) {

		Map<String, HashMap<String, String>> mapList = new HashMap<String, HashMap<String, String>>();
		File input = new File(ContentDefinitionCsv.class.getClassLoader().getResource("contentDef.csv").getFile());
		try {
			mapList = readObjectsFromCsv(input, mapList);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@CoverageIgnore
	private static Map<String, HashMap<String, String>> readObjectsFromCsv(File input,
			Map<String, HashMap<String, String>> mapList) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(input));
		String[] header = reader.readNext();
		if (header.length < 3) {
			System.out.println("cannot generate json as the number of columns are less than 3");
		} else {
			for (int j = 2; j < header.length; j++) {
				langHdr.put(header[j], j);
				mapList.put(header[j], new HashMap<String, String>());
			}
		}
		mapList = readRowFromCsv(reader, (HashMap<String, Integer>) langHdr, mapList);
		return mapList;
	}

	@CoverageIgnore
	private static Map<String, HashMap<String, String>> readRowFromCsv(CSVReader reader,
			HashMap<String, Integer> langHdr, Map<String, HashMap<String, String>> mapList) {
		String[] nextLine;
		try {
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine.length < 2)
					continue;
				else {
					for (Map.Entry<String, Integer> entry : langHdr.entrySet()) {
						int cIndex = entry.getValue();
						String val = "";
						String key = nextLine[0];
						if (nextLine.length > cIndex && null != nextLine[cIndex]) {
							val = nextLine[cIndex];
						} else {
							if (nextLine.length > 2 && null != nextLine[2])
								val = nextLine[2];
							else
								val = nextLine[0];
						}
						mapList.get(entry.getKey()).put(key, val);
					}
				}
			}
			for (Entry<String, HashMap<String, String>> entry : mapList.entrySet()) {
				File file = new File(entry.getKey() + ".json");
				if (file.exists()) {
					file.delete();
				} else {
					writeAsJson("src/main/resources/resourceBundle/" + file, entry.getValue(), entry.getKey());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapList;
	}

	@CoverageIgnore
	private static void writeAsJson(String file, Map<String, String> map, String Key) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		writer.writeValue(new File(file), map);
		String[] apiUrl = null;
		try {
			File output = new File(file);
			apiUrl = AWSUploader.uploadFile("resources", output);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String url : apiUrl)
			System.out.println(url);
	}
}
