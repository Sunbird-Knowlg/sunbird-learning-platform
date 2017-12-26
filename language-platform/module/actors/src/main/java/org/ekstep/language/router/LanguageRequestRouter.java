package org.ekstep.language.router;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.MiddlewareException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.logger.LoggerEnum;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.language.actor.EnrichActor;
import org.ekstep.language.actor.IndexesActor;
import org.ekstep.language.actor.IndowordnetActor;
import org.ekstep.language.actor.LanguageCacheActor;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.actor.LexileMeasuresActor;
import org.ekstep.language.services.ImportActor;
import org.ekstep.language.transliterate.actor.TransliteratorActor;

import org.ekstep.common.router.RequestRouterPool;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.routing.SmallestMailboxPool;
import scala.concurrent.Future;

/**
 * The Class LanguageRequestRouter. handles initialization of Language Actor
 * Pool, routes request to corresponding actor and handles akka actor future
 * response
 * 
 * @author rayulu and amarnath
 */
public class LanguageRequestRouter extends UntypedActor {

	/** The logger. */
	

	/** The timeout. */
	protected long timeout = 30000;

	/** The bulk timeout. */
	protected long bulk_timeout = 600000;

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
				Boolean isBulkRequest = (Boolean) request.getContext().get(LanguageParams.bulk_request.name());
				if (null != isBulkRequest && isBulkRequest.booleanValue())
					t = bulk_timeout;
				Future<Object> future = Patterns.ask(actorRef, request, t);
				handleFuture(request, future, parent);
			} catch (Exception e) {
				handleException(request, e, parent);
			}
		}
	}

	/**
	 * Initialize all Language actor pool.
	 */
	private void initActorPool() {
		ActorSystem system = RequestRouterPool.getActorSystem();
		int poolSize = 4;

		Props lexileMeasuresProps = Props.create(LexileMeasuresActor.class);
		ActorRef graphMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(lexileMeasuresProps));
		LanguageActorPool.addActorRefToPool(null, LanguageActorNames.LEXILE_MEASURES_ACTOR.name(), graphMgr);

		Props importProps = Props.create(ImportActor.class);
		ActorRef importMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(importProps));
		LanguageActorPool.addActorRefToPool(null, LanguageActorNames.IMPORT_ACTOR.name(), importMgr);

		Props indexesProps = Props.create(IndexesActor.class);
		ActorRef indexMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(indexesProps));
		LanguageActorPool.addActorRefToPool(null, LanguageActorNames.INDEXES_ACTOR.name(), indexMgr);

		Props enrichProps = Props.create(EnrichActor.class);
		ActorRef enrichMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(enrichProps));
		LanguageActorPool.addActorRefToPool(null, LanguageActorNames.ENRICH_ACTOR.name(), enrichMgr);

		Props indowordnetProps = Props.create(IndowordnetActor.class);
		ActorRef indowordnetMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(indowordnetProps));
		LanguageActorPool.addActorRefToPool(null, LanguageActorNames.INDOWORDNET_ACTOR.name(), indowordnetMgr);

		Props languageCacheProps = Props.create(LanguageCacheActor.class);
        ActorRef languageCacheMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(languageCacheProps));
        LanguageActorPool.addActorRefToPool(null, LanguageActorNames.LANGUAGE_CACHE_ACTOR.name(), languageCacheMgr);

		Props transliteratorActorProps = Props.create(TransliteratorActor.class);
        ActorRef transliterator = system.actorOf(new SmallestMailboxPool(poolSize).props(transliteratorActorProps));
        LanguageActorPool.addActorRefToPool(null, LanguageActorNames.TRANSLITERATOR_ACTOR.name(), transliterator);

	}

	/**
	 * Gets the actor from pool.
	 *
	 * @param request
	 *            the request
	 * @return the actor from pool
	 */
	private ActorRef getActorFromPool(Request request) {
		String graphId = (String) request.getContext().get(LanguageParams.language_id.name());
		if (StringUtils.isBlank(graphId))
			throw new ClientException(LanguageErrorCodes.ERR_ROUTER_INVALID_GRAPH_ID.name(),
					"LanguageId cannot be empty");
		String manager = request.getManagerName();
		ActorRef ref = LanguageActorPool.getActorRefFromPool(graphId, manager);
		if (null == ref)
			throw new ClientException(LanguageErrorCodes.ERR_ROUTER_ACTOR_NOT_FOUND.name(),
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
				PlatformLogger.log(
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
		PlatformLogger.log(request.getManagerName() + "," + request.getOperation() , e.getMessage(), LoggerEnum.WARN.name());
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus(StatusType.failed.name());
		if (e instanceof MiddlewareException) {
			MiddlewareException mwException = (MiddlewareException) e;
			params.setErr(mwException.getErrCode());
		} else {
			params.setErr(LanguageErrorCodes.ERR_SYSTEM_EXCEPTION.name());
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
