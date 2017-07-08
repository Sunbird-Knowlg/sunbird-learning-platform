package com.ilimi.orchestrator.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
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
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogger;;
import com.ilimi.common.util.PlatformLogger;
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

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	@Autowired
	private IOrchestratorManager manager;

	@Autowired
	private Executor executor;

	@RequestMapping(value = "/**", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> get(HttpServletRequest request, HttpServletResponse response) {
		return executeScript(request, response, RequestTypes.GET.name(), null);
	}

	@RequestMapping(value = "/**", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> post(@RequestBody Map<String, Object> map, HttpServletRequest request,
			HttpServletResponse response) {
		return executeScript(request, response, RequestTypes.POST.name(), map);
	}

	@RequestMapping(value = "/**", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> patch(@RequestBody Map<String, Object> map, HttpServletRequest request,
			HttpServletResponse response) {
		return executeScript(request, response, RequestTypes.PATCH.name(), map);
	}

	@RequestMapping(value = "/**", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> delete(HttpServletRequest request, HttpServletResponse response) {
		return executeScript(request, response, RequestTypes.DELETE.name(), null);
	}

	private ResponseEntity<Response> executeScript(HttpServletRequest request, HttpServletResponse response,
			String type, Map<String, Object> map) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		OrchestratorScript script = getScript(path, type);
		if (null == script) {
			return getExceptionResponseEntity(new MiddlewareException(ExecutionErrorCodes.ERR_SCRIPT_NOT_FOUND.name(),
					"Invalid request path: " + path), path);
		} else {
			try {
				Map<String, Object> params = getParams(request, script, path, map);
				LOGGER.log(script.getName() + "," + params);
				if (null != params) {
					LOGGER.log("URL: " + getEnvBaseUrl());
					params.put("server_env", getEnvBaseUrl());
				}
				Response resp = executor.execute(script, params);
				if (ResponseCode.OK.equals(resp.getResponseCode())) {
					String format = request.getParameter("format");
					if (StringUtils.isNotBlank(format) && StringUtils.equalsIgnoreCase("csv", format)) {
						String csv = (String) resp.getResult().get("result");
						if (StringUtils.isNotBlank(csv)) {
							byte[] bytes = csv.getBytes();
							response.setContentType("text/csv");
							response.setHeader("Content-Disposition", "attachment; filename=graph.csv");
							response.getOutputStream().write(bytes);
							response.getOutputStream().close();
						}
					} else if (StringUtils.isNotBlank(format) && StringUtils.equalsIgnoreCase("base64", format)) {
						String result = (String) resp.getResult().get("result");
						if (StringUtils.isNotBlank(result)) {
							String data = result.split(",")[1];
							byte[] buffer = Base64.decodeBase64(data);
							String contype = result.split(";")[0];
							contype = contype.split(":")[1];
							response.setContentType(contype);
							response.setHeader("Content-Transfer-Encoding", "base64");
							BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(buffer));
							ImageIO.write(bufferedImage, "png", response.getOutputStream());
							response.getOutputStream().close();
							return null;
						}
					}
				}
				return getResponseEntity(resp, script);
			} catch (Exception e) {
				LOGGER.log("Error executing script: " , script.getName(), e);
				return getExceptionResponseEntity(e, script);
			}
		}
	}

	@PostConstruct
	public void init() {
		List<OrchestratorScript> commands = manager.getAllCommands();
		List<OrchestratorScript> scripts = manager.getAllScripts();
		OrchestratorScriptMap.loadScripts(scripts, commands);
	}

	private String escapeSpecialChars(String param) {
		if (StringUtils.isNotBlank(param)) {
			param = param.replace("\\", "\\\\");
			param = param.replace("$", "\\$");
		}
		return param;
	}

	private OrchestratorScript getScript(String path, String type) {
		Map<String, OrchestratorScript> map = OrchestratorScriptMap.scriptMap.get(type);
		if(path.endsWith("/")){
			path = StringUtils.stripEnd(path, "/");
		}
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
								param = escapeSpecialChars(param);
								url = url.replaceFirst("\\*", param);
							}
							index = url.indexOf("*", 0);
						} else {
							break;
						}
					}
				}
				if(url.endsWith("/")){
					url = StringUtils.stripEnd(url, "/");
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

	private Map<String, Object> getParams(HttpServletRequest request, OrchestratorScript script, String path,
			Map<String, Object> map) {
		Map<String, Object> params = new HashMap<String, Object>();
		RequestPath reqPath = script.getRequestPath();
		Map<String, Object> pathParamMap = new HashMap<String, Object>();
		Map<String, Object> reqParamMap = new HashMap<String, Object>();
		Map<String, Object> bodyParamMap = new HashMap<String, Object>();
		getPathParamMap(pathParamMap, reqPath, path);
		getRequestParamMap(reqParamMap, reqPath, request);
		getBodyParamMap(bodyParamMap, map);
		if (null != script.getParameters() && !script.getParameters().isEmpty()) {
			for (ScriptParams param : script.getParameters()) {
				params.put(param.getName(), getParamValue(param.getName(), pathParamMap, reqParamMap, bodyParamMap));
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
					param = escapeSpecialChars(param);
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
				String value = request.getParameter(paramName);
				if (StringUtils.isNotBlank(paramName)) {
					if (StringUtils.isNotBlank(value)) {
						if (value.indexOf(",") >= 0) {
							reqParamMap.put(paramName, value.split(","));
						} else {
							reqParamMap.put(paramName, value);
						}
					}
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
