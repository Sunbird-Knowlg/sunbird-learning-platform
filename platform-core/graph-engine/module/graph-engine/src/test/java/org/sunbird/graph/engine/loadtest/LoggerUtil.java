package org.sunbird.graph.engine.loadtest;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class LoggerUtil {

    public static void config(String logFileName) {
        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile("logs/"+logFileName+".log");
        fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        fa.setThreshold(Level.DEBUG);
        fa.setAppend(true);
        fa.activateOptions();

        //add appender to any Logger (here is root)
        Logger.getLogger("PerformanceTestLogger").addAppender(fa);

    }
    
    public static void main(String[] args) {
        config("Test");
        Logger fileLogger = Logger.getLogger("FileLogger");
        fileLogger.info("Test message.");
        
        Logger fileLogger2 = Logger.getLogger("FileLogger2");
        fileLogger2.info("Test message2.");
    }
    
}
