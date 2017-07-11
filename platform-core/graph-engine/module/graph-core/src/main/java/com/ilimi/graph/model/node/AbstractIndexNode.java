package com.ilimi.graph.model.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.model.AbstractDomainObject;
import com.ilimi.graph.model.IRelation;

import akka.actor.ActorRef;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import scala.concurrent.Future;
import scala.concurrent.Promise;

public abstract class AbstractIndexNode extends AbstractDomainObject {

    private String nodeId;
    
    public AbstractIndexNode(BaseGraphManager manager, String graphId) {
        super(manager, graphId);
    }

    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    protected void failPromise(Promise<Map<String, Object>> promise, String errorCode, String msg) {
        List<String> msgs = new ArrayList<String>();
        msgs.add(msg);
        failPromise(promise, errorCode, msgs);
    }

    protected void failPromise(Promise<Map<String, Object>> promise, String errorCode, List<String> msgs) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(errorCode, msgs);
        promise.success(map);
    }

    protected Future<Object> getNodeObject(Request req, ActorRef dacRouter, String nodeId) {
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
        request.setOperation("getNodeByUniqueId");
        request.put(GraphDACParams.node_id.name(), nodeId);
        Future<Object> future = Patterns.ask(dacRouter, request, timeout);
        return future;
    }

    protected boolean checkIfNodeExists(Promise<Map<String, Object>> promise, Throwable arg0, Object arg1, String errorCode) {
        boolean valid = false;
        if (null != arg0) {
            promise.failure(arg0);
        } else {
            if (arg1 instanceof Response) {
                Response res = (Response) arg1;
                if (manager.checkError(res)) {
                    if (!StringUtils.equals(ResponseCode.RESOURCE_NOT_FOUND.name(), res.getResponseCode().name())) {
                        failPromise(promise, errorCode, manager.getErrorMessage(res));
                    } else {
                        valid = true;
                    }
                } else {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(GraphDACParams.node_id.name(), getNodeId());
                    promise.success(map);
                }
            } else {
                failPromise(promise, errorCode, "Internal Error");
            }
        }
        return valid;
    }

    protected boolean validateResponse(Promise<Map<String, Object>> promise, Throwable arg0, Object arg1, String errorCode, String errorMsg) {
        boolean valid = false;
        if (null != arg0) {
            promise.failure(arg0);
        } else {
            if (arg1 instanceof Response) {
                Response res = (Response) arg1;
                if (manager.checkError(res)) {
                    failPromise(promise, errorCode, manager.getErrorMessage(res));
                } else {
                    valid = true;
                }
            } else {
                failPromise(promise, errorCode, errorMsg);
            }
        }
        return valid;
    }

    protected void createIndexNodeRelation(final Promise<Map<String, Object>> promise, Throwable arg0, Object arg1, final Request req,
            final IRelation rel, final String errorCode, String errorMsg) {
        boolean valid = validateResponse(promise, arg0, arg1, errorCode, errorMsg);
        if (valid) {
            Response res = (Response) arg1;
            String nodeId = (String) res.get(GraphDACParams.node_id.name());
            setNodeId(nodeId);
            Future<Map<String, List<String>>> relValidation = rel.validateRelation(req);
            relValidation.onSuccess(new OnSuccess<Map<String, List<String>>>() {
                @Override
                public void onSuccess(Map<String, List<String>> messageMap) throws Throwable {
                    List<String> errMessages = getErrorMessages(messageMap);
                    if (null == errMessages || errMessages.isEmpty()) {
                        rel.createRelation(req);
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put(GraphDACParams.node_id.name(), getNodeId());
                        promise.success(map);
                    } else {
                        failPromise(promise, errorCode, errMessages);
                    }
                }
            }, manager.getContext().dispatcher());
        }
    }

}
