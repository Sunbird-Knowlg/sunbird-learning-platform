package controllers;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.search.router.SearchRequestRouterPool;

import com.fasterxml.jackson.databind.JsonNode;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.common.util.LogTelemetryEventUtil;

import managers.PlaySearchManager;
import play.mvc.Controller;
import play.mvc.Result;

public class SearchController extends Controller {
	
	private static LogHelper LOGGER = LogHelper.getInstance(SearchController.class.getName());
	
	static{
		System.out.println("inside static block");
		SearchRequestRouterPool.init();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	protected ObjectMapper mapper = new ObjectMapper();

	private PlaySearchManager mgr = new PlaySearchManager();
	
	public Result search() {
		 JsonNode data = request().body().asJson();
		 System.out.println(data);
		 Map<String, Object> map = mapper.convertValue(data, Map.class);
		 System.out.println(map);
    		String apiId = "search-api.search";
    		LOGGER.info(apiId + " | Request : " + map);
    		Response  response;
    		int count = 0;
            Request request = getRequest(map);
    		Map<String, Object> requestMap = (Map<String, Object>) map.get("request"); 
    		System.out.println("requestMap" + requestMap);
    		String queryString = (String) requestMap.get(CompositeSearchParams.query.name());
    		Object filters = requestMap.get(CompositeSearchParams.filters.name());
    		Object sort = requestMap.get(CompositeSearchParams.sort_by.name());
    		try {
    		Response searchResponse = mgr.search(request);
//    		if(!checkError(searchResponse)){
    			response = mgr.getSearchResponse(searchResponse);
    			if(null != response.getResult() && !response.getResult().isEmpty()) {
    				count = (Integer) response.getResult().get("count");
    			}
//    		}else {
//    			response = searchResponse;
//    		}
    		}catch(NullPointerException e) {
    			Response res = new Response();
    			res.setResponseCode(ResponseCode.SERVER_ERROR);
    			ResponseParams params = new ResponseParams(); 
    			params.setErrmsg("Not able to process data");
    			params.setStatus("Failed");
    			res.setParams(params);
    			response = res;
    		}
    		String correlationId = UUID.randomUUID().toString();
    		LogTelemetryEventUtil.logContentSearchEvent(queryString, filters, sort, correlationId, count);
    		return ok( response.toString());
    	}
	@SuppressWarnings("unchecked")
    protected Request getRequest(Map<String, Object> requestMap) {
        Request request = new Request();
        if (null != requestMap && !requestMap.isEmpty()) {
            String id = (String) requestMap.get("id");
            String ver = (String) requestMap.get("ver");
            String ts = (String) requestMap.get("ts");
            request.setId(id);
            request.setVer(ver);
            request.setTs(ts);
            Object reqParams = requestMap.get("params");
            if (null != reqParams) {
                try {
                    RequestParams params = (RequestParams) mapper.convertValue(reqParams, RequestParams.class);
                    request.setParams(params);
                } catch (Exception e) {
                }
            }
            Object requestObj = requestMap.get("request");
            if (null != requestObj) {
                try {
                    String strRequest = mapper.writeValueAsString(requestObj);
                    Map<String, Object> map = mapper.readValue(strRequest, Map.class);
                    if (null != map && !map.isEmpty())
                        request.setRequest(map);
                } catch (Exception e) {
                }
            }
        }
        return request;
    }
}
