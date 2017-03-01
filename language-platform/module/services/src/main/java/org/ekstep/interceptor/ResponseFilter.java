package org.ekstep.interceptor;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ekstep.common.util.RequestWrapper;
import org.ekstep.common.util.ResponseWrapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.ExecutionContext;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.TelemetryBEAccessEvent;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.common.util.LogTelemetryEventUtil;

public class ResponseFilter implements Filter {

	private static LogHelper LOGGER = LogHelper.getInstance(ResponseFilter.class.getName());
	private static ObjectMapper mapper = new ObjectMapper();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		Request requestObj = null;
		try {
			String requestId = getUUID();
			RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
			String body = requestWrapper.getBody();
			if ("POST".equalsIgnoreCase(requestWrapper.getMethod())) {
				requestObj = mapper.readValue(body, Request.class);
			}

			ExecutionContext.setRequestId(requestId);
			LOGGER.info("Path: " + requestWrapper.getServletPath() + " | Remote Address: " + request.getRemoteAddr()
					+ " | Params: " + request.getParameterMap());

			ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
			requestWrapper.setAttribute("startTime", System.currentTimeMillis());

			chain.doFilter(requestWrapper, responseWrapper);

			long timeDuration = System.currentTimeMillis() - (long) request.getAttribute("startTime");
			byte responseContent[] = responseWrapper.getData();
			Response responseObj = mapper.readValue(responseContent, Response.class);

			TelemetryBEAccessEvent accessData = new TelemetryBEAccessEvent();
			accessData.setRid(responseObj.getId());
			accessData.setUip(request.getRemoteHost());
			accessData.setType("api");
			accessData.setSize(responseContent.length);
			accessData.setDuration(timeDuration);
			accessData.setStatus(responseWrapper.getStatus());
			accessData.setProtocol(requestWrapper.getProtocol());
			accessData.setMethod(requestWrapper.getMethod());
			RequestParams parameterMap = null;
			if (null != requestObj && null != requestObj.getParams()) {
				parameterMap = requestObj.getParams();
			}
			if (null != requestWrapper.getHeader("X-Session-ID")) {
				parameterMap = new RequestParams();
				if (null == parameterMap.getSid())
					parameterMap.setSid(requestWrapper.getHeader("X-Session-ID"));
			}
			accessData.setParams(parameterMap);
			LogTelemetryEventUtil.logAccessEvent(accessData);

			response.getOutputStream().write(responseWrapper.getData());
		} catch (JsonParseException e) {
			LOGGER.error(e);
		} catch (JsonMappingException e) {
			LOGGER.error(e);
		}
	}

	@Override
	public void destroy() {

	}

	private String getUUID() {
		UUID uid = UUID.randomUUID();
		return uid.toString();
	}
}
