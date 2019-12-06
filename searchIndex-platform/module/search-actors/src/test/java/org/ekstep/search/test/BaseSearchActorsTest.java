package org.ekstep.search.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.compositesearch.enums.SearchOperations;
import org.ekstep.search.router.SearchRequestRouterPool;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * @author rayulu
 *
 */
public class BaseSearchActorsTest {
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	public static void beforeTest() throws Exception {
		SearchRequestRouterPool.init();
		createCompositeSearchIndex();
		Thread.sleep(3000);
	}
	
	@AfterClass
	public static void afterTest() throws Exception {
		System.out.println("deleting index: " + CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
		ElasticSearchUtil.deleteIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
	}
	
//	protected Response jsonToObject(ResultActions actions) {
//		String content = null;
//		Response resp = null;
//		try {
//			content = actions.andReturn().getResponse().getContentAsString();
//			ObjectMapper objectMapper = new ObjectMapper();
//			if (StringUtils.isNotBlank(content))
//				resp = objectMapper.readValue(content, Response.class);
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return resp;
//	}
	
	protected Request getSearchRequest() {
        Request request = new Request();
        return setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.INDEX_SEARCH.name());
    }
	
	protected Request getCountRequest() {
        Request request = new Request();
        return setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.COUNT.name());
    }
	
	protected Request getMetricsRequest() {
        Request request = new Request();
        return setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.METRICS.name());
    }
	
	protected Request getGroupSearchResultsRequest() {
        Request request = new Request();
        return setSearchContext(request, SearchActorNames.SEARCH_MANAGER.name() ,SearchOperations.GROUP_SEARCH_RESULT_BY_OBJECTTYPE.name());
    }
	
	protected Request setSearchContext(Request request, String manager, String operation) {
        request.setManagerName(manager);
        request.setOperation(operation);
        return request;
    }
	
	protected Response getSearchResponse(Request request) {
        ActorRef router = SearchRequestRouterPool.getRequestRouter();
        try {
            Future<Object> future = Patterns.ask(router, request, SearchRequestRouterPool.REQ_TIMEOUT);
            Object obj = Await.result(future, SearchRequestRouterPool.WAIT_TIMEOUT.duration());
            if (obj instanceof Response) {
                return (Response) obj;
            } else {
                return ERROR(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
            }
        } catch (Exception e) {
            throw new ServerException(CompositeSearchErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }
	
	protected Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
        Response response = new Response();
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(responseCode);
        return response;
    }
	
	private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
        ResponseParams params = new ResponseParams();
        params.setErr(errorCode);
        params.setStatus(StatusType.failed.name());
        params.setErrmsg(errorMessage);
        return params;
    }
	
	private static void createCompositeSearchIndex() throws Exception {
		CompositeSearchConstants.COMPOSITE_SEARCH_INDEX = "testcompositeindex";
		ElasticSearchUtil.initialiseESClient(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				Platform.config.getString("search.es_conn_info"));
		System.out.println("creating index: " + CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
		String settings = "{\"analysis\": {       \"analyzer\": {         \"cs_index_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"lowercase\",             \"mynGram\"           ]         },         \"cs_search_analyzer\": {           \"type\": \"custom\",           \"tokenizer\": \"standard\",           \"filter\": [             \"standard\",             \"lowercase\"           ]         },         \"keylower\": {           \"tokenizer\": \"keyword\",           \"filter\": \"lowercase\"         }       },       \"filter\": {         \"mynGram\": {           \"type\": \"nGram\",           \"min_gram\": 1,           \"max_gram\": 20,           \"token_chars\": [             \"letter\",             \"digit\",             \"whitespace\",             \"punctuation\",             \"symbol\"           ]         }       }     }   }";
		String mappings = "{\"dynamic_templates\":[{\"longs\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}}}},{\"booleans\":{\"match_mapping_type\":\"boolean\",\"mapping\":{\"type\":\"boolean\",\"fields\":{\"raw\":{\"type\":\"boolean\"}}}}},{\"doubles\":{\"match_mapping_type\":\"double\",\"mapping\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}}}},{\"dates\":{\"match_mapping_type\":\"date\",\"mapping\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}}}},{\"strings\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"text\",\"copy_to\":\"all_fields\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":\"true\",\"analyzer\":\"keylower\"}}}}}],\"properties\":{\"all_fields\":{\"type\":\"text\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\",\"fields\":{\"raw\":{\"type\":\"text\",\"fielddata\":\"true\",\"analyzer\":\"keylower\"}}}}}";
		ElasticSearchUtil.addIndex(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, settings, mappings);
		insertTestRecords();
	}
	
	private static void insertTestRecords() throws Exception {
		for (int i=1; i<=30; i++) {
			Map<String, Object> content = getContentTestRecord(null, i, null);
			String id = (String) content.get("identifier");
			addToIndex(id, content);
		}
		Map<String, Object> content = getContentTestRecord("do_10000031", 31, null);
		content.put("name", "31 check name match");
		content.put("description", "हिन्दी description");
		addToIndex("do_10000031", content);
		
		content = getContentTestRecord("do_10000032", 32, null);
		content.put("name", "check ends with value32");
		addToIndex("do_10000032", content);

		content = getContentTestRecord("do_10000033", 33, "test-board1");
		content.put("name", "Content To Test Consumption");
		addToIndex("10000033", content);

		content = getContentTestRecord("do_10000034", 34, "test-board3");
		content.put("name", "Textbook-10000034");
		content.put("description", "Textbook for other tenant");
		content.put("status","Live");
		content.put("relatedBoards", new ArrayList<String>(){{
			add("test-board1");
			add("test-board2");
		}});
		addToIndex("10000034", content);
	}
	
	private static void addToIndex(String uniqueId, Map<String, Object> doc) throws Exception {
		String jsonIndexDocument = mapper.writeValueAsString(doc);
		ElasticSearchUtil.addDocumentWithId(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, uniqueId, jsonIndexDocument);
	}
	
	private static Map<String, Object> getContentTestRecord(String id, int index, String board) {
		String objectType = "Content";
		Date d = new Date();
		Map<String, Object> map = getTestRecord(id, index, "do", objectType);
		map.put("name", "Content_" + System.currentTimeMillis() + "_name");
		map.put("code", "code_" + System.currentTimeMillis());
		map.put("contentType", getContentType());
		map.put("createdOn", new Date().toString());
		map.put("lastUpdatedOn", new Date().toString());
		if(StringUtils.isNotBlank(board))
			map.put("board",board);
		if (index % 5 == 0) {
			map.put("lastPublishedOn", d.toString());
			map.put("status", "Live");
			map.put("size", 1000432);
		} else {
			map.put("status", "Draft");
			if (index % 3 == 0) 
				map.put("size", 564738);
		}
		Set<String> tagList = getTags();
		if (null != tagList && !tagList.isEmpty() && index % 7 != 0)
			map.put("tags", tagList);
		map.put("downloads", index);
		return map;
	}
	
	private static Map<String, Object> getTestRecord(String id, int index, String prefix, String objectType) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(id))
			map.put("identifier", id);
		else {
			long suffix = 10000000 + index;
			map.put("identifier", prefix + "_" + suffix);
		}
		map.put("objectType", objectType);
		return map;
	}
	
	private static String[] contentTypes = new String[]{"Story", "Worksheet", "Game", "Collection", "Asset"};
	private static String getContentType() {
		return contentTypes[RandomUtils.nextInt(5)];
	}
	
	private static String[] tags = new String[]{"hindi story", "NCERT", "Pratham", "एकस्टेप", "हिन्दी", "हाथी और भालू", "worksheet", "test"};
	private static Set<String> getTags() {
		Set<String> list = new HashSet<String>();
		int count = RandomUtils.nextInt(9);
		for (int i=0; i<count; i++) {
			list.add(tags[RandomUtils.nextInt(8)]);
		}
		return list;
	}

}
