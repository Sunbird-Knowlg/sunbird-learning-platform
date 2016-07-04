package org.ekstep.config.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONObject;

public class ContentDefinitionCsvTest {
	String validFileName = "src/main/resources/content_definition.json";
	String emptyFileName = "";
	String invalidFileName = "src/main/resources/content_definitn.json";
	ContentDefinitionCsv content = new ContentDefinitionCsv();
	@SuppressWarnings("static-access")
	Map<String, Object> definitionMap = content.getProperties(content.readJsonFile(validFileName));
	CSVPrinter csvFilePrinter = null;

	@SuppressWarnings("static-access")

	@Test
	public void readJsonFileWithValidFileName() {
		JSONObject json = content.readJsonFile(validFileName);
		assertEquals(false, json.isEmpty());
	}

	@SuppressWarnings("static-access")
	@Test
	public void readJsonFileWithEmptyFileName() {
		assertEquals(null, content.readJsonFile(emptyFileName));
	}

	@SuppressWarnings("static-access")
	@Test
	public void readJsonFileWithInvalidFileName() {
		assertEquals(null, content.readJsonFile(invalidFileName));
	}

	@SuppressWarnings("static-access")
	@Test
	public void readJsonFileCheckProperties() {
		JSONObject json = content.readJsonFile(validFileName);
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
			assertEquals(true, definitionMap.containsKey("inOutRelations"));
		}
}
}