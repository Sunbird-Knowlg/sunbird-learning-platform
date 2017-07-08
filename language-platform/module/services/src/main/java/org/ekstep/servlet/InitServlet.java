package org.ekstep.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.search.router.SearchRequestRouterPool;

import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogger;;
import com.ilimi.common.util.PlatformLogger;

public class InitServlet extends HttpServlet {

private static final long serialVersionUID = 8162107839763607722L;
    
    private static ILogger LOGGER = PlatformLogManager.getLogger();
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Initialising Request Router Pool
        LOGGER.log("Initialising Language Request Router Pool", config);
        LanguageRequestRouterPool.init();
        SearchRequestRouterPool.init(RequestRouterPool.getActorSystem());
    }
}
