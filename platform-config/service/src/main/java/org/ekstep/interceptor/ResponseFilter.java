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
import org.ekstep.common.util.TelemetryAccessEventUtil;

import com.ilimi.common.dto.ExecutionContext;
import com.ilimi.common.dto.HeaderParam;
import com.ilimi.common.logger.PlatformLogger;

public class ResponseFilter implements Filter {

	

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String requestId = getUUID();
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		ExecutionContext.setRequestId(requestId);
		boolean isMultipart = (httpRequest.getHeader("content-type") != null
				&& httpRequest.getHeader("content-type").indexOf("multipart/form-data") != -1);
		String consumerId = httpRequest.getHeader("X-Consumer-ID");
		String channelId = httpRequest.getHeader("X-Channel-Id");
		String appId = httpRequest.getHeader("X-App-Id");
		if (StringUtils.isNotBlank(consumerId))
			ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CONSUMER_ID.name(), consumerId);

		if (StringUtils.isNotBlank(channelId))
			ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CHANNEL_ID.name(), channelId);
		else
			ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CHANNEL_ID.name(), "in.ekstep");
		
		if (StringUtils.isNotBlank(appId))
			ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.APP_ID.name(), appId);

		if (!isMultipart) {
			RequestWrapper requestWrapper = new RequestWrapper(httpRequest);
			PlatformLogger.log("Path: " + requestWrapper.getServletPath() + " | Remote Address: " + request.getRemoteAddr()
			+ " | Params: " + request.getParameterMap());
			
			ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);
			requestWrapper.setAttribute("startTime", System.currentTimeMillis());
			chain.doFilter(requestWrapper, responseWrapper);
			
			TelemetryAccessEventUtil.writeTelemetryEventLog(requestWrapper, responseWrapper);
			response.getOutputStream().write(responseWrapper.getData());
		} else {
			PlatformLogger.log("Path: " + httpRequest.getServletPath() + " | Remote Address: " + request.getRemoteAddr()
			+ " | Params: " + request.getParameterMap());
			chain.doFilter(request, response);
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
