package org.ekstep.literacy.services;

import com.ilimi.common.dto.Request;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class LexileMeasuresActor extends UntypedActor {
	
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	public LexileMeasuresActor() {
		log.info("LexileMeasuresActor constructor | Invocked");
	}
	
	@Override
    public void onReceive(Object msg) throws Exception {

        log.info("Received Command: " + msg);

        if (msg instanceof Request) {
            
        } else if (msg.equals("echo")) {
            log.info("ECHO!");
        }
    }
}