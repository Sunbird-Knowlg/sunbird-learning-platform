package org.ekstep.config.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.ilimi.common.dto.CoverageIgnore;

import org.ekstep.common.util.AWSUploader;
import org.json.simple.JSONObject;

public class ContentOrdinals {
	
	@SuppressWarnings("unchecked")
	@CoverageIgnore
	public static void main(String[] args) {
		
		String fileName = "content_definition.json";
		JSONObject getDefinitions = ContentDefinitionCsv.readJsonFile(fileName);
		Map<String, Object> definitionMap = ContentDefinitionCsv.getProperties(getDefinitions);
		List<String> range = new ArrayList<String>();
		Map<String, ArrayList<String>> ordinals = new HashMap<String, ArrayList<String>>();
		
		for (Object properties : (List<String>) definitionMap.get("properties")) {
			range = (List<String>) ((HashMap<String, Object>) properties).get("range");
			if (range != null) {
				ordinals.put(((String) ((HashMap<String, Object>) properties).get("propertyName")),(ArrayList<String>) range);
			}
		}
		File output = writeToJson(ordinals);
		try {
		String[] ApiUrl = AWSUploader.uploadFile("ekstep-public","src/main/resources", output);
		for(String url:ApiUrl)
     	   System.out.println(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@CoverageIgnore
	public static File writeToJson(Map<String, ArrayList<String>> ordinals){
		File file = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
		    file = new File("src/main/resources/ordinals.json");
			ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
			writer.writeValue(file, ordinals);

		} catch (IOException e) {
			System.out.println("file not found in the given location");
		}
		return file;
	}
}
	