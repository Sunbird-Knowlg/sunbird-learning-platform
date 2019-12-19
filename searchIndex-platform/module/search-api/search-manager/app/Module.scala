import com.google.inject.AbstractModule
import play.libs.akka.AkkaGuiceSupport

import org.ekstep.search.router.SearchRequestRouterPool
import org.ekstep.telemetry.TelemetryGenerator

class Module extends AbstractModule with AkkaGuiceSupport {

    override def configure() = {
        super.configure()
        System.setProperty("es.set.netty.runtime.available.processors", "false")
        SearchRequestRouterPool.init()
        TelemetryGenerator.setComponent("search-service")
    }
}
