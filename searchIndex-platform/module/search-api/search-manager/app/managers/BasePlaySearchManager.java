package managers;

import static akka.pattern.Patterns.ask;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.compositesearch.enums.CompositeSearchErrorCodes;
import org.ekstep.compositesearch.enums.CompositeSearchParams;
import org.ekstep.compositesearch.enums.SearchActorNames;
import org.ekstep.compositesearch.enums.SearchOperations;
import org.ekstep.search.router.SearchRequestRouterPool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.util.LogTelemetryEventUtil;
import com.ilimi.graph.common.enums.GraphHeaderParams;

import akka.actor.ActorRef;
import play.libs.F;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.mvc.Result;
import play.mvc.Results;

public class BasePlaySearchManager extends Results {

	protected ObjectMapper mapper = new ObjectMapper();
	private static final Logger perfLogger = LogManager.getLogger("PerformanceTestLogger");
	private static Logger LOGGER = LogManager.getLogger(BasePlaySearchManager.class.getName());

	protected Promise<Result> getSearchResponse(Request request, Logger logger) {
		ActorRef router = SearchRequestRouterPool.getRequestRouter();
		Promise<Result> res = null;
		try {
			long startTime = System.currentTimeMillis();
			request.getContext().put(GraphHeaderParams.start_time.name(), startTime);
			perfLogger.info(request.getContext().get(GraphHeaderParams.scenario_name.name()) + ","
					+ request.getContext().get(GraphHeaderParams.request_id.name()) + "," + request.getManagerName()
					+ "," + request.getOperation() + ",STARTTIME," + startTime);
			res = Promise.wrap(ask(router, request, SearchRequestRouterPool.REQ_TIMEOUT))
					.map(new Function<Object, Result>() {
						public Result apply(Object result) {
							if (result instanceof Response) {
								Response response = (Response) result;
								if (checkError(response)) {
									String errMsg = (response.getParams() == null ? "System Error"
											: response.getParams().getErrmsg());
									return notFound(getErrorMsg(errMsg)).as("application/json");
								} else if (request.getOperation()
										.equalsIgnoreCase(SearchOperations.INDEX_SEARCH.name())) {
									Promise<Result> searchResult = getSearchResponse(response,request);
									int count = (response.getResult() == null ? 0
											: (Integer) response.getResult().get("count"));
									writeTelemetryLog(request, null, count);
									return searchResult.get(SearchRequestRouterPool.REQ_TIMEOUT);
								}
								return ok(getResult(response, request)).as("application/json");
							}
							ResponseParams params = new ResponseParams();
							params.setErrmsg("Invalid Response object");
							Response error = new Response();
							error.setParams(params);
							return ok(getResult(error, request)).as("application/json");
						}
					});
			res.onRedeem(new F.Callback<Result>() {
				@Override
				public void invoke(Result result) throws Throwable {
					if (request.getOperation().equalsIgnoreCase(SearchOperations.INDEX_SEARCH.name())) {
					}
					long endTime = System.currentTimeMillis();
					long exeTime = endTime - (Long) request.getContext().get(GraphHeaderParams.start_time.name());
					perfLogger.info(request.getContext().get(GraphHeaderParams.scenario_name.name()) + ","
							+ request.getContext().get(GraphHeaderParams.request_id.name()) + ","
							+ request.getManagerName() + "," + request.getOperation() + ",ENDTIME," + endTime);
					perfLogger.info(request.getContext().get(GraphHeaderParams.scenario_name.name()) + ","
							+ request.getContext().get(GraphHeaderParams.request_id.name()) + ","
							+ request.getManagerName() + "," + request.getOperation() + "," + result.status() + ","
							+ exeTime);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
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

	private String getResult(Response response, Request req) {
		if (req == null) {
			ResponseParams params = new ResponseParams();
			params.setErrmsg("Null Content");
			Response error = new Response();
			error.setParams(params);
			response = error;
		}
		return getResult(response, req.getId(), req.getVer());
	}

	public String getResult(Response response, String apiId, String version) {
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
			if (StringUtils.equalsIgnoreCase(ResponseParams.StatusType.successful.name(), params.getStatus())) {
				params.setErr(null);
				params.setErrmsg(null);
			}
			response.setParams(params);
			return mapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "";
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

	protected void writeTelemetryLog(Request request, String correlationId, Integer count) {
		String queryString = (String) request.get(CompositeSearchParams.query.name());
		Object filters = request.get(CompositeSearchParams.filters.name());
		Object sort = request.get(CompositeSearchParams.sort_by.name());
		LogTelemetryEventUtil.logContentSearchEvent(queryString, filters, sort, correlationId, count);
	}

	public Promise<Result> getSearchResponse(Response searchResult, Request req) {
		Request request = getSearchRequest(SearchActorNames.SEARCH_MANAGER.name(),
				SearchOperations.GROUP_SEARCH_RESULT_BY_OBJECTTYPE.name());
		request.put("searchResult", searchResult.getResult());
		request.setId(req.getId());
		request.setVer(req.getVer());
		Promise<Result> getRes = getSearchResponse(request, LOGGER);
		return getRes;
	}
}
