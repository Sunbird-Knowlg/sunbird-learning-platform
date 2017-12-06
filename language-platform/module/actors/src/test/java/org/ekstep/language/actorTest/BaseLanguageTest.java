package org.ekstep.language.actorTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.graph.common.enums.GraphEngineParams;
import org.ekstep.graph.common.enums.GraphHeaderParams;
import org.ekstep.graph.dac.util.Neo4jGraphFactory;
import org.ekstep.graph.engine.router.ActorBootstrap;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;

public class BaseLanguageTest {
	
	protected static String TEST_LANGUAGE = "hi";
	protected static String TEST_COMMON_LANGUAGE = "language";

	static{
		ActorBootstrap.loadConfiguration();
	}

	@BeforeClass
	public static void init() throws Exception {
		createGraph();
		createDefinition();
	}
	
	@AfterClass
	public static void close() throws IOException, InterruptedException {
		deleteGraph();
	}
		
	protected static void createGraph(){
		if (!Neo4jGraphFactory.graphExists(TEST_LANGUAGE)) 
			Neo4jGraphFactory.createGraph(TEST_LANGUAGE);
		if (!Neo4jGraphFactory.graphExists(TEST_COMMON_LANGUAGE)) 
			Neo4jGraphFactory.createGraph(TEST_COMMON_LANGUAGE);	
	}

	protected static void deleteGraph(){
        GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(TEST_LANGUAGE);
        if (null != graphDb) {
            Neo4jGraphFactory.shutdownGraph(TEST_LANGUAGE);
        }
        Neo4jGraphFactory.deleteGraph(TEST_LANGUAGE);
        graphDb = Neo4jGraphFactory.getGraphDb(TEST_COMMON_LANGUAGE);
        if (null != graphDb) {
            Neo4jGraphFactory.shutdownGraph(TEST_COMMON_LANGUAGE);
        }
        Neo4jGraphFactory.deleteGraph(TEST_COMMON_LANGUAGE);
	}
	
	protected static void createDefinition() throws IOException{
		 String gradeComplexity_def_json =getJSONString(new File("src/test/resources/GradeLevelComplexity.json"));
		 createDefinition(gradeComplexity_def_json, TEST_COMMON_LANGUAGE);
//		 String wordComplexity_def_json =getJSONString(new File("src/test/resources/WordComplexityDefinition.json"));
//		 createDefinition(wordComplexity_def_json, TEST_COMMON_LANGUAGE);
//		 String word_def_json =getJSONString(new File("src/test/resources/WordDefinitionNode.json"));
//		 createDefinition(word_def_json, TEST_LANGUAGE);
//		 String synset_def_json =getJSONString(new File("src/test/resources/SynsetDefinition.json"));
//		 createDefinition(synset_def_json, TEST_LANGUAGE);
//		 String varna_def_json =getJSONString(new File("src/test/resources/VarnaDefinition.json"));
//		 createDefinition(varna_def_json, TEST_LANGUAGE);
//		 String travelRule_def_json =getJSONString(new File("src/test/resources/TraversalRuleDefinition.json"));
//		 createDefinition(travelRule_def_json, TEST_LANGUAGE);
//		 String wordSet_def_json =getJSONString(new File("src/test/resources/wordset_definition.json"));
//		 createDefinition(wordSet_def_json, TEST_LANGUAGE);
	}
	
	protected static String getJSONString(File initialFile) throws IOException{
		InputStream targetStream = new FileInputStream(initialFile);
		
		InputStreamReader isReader = new InputStreamReader(targetStream, "UTF8");
		String jsonContent = IOUtils.toString(isReader);
		
		return jsonContent;
	}
		
	@SuppressWarnings("unused")
	protected static void createDefinition(String contentString, String graph_id) throws IOException{
		
		String apiId = "definition.create";
//		Map<String, Object> map = mapper.readValue("",
//				new TypeReference<Map<String, Object>>() {
//				});

		Request request = new Request();
		request.setManagerName(GraphEngineManagers.NODE_MANAGER);
		request.setOperation("importDefinitions");
		request.getContext().put(GraphHeaderParams.graph_id.name(),
				graph_id);
		request.put(GraphEngineParams.input_stream.name(), contentString);
		PlatformLogger.log("List | Request: " , request);
		Response response = LanguageCommonTestHelper.getResponse(
				request);
		PlatformLogger.log("List | Response: " , response);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
	}
}
