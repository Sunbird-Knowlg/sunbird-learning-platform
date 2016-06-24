package com.ilimi.common.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ilimi.common.dto.ExecutionContext;


public class LogHelper {

    private Logger LOGGER = null;
    
    private LogHelper(Logger logger) {
        this.LOGGER = logger;
    }
    
    public static LogHelper getInstance(String classname) {
        Logger logger = LogManager.getLogger(classname);
        LogHelper helper = new LogHelper(logger);
        return helper;
    }
    
    public void info(String msg) {
        LOGGER.info(ExecutionContext.getRequestId() + " | " +  msg);
    }
    
    public void debug(String msg) {
        LOGGER.debug(ExecutionContext.getRequestId() + " | " +  msg);
    }
    
    public void error(String msg, Throwable e) {
        LOGGER.error(ExecutionContext.getRequestId() + " | " +  msg, e);
    }
    
    public void error(Throwable e) {
        LOGGER.error(ExecutionContext.getRequestId() + " | " +  e.getMessage(), e);
    }
}
