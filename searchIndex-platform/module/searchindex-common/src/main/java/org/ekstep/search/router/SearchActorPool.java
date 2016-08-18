package org.ekstep.search.router;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;

public class SearchActorPool {

	private static Map<String, ActorRef> actorRefMap = new HashMap<String, ActorRef>();
	
	  public static ActorRef getActorRefFromPool(String managerName) {
	        if (StringUtils.isNotBlank(managerName)) {
	                return actorRefMap.get(managerName);
	        }
	        return null;
	   }
	
	  public static void addActorRefToPool(String managerName, ActorRef ref) {
		  actorRefMap.put(managerName, ref);
	  }
}
