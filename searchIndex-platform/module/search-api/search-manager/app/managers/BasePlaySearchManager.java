package managers;

import akka.actor.ActorRef;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.compositesearch.enums.SearchOperations;
import org.ekstep.search.router.SearchRequestRouterPool;
import org.ekstep.telemetry.logger.TelemetryManager;
import play.libs.F;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Result;
import play.mvc.Results;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static akka.pattern.Patterns.ask;

public class BasePlaySearchManager extends Results {
	protected ObjectMapper mapper = new ObjectMapper();
	private static final Logger perfLogger = LogManager.getLogger("PerformanceTestLogger");
	private static Boolean contentTaggingFlag = Platform.config.hasPath("content.tagging.backward_enable")?
			Platform.config.getBoolean("content.tagging.backward_enable"): false;
	private static List <String> contentTaggedKeys = Platform.config.hasPath("content.tagging.property") ?
			Platform.config.getStringList("content.tagging.property"):
			new ArrayList<>(Arrays.asList("subject","medium"));
	private static final String JSON_TYPE = "application/json";

	protected Promise<Result> getSearchResponse(Request request) {
		ActorRef router = SearchRequestRouterPool.getRequestRouter();
		Promise<Result> res = null;
		try {
			long startTime = System.currentTimeMillis();
			request.getContext().put("start_time", startTime);
			perfLogger.info(request.getContext().get("scenario_name") + ","
					+ request.getContext().get("request_id") + "," + request.getManagerName()
					+ "," + request.getOperation() + ",STARTTIME," + startTime);
			res = Promise.wrap(ask(router, request, SearchRequestRouterPool.REQ_TIMEOUT))
					.map(new Function<Object, Result>() {
						public Result apply(Object result) {
							String correlationId = UUID.randomUUID().toString();
							if (result instanceof Response) {
								Response response = (Response) result;
								if (checkError(response)) {
									return getErrorResult(response);
								} else if (request.getOperation()
										.equalsIgnoreCase(SearchOperations.INDEX_SEARCH.name())) {
									Promise<Result> searchResult = getSearchResponse(response, request);
									int count = (response.getResult() == null ? 0
											: (Integer) response.getResult().get("count"));
									writeTelemetryLog(request, response);
									return searchResult.get(SearchRequestRouterPool.REQ_TIMEOUT);
								}
								return ok(getResult(response, request, null, correlationId)).as(JSON_TYPE);
							}
							ResponseParams params = new ResponseParams();
							params.setErrmsg("Invalid Response object");
							Response error = new Response();
							error.setParams(params);
							return ok(getResult(error, request, null, correlationId)).as(JSON_TYPE);
						}
					});
			res.onRedeem(new F.Callback<Result>() {
				@Override
				public void invoke(Result result) throws Throwable {
					long endTime = System.currentTimeMillis();
					long exeTime = endTime - (Long) request.getContext().get("start_time");
					perfLogger.info(request.getManagerName() + "," + request.getOperation() + ",ENDTIME," + endTime);
					perfLogger.info(request.getManagerName() + "," + request.getOperation() + "," + result.status()
							+ "," + exeTime);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private String getUUID() {
		UUID uid = UUID.randomUUID();
		return uid.toString();
	}

	protected Request setSearchContext(Request request, String manager, String operation) {
		request.setManagerName(manager);
		request.setOperation(operation);
		return request;
	}

	protected Request getSearchRequest(String manager, String operation) {
		Request request = new Request();
		return setSearchContext(request, manager, operation);
	}

	private String getResult(Response response, Request req, String msgId, String resMsgId) {
		if (req == null) {
			ResponseParams params = new ResponseParams();
			params.setErrmsg("Null Content");
			Response error = new Response();
			error.setParams(params);
			response = error;
		}
		return getResult(response, req.getId(), req.getVer(), msgId, resMsgId);
	}

	@SuppressWarnings("unchecked")
	public String getResult(Response response, String apiId, String version, String msgId, String resMsgId) {
		try {
			if (response == null) {
				ResponseParams params = new ResponseParams();
				params.setErrmsg("Null Content");
				Response error = new Response();
				error.setParams(params);
				response = error;
			}
			response.setId(apiId);
			response.setVer(version);
			response.setTs(getResponseTimestamp());
			ResponseParams params = response.getParams();
			if (null == params)
				params = new ResponseParams();
			if (StringUtils.isNotBlank(msgId))
				params.setMsgid(msgId);
			if (StringUtils.isNotBlank(resMsgId))
				params.setResmsgid(resMsgId);
			else
				params.setResmsgid(getUUID());
			if (StringUtils.equalsIgnoreCase(ResponseParams.StatusType.successful.name(), params.getStatus())) {
				params.setErr(null);
				params.setErrmsg(null);
			}
			response.setParams(params);
			if(response.getResult().containsKey("content")) {
				List<Map<String,Object>> contentMap = (List<Map<String, Object>>) response.getResult().get("content");
				for(Map<String,Object> content : contentMap){
					if(content.containsKey("variants")){
						Map<String,Object> variantsMap = (Map<String,Object>) mapper.readValue((String) content.get("variants"), Map.class);
						content.put("variants",variantsMap);
						contentMap.set(contentMap.indexOf(content), content);
					}
                    updateContentTaggedProperty(content);
                    contentMap.set(contentMap.indexOf(content), content);
				}
				response.getResult().put("content", contentMap);
			}
			if(response.getResult().containsKey("collections")) {
				List<Map<String,Object>> collectionList = (List<Map<String, Object>>) response.getResult().get("collections");
				for(Map<String,Object> collection : collectionList){
					updateContentTaggedProperty(collection);
					collectionList.set(collectionList.indexOf(collection), collection);
				}
				response.getResult().put("collections", collectionList);
			}
			return mapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private void updateContentTaggedProperty(Map<String,Object> content) {
		if(contentTaggingFlag) {
			for(String contentTagKey : contentTaggedKeys) {
				if(content.containsKey(contentTagKey)) {
					List<String> prop = null;
					try {
					    Object value = content.get(contentTagKey);
					    if (value instanceof String[]) {
					        prop = Arrays.asList((String[]) value);
                        } else if(value instanceof  List) {
							prop = (List<String>) value;
						}else {
							prop = mapper.readValue((String) value, List.class);
						}
					    if (CollectionUtils.isNotEmpty(prop))
							content.put(contentTagKey, prop.get(0));
					} catch (IOException e) {
						content.put(contentTagKey, (String) content.get(contentTagKey));
					}

				}
			}
		}
	}

	private String getErrorMsg(String errorMsg) {
		try {
			Response response = new Response();
			ResponseParams params = new ResponseParams();
			params.setErr(CompositeSearchErrorCodes.SYSTEM_ERROR.name());
			params.setErrmsg(errorMsg);
			response.setResponseCode(ResponseCode.SERVER_ERROR);
			response.setParams(params);
			return mapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "";
	}

	protected boolean checkError(Response response) {
		ResponseParams params = response.getParams();
		if (null != params) {
			if (StringUtils.equals(StatusType.failed.name(), params.getStatus())) {
				return true;
			}
		}
		return false;
	}

	private String getResponseTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'XXX");
		return sdf.format(new Date());
	}

	protected void writeTelemetryLog(Request request, Response response) {
		Map<String,Object> context=request.getContext();
		String query = StringUtils.isBlank((String) request.get(CompositeSearchParams.query.name())) ? ""
				: (String) request.get(CompositeSearchParams.query.name());
		Map<String, Object> filters = (null != request.get(CompositeSearchParams.filters.name()))
				? (Map<String, Object>) request.get(CompositeSearchParams.filters.name()) : new HashMap();
		Object sort = (null != request.get(CompositeSearchParams.sort_by.name()))
				? request.get(CompositeSearchParams.sort_by.name()) : new HashMap();
		int count = getCount(filters, response);
		Object topN = getTopNResult(response.getResult());
		String type = getType(filters);
		populateTargetDialObject(context, filters);
		TelemetryManager.search(context, query, filters, sort, count, topN, type);
	}

	@SuppressWarnings("unchecked")
	private String getType(Map<String, Object> filters) {
		if (null != filters.get("objectType")) {
			List<String> objectType = (List<String>) filters.get("objectType");
			if (objectType.size() <= 2) {
				return objectType.get(0).toLowerCase();
			} else {
				return "all";
			}
		} else {
			return "all";
		}
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getTopNResult(Map<String, Object> result) {
		List<Map<String, Object>> contentList = (List<Map<String, Object>>) result.get("results");
		if (null == contentList || contentList.isEmpty()) {
			return new ArrayList<>();
		}
		Integer topN = Platform.config.hasPath("telemetry.search.topn")
				? Platform.config.getInt("telemetry.search.topn") : 5;
		List<Map<String, Object>> list = new ArrayList<>();
		if (topN < contentList.size()) {
			for (int i = 0; i < topN; i++) {
				Map<String, Object> m = new HashMap<>();
				m.put("identifier", contentList.get(i).get("identifier"));
				list.add(m);
			}
		} else {
			for (int i = 0; i < contentList.size(); i++) {
				Map<String, Object> m = new HashMap<>();
				m.put("identifier", contentList.get(i).get("identifier"));
				list.add(m);
			}
		}
		return list;
	}

	public Promise<Result> getSearchResponse(Response searchResult, Request req) {
		Request request = getSearchRequest(SearchActorNames.SEARCH_MANAGER.name(),
				SearchOperations.GROUP_SEARCH_RESULT_BY_OBJECTTYPE.name());
		request.put("searchResult", searchResult.getResult());
		request.setId(req.getId());
		request.setVer(req.getVer());
		Promise<Result> getRes = getSearchResponse(request);
		return getRes;
	}

	protected String getMessage(Response res) {
		if (res.getParams() != null) {
			return res.getParams().getErrmsg();
		} else {
			return "Something went wrong in server while processing the request";
		}
		
	}

	/**
	 * This method populates Target Object field for DIAL Scan.
	 * @param context
	 * @param filters
	 */
	private void populateTargetDialObject(Map<String, Object> context, Map<String, Object> filters) {
		if (MapUtils.isNotEmpty(filters) && null != filters.get("dialcodes")) {
			List<String> dialcodes = getList(filters.get("dialcodes"));
			if (dialcodes.size() == 1) {
				context.put("objectId", dialcodes.get(0));
				context.put("objectType", "DialCode");
			}
		}
	}

	/**
	 * @param param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected static List<String> getList(Object param) {
		List<String> paramList = null;
		try {
			paramList = (List<String>) param;
		} catch (Exception e) {
			String str = (String) param;
			paramList = Arrays.asList(str);
		}
		if (null != paramList) {
			paramList = paramList.stream().filter(x -> StringUtils.isNotBlank(x) && !StringUtils.equals(" ", x)).collect(Collectors.toList());
		}
		return paramList;
	}

	/**
	 * This Method Checks Whether Search Request is from DIAL Scan Or Not.
	 * @param filters
	 * @return Boolean
	 */
	private static Boolean isDIALScan(Map<String, Object> filters) {
		if (MapUtils.isNotEmpty(filters) && null != filters.get("dialcodes")) {
			List<String> dialcodes = getList(filters.get("dialcodes"));
			return (dialcodes.size() == 1) ? true : false;
		}
		return false;
	}

	/**
	 * This Method Returns Total Number of Content Found in Search
	 * @param filters
	 * @param response
	 * @return
	 */
	private static Integer getCount(Map<String, Object> filters, Response response) {
		Integer count = 0;
		List<Map<String, Object>> contents = (List<Map<String, Object>>) response.getResult().get("results");
		if (null == contents || contents.isEmpty()) {
			return count;
		}
		if (isDIALScan(filters)) {
			try {
				List<Integer> counts = contents.stream().filter(content -> (null != content.get("leafNodesCount")))
						.map(content -> ((Number) content.get("leafNodesCount")).intValue()).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(counts))
					count = Collections.max(counts);
			} catch (Exception e) {
				TelemetryManager.error("Error while getting leaf node count for dial scan : ", e);
				throw e;
			}
		} else {
			count = (Integer) response.getResult().get("count");
		}

		return count;
	}

	private Result getErrorResult(Response response) {
		try {
			if (response.getResponseCode().compareTo(ResponseCode.CLIENT_ERROR) == 0) {
				return badRequest(mapper.writeValueAsString(response)).as(JSON_TYPE);
			} else if (response.getResponseCode().compareTo(ResponseCode.RESOURCE_NOT_FOUND) == 0) {
				return notFound(mapper.writeValueAsString(response)).as(JSON_TYPE);
			} else {
				return internalServerError(mapper.writeValueAsString(response)).as(JSON_TYPE);
			}
		} catch (JsonProcessingException e) {
			TelemetryManager.error("Error occurred while handling error response: ", e);
			return null;
		}
	}

}
