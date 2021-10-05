package org.sunbird.sync.tool.shell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.common.enums.GraphEngineParams;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.enums.ImportType;
import org.sunbird.graph.importer.InputStreamValue;
import org.sunbird.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.searchindex.util.CompositeSearchConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.shell.core.CommandResult;

import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

@Ignore
public class SyncCommandTest extends SpringShellTest{

	@BeforeClass
	public static void init() throws Exception {
		ElasticSearchUtil.initialiseESClient("testcompositeindex",
				Platform.config.getString("search.es_conn_info"));
		//load definition
		loadDefinition("definitions/content_definition.json", "definitions/item_definition.json");
		//load data(content and assessment)
		importGraphData("domain", "contentData.csv");
		importGraphData("domain", "assessmentitemData.csv");

	}
	
	@AfterClass
	public static void destroy(){

	}

	@Before
	public void beforeTest() throws Exception {
		createCompositeSearchIndex();
	}
	
	@After
	public void afterTest() throws Exception {
		deleteCompositeSearchIndex();		
	}
	
	@Test
	public void testNodeNotFoundException() {
		CommandResult cr = new CommandResult(false);
		//Execute command
		PrintStream err = System.err;
		System.setErr(null);	
		try {
			cr = getShell().executeCommand("syncbyids --ids do_121212");
		}catch (Exception e) {
			Assert.assertEquals("ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND: Objects not found ", cr.getException());			
		}
		Assert.assertFalse( cr.isSuccess() );
		System.setErr(err);	
	}

	@Test
	public void testSyncNodeById() throws Exception {
		CommandResult cr = new CommandResult(false);
		//Execute command
		cr = getShell().executeCommand("syncbyids --ids do_112178562079178752118");
		Assert.assertNull(cr.getException());
		Assert.assertTrue( cr.isSuccess() );
		String doc = ElasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, "do_112178562079178752118");
		assertTrue(StringUtils.contains(doc, "do_112178562079178752118"));
		//Execute command
		cr = getShell().executeCommand("syncbyids --ids domain_48763,domain_14658,do_112178562079178752118");
		Assert.assertNull(cr.getException());
		Assert.assertTrue( cr.isSuccess() );
		List<String> resultDocs = ElasticSearchUtil.getMultiDocumentAsStringByIdList(
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE,
				Arrays.asList("domain_48763", "domain_14658", "do_112178562079178752118"));

		assertNotNull(resultDocs);
		assertEquals(3, resultDocs.size());
	}
	
	@Test
	public void testSyncNodeByObjectType() throws Exception {
		CommandResult cr = new CommandResult(false);
		//Execute command
		cr = getShell().executeCommand("syncbyobjecttype --objectType AssessmentItem");
		Assert.assertNull(cr.getException());
		Assert.assertTrue( cr.isSuccess() );
		List<String> ids = Arrays.asList("ek.n.q_QAFTB_88","do_10096483","do_112178562079178752118","test.ftb_010","do_11225380345346457614","do_11225380550993510416","do_11225380647183155217","do_11225381096194867211","do_11225562396351692815","do_112255733460467712120","do_112255733481611264121","do_112257049614336000119","do_112257049883033600120","do_112257050142777344121","do_112257050369056768122","do_112257683409166336123","do_112257683430334464124","do_112257852917121024153","do_112258573914611712145","do_112260519610449920119");
		List<String> resultDocs = ElasticSearchUtil.getMultiDocumentAsStringByIdList(
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE,
				ids);

		assertNotNull(resultDocs);
		assertEquals(20, resultDocs.size());

	}
	
	@Test
	public void testSyncNodeByInvalidObjectType() throws Exception {
		CommandResult cr = new CommandResult(false);
		//Execute command
		cr = getShell().executeCommand("syncbyobjecttype --objectType Word");
		Assert.assertNull(cr.getException());
		Assert.assertTrue( cr.isSuccess() );
		List<String> ids = Arrays.asList("ek.n.q_QAFTB_88","do_10096483","do_112178562079178752118","test.ftb_010","do_11225380345346457614","do_11225380550993510416","do_11225380647183155217","do_11225381096194867211","do_11225562396351692815","do_112255733460467712120","do_112255733481611264121","do_112257049614336000119","do_112257049883033600120","do_112257050142777344121","do_112257050369056768122","do_112257683409166336123","do_112257683430334464124","do_112257852917121024153","do_112258573914611712145","do_112260519610449920119");
		List<String> resultDocs = ElasticSearchUtil.getMultiDocumentAsStringByIdList(
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE,
				ids);

		assertNotNull(resultDocs);
		assertEquals(0, resultDocs.size());

	}

	@Test
	public void testSyncNodeByDateRangeIncorrectFormat() throws Exception {
		CommandResult cr = new CommandResult(false);

		//Execute command
		PrintStream err = System.err;
		System.setErr(null);	
		try {
			cr = getShell().executeCommand("syncbydaterange --objectType Content --startDate 2017-05-20 --endDate 2017-05-19");
		}catch (Exception e) {
			Assert.assertEquals("ERR_DATE_RANGE_INCORRECT: End Date should be more than or equal to Start Date", cr.getException());			
		}
		Assert.assertFalse( cr.isSuccess() );
		System.setErr(err);

	}
	
	@Test
	public void testSyncNodeByDateRangeIncorrectDateFormat() throws Exception {
		CommandResult cr = new CommandResult(false);

		//Execute command
		PrintStream err = System.err;
		System.setErr(null);	
		try {
			cr = getShell().executeCommand("syncbydaterange --objectType Content --startDate 2017-may-20 --endDate 2017-may-25");
		}catch (Exception e) {
			Assert.assertEquals("ERR_DATE_FORMAT: DATE Should be in the format of yyyy-MM-dd", cr.getException());			
		}
		Assert.assertFalse( cr.isSuccess() );
		System.setErr(err);

	}

	@Test
	public void testSyncNodeByDateRange() throws Exception {
		CommandResult cr = new CommandResult(false);
		//Execute command
		String lastUpdateOn = "2017-05-24";
		cr = getShell().executeCommand("syncbydaterange --objectType Content --startDate "+lastUpdateOn+" --endDate "+lastUpdateOn);
		Assert.assertNull(cr.getException());
		Assert.assertTrue( cr.isSuccess() );
	}
	
	@Test
	public void testSyncNodeByFile() throws Exception {
		CommandResult cr = new CommandResult(false);
		//Execute command
		String testFilepath = SyncShellCommands.class.getClassLoader().getResource("testFile.csv").getPath();
		cr = getShell().executeCommand("syncbyfile  --filePath "+testFilepath);
		Assert.assertNull(cr.getException());
		Assert.assertTrue( cr.isSuccess() );
		List<String> ids = Arrays.asList("domain_45479","domain_59928","domain_60417","domain_60416","domain_64218","domain_66114","domain_66054","domain_66660","domain_67990","domain_71704","domain_71696","do_20072217","do_20072974","do_20090738","do_20091628","do_20092171","do_20092600","do_20092936","do_10094603","do_112210971276910592140","do_112210971791319040141","do_1122231162122158081270","do_11223461584894361615","do_11223581104224665618","do_112237494329991168117","do_112240998295363584176","do_11224138489488179211","do_11224140303463219212","do_112241625578528768126","do_11224287670964224011","do_112246524249202688192","do_1122470219843502081120","do_112257050142777344121","do_112257050369056768122","do_112257683409166336123","do_112257683430334464124","do_112257852917121024153","do_112258573914611712145","do_112260519610449920119");
		List<String> resultDocs = ElasticSearchUtil.getMultiDocumentAsStringByIdList(
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE,
				ids);

		assertNotNull(resultDocs);
		assertEquals(38, resultDocs.size());
	}
	
	@Test
	public void testSyncNodeByFileWithObjectType() throws Exception {
		CommandResult cr = new CommandResult(false);
		//Execute command
		String testFilepath = SyncShellCommands.class.getClassLoader().getResource("testFile.csv").getPath();
		cr = getShell().executeCommand("syncbyfile  --objectType Content --filePath "+testFilepath);
		Assert.assertNull(cr.getException());
		Assert.assertTrue( cr.isSuccess() );
		List<String> ids = Arrays.asList("domain_45479", "domain_59928", "domain_60417", "domain_60416", "domain_64218",
				"domain_66114", "domain_66054", "domain_66660", "domain_67990", "domain_71704", "domain_71696",
				"do_20072217", "do_20072974", "do_20090738", "do_20091628", "do_20092171", "do_20092600", "do_20092936",
				"do_10094603", "do_112210971276910592140", "do_112210971791319040141", "do_1122231162122158081270",
				"do_11223461584894361615", "do_11223581104224665618", "do_112237494329991168117",
				"do_112240998295363584176", "do_11224138489488179211", "do_11224140303463219212",
				"do_112241625578528768126", "do_11224287670964224011", "do_112246524249202688192",
				"do_1122470219843502081120", "do_112257050142777344121", "do_112257050369056768122",
				"do_112257683409166336123", "do_112257683430334464124", "do_112257852917121024153",
				"do_112258573914611712145", "do_112260519610449920119");
		List<String> resultDocs = ElasticSearchUtil.getMultiDocumentAsStringByIdList(
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE,
				ids);

		assertNotNull(resultDocs);
		assertEquals(31, resultDocs.size());
	}
	private static void createCompositeSearchIndex() throws Exception {
		CompositeSearchConstants.COMPOSITE_SEARCH_INDEX = "testcompositeindex";
		String settings = "{\"analysis\": {       \"analyzer\": {         \"cs_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"cs_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   }";
		String mappings = "{\"dynamic_templates\":[{\"nested\":{\"match_mapping_type\":\"object\",\"mapping\":{\"type\":\"nested\",\"fields\":{\"type\":\"nested\"}}}},{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":true,\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"screenshots\":{\"type\":\"text\",\"index\":false},\"body\":{\"type\":\"text\",\"index\":false},\"appIcon\":{\"type\":\"text\",\"index\":false},\"all_fields\":{\"type\":\"text\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":true,\"analyzer\":\"keylower\"}}}}}";
		ElasticSearchUtil.addIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, settings, mappings);
	}

	private static void deleteCompositeSearchIndex() throws Exception {
		ElasticSearchUtil.deleteIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
	}
	
	private static void importGraphData(String graphId, String fileName) throws Exception {
		Request request = new Request();
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
        request.setOperation("importGraph");
        request.put(GraphEngineParams.format.name(), ImportType.CSV.name());

        InputStream inputStream = SyncCommandTest.class.getClassLoader().getResourceAsStream(fileName);

        request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));

		Future<Object> response = Patterns.ask(reqRouter, request, timeout);
		Object obj = Await.result(response, t.duration());

		Response resp = (Response) obj;
		if (!resp.getParams().getStatus().equalsIgnoreCase(TestParams.successful.name())) {
			System.out.println("file upload failed");
		}
	}
}
