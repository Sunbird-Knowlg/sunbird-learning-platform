package org.sunbird.learning.router;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;

// TODO: Auto-generated Javadoc
/**
 * The Class LearningActorPool, provides functionality for pooling all Learning
 * actors
 *
 * @author karthik
 */
public class LearningActorPool {

	/** The actor ref map. */
	private static Map<String, ActorRef> actorRefMap = new HashMap<String, ActorRef>();

	/**
	 * Gets the actor ref from pool.
	 *
	 * @param managerName
	 *            the manager name
	 * @return the actor ref from pool
	 */
	public static ActorRef getActorRefFromPool(String managerName) {
		if (StringUtils.isNotBlank(managerName)) {
			return actorRefMap.get(managerName);
		}
		return null;
	}

	/**
	 * Adds the actor ref to pool.
	 *
	 * @param managerName
	 *            the manager name
	 * @param ref
	 *            the ref
	 */
	public static void addActorRefToPool(String managerName, ActorRef ref) {
		actorRefMap.put(managerName, ref);
	}
}