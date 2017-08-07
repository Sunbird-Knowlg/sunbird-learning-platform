package org.ekstep.jobs.samza.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ekstep.jobs.samza.service.ObjectLifecycleService;
import org.ekstep.learning.util.ControllerUtil;
import org.junit.Test;
import org.ekstep.jobs.samza.model.Event;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

import static org.junit.Assert.assertEquals;

public class ObjectLifecycleServiceTest{

	 String messageWithStatusChange = "{\"nodeUniqueId\":\"org.ekstep.jul03.story.test01\",\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"mimeType\":{\"ov\":null,\"nv\":\"application/vnd.ekstep.ecml-archive\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"},\"status\":{\"ov\":null,\"nv\":\"Draft\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"Content\"}";
	 String messageWithoutStatusChange = "{\"nodeUniqueId\":\"org.ekstep.jul03.story.test01\",\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"mediaType\":{\"ov\":null,\"nv\":\"content\"},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"mimeType\":{\"ov\":null,\"nv\":\"application/vnd.ekstep.ecml-archive\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"contentType\":{\"ov\":null,\"nv\":\"Story\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"Content\"}";
	 String assessmentItem = "{\"nodeUniqueId\":\"org.ekstep.jul03.story.test01\",\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"status\":{\"ov\":null,\"nv\":\"Live\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"AssessmentItem\"}";
	 String itemset = "{\"nodeUniqueId\":\"org.ekstep.jul03.story.test01\",\"requestId\":\"110dc48a-b0ee-4a64-b822-1053ab7ef276\",\"transactionData\":{\"properties\":{\"owner\":{\"ov\":null,\"nv\":\"EkStep\"},\"code\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"IL_SYS_NODE_TYPE\":{\"ov\":null,\"nv\":\"DATA_NODE\"},\"visibility\":{\"ov\":null,\"nv\":\"Default\"},\"os\":{\"ov\":null,\"nv\":[\"All\"]},\"subject\":{\"ov\":null,\"nv\":\"literacy\"},\"portalOwner\":{\"ov\":null,\"nv\":\"EkStep\"},\"description\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"language\":{\"ov\":null,\"nv\":[\"English\"]},\"osId\":{\"ov\":null,\"nv\":\"org.ekstep.quiz.app\"},\"ageGroup\":{\"ov\":null,\"nv\":[\"5-6\"]},\"idealScreenSize\":{\"ov\":null,\"nv\":\"normal\"},\"createdOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"idealScreenDensity\":{\"ov\":null,\"nv\":\"hdpi\"},\"gradeLevel\":{\"ov\":null,\"nv\":[\"Grade 1\"]},\"IL_FUNC_OBJECT_TYPE\":{\"ov\":null,\"nv\":\"Content\"},\"name\":{\"ov\":null,\"nv\":\"शेर का साथी हाथी\"},\"lastUpdatedOn\":{\"ov\":null,\"nv\":\"2016-07-03T15:39:34.570+0530\"},\"developer\":{\"ov\":null,\"nv\":\"EkStep\"},\"IL_UNIQUE_ID\":{\"ov\":null,\"nv\":\"org.ekstep.jul03.story.test01\"},\"status\":{\"ov\":null,\"nv\":\"Live\"}}},\"operationType\":\"CREATE\",\"nodeGraphId\":974,\"label\":\"शेर का साथी हाथी\",\"graphId\":\"domain\",\"nodeType\":\"DATA_NODE\",\"userId\":\"ANONYMOUS\",\"createdOn\":\"2016-07-03T15:39:34.570+0530\",\"objectType\":\"ItemSet\"}";
	 
	 ControllerUtil util = new ControllerUtil();
	 ObjectLifecycleService service = new ObjectLifecycleService();
	 Map<String, Object> messageData = new HashMap<String, Object>();
     ObjectMapper mapper = new ObjectMapper();
	 
	 @Test
	 public void testLifecycleEventWithStatusChange() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Map<String,Object>  map = service.getStateChangeEvent(messageData);
		 assertEquals(true, map.containsKey("nv"));
		 String status = (String) map.get("nv");
		 assertEquals("Draft", status);
	 }
	 
	 @Test
	 public void testLifecycleEventWithoutStatusChange() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithoutStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Map<String,Object>  map = service.getStateChangeEvent(messageData);
		 assertEquals(map, null);
	 }
	 
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	 @Test
	 public void generateLifecycleEventForImage() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.story.test01");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("Content");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("contentType", "Asset");
		 metadata.put("name", "testImage");
		 metadata.put("code", "org.ekstep.test.image");
		 metadata.put("channel", "apssdc");
		 metadata.put("mediaType", "image");
		 node.setMetadata(metadata);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("Asset", eks.get("type"));
		 assertEquals("image", eks.get("subtype"));
		 assertEquals("Draft", eks.get("state"));
		 assertEquals("", eks.get("prevstate"));
	 }
	 
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	 @Test
	 public void generateLifecycleEventForAudio() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.story.test01");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("Content");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("contentType", "Asset");
		 metadata.put("name", "testAudio");
		 metadata.put("code", "org.ekstep.test.audio");
		 metadata.put("channel", "apssdc");
		 metadata.put("mediaType", "audio");
		 node.setMetadata(metadata);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("Asset", eks.get("type"));
		 assertEquals("audio", eks.get("subtype"));
		 assertEquals("Draft", eks.get("state"));
		 assertEquals("", eks.get("prevstate"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEventForPlugins() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.story.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("Content");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("contentType", "Plugin");
		 String[] categories = new String[] {"Library"};
		 metadata.put("category", categories);
		 metadata.put("name", "testPlugin");
		 metadata.put("code", "org.ekstep.test.plugin");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("Plugin", eks.get("type"));
		 assertEquals("Library", eks.get("subtype"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEventForStory() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.story.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("Content");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("contentType", "Story");
		 metadata.put("name", "test");
		 metadata.put("code", "org.ekstep.test.story");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("Content", eks.get("type"));
		 assertEquals("Story", eks.get("subtype"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEventForCollectionsWithRelations() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.story.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("Content");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("contentType", "Collection");
		 metadata.put("name", "test");
		 metadata.put("code", "org.ekstep.test.collection");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Relation relation = new Relation();
		 relation.setEndNodeId("org.ekstep.test.collection.01");
		 relation.setEndNodeObjectType("Content");
		 relation.setRelationType("hasSequenceMember");
		 List<Relation> inRelations = new ArrayList<Relation>();
		 inRelations.add(relation);
		 node.setInRelations(inRelations);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("Content", eks.get("type"));
		 assertEquals("Collection", eks.get("subtype"));
		 assertEquals("org.ekstep.test.collection.01", eks.get("parentid"));
		 assertEquals("Content", eks.get("parenttype"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEventForConcept() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.concept.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("Concept");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("name", "test");
		 metadata.put("code", "org.ekstep.test.concept");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Relation relation = new Relation();
		 relation.setEndNodeId("org.ekstep.test.concept.01");
		 relation.setEndNodeObjectType("Concept");
		 relation.setRelationType("isParentOf");
		 List<Relation> inRelations = new ArrayList<Relation>();
		 inRelations.add(relation);
		 node.setInRelations(inRelations);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("Concept", eks.get("type"));
		 assertEquals("", eks.get("subtype"));
		 assertEquals("org.ekstep.test.concept.01", eks.get("parentid"));
		 assertEquals("Concept", eks.get("parenttype"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEventForConceptWithRelations() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.concept.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("Concept");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("name", "test");
		 metadata.put("code", "org.ekstep.test.concept");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Relation relation = new Relation();
		 relation.setEndNodeId("org.ekstep.test.dimension.01");
		 relation.setEndNodeObjectType("Dimension");
		 relation.setRelationType("isParentOf");
		 List<Relation> inRelations = new ArrayList<Relation>();
		 inRelations.add(relation);
		 node.setInRelations(inRelations);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("Concept", eks.get("type"));
		 assertEquals("", eks.get("subtype"));
		 assertEquals("org.ekstep.test.dimension.01", eks.get("parentid"));
		 assertEquals("Dimension", eks.get("parenttype"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEventForDimension() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.dimension.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("Dimension");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("name", "test");
		 metadata.put("code", "org.ekstep.test.dimension");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Relation relation = new Relation();
		 relation.setEndNodeId("org.ekstep.test.domain.01");
		 relation.setEndNodeObjectType("Domain");
		 relation.setRelationType("isParentOf");
		 List<Relation> inRelations = new ArrayList<Relation>();
		 inRelations.add(relation);
		 node.setInRelations(inRelations);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("Dimension", eks.get("type"));
		 assertEquals("", eks.get("subtype"));
		 assertEquals("org.ekstep.test.domain.01", eks.get("parentid"));
		 assertEquals("Domain", eks.get("parenttype"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEventForTextBook() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.story.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("Content");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("contentType", "textBook");
		 metadata.put("name", "test");
		 metadata.put("code", "org.ekstep.test.story");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("Content", eks.get("type"));
		 assertEquals("textBook", eks.get("subtype"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEventForTextBookWithRelations() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.story.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("Content");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("contentType", "textBook");
		 metadata.put("name", "test");
		 metadata.put("code", "org.ekstep.test.story");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Relation relation = new Relation();
		 relation.setEndNodeId("org.ekstep.test.textbook.01");
		 relation.setEndNodeObjectType("Content");
		 relation.setRelationType("hasSequenceMember");
		 List<Relation> outRelations = new ArrayList<Relation>();
		 outRelations.add(relation);
		 node.setOutRelations(outRelations);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("Content", eks.get("type"));
		 assertEquals("textBook", eks.get("subtype"));
		 assertEquals("org.ekstep.test.textbook.01", eks.get("parentid"));
		 assertEquals("Content", eks.get("parenttype"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEventForAssessmentItem() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(assessmentItem, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.story.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("AssessmentItem");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("type", "ftb");
		 metadata.put("name", "testItem");
		 metadata.put("code", "org.ekstep.test.item");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("AssessmentItem", eks.get("type"));
		 assertEquals("ftb", eks.get("subtype"));
		 assertEquals("Live", eks.get("state"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEventForAssessmentItemWithRelations() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(assessmentItem, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.story.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("AssessmentItem");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("type", "ftb");
		 metadata.put("name", "testItem");
		 metadata.put("code", "org.ekstep.test.item");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Relation relation = new Relation();
		 relation.setEndNodeId("org.ekstep.test.itemSet.01");
		 relation.setEndNodeObjectType("ItemSet");
		 relation.setRelationType("hasMember");
		 List<Relation> inRelations = new ArrayList<Relation>();
		 inRelations.add(relation);
		 node.setInRelations(inRelations);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("AssessmentItem", eks.get("type"));
		 assertEquals("ftb", eks.get("subtype"));
		 assertEquals("Live", eks.get("state"));
		 assertEquals("org.ekstep.test.itemSet.01", eks.get("parentid"));
		 assertEquals("ItemSet", eks.get("parenttype"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEventForAssessmentItemSet() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(itemset, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("domain");
		 node.setIdentifier("org.ekstep.jul03.story.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("ItemSet");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("type", "materialised");
		 metadata.put("name", "testItemSet");
		 metadata.put("code", "org.ekstep.test.itemSet");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("ItemSet", eks.get("type"));
		 assertEquals("materialised", eks.get("subtype"));
		 assertEquals("Live", eks.get("state"));
	 }
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 @Test
	 public void generateLifecycleEvent() throws JsonParseException, JsonMappingException, IOException{
		 messageData = mapper.readValue(messageWithStatusChange, new TypeReference<Map<String, Object>>() {
			});
		 Node node = new Node();
		 node.setGraphId("En");
		 node.setIdentifier("org.ekstep.jul03.story.test02");
		 node.setNodeType("DATA_NODE");
		 node.setObjectType("Word");
		 Map<String,Object> metadata = new HashMap<String,Object>();
		 metadata.put("name", "test");
		 metadata.put("code", "org.ekstep.test");
		 metadata.put("channel", "apssdc");
		 node.setMetadata(metadata);
		 Map<String,Object> stateChangeEvent = service.getStateChangeEvent(messageData);
		 Event event = service.generateLifecycleEvent(stateChangeEvent, node);
		 assertEquals("BE_OBJECT_LIFECYCLE", event.getEid());
		 Map<String,Object> edata = event.getEdata();
		 assertEquals(true, edata.containsKey("eks"));
		 Map<String,Object> eks = (Map)edata.get("eks");
		 assertEquals(true, eks.containsKey("type"));
		 assertEquals("Word", eks.get("type"));
		 assertEquals("", eks.get("subtype"));
		 assertEquals("Draft", eks.get("state"));
	 }
}
