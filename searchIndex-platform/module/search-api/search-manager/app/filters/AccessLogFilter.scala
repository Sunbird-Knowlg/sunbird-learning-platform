package filters

import akka.util.ByteString
import javax.inject.Inject
import org.codehaus.jackson.map.ObjectMapper
import org.ekstep.telemetry.util.TelemetryAccessEventUtil
import play.api.Logging
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._

class AccessLogFilter @Inject() (implicit ec: ExecutionContext) extends EssentialFilter with Logging {
    val mapper = new ObjectMapper()
    val xHeaderNames = Map("x-session-id" -> "X-Session-ID", "X-Consumer-ID" -> "x-consumer-id", "x-device-id" -> "X-Device-ID", "x-app-id" -> "APP_ID", "x-authenticated-userid" -> "X-Authenticated-Userid", "x-channel-id" -> "X-Channel-Id")

    def apply(nextFilter: EssentialAction) = new EssentialAction {
        def apply(requestHeader: RequestHeader) = {

            val startTime = System.currentTimeMillis

            val accumulator: Accumulator[ByteString, Result] = nextFilter(requestHeader)

            accumulator.map { result =>
                val endTime     = System.currentTimeMillis
                val requestTime = endTime - startTime

                val path = requestHeader.uri
                if(!path.contains("/health")){
                    val headers = requestHeader.headers.headers.groupBy(_._1).mapValues(_.map(_._2))
                    val appHeaders = headers.filter(header => xHeaderNames.keySet.contains(header._1.toLowerCase))
                        .map(entry => (xHeaderNames(entry._1.toLowerCase()), entry._2.head))
                    val otherDetails = Map[String, Any]("StartTime" -> startTime,
                        "env" -> "content",
                        "RemoteAddress" -> requestHeader.remoteAddress,
                        "Response" -> result.body.asJava,
                        "ContentLength" -> result.body.contentLength.getOrElse(0),
                        "Status" -> result.header.status,
                        "Protocol" -> "http",
                        "path" -> path,
                        "Method" -> requestHeader.method.toString,
                        "X-Session-ID" -> headers.get("X-Session-ID"),
                        "X-Consumer-ID" -> headers.get("X-Consumer-ID"),
                        "X-Device-ID" -> headers.get("X-Device-ID"),
                        "X-Authenticated-Userid" -> headers.get("X-Authenticated-Userid"),
                        "X-Channel-ID" -> headers.get("X-Channel-ID"),
                        "APP_ID" -> headers.get("X-App-ID"))
                    TelemetryAccessEventUtil.writeTelemetryEventLog((otherDetails ++ appHeaders).asInstanceOf[Map[String, AnyRef]].asJava)
                }
                result.withHeaders("Request-Time" -> requestTime.toString)
            }
        }
    }
}