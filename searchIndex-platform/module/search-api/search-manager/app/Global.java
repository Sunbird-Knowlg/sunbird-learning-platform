import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.search.router.SearchRequestRouterPool;

import play.Application;
import play.GlobalSettings;

public class Global extends GlobalSettings {
	
	private static Logger LOGGER = LogManager.getLogger(Global.class.getName());
	
	public void onStart(Application app) {
		LOGGER.info("Initialising Search Request Router Pool");
        SearchRequestRouterPool.init();
        LOGGER.info("Application has started");
    }
	
}
