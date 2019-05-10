import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.common.dto.Response;
import org.ekstep.telemetry.util.TelemetryAccessEventUtil;
import org.ekstep.search.router.SearchRequestRouterPool;
import org.ekstep.telemetry.TelemetryGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		System.setProperty("es.set.netty.runtime.available.processors", "false");
		SearchRequestRouterPool.init();
		TelemetryGenerator.setComponent("search-service");
	}

	@SuppressWarnings("rawtypes")
	public Action onRequest(Request request, Method actionMethod) {
		long startTime = System.currentTimeMillis();
		return new Action.Simple() {
			public Promise<Result> call(Context ctx) throws Throwable {
				Promise<Result> call = delegate.call(ctx);
				call.onRedeem((r) -> {
					try {
						String path = request.uri();
						if (!path.contains("/health")) {
							JsonNode requestData = request.body().asJson();
							org.ekstep.common.dto.Request req = mapper.convertValue(requestData,
									org.ekstep.common.dto.Request.class);

							byte[] body = JavaResultExtractor.getBody(r, 0l);
							Response responseObj = mapper.readValue(body, Response.class);

							Map<String, Object> data = new HashMap<String, Object>();
							data.put("StartTime", startTime);
							data.put("Request", req);
							data.put("Response", responseObj);
							data.put("RemoteAddress", request.remoteAddress());
							data.put("ContentLength", body.length);
							data.put("Status", r.status());
							data.put("Protocol", request.secure() ? "HTTPS" : "HTTP");
							data.put("Method", request.method());
							data.put("X-Session-ID", request.getHeader("X-Session-ID"));
							String consumerId = request.getHeader("X-Consumer-ID");
							data.put("X-Consumer-ID", consumerId);
							String deviceId = request.getHeader("X-Device-ID");
							data.put("X-Device-ID", deviceId);
							if(StringUtils.isNotBlank(deviceId))
								ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.DEVICE_ID.name(),
										deviceId);
							data.put(HeaderParam.APP_ID.name(), request.getHeader("X-App-Id"));

							data.put("X-Authenticated-Userid", request.getHeader("X-Authenticated-Userid"));
							if (StringUtils.isNotBlank(consumerId))
								ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CONSUMER_ID.name(),
										consumerId);
							data.put("env", "search");
							data.put("path", path);
							String channelId = request.getHeader("X-Channel-ID");
							if (StringUtils.isNotBlank(channelId))
								ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CHANNEL_ID.name(),
										channelId);
							else
								ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CHANNEL_ID.name(),
										Platform.config.getString("channel.default"));
							TelemetryAccessEventUtil.writeTelemetryEventLog(data);
							accessLogger.info(request.remoteAddress() + " " + request.host() + " " + request.method()
									+ " " + request.uri() + " " + r.status() + " " + body.length);
						}
					} catch (Exception e) {
						accessLogger.error(e.getMessage());
					}
				});
				return call;
			}
		};
	}
}
