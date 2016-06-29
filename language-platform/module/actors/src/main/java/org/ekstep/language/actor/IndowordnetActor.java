package org.ekstep.language.actor;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.util.IndowordnetUtil;

import com.ilimi.common.dto.Request;

import akka.actor.ActorRef;

public class IndowordnetActor extends LanguageBaseActor {

    private static Logger LOGGER = LogManager.getLogger(IndowordnetActor.class.getName());
    private IndowordnetUtil util = new IndowordnetUtil();

    @Override
    public void onReceive(Object msg) throws Exception {
        LOGGER.info("Received Command: " + msg);
        if (msg instanceof Request) {
            Request request = (Request) msg;
            String languageId = (String) request.getContext().get(LanguageParams.language_id.name());
            String operation = request.getOperation();
            try {
                if (StringUtils.equalsIgnoreCase(LanguageOperations.importIndowordnet.name(), operation)) {
                	int batchSize = 1000;
                	int maxRecords = 50000;
                	int offset = 0;
                	if(request.get(LanguageParams.batch_size.name()) != null){
                		batchSize = (int) request.get(LanguageParams.batch_size.name());
                	}
                	if(request.get(LanguageParams.max_records.name()) != null){
                		maxRecords = (int) request.get(LanguageParams.max_records.name());
                	}
                	if(request.get(LanguageParams.offset.name()) != null){
                		offset = (int) request.get(LanguageParams.offset.name());
                	}
                	util.loadWords(languageId, batchSize, maxRecords, offset);
                	OK(getSender());
                }else {
                    LOGGER.info("Unsupported operation: " + operation);
                    unhandled(msg);
                }
            } catch(Exception e) {
            	e.printStackTrace();
                handleException(e, getSender());
            }
        } else {
            LOGGER.info("Unsupported operation!");
            unhandled(msg);
        }
        
    }
    
    @Override
    protected void invokeMethod(Request request, ActorRef parent) {
    }
}