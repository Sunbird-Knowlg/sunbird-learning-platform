package org.ekstep.sync.tool.shell;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class csvFileParser {

	public List<String> csvData(String filePath, String objectType) throws Exception{
		String path = filePath;
		String objType;
		List<String> identifiers = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(path));
		try {
			String line = "";
			while ((line = reader.readLine()) != null) {
				String[] rows = line.split(",");
				objType = rows[1].trim();
				if(objType.equals(objectType)){
					identifiers.add(rows[0]);
				}
			}
		}
		catch(Exception e){			
		}
		finally{
			reader.close();
		}	
	return identifiers;
	}	
	
	public List<String> csvData(String filePath) throws Exception{
		String path = filePath;
		List<String> identifiers = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(path));
		try {
			String line = "";
			while ((line = reader.readLine()) != null) {
				String[] rows = line.split(",");
				identifiers.add(rows[0]);
			}
		}
		catch(Exception e){			
		}
		finally{
			reader.close();
		}	
		return identifiers;
	}	
}