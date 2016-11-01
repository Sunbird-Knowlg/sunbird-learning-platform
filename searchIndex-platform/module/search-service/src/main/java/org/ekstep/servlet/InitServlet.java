package org.ekstep.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.search.router.SearchRequestRouterPool;

public class InitServlet extends HttpServlet {

	private static final long serialVersionUID = 8344434947699370881L;
	private static Logger LOGGER = LogManager.getLogger(InitServlet.class.getName());
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Initialising Search Request Router Pool
        LOGGER.info("Initialising Search Request Router Pool");
        SearchRequestRouterPool.init();
    }
}
