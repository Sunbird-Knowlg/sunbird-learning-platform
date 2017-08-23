package org.ekstep.config.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;
import org.ekstep.config.util.ContentDefinitionCsv;
import org.json.simple.JSONObject;
import org.junit.Test;

public class ContentDefinitionCsvTest {
	String validFileName = "content_definition.json";
	String emptyFileName = "";
	String invalidFileName = "content_definitn.json";
	ContentDefinitionCsv content = new ContentDefinitionCsv();
	@SuppressWarnings("static-access")
	Map<String, Object> definitionMap = content.getProperties(content.readJsonFile(validFileName));
	CSVPrinter csvFilePrinter = null;

	@Test
	public void readJsonFileWithValidFileName() {
		JSONObject json = ContentDefinitionCsv.readJsonFile(validFileName);
		assertEquals(false, json.isEmpty());
	}

	@Test
	public void readJsonFileWithEmptyFileName() {
		assertEquals(null, ContentDefinitionCsv.readJsonFile(emptyFileName));
	}

	@Test
	public void readJsonFileCheckProperties() {
		JSONObject json = ContentDefinitionCsv.readJsonFile(validFileName);
		assertEquals(1, json.size());
	}

	@SuppressWarnings({})
	@Test
	public void getPropertiesWithValidMap() {
		assertEquals(false, definitionMap.isEmpty());
		assertEquals(6, definitionMap.size());
	}

	@SuppressWarnings({})
	@Test
	public void getPropertiesWithValidMapProperties() {
		assertEquals(true, definitionMap.containsKey("properties"));
		assertEquals(true, definitionMap.containsKey("inRelations"));
		assertEquals(true, definitionMap.containsKey("systemTags"));
	}

	@SuppressWarnings({})
	@Test
	public void getPropertiesDetailsCheckNonExistingProperties() {

		assertEquals(false, definitionMap.containsKey("range"));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void getAllRangeDetailsWithValidData() {
		for (Object properties : (List<String>) definitionMap.get("properties")) {
			List<String> range = new ArrayList<String>();
			range = (List<String>) ((HashMap<String, Object>) properties).get("range");
			if (range != null) {
				assertEquals(false, range.isEmpty());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getRelationsCheck() {
		for (Object inrelations : (List<String>) definitionMap.get("inRelations")) {
			String title = (String) ((HashMap<String, Object>) inrelations).get("title");
			String description = (String) ((HashMap<String, Object>) inrelations).get("description");
			assertEquals(false, title.isEmpty());
			assertEquals(false, description.isEmpty());
			assertEquals(true, definitionMap.containsKey("inRelations"));
			assertEquals(6, definitionMap.keySet().size());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void getRelationsForNonExistingRelationsTypeCheck() {
		for (Object inrelations : (List<String>) definitionMap.get("inRelations")) {
			String title = (String) ((HashMap<String, Object>) inrelations).get("title");
			String description = (String) ((HashMap<String, Object>) inrelations).get("description");
			assertEquals(false, title.isEmpty());
			assertEquals(false, description.isEmpty());
//			assertEquals(true, definitionMap.containsKey("inOutRelations"));
		}
}
}