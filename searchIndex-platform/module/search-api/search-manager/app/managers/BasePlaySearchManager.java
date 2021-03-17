package managers;

import akka.actor.ActorRef;
import akka.dispatch.ExecutionContexts;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.util.Timeout;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.dto.ResponseParams.StatusType;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.compositesearch.enums.CompositeSearchErrorCodes;
import org.sunbird.compositesearch.enums.CompositeSearchParams;
import org.sunbird.compositesearch.enums.SearchActorNames;
import org.sunbird.compositesearch.enums.SearchOperations;
import org.sunbird.telemetry.logger.TelemetryManager;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static akka.pattern.Patterns.ask;
import java.io.IOException;

public class BasePlaySearchManager {
	protected ObjectMapper mapper = new ObjectMapper();
	private static final Logger perfLogger = LogManager.getLogger("PerformanceTestLogger");
	private static final String JSON_TYPE = "application/json";
	private static Timeout WAIT_TIMEOUT = new Timeout(Duration.create(30000, TimeUnit.MILLISECONDS));
	private static Boolean contentTaggingFlag = Platform.config.hasPath("content.tagging.backward_enable")?
			Platform.config.getBoolean("content.tagging.backward_enable"): false;
	private static List <String> contentTaggedKeys = Platform.config.hasPath("content.tagging.property") ?
			Platform.config.getStringList("content.tagging.property"):
			new ArrayList<>(Arrays.asList("subject","medium"));

	protected Future<Response> getSearchResponse(Request request, ActorRef actor) {
		Future<Response> res = null;
		try {
			long startTime = System.currentTimeMillis();
			request.getContext().put("start_time", startTime);
			perfLogger.info(request.getContext().get("scenario_name") + ","
					+ request.getContext().get("request_id") + "," + request.getManagerName()
					+ "," + request.getOperation() + ",STARTTIME," + startTime);
			res = ask(actor, request, WAIT_TIMEOUT)
					.map(new Mapper<Object, Future<Response>>() {
						public Future<Response> apply(Object result) {
							String correlationId = UUID.randomUUID().toString();
							if (result instanceof Response) {
								Response response = (Response) result;
								if (checkError(response)) {
									return Futures.successful(response);
								} else if (request.getOperation()
										.equalsIgnoreCase(SearchOperations.INDEX_SEARCH.name())) {
									Future<Response> searchResult = getSearchResponse(response, request, actor);
									writeTelemetryLog(request, response);
									return searchResult;
								}
								return Futures.successful(getResult(response, request, null, correlationId));
							}
							ResponseParams params = new ResponseParams();
							params.setErrmsg("Invalid Response object");
							Response error = new Response();
							error.setParams(params);
							return Futures.successful(getResult(error, request, null, correlationId));
						}
					}, ExecutionContexts.global()).flatMap(new Mapper<Future<Response>, Future<Response>>() {
						@Override
						public Future<Response> apply(Future<Response> parameter) {
							return parameter;
						}
					}, ExecutionContexts.global());
//			res.onRedeem(new F.Callback<Result>() {
//				@Override
//				public void invoke(Result result) throws Throwable {
//					long endTime = System.currentTimeMillis();
//					long exeTime = endTime - (Long) request.getContext().get("start_time");
//					perfLogger.info(request.getManagerName() + "," + request.getOperation() + ",ENDTIME," + endTime);
//					perfLogger.info(request.getManagerName() + "," + request.getOperation() + "," + result.status()
//							+ "," + exeTime);
//				}
//			});
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

	private Response getResult(Response response, Request req, String msgId, String resMsgId) {
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
	public Response getResult(Response response, String apiId, String version, String msgId, String resMsgId) {
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
			return response;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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

	public Future<Response> getSearchResponse(Response searchResult, Request req, ActorRef actor) {
		Request request = getSearchRequest(SearchActorNames.SEARCH_MANAGER.name(),
				SearchOperations.GROUP_SEARCH_RESULT_BY_OBJECTTYPE.name());
		request.put("searchResult", searchResult.getResult());
		request.setId(req.getId());
		request.setVer(req.getVer());
		Future<Response> getRes = getSearchResponse(request, actor);
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

}
