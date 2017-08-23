package com.ilimi.orchestrator.interpreter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ilimi.common.dto.ExecutionContext;
import com.ilimi.common.dto.HeaderParam;
import com.ilimi.orchestrator.dac.model.OrchestratorScript;

public class OrchestratorRequest implements Serializable {

	private static final long serialVersionUID = 6662469130673179559L;
	
	{   
		Map<String, Object> currContext = ExecutionContext.getCurrent().getContextValues();
        context = currContext == null ? new HashMap<String, Object>() : new HashMap<String, Object>(currContext);
		
        //set request id
        requestId = (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.REQUEST_ID.getParamName());
        consumerId = (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.CONSUMER_ID.getParamName());
        channelId = (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.CHANNEL_ID.getParamName());
        appId = (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.APP_ID.getParamName());
    }

	private String action;
	private List<OrchestratorScript> scripts;
	private OrchestratorScript script;
	private Map<String, Object> params;
	private String requestId;
	private String consumerId;
	private String channelId;
	private String appId;
	protected Map<String, Object> context;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<OrchestratorScript> getScripts() {
		return scripts;
	}

	public void setScripts(List<OrchestratorScript> scripts) {
		this.scripts = scripts;
	}

	public OrchestratorScript getScript() {
		return script;
	}

	public void setScript(OrchestratorScript script) {
		this.script = script;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public static enum ACTION_TYPES {
		INIT, EXECUTE;
	}

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}
	
	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

}
