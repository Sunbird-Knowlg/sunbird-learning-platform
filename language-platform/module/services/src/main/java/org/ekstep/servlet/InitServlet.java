package org.ekstep.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.router.LanguageRequestRouterPool;

public class InitServlet extends HttpServlet {

private static final long serialVersionUID = 8162107839763607722L;
    
    private static Logger LOGGER = LogManager.getLogger(InitServlet.class.getName());
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Initialising Request Router Pool
        LOGGER.info("Initialising Language Request Router Pool");
        LanguageRequestRouterPool.init();
    }
}
