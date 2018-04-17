package org.ekstep.sync.tool.shell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.common.enums.GraphEngineParams;
import org.ekstep.graph.common.enums.GraphHeaderParams;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.enums.ImportType;
import org.ekstep.graph.importer.InputStreamValue;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.shell.core.CommandResult;

import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;


public class SyncCommandTest extends SpringShellTest{

	private static ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
	
	@BeforeClass
	public static void init() throws Exception {
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
		System.setErr(err);	
		Assert.assertFalse( cr.isSuccess() );
	}

	@Test
	public void testNodeById() throws Exception {
		CommandResult cr = new CommandResult(false);
		//Execute command
		cr = getShell().executeCommand("syncbyids --ids do_112178562079178752118");
		Assert.assertNull(cr.getException());
		Assert.assertTrue( cr.isSuccess() );
		String doc = elasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, "do_112178562079178752118");
		assertTrue(StringUtils.contains(doc, "do_112178562079178752118"));
		//Execute command
		cr = getShell().executeCommand("syncbyids --ids domain_48763,domain_14658,do_112178562079178752118");
		Assert.assertNull(cr.getException());
		Assert.assertTrue( cr.isSuccess() );
		List<String> resultDocs = elasticSearchUtil.getMultiDocumentAsStringByIdList(
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE,
				Arrays.asList("domain_48763","domain_14658","do_112178562079178752118"));

		assertNotNull(resultDocs);
		assertEquals(3, resultDocs.size());
	}
	
	@Test
	public void testNodeByObjectType() throws Exception {
		CommandResult cr = new CommandResult(false);
		//Execute command
		cr = getShell().executeCommand("syncbyobjecttype --objectType AssessmentItem");
		Assert.assertNull(cr.getException());
		Assert.assertTrue( cr.isSuccess() );
		List<String> ids = Arrays.asList("ek.n.q_QAFTB_88","do_10096483","do_112178562079178752118","test.ftb_010","do_11225380345346457614","do_11225380550993510416","do_11225380647183155217","do_11225381096194867211","do_11225562396351692815","do_112255733460467712120","do_112255733481611264121","do_112257049614336000119","do_112257049883033600120","do_112257050142777344121","do_112257050369056768122","do_112257683409166336123","do_112257683430334464124","do_112257852917121024153","do_112258573914611712145","do_112260519610449920119");
		List<String> resultDocs = elasticSearchUtil.getMultiDocumentAsStringByIdList(
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE,
				ids);

		assertNotNull(resultDocs);
		assertEquals(20, resultDocs.size());

	}
	
	@Test
	public void testNodeByDateRangeIncorrectFormat() throws Exception {
		CommandResult cr = new CommandResult(false);

		//Execute command
		PrintStream err = System.err;
		System.setErr(null);	
		try {
			cr = getShell().executeCommand("syncbydaterange --objectType Content --startDate 2017-05-20 --endDate 2017-05-31");
		}catch (Exception e) {
			Assert.assertEquals("ERR_DATE_RANGE_INCORRECT: End Date should be more than or equal to Start Date", cr.getException());			
		}
		Assert.assertFalse( cr.isSuccess() );
		System.setErr(err);

	}
	
	@Test
	public void testNodeByDateRange() throws Exception {
		CommandResult cr = new CommandResult(false);
		//Execute command
		Date date = new Date();
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
		String today = inputFormat.format(date);
		cr = getShell().executeCommand("syncbydaterange --objectType Content --startDate "+today+" --endDate "+today);
		Assert.assertNull(cr.getException());
		Assert.assertTrue( cr.isSuccess() );
	}
	
	private static void createCompositeSearchIndex() throws Exception {
		CompositeSearchConstants.COMPOSITE_SEARCH_INDEX = "testcompositeindex";
		String settings = "{\"analysis\": {       \"analyzer\": {         \"cs_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"cs_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   }";
		String mappings = "{    \"dynamic_templates\": [      {        \"longs\": {          \"match_mapping_type\": \"long\",          \"mapping\": {            \"type\": \"long\",            fields: {              \"raw\": {                \"type\": \"long\"              }            }          }        }      },      {        \"booleans\": {          \"match_mapping_type\": \"boolean\",          \"mapping\": {            \"type\": \"boolean\",            fields: {              \"raw\": {                \"type\": \"boolean\"              }            }          }        }      },{        \"doubles\": {          \"match_mapping_type\": \"double\",          \"mapping\": {            \"type\": \"double\",            fields: {              \"raw\": {                \"type\": \"double\"              }            }          }        }      },	  {        \"dates\": {          \"match_mapping_type\": \"date\",          \"mapping\": {            \"type\": \"date\",            fields: {              \"raw\": {                \"type\": \"date\"              }            }          }        }      },      {        \"strings\": {          \"match_mapping_type\": \"string\",          \"mapping\": {            \"type\": \"string\",            \"copy_to\": \"all_fields\",            \"analyzer\": \"cs_index_analyzer\",            \"search_analyzer\": \"cs_search_analyzer\",            fields: {              \"raw\": {                \"type\": \"string\",                \"analyzer\": \"keylower\"              }            }          }        }      }    ],    \"properties\": {      \"all_fields\": {        \"type\": \"string\",        \"analyzer\": \"cs_index_analyzer\",        \"search_analyzer\": \"cs_search_analyzer\",        fields: {          \"raw\": {            \"type\": \"string\",            \"analyzer\": \"keylower\"          }        }      }    }  }";
		elasticSearchUtil.addIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, settings, mappings);
	}

	private static void deleteCompositeSearchIndex() throws Exception {
		elasticSearchUtil.deleteIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
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
