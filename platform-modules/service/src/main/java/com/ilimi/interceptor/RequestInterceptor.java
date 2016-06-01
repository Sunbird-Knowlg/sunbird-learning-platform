package com.ilimi.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.ilimi.common.dto.ExecutionContext;
import com.ilimi.common.logger.LogHelper;

@Component
public class RequestInterceptor extends HandlerInterceptorAdapter {

    private static LogHelper LOGGER = LogHelper.getInstance(RequestInterceptor.class.getName());

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestId = getUUID();
        ExecutionContext.setRequestId(requestId);
        LOGGER.info("Path: " + request.getServletPath() + " | Remote Address: " + request.getRemoteAddr()
                + " | Params: " + request.getParameterMap());
        return true;
    }

    private String getUUID() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }
}
