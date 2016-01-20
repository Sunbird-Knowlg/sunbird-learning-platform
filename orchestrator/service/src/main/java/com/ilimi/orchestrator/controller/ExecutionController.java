package com.ilimi.orchestrator.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.orchestrator.dac.model.OrchestratorScript;
import com.ilimi.orchestrator.dac.model.RequestPath;
import com.ilimi.orchestrator.dac.model.RequestTypes;
import com.ilimi.orchestrator.dac.model.ScriptParams;
import com.ilimi.orchestrator.interpreter.Executor;
import com.ilimi.orchestrator.interpreter.exception.ExecutionErrorCodes;
import com.ilimi.orchestrator.mgr.service.IOrchestratorManager;
import com.ilimi.orchestrator.mgr.service.OrchestratorScriptMap;

@Controller
@RequestMapping("")
public class ExecutionController extends BaseOrchestratorController {

    @Autowired
    private IOrchestratorManager manager;

    @Autowired
    private Executor executor;

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> get(HttpServletRequest request) {
        return executeScript(request, RequestTypes.GET.name(), null);
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> post(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        return executeScript(request, RequestTypes.POST.name(), map);
    }

    @RequestMapping(value = "/**", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> patch(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        return executeScript(request, RequestTypes.PATCH.name(), map);
    }
    
    @RequestMapping(value = "/**", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(HttpServletRequest request) {
        return executeScript(request, RequestTypes.DELETE.name(), null);
    }

    private ResponseEntity<Response> executeScript(HttpServletRequest request, String type, Map<String, Object> map) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        OrchestratorScript script = getScript(path, type);
        try {
            if (null == script) {
                throw new MiddlewareException(ExecutionErrorCodes.ERR_SCRIPT_NOT_FOUND.name(),
                        "Script not found for the request path: " + path);
            }
            List<Object> params = getParams(request, script, path, map);
            Response response = executor.execute(script, params);
            return getResponseEntity(response, path);
        } catch (Exception e) {
            return getExceptionResponseEntity(e, path);
        }
    }

    @PostConstruct
    public void init() {
        List<OrchestratorScript> commands = manager.getAllCommands();
        List<OrchestratorScript> scripts = manager.getAllScripts();
        OrchestratorScriptMap.loadScripts(scripts, commands);
    }

    private OrchestratorScript getScript(String path, String type) {
        Map<String, OrchestratorScript> map = OrchestratorScriptMap.scriptMap.get(type);
        if (null != map && !map.isEmpty()) {
            int pathParams = -1;
            OrchestratorScript script = null;
            for (Entry<String, OrchestratorScript> entry : map.entrySet()) {
                String url = entry.getKey();
                OrchestratorScript scriptObj = entry.getValue();
                List<String> params = scriptObj.getRequestPath().getPathParams();
                int index = url.indexOf("*", 0);
                if (null != params && !params.isEmpty()) {
                    for (int i = 0; i < params.size(); i++) {
                        if (index > -1) {
                            int lastIndex = path.indexOf("/", index);
                            String param = null;
                            if (index < path.length()) {
                                if (lastIndex >= 0) {
                                    param = path.substring(index, lastIndex);
                                } else {
                                    param = path.substring(index);
                                }
                                url = url.replaceFirst("\\*", param);
                            }
                            index = url.indexOf("*", 0);
                        } else {
                            break;
                        }
                    }
                }
                if (StringUtils.equals(url, path)) {
                    int paramsCount = null == params ? 0 : params.size();
                    if (pathParams == -1 || paramsCount < pathParams)
                        script = entry.getValue();
                }
            }
            return script;
        }
        return null;
    }

    private List<Object> getParams(HttpServletRequest request, OrchestratorScript script, String path,
            Map<String, Object> map) {
        List<Object> params = new ArrayList<Object>();
        RequestPath reqPath = script.getRequestPath();
        Map<String, Object> pathParamMap = new HashMap<String, Object>();
        Map<String, Object> reqParamMap = new HashMap<String, Object>();
        Map<String, Object> bodyParamMap = new HashMap<String, Object>();
        getPathParamMap(pathParamMap, reqPath, path);
        getRequestParamMap(reqParamMap, reqPath, request);
        getBodyParamMap(bodyParamMap, map);
        if (null != script.getParameters() && !script.getParameters().isEmpty()) {
            for (ScriptParams param : script.getParameters()) {
                params.add(getParamValue(param.getName(), pathParamMap, reqParamMap, bodyParamMap));
            }
        }
        return params;
    }

    private Object getParamValue(String name, Map<String, Object> pathParamMap, Map<String, Object> reqParamMap,
            Map<String, Object> bodyParamMap) {
        if (null != pathParamMap.get(name))
            return pathParamMap.get(name);
        if (null != reqParamMap.get(name))
            return reqParamMap.get(name);
        if (null != bodyParamMap.get(name))
            return bodyParamMap.get(name);
        return null;
    }

    private void getPathParamMap(Map<String, Object> pathParamMap, RequestPath reqPath, String path) {
        String url = reqPath.getUrl();
        if (null != reqPath.getPathParams() && !reqPath.getPathParams().isEmpty()) {
            int index = url.indexOf("*", 0);
            for (int i = 0; i < reqPath.getPathParams().size(); i++) {
                if (index > -1) {
                    int lastIndex = path.indexOf("/", index);
                    String param = null;
                    if (lastIndex >= 0) {
                        param = path.substring(index, lastIndex);
                    } else {
                        param = path.substring(index);
                    }
                    pathParamMap.put(reqPath.getPathParams().get(i), param);
                    url = url.replaceFirst("\\*", param);
                    index = url.indexOf("*", 0);
                } else {
                    break;
                }
            }
        }
    }

    private void getRequestParamMap(Map<String, Object> reqParamMap, RequestPath reqPath, HttpServletRequest request) {
        if (null != reqPath.getRequestParams() && !reqPath.getRequestParams().isEmpty()) {
            for (String paramName : reqPath.getRequestParams()) {
                String value = null == request.getParameter(paramName) ? "" : request.getParameter(paramName);
                if (value.indexOf(",") >= 0) {
                    reqParamMap.put(paramName, value.split(","));
                } else {
                    reqParamMap.put(paramName, value);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void getBodyParamMap(Map<String, Object> bodyParamMap, Map<String, Object> map) {
        if (null != map && !map.isEmpty()) {
            Object requestObj = map.get("request");
            if (null != requestObj) {
                try {
                    String strRequest = mapper.writeValueAsString(requestObj);
                    Map<String, Object> requestMap = mapper.readValue(strRequest, Map.class);
                    if (null != requestMap && !requestMap.isEmpty())
                        bodyParamMap.putAll(requestMap);
                } catch (Exception e) {
                }
            }
        }
            
    }
}
