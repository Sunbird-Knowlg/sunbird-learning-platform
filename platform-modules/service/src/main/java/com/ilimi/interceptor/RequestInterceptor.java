package com.ilimi.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.ilimi.common.dto.ExecutionContext;
import com.ilimi.common.dto.HeaderParam;

@Component
public class RequestInterceptor extends HandlerInterceptorAdapter {

	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		
		ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.REQUEST_ID.getParamName(), getUUID());
		return true;
	}

    private String getUUID() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }
}
