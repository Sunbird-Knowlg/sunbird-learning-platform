package org.ekstep.language.router;

import java.util.concurrent.TimeUnit;

import org.ekstep.language.common.enums.LanguageErrorCodes;

import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxPool;
import akka.util.Timeout;
import scala.concurrent.duration.Duration;

/**
 * The Class LanguageRequestRouterPool. initialize Base ActorSystem, creates
 * ActorRef for LanguageRequestRouter and initialize Language request actor Pool
 *
 * @author rayulu and amarnath
 */
public class LanguageRequestRouterPool {

	/** The actor. */
	private static ActorRef actor;

	/** The count. */
	private static int count = 5;

	/** The req timeout. */
	public static long REQ_TIMEOUT = 30000;

	/** The bulk req timeout. */
	public static long BULK_REQ_TIMEOUT = 600000;

	/** The wait timeout. */
	public static Timeout WAIT_TIMEOUT = new Timeout(Duration.create(30, TimeUnit.SECONDS));

	/** The bulk wait timeout. */
	public static Timeout BULK_WAIT_TIMEOUT = new Timeout(Duration.create(10, TimeUnit.MINUTES));

	/**
	 * Inits the.
	 */
	public static void init() {
		ActorSystem system = RequestRouterPool.getActorSystem();
		Props actorProps = Props.create(LanguageRequestRouter.class);
		actor = system.actorOf(new SmallestMailboxPool(count).props(actorProps));
		actor.tell("init", system.deadLetters());
	}

	/**
	 * Gets the request router.
	 *
	 * @return the request router
	 */
	public static ActorRef getRequestRouter() {
		if (null == actor) {
			throw new ServerException(LanguageErrorCodes.ERR_LANGUAGE_REQUEST_ROUTER_NOT_FOUND.name(),
					"Request Router not found");
		}
		return actor;
	}

	/**
	 * Destroy.
	 */
	public static void destroy() {
		actor = null;
	}
}
