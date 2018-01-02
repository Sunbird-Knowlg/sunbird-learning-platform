package org.ekstep.language.actor;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.util.IndowordnetUtil;
import org.ekstep.telemetry.logger.PlatformLogger;

import akka.actor.ActorRef;

/**
 * The Class IndowordnetActor processes Indo-wordnet import related messages from the request router.
 * 
 * @author Azhar, Amarnath
 * 
 */
public class IndowordnetActor extends LanguageBaseActor {

    /** The logger. */
    
    
    /** The util. */
    private IndowordnetUtil util = new IndowordnetUtil();

    /* (non-Javadoc)
     * @see org.ekstep.graph.common.mgr.BaseGraphManager#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object msg) throws Exception {
        PlatformLogger.log("Received Command: " + msg);
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
				} else if (StringUtils.equalsIgnoreCase(LanguageOperations.importIndowordnetTranslations.name(),
						operation)) {
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
					util.loadTranslations(languageId, batchSize, maxRecords, offset);
                	OK(getSender());
				} else {
                    PlatformLogger.log("Unsupported operation: " + operation);
                    unhandled(msg);
                }
            } catch(Exception e) {
            	e.printStackTrace();
                handleException(e, getSender());
            }
        } else {
            PlatformLogger.log("Unsupported operation!");
            unhandled(msg);
        }
        
    }
    
    /* (non-Javadoc)
     * @see org.ekstep.graph.common.mgr.BaseGraphManager#invokeMethod(org.ekstep.common.dto.Request, akka.actor.ActorRef)
     */
    @Override
    protected void invokeMethod(Request request, ActorRef parent) {
    }
}