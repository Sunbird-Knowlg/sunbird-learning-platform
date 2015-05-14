package com.ilimi.graph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.exception.GraphEngineErrorCodes;

public abstract class AbstractDomainObject {

    protected long timeout = Configuration.TIMEOUT;
    protected BaseGraphManager manager;
    protected String graphId;
    private ActorRef parent;

    public AbstractDomainObject(BaseGraphManager manager, String graphId) {
        if (StringUtils.isBlank(graphId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_GRAPH_ID.name(), "GraphId is blank");
        }
        if (checkForWhiteSpace(graphId)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_GRAPH_ID.name(), "GraphId should not have white spaces");
        }
        this.manager = manager;
        this.graphId = graphId;
        this.parent = manager.getSender();
    }

    public ActorRef getParent() {
        if (null == parent) {
            return manager.getSender();
        }
        return parent;
    }

    public BaseGraphManager getManager() {
        return manager;
    }

    public void setManager(BaseGraphManager manager) {
        this.manager = manager;
    }

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    protected boolean checkForWhiteSpace(String name) {
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(name);
        boolean found = matcher.find();
        return found;
    }

    protected boolean checkForCharacter(String name, String character) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(character)) {
            if (name.contains(character))
                return true;
        }
        return false;
    }

    protected Request getRequestObject(Request req, String manager, String operation, String key, Object value) {
        Request request = new Request(req);
        request.setManagerName(manager);
        request.setOperation(operation);
        if (StringUtils.isNotBlank(key) && null != value) {
            request.put(key, value);
        }
        return request;
    }

    protected Request getRequestObject(Request req, String manager, String operation, Map<String, Object> params) {
        Request request = new Request(req);
        request.setManagerName(manager);
        request.setOperation(operation);
        if (null != params && !params.isEmpty()) {
            for (Entry<String, Object> param : params.entrySet()) {
                request.put(param.getKey(), param.getValue());
            }
        }
        return request;
    }

    protected List<String> getErrorMessages(Map<String, List<String>> messageMap) {
        List<String> errMessages = new ArrayList<String>();
        if (null != messageMap) {
            for (List<String> list : messageMap.values()) {
                if (null != list && !list.isEmpty()) {
                    for (String msg : list) {
                        if (StringUtils.isNotBlank(msg)) {
                            errMessages.add(msg);
                        }
                    }
                }
            }
        }
        return errMessages;
    }
}
