import java.lang.reflect.Method;

import org.ekstep.search.router.SearchRequestRouterPool;

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

	public void onStart(Application app) {
		SearchRequestRouterPool.init();
	}

	@SuppressWarnings("rawtypes")
	public Action onRequest(Request request, Method actionMethod) {
		return new Action.Simple() {
			public Promise<Result> call(Context ctx) throws Throwable {
				Promise<Result> call = delegate.call(ctx);
				call.onRedeem((r) -> {
					byte[] body = JavaResultExtractor.getBody(r, 0l);
					
					accessLogger.info(request.remoteAddress() + " " + request.host()+ " " + request.method() + " " + request.uri() + " " + r.status() + " " + body.length);
				});
				return call;
			}
		};
	}

}
