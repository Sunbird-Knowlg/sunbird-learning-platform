package com.ilimi.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.search.router.SearchRequestRouterPool;
import org.ekstep.searchindex.consumer.ConsumerRunner;

import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogger;;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;


public class InitServlet extends HttpServlet {

    private static final long serialVersionUID = 8162107839763607722L;
    
    private static ILogger LOGGER = PlatformLogManager.getLogger();
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Initialising Request Router Pool
        LOGGER.log("Initialising Request Router Pool");
        LearningRequestRouterPool.init();
        SearchRequestRouterPool.init(RequestRouterPool.getActorSystem());
        try {
			ConsumerRunner.startConsumers();
		} catch (Exception e) {
			throw new ServletException(e);
		} 
    }
}
