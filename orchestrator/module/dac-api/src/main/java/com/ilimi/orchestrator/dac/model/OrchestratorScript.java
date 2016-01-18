package com.ilimi.orchestrator.dac.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "scripts")
public class OrchestratorScript implements Serializable {

	private static final long serialVersionUID = -1797595876134226159L;
	
	@Id
	private String id;
	private String name;
	private String description;
	private String body;
	private String type;
	@Field("command_class")
	private String cmdClass;
	private List<ScriptParams> parameters;
	@Field("actor_path")
	private ActorPath actorPath;
	@Field("request_path")
	private RequestPath requestPath;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<ScriptParams> getParameters() {
		return parameters;
	}

	public void setParameters(List<ScriptParams> parameters) {
		this.parameters = parameters;
	}

	public ActorPath getActorPath() {
		return actorPath;
	}

	public void setActorPath(ActorPath actorPath) {
		this.actorPath = actorPath;
	}

	public RequestPath getRequestPath() {
		return requestPath;
	}

	public void setRequestPath(RequestPath requestPath) {
		this.requestPath = requestPath;
	}

	public String getCmdClass() {
		return cmdClass;
	}

	public void setCmdClass(String cmdClass) {
		this.cmdClass = cmdClass;
	}

}
