package org.ekstep.config.controller;

public class ResourceBundleJson {
//
//	static ArrayList<String> langHdr = new ArrayList<String>();
//
//	public static void main(String[] args) throws Exception {
//
//		File input = new File("src/main/resources/contentDef.csv");
//		List<HashMap<String, String>> mapList = new ArrayList<HashMap<String,String>>();
//		mapList = readObjectsFromCsv(input);
//		int count = 0;
//		for (Map<String, String> map : mapList)
//			writeAsJson(new File("src/main/resources/output_" + langHdr.get(count++) + " .json"), map, langHdr.get(count - 1));
//
//	}
//
//	public static List<HashMap<String, String>> readObjectsFromCsv(File file) throws IOException {
//		@SuppressWarnings("resource")
//		CSVReader reader = new CSVReader(new FileReader(file));
//		String[] nextLine;
//		List<HashMap<String, String>> mapList = new ArrayList<HashMap<String, String>>();
//		boolean hdr = true;
//		int len = 0;
//		while ((nextLine = reader.readNext()) != null) {
//			String Key = "";
//			if (hdr) {
//				len = nextLine.length;
//				for (int i = 0; i < len - 2; i++) {
//					mapList.add(new HashMap<String, String>());
//					langHdr.add(nextLine[i + 2]);
//				}
//				hdr = false;
//			} else {
//				for (int j = 2; j < len; j++) {
//					if (Key.isEmpty())
//						Key = nextLine[0];
//					String val = (!nextLine[j].isEmpty()) ? nextLine[j] : ((!nextLine[2].isEmpty()) ? nextLine[2] : nextLine[0]);
//					mapList.get(j - 2).put(Key, val);
//				}
//			}
//		}
//		return mapList;
//	}
//
//	public static void writeAsJson(File output, Map<String, String> map, String Key) throws IOException {
//
//		FileOutputStream os = new FileOutputStream(output, true);
//		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
//		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
//		result.put(Key, map);
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		String str = gson.toJson(result);
//		bw.write(str);
//		bw.flush();
//		bw.close();
//	}
}
