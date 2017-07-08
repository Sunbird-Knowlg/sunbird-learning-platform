package org.ekstep.language.index.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.engine.router.ActorBootstrap;
import com.ilimi.graph.engine.router.GraphEngineManagers;

public class BaseLanguageTest {

	private static ILogger LOGGER = PlatformLogManager.getLogger()
			.getName());
	protected static String TEST_LANGUAGE = "en";
	protected static String TEST_COMMON_LANGUAGE = "language";
	private static String definitionFolder = "src/test/resources/definitions";
	
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
		File folder = new File(definitionFolder);
		for (File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".json")) {
				String def_json =getJSONString(fileEntry);
				 createDefinition(def_json, TEST_LANGUAGE);
			}
		}
		File languageFolder = new File(definitionFolder+File.separatorChar+"language");
		for (File fileEntry : languageFolder.listFiles()) {
			if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".json")) {
				String def_json =getJSONString(fileEntry);
				 createDefinition(def_json, TEST_COMMON_LANGUAGE);
			}
		}
	}
	
	protected static String getJSONString(File initialFile) throws IOException{
		InputStream targetStream = new FileInputStream(initialFile);
		
		InputStreamReader isReader = new InputStreamReader(targetStream, "UTF8");
		String jsonContent = IOUtils.toString(isReader);
		
		return jsonContent;
	}
		
	protected static void createDefinition(String contentString, String graph_id) throws IOException{
		
		Request request = new Request();
		request.setManagerName(GraphEngineManagers.NODE_MANAGER);
		request.setOperation("importDefinitions");
		request.getContext().put(GraphHeaderParams.graph_id.name(),
				graph_id);
		request.put(GraphEngineParams.input_stream.name(), contentString);
		LOGGER.log("List | Request: " + request);
		Response response = LanguageCommonTestHelper.getResponse(
				request, LOGGER);
		LOGGER.log("List | Response: " + response);
		
		Assert.assertEquals("successful", response.getParams().getStatus());
	}
}
