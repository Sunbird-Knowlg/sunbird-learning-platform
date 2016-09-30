package org.ekstep.language.router;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;

/**
 * The Class LanguageActorPool, maintains Pooling of Language Actors
 *
 * @author rayulu and amarnath
 */
public class LanguageActorPool {

	/** The Constant DEFAULT_LANGUAGE_ID. */
	private static final String DEFAULT_LANGUAGE_ID = "*";

	/** The actor map. */
	private static Map<String, Map<String, ActorRef>> actorMap = null;

	static {
		actorMap = new HashMap<String, Map<String, ActorRef>>();
		Map<String, ActorRef> defaultActorMap = new HashMap<String, ActorRef>();
		actorMap.put(DEFAULT_LANGUAGE_ID, defaultActorMap);
	}

	/**
	 * Gets the actor ref from pool.
	 *
	 * @param languageId
	 *            the language id
	 * @param managerName
	 *            the manager name
	 * @return the actor ref from pool
	 */
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

	/**
	 * Adds the actor ref to pool.
	 *
	 * @param languageId
	 *            the language id
	 * @param managerName
	 *            the manager name
	 * @param ref
	 *            the ref
	 */
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
