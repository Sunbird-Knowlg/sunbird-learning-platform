package org.ekstep.language.router;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;

public class LanguageActorPool {

    private static final String DEFAULT_LANGUAGE_ID = "*";

    private static Map<String, Map<String, ActorRef>> actorMap = null;

    static {
        actorMap = new HashMap<String, Map<String, ActorRef>>();
        Map<String, ActorRef> defaultActorMap = new HashMap<String, ActorRef>();
        actorMap.put(DEFAULT_LANGUAGE_ID, defaultActorMap);
    }

    public static ActorRef getActorRefFromPool(String languageId, String managerName) {
        if (StringUtils.isNotBlank(managerName)) {
            Map<String, ActorRef> actorRefs = null;
            if (StringUtils.isNotBlank(languageId)) {
                actorRefs = actorMap.get(languageId);
                if (null != actorRefs) {
                    return actorRefs.get(managerName);
                } else {
                    actorRefs = actorMap.get(DEFAULT_LANGUAGE_ID);
                    return actorRefs.get(managerName);
                }
            } else {
                actorRefs = actorMap.get(DEFAULT_LANGUAGE_ID);
                return actorRefs.get(managerName);
            }
        }
        return null;
    }

    public static void addActorRefToPool(String languageId, String managerName, ActorRef ref) {
        Map<String, ActorRef> actorRefs = null;
        if (StringUtils.isNotBlank(managerName) && null != ref) {
            if (StringUtils.isNotBlank(languageId)) {
                actorRefs = actorMap.get(languageId);
                if (null == actorRefs) {
                    actorRefs = new HashMap<String, ActorRef>();
                    actorRefs.put(managerName, ref);
                    actorMap.put(languageId, actorRefs);
                } else {
                    actorRefs.put(managerName, ref);
                }
            } else {
                actorRefs = actorMap.get(DEFAULT_LANGUAGE_ID);
                actorRefs.put(managerName, ref);
            }
        }
    }
}
