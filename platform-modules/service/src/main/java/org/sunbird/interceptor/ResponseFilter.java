package org.sunbird.interceptor;

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

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.ExecutionContext;
import org.sunbird.common.dto.HeaderParam;
import org.sunbird.common.util.RequestWrapper;
import org.sunbird.common.util.ResponseWrapper;
import org.sunbird.telemetry.TelemetryGenerator;
import org.sunbird.telemetry.TelemetryParams;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.sunbird.common.util.AccessEventGenerator;;

public class ResponseFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		TelemetryGenerator.setComponent("learning-service");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String requestId = getUUID();
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		ExecutionContext.setRequestId(requestId);
		String consumerId = httpRequest.getHeader("X-Consumer-ID");
		String channelId = httpRequest.getHeader("X-Channel-Id");
		String appId = httpRequest.getHeader("X-App-Id");
		String path = httpRequest.getRequestURI();
		String deviceId=httpRequest.getHeader("X-Device-ID");

		if (StringUtils.isNotBlank(consumerId))
			ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CONSUMER_ID.name(), consumerId);
		if (StringUtils.isNotBlank(channelId))
			ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CHANNEL_ID.name(), channelId);
		else
			ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CHANNEL_ID.name(), Platform.config.getString("channel.default"));

		if (StringUtils.isNotBlank(appId))
			ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.APP_ID.name(), appId);
		
		if (StringUtils.isNotBlank(deviceId))
			ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.DEVICE_ID.name(), deviceId);

		if (!path.contains("/health")) {
			RequestWrapper requestWrapper = new RequestWrapper(httpRequest);
			TelemetryManager.log("Path: " + requestWrapper.getServletPath()+ " | Remote Address: " + request.getRemoteAddr());

			ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
			requestWrapper.setAttribute("startTime", System.currentTimeMillis());
			String env = getEnv(requestWrapper);
			ExecutionContext.getCurrent().getGlobalContext().put(TelemetryParams.ENV.name(), env);
			requestWrapper.setAttribute("env", env);

			chain.doFilter(requestWrapper, responseWrapper);

			AccessEventGenerator.writeTelemetryEventLog(requestWrapper, responseWrapper);
			response.getOutputStream().write(responseWrapper.getData());
		} else {
			TelemetryManager.log("Path: " + httpRequest.getServletPath() +" | Remote Address: " + request.getRemoteAddr());
			chain.doFilter(httpRequest, response);
		}
	}

	private String getEnv(RequestWrapper requestWrapper) {
		String path = requestWrapper.getRequestURI();
		if (path.contains("/v3/definitions") || path.contains("/v3/import") || path.contains("/v3/export")
				|| path.contains("/taxonomy/")) {
			return "core";
		}
		if (path.contains("/sync/") || path.contains("v3/system") || path.contains("/v3/languages/list")) {
			return "system";
		}
		if (path.contains("/domain")) {
			return "domain";
		} else {
			return path.split("/")[2];
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
