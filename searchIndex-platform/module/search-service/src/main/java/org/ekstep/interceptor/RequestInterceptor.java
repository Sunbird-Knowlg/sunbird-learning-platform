package org.ekstep.interceptor;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.ilimi.common.dto.ExecutionContext;

@Component
public class RequestInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestId = getUUID();
        ExecutionContext.setRequestId(requestId);
        PlatformLogger.log("Path: " + request.getServletPath() + " | Remote Address: " + request.getRemoteAddr()
                + " | Params: " + request.getParameterMap());
        return true;
    }

    private String getUUID() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }
}
