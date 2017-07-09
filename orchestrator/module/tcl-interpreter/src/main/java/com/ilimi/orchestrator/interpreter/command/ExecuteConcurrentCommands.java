package com.ilimi.orchestrator.interpreter.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.pattern.Patterns;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.orchestrator.dac.model.OrchestratorScript;
import com.ilimi.orchestrator.interpreter.ICommand;
import com.ilimi.orchestrator.interpreter.OrchestratorRequest;
import com.ilimi.orchestrator.interpreter.actor.TclExecutorActorRef;
import com.ilimi.orchestrator.interpreter.exception.ExecutionErrorCodes;
import com.ilimi.orchestrator.mgr.service.OrchestratorScriptMap;
import com.ilimi.orchestrator.router.AkkaRequestRouter;

public class ExecuteConcurrentCommands implements ICommand, Command {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
		if (argv.length == 2) {
			ActorRef actorRef = TclExecutorActorRef.getRef();
			try {
				TclObject tclObject = argv[1];
				Object obj = ReflectObject.get(interp, tclObject);
				List<Map<String, Object>> commandsMap = (List<Map<String, Object>>) obj;
				List<Future<Object>> futures = new ArrayList<Future<Object>>();

				for (Map commandMap : commandsMap) {
					String commandName = (String) commandMap.get("commandName");
					Map<String, Object> commandParams = (Map<String, Object>) commandMap.get("commandParams");

					if (commandName == null || commandName.isEmpty()) {
						throw new Exception("Command name is mandatory");
					}

					if (commandParams == null) {
						throw new Exception("Command params are mandatory");
					}
					OrchestratorScript script = OrchestratorScriptMap.getScriptByName(commandName);

					OrchestratorRequest request = new OrchestratorRequest();
					request.setAction(OrchestratorRequest.ACTION_TYPES.EXECUTE.name());
					request.setScript(script);
					request.setParams(commandParams);
					Future<Object> future = Patterns.ask(actorRef, request, AkkaRequestRouter.timeout);
					futures.add(future);
				}
				Response response = new Response();
				Future<Iterable<Object>> objects = Futures.sequence(futures,
						RequestRouterPool.getActorSystem().dispatcher());
				Iterable<Object> responses = Await.result(objects, Duration.create(30, TimeUnit.SECONDS));
				if (null != responses) {
					List<Object> list = new ArrayList<Object>();
					for (Object responseObj : responses) {
						if (responseObj instanceof Response) {
							Response res = (Response) responseObj;
							list.add(res);
						} else {
							throw new Exception("Tcl response is not a Response obeject");
						}
					}
					response.put("responses", list);
				} else {
					throw new Exception("Response is null");
				}
				TclObject tclResp = ReflectObject.newInstance(interp, response.getClass(), response);
				interp.setResult(tclResp);
			} catch (Exception e) {
				throw new MiddlewareException(ExecutionErrorCodes.ERR_SYSTEM_ERROR.name(), e.getMessage(), e);
			}
		} else {
			throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_traversal_description command");
		}
	}

	@Override
	public String getCommandName() {
		return "execute_commands_concurrently";
	}
}
