package com.ilimi.orchestrator.dac.model;

import java.io.Serializable;

public class ActorPath implements Serializable {

	private static final long serialVersionUID = -6130032116962658440L;
	private String manager;
	private String operation;
	private String router;

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

    public String getRouter() {
        return router;
    }

    public void setRouter(String router) {
        this.router = router;
    }

}
