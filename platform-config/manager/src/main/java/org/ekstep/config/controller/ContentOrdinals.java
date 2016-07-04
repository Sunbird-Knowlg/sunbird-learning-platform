package org.ekstep.config.controller;

public class ContentOrdinals {
//
//	
//	@SuppressWarnings("unchecked")
//	public static void main(String[] args) {
//		
//		String fileName = "src/main/resources/content_definition.json";
//		JSONObject getDefinitions = ContentDefinitionCsv.readJsonFile(fileName);
//		Map<String, Object> definitionMap = ContentDefinitionCsv.getProperties(getDefinitions);
//		List<String> range = new ArrayList<String>();
//		Map<String, ArrayList<String>> ordinals = new HashMap<String, ArrayList<String>>();
//		
//		for (Object properties : (List<String>) definitionMap.get("properties")) {
//			range = (List<String>) ((HashMap<String, Object>) properties).get("range");
//			if (range != null) {
//				ordinals.put(((String) ((HashMap<String, Object>) properties).get("propertyName")),(ArrayList<String>) range);
//			}
//		}
//		writeToJson(ordinals);
//	}
//	public static void writeToJson(Map<String, ArrayList<String>> ordinals){
//		
//		try {
//	    FileOutputStream os = new FileOutputStream("src/main/resources/ordinals.json",true);
//        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//	    String str = gson.toJson(ordinals);
//	    bw.flush();
//	    bw.write(str);
//	    bw.flush();
//        bw.close();
//		
//	    }catch (IOException e) {
//		System.out.println("file not found in the given location");
//	    }
//	}
}
	