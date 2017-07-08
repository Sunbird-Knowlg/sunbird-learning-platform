package com.ilimi.interceptor;

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
import org.ekstep.common.util.TelemetryAccessEventUtil;

import com.ilimi.common.dto.ExecutionContext;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;

public class ResponseFilter implements Filter {

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String requestId = getUUID();
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		ExecutionContext.setRequestId(requestId);
		RequestWrapper requestWrapper = new RequestWrapper(httpRequest);
		LOGGER.log("Path: " + requestWrapper.getServletPath() , " | Remote Address: " + request.getRemoteAddr()
				+ " | Params: " + request.getParameterMap());

		ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
		requestWrapper.setAttribute("startTime", System.currentTimeMillis());

		chain.doFilter(requestWrapper, responseWrapper);

		TelemetryAccessEventUtil.writeTelemetryEventLog(requestWrapper, responseWrapper);
		response.getOutputStream().write(responseWrapper.getData());
	}

	@Override
	public void destroy() {

	}

	private String getUUID() {
		UUID uid = UUID.randomUUID();
		return uid.toString();
	}
}
