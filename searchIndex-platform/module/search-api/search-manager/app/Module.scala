import com.google.inject.AbstractModule
import org.sunbird.search.actor.{DefinitionSyncScheduler, HealthCheckManager, SearchManager}
import play.libs.akka.AkkaGuiceSupport
import org.sunbird.telemetry.TelemetryGenerator

class Module extends AbstractModule with AkkaGuiceSupport {

    override def configure() = {
        super.configure()
        System.setProperty("es.set.netty.runtime.available.processors", "false")
        TelemetryGenerator.setComponent("search-service")
        bindActor(classOf[SearchManager], "SearchManager")
        bindActor(classOf[HealthCheckManager], "HealthCheckManager")
        DefinitionSyncScheduler.init()
    }
}
