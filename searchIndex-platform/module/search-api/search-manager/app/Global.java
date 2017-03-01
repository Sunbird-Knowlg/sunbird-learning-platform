import java.lang.reflect.Method;

import org.ekstep.search.router.SearchRequestRouterPool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.TelemetryBEAccessEvent;
import com.ilimi.common.util.LogTelemetryEventUtil;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Logger.ALogger;
import play.core.j.JavaResultExtractor;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Result;

public class Global extends GlobalSettings {

	private static final ALogger accessLogger = Logger.of("accesslog");
	private static ObjectMapper mapper = new ObjectMapper();

	public void onStart(Application app) {
		SearchRequestRouterPool.init();
	}

	@SuppressWarnings("rawtypes")
	public Action onRequest(Request request, Method actionMethod) {
		long startTime = System.currentTimeMillis();
		return new Action.Simple() {
			public Promise<Result> call(Context ctx) throws Throwable {
				Promise<Result> call = delegate.call(ctx);
				call.onRedeem((r) -> {
					try {
						long timeDuration = System.currentTimeMillis() - startTime;
						JsonNode data = request.body().asJson();
						com.ilimi.common.dto.Request req = mapper.convertValue(data,
								com.ilimi.common.dto.Request.class);
						byte[] body = JavaResultExtractor.getBody(r, 0l);
						Response response = mapper.readValue(body, Response.class);
						TelemetryBEAccessEvent accessData = new TelemetryBEAccessEvent();
						accessData.setRid(response.getId());
						accessData.setUip(request.remoteAddress());
						accessData.setType("api");
						accessData.setSize(body.length);
						accessData.setDuration(timeDuration);
						accessData.setStatus(r.status());
						accessData.setProtocol(request.secure() ? "HTTPS" : "HTTP");
						accessData.setMethod(request.method());
						RequestParams parameterMap = null;
						if (null != req && null != req.getParams()) {
							parameterMap = req.getParams();
						}
						if (null != request.getHeader("X-Session-ID")) {
							parameterMap = new RequestParams();
							if (null == parameterMap.getSid())
								parameterMap.setSid(request.getHeader("X-Session-ID"));
						}
						accessData.setParams(parameterMap);
						LogTelemetryEventUtil.logAccessEvent(accessData);
						accessLogger.info(request.remoteAddress() + " " + request.host() + " " + request.method() + " "
								+ request.uri() + " " + r.status() + " " + body.length);
					} catch (Exception e) {
						accessLogger.error(e.getMessage());
					}
				});
				return call;
			}
		};
	}
}
