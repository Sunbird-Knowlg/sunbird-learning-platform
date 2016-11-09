package org.ekstep.learning.router;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.learning.actor.OptimizerActor;
import org.ekstep.learning.common.enums.LearningActorNames;
import org.ekstep.learning.common.enums.LearningErrorCodes;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.routing.SmallestMailboxPool;
import scala.concurrent.Future;

// TODO: Auto-generated Javadoc
/**
 * The Class LearningRequestRouter, provides functionality to route the request
 * for all learning actors
 *
 * @author karthik
 */
public class LearningRequestRouter extends UntypedActor {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(LearningRequestRouter.class.getName());

	/** The timeout. */
	protected long timeout = 30000;

	/*
	 * (non-Javadoc)
	 * 
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof String) {
			if (StringUtils.equalsIgnoreCase("init", message.toString())) {
				initActorPool();
				getSender().tell("initComplete", getSelf());
			} else {
				getSender().tell(message, getSelf());
			}
		} else if (message instanceof Request) {
			Request request = (Request) message;
			ActorRef parent = getSender();
			try {
				ActorRef actorRef = getActorFromPool(request);
				long t = timeout;
				Future<Object> future = Patterns.ask(actorRef, request, t);
				handleFuture(request, future, parent);
			} catch (Exception e) {
				handleException(request, e, parent);
			}
		}
	}

	/**
	 * Inits the actor pool.
	 */
	private void initActorPool() {
		ActorSystem system = RequestRouterPool.getActorSystem();
		int poolSize = 4;

		Props propertyProps = Props.create(OptimizerActor.class);
		ActorRef propertyMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(propertyProps));
		LearningActorPool.addActorRefToPool(LearningActorNames.OPTIMIZER_ACTOR.name(), propertyMgr);
	}

	/**
	 * Gets the actor from pool.
	 *
	 * @param request
	 *            the request
	 * @return the actor from pool
	 */
	private ActorRef getActorFromPool(Request request) {
		String manager = request.getManagerName();
		ActorRef ref = LearningActorPool.getActorRefFromPool(manager);
		if (null == ref)
			throw new ClientException(LearningErrorCodes.ERR_ROUTER_ACTOR_NOT_FOUND.name(),
					"Actor not found in the pool for manager: " + manager);
		return ref;
	}

	/**
	 * Handle future.
	 *
	 * @param request
	 *            the request
	 * @param future
	 *            the future
	 * @param parent
	 *            the parent
	 */
	protected void handleFuture(final Request request, Future<Object> future, final ActorRef parent) {
		future.onSuccess(new OnSuccess<Object>() {
			@Override
			public void onSuccess(Object arg0) throws Throwable {
				parent.tell(arg0, getSelf());
				Response res = (Response) arg0;
				ResponseParams params = res.getParams();
				LOGGER.info(
						request.getManagerName() + "," + request.getOperation() + ", SUCCESS, " + params.toString());
			}
		}, getContext().dispatcher());

		future.onFailure(new OnFailure() {
			@Override
			public void onFailure(Throwable e) throws Throwable {
				handleException(request, e, parent);
			}
		}, getContext().dispatcher());
	}

	/**
	 * Handle exception.
	 *
	 * @param request
	 *            the request
	 * @param e
	 *            the e
	 * @param parent
	 *            the parent
	 */
	protected void handleException(final Request request, Throwable e, final ActorRef parent) {
		LOGGER.error(request.getManagerName() + "," + request.getOperation() + ", ERROR: " + e.getMessage(), e);
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus(StatusType.failed.name());
		if (e instanceof MiddlewareException) {
			MiddlewareException mwException = (MiddlewareException) e;
			params.setErr(mwException.getErrCode());
		} else {
			params.setErr(LearningErrorCodes.ERR_SYSTEM_EXCEPTION.name());
		}
		params.setErrmsg(e.getMessage());
		response.setParams(params);
		setResponseCode(response, e);
		parent.tell(response, getSelf());
	}

	/**
	 * Sets the response code.
	 *
	 * @param res
	 *            the res
	 * @param e
	 *            the e
	 */
	private void setResponseCode(Response res, Throwable e) {
		if (e instanceof ClientException) {
			res.setResponseCode(ResponseCode.CLIENT_ERROR);
		} else if (e instanceof ServerException) {
			res.setResponseCode(ResponseCode.SERVER_ERROR);
		} else if (e instanceof ResourceNotFoundException) {
			res.setResponseCode(ResponseCode.RESOURCE_NOT_FOUND);
		} else {
			res.setResponseCode(ResponseCode.SERVER_ERROR);
		}
	}
}