import com.typesafe.config.Config
import org.ekstep.content.util.JSONUtils
import org.ekstep.common.dto._
import org.ekstep.learning.router.LearningRequestRouterPool
import org.ekstep.telemetry.TelemetryGenerator
import org.ekstep.telemetry.logger.TelemetryManager
import org.ekstep.telemetry.util.TelemetryAccessEventUtil
import play.api._
import play.api.libs.iteratee.Iteratee
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.collection.JavaConverters._


object AccessLoggerFilter extends Filter {

  val accessLogger = Logger("accesslog")

  override def apply(next: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    implicit val ec: ExecutionContext = ExecutionContext.global

    val startTime = System.currentTimeMillis()
    val resultFuture = next(request)

    resultFuture.foreach(result => {
      val bodyStringFuture = result.body.run(Iteratee.fold(Array.empty[Byte]) { (memo, nextChunk) => memo ++ nextChunk }).map(bytes => new String(bytes))

      val body = Await.result(bodyStringFuture, 1 seconds)

      val responseObj: Response = JSONUtils.deserialize[Response](body.toString)
      val headers = request.headers.toMap
      val data: Map[String, Any] = Map(
        "StartTime"-> startTime,
        "Response"-> responseObj,
      "RemoteAddress"-> request.remoteAddress,
        "ContentLength"-> body.size,
        "path"-> request.uri,
        "Status"-> result.header.status,
        "Protocol"-> (if (request.secure) "HTTPS"
        else "HTTP"),
        "Method"-> request.method,
        "X-Session-ID"-> headers.getOrElse("X-Session-ID", ""),
        "X-Consumer-ID"-> headers.getOrElse("X-Consumer-ID",""),
        "X-Device-ID"-> headers.getOrElse("X-Device-ID",""),
        "X-Authenticated-Userid"-> headers.getOrElse("X-Authenticated-Userid","")
      )

      TelemetryAccessEventUtil.writeTelemetryEventLog(data.asInstanceOf[Map[String, AnyRef]].asJava)
      accessLogger.info(request.remoteAddress + " " + request.host + " " + request.method + " " + request.uri + " " + result.header.status + " " + body.length)

    })

    resultFuture
  }
}


object Global extends WithFilters(AccessLoggerFilter) {

  override def beforeStart(app: Application) {
    val config: Config = play.Play.application.configuration.underlying()
    Logger.info("Application has started...")
  }

  override def onStart(app: Application) = {
    //TODO: check name for component
    TelemetryGenerator.setComponent("content-api")
    TelemetryManager.log("Initialising Request Router Pool")
    LearningRequestRouterPool.init()
    //TODO: Enable local cache updater.
    //TelemetryManager.log("Initialising Local Cache Updater")
    //LocalCacheUpdater.init()
  }


  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}