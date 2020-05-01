package filters

import akka.util.ByteString
import javax.inject.Inject
import org.ekstep.telemetry.util.TelemetryAccessEventUtil
import play.api.Logging
import play.api.libs.streams.Accumulator
import play.api.mvc.{EssentialAction, EssentialFilter, RequestHeader, Result}

import scala.collection.JavaConverters
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

class AccessLogFilter @Inject() (implicit ec: ExecutionContext) extends EssentialFilter with Logging {

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
                            .map(entry => (xHeaderNames.get(entry._1.toLowerCase()).get, entry._2.head))
                    val otherDetails = Map[String, Any]("StartTime" -> startTime, "env" -> "search",
                        "RemoteAddress" -> requestHeader.remoteAddress,
                        "ContentLength" -> result.body.contentLength.getOrElse(0),
                        "Status" -> result.header.status, "Protocol" -> "http",
                        "path" -> path,
                        "Method" -> requestHeader.method.toString)
                    TelemetryAccessEventUtil.writeTelemetryEventLog(JavaConverters.mapAsJavaMapConverter((otherDetails ++ appHeaders).asInstanceOf[Map[String,AnyRef]]).asJava)
                }
                result.withHeaders("Request-Time" -> requestTime.toString)
            }
        }
    }
}
