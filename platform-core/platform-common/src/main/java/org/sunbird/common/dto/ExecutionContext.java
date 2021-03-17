package org.sunbird.common.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 
 * @author rayulu
 * 
 */

public class ExecutionContext {

    public static String USER_ID = "userId";
    public static String USER_ROLE = "userRole";

    private static ThreadLocal<ExecutionContext> context = new ThreadLocal<ExecutionContext>() {

        @Override
        protected ExecutionContext initialValue() {
            ExecutionContext context = new ExecutionContext();
            return context;
        }

    };

    public static ExecutionContext getCurrent() {
        return context.get();
    }
    
    public static void setRequestId(String requestId) {
        ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.REQUEST_ID.getParamName(), requestId);
    }
    
    public static String getRequestId() {
        return (String) ExecutionContext.getCurrent().getGlobalContext().get(HeaderParam.REQUEST_ID.getParamName());
    }

    Stack<String> serviceCallStack = new Stack<String>();

    private Map<String, Map<String, Object>> contextStackValues = new HashMap<String, Map<String, Object>>();
    private Map<String, Object> globalContext = new HashMap<String, Object>();

    public Map<String, Object> getContextValues() {
        String serviceCallStack = getServiceCallStack();
        Map<String, Object> contextValues = contextStackValues.get(serviceCallStack);
        if (contextValues == null) {
            contextValues = new HashMap<String, Object>();
            setContextValues(contextValues, serviceCallStack);
        }

        return contextStackValues.get(serviceCallStack);
    }

    public void setContextValues(Map<String, Object> currentContextValues) {
        this.contextStackValues.put(getServiceCallStack(), new HashMap<String, Object>(currentContextValues));
    }

    public void setContextValues(Map<String, Object> currentContextValues, String serviceCallStack) {
        this.contextStackValues.put(serviceCallStack, new HashMap<String, Object>(currentContextValues));
    }

    public void removeContext() {
        this.contextStackValues.remove(getServiceCallStack());
    }

    public void cleanup() {
        removeContext();
        pop();
        if (serviceCallStack.size() == 0) {
            this.globalContext.remove(HeaderParam.REQUEST_ST_ED_PATH.getParamName());
        }
    }

    // TODO move Response out of context
    public Response getResponse() {
        Response contextResponse = (Response) ExecutionContext.getCurrent().getContextValues().get("RESPONSE");
        if (contextResponse == null) {
            contextResponse = new Response();
            ExecutionContext.getCurrent().getContextValues().put("RESPONSE", contextResponse);
        }
        return contextResponse;
    }

    public void push(String methodName) {
        serviceCallStack.push(methodName);
    }

    public String pop() {
        return serviceCallStack.pop();
    }

    public String peek() {
        return serviceCallStack.peek();
    }

    public String getServiceCallStack() {
        String serviceCallPath = "";
        for (String value : serviceCallStack) {
            if (serviceCallPath.equals(""))
                serviceCallPath = value;
            else
                serviceCallPath = serviceCallPath + "/" + value;
        }

        if (serviceCallPath.equals("")) {
            serviceCallStack.push("default");
            return "default";
        }
        return serviceCallPath;
    }

    public Map<String, Object> getGlobalContext() {
        return globalContext;
    }

    public void setGlobalContext(Map<String, Object> globalContext) {
        this.globalContext = globalContext;
    }

    /**
     * This should be called when performing cross tenant operations (like
     * operations performed by system admins, cache reload etc)
     * 
     * @param Nothing
     *            .
     * 
     * @return void
     * 
     * 
     */

    // public void setCrossTenant() {
    // Map<String, Object> contextValues =
    // ExecutionContext.getCurrent().getContextValues();
    // if(contextValues != null) {
    // ExecutionContext.getCurrent().getContextValues().put(HeaderParam.CROSS_TENANT.getParamName(),
    // true);
    //
    //
    // Integer tenantId = (Integer)
    // contextValues.get(HeaderParam.TENANT_ID.getParamName());
    // if(tenantId == null) {
    // contextValues.put(HeaderParam.TENANT_ID.getParamName(),
    // getSystemTenantId());
    // }
    //
    // }
    //
    // }

    /**
     * This should be called to swith back to noraml mode from cross Tenant
     * operation.
     * 
     * @param Nothing
     *            .
     * 
     * @return void
     * 
     * 
     */

    // public void unSetCrossTenant() {
    // Map<String, Object> contextValues =
    // ExecutionContext.getCurrent().getContextValues();
    // if(contextValues != null) {
    // ExecutionContext.getCurrent().getContextValues().put(HeaderParam.CROSS_TENANT.getParamName(),
    // false);
    // }
    // }

    /**
     * This is to know the value of the cross tenant.
     * 
     * @param Nothing
     *            .
     * 
     * @return boolean
     * 
     * 
     */

    // public boolean isCrossTenant() {
    // if(ExecutionContext.getCurrent().getContextValues().containsKey(HeaderParam.CROSS_TENANT.getParamName())){
    // return (Boolean)
    // ExecutionContext.getCurrent().getContextValues().get(HeaderParam.CROSS_TENANT.getParamName());
    // } else {
    // return false;
    // }
    //
    // }

    // public static Integer getTenantId() {
    // ExecutionContext context = ExecutionContext.getCurrent();
    // Map<String, Object> contextValues = context.getContextValues();
    // Integer tenantId = (Integer)
    // contextValues.get(HeaderParam.TENANT_ID.getParamName());
    //
    // if(tenantId == null) {
    // throw new MiddlewareException(MiddlewareService.ERR_NO_TENANT_TO_CXT,
    // "No tenant id set to the context.");
    // } else {
    // return tenantId;
    // }
    // }

    // public static void setTenantId(Integer tenantId) {
    // ExecutionContext context = ExecutionContext.getCurrent();
    // Map<String, Object> contextValues = context.getContextValues();
    // contextValues.put(HeaderParam.TENANT_ID.getParamName(), tenantId);
    // }
    //
    // public static Integer getSystemTenantId() {
    // return SYSTEM_TENANT_ID;
    // }
}
