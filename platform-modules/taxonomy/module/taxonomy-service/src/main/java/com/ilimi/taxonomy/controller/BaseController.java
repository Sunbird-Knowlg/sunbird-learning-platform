package com.ilimi.taxonomy.controller;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.RequestParams;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.taxonomy.enums.TaxonomyErrorCodes;

public abstract class BaseController {

    private static final String API_ID_PREFIX = "ekstep.lp";
    private static final String API_VERSION = "1.0";

    protected ResponseEntity<Response> getResponseEntity(Response response, String apiId, String msgId) {
        int statusCode = response.getResponseCode().code();
        HttpStatus status = getStatus(statusCode);
        setResponseEnvelope(response, apiId, msgId);
        return new ResponseEntity<Response>(response, status);
    }

    protected Response getErrorResponse(Exception e) {
        Response response = new Response();
        ResponseParams resStatus = new ResponseParams();
        resStatus.setErrmsg(e.getMessage());
        resStatus.setStatus(StatusType.failed.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException me = (MiddlewareException) e;
            resStatus.setErr(me.getErrCode());
            response.setResponseCode(me.getResponseCode());
        } else {
            resStatus.setErr(TaxonomyErrorCodes.SYSTEM_ERROR.name());
            response.setResponseCode(ResponseCode.SERVER_ERROR);
        }
        response.setParams(resStatus);
        return response;
    }

    protected HttpStatus getHttpStatus(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (e instanceof ClientException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (e instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        return status;
    }

    protected ResponseEntity<Response> getExceptionResponseEntity(Exception e, String apiId, String msgId) {
        HttpStatus status = getHttpStatus(e);
        Response response = getErrorResponse(e);
        setResponseEnvelope(response, apiId, msgId);
        return new ResponseEntity<Response>(response, status);
    }

    protected HttpStatus getStatus(int statusCode) {
        HttpStatus status = null;
        try {
            status = HttpStatus.valueOf(statusCode);
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return status;
    }

    protected void writeToResponse(ResponseParams params, String content, String contentType, HttpServletResponse response)
            throws Exception {
        response.setContentType(contentType);
        OutputStream resOs = response.getOutputStream();
        OutputStream buffOs = new BufferedOutputStream(resOs);
        OutputStreamWriter outputwriter = new OutputStreamWriter(buffOs);
        outputwriter.write(params.toString() + "\n");
        if (StringUtils.isNotBlank(content)) {
            outputwriter.write(content);
        }
        outputwriter.flush();
        outputwriter.close();
    }

    protected void writeError(Exception e, HttpServletResponse response) {
        Response appResponse = getErrorResponse(e);
        HttpStatus status = getHttpStatus(e);
        response.setStatus(status.value());
        try {
            writeToResponse(appResponse.getParams(), e.getMessage(), "text/csv;charset=utf-8", response);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    protected Request getRequest(Map<String, Object> requestMap) {
        Request request = new Request();
        if (null != requestMap && !requestMap.isEmpty()) {
            String id = (String) requestMap.get("id");
            String ver = (String) requestMap.get("ver");
            String ts = (String) requestMap.get("ts");
            request.setId(id);
            request.setVer(ver);
            request.setTs(ts);
            ObjectMapper mapper = new ObjectMapper();
            Object reqParams = requestMap.get("params");
            if (null != reqParams) {
                try {
                    RequestParams params = (RequestParams) mapper.convertValue(reqParams, RequestParams.class);
                    request.setParams(params);
                } catch (Exception e) {
                }
            }
            Object requestObj = requestMap.get("request");
            if (null != requestObj) {
                try {
                    String strRequest = mapper.writeValueAsString(requestObj);
                    Map<String, Object> map = mapper.readValue(strRequest, Map.class);
                    if (null != map && !map.isEmpty())
                        request.setRequest(map);
                } catch (Exception e) {
                }
            }
        }
        return request;
    }

    private void setResponseEnvelope(Response response, String apiId, String msgId) {
        if (null != response) {
            response.setId(API_ID_PREFIX + "." + apiId);
            response.setVer(API_VERSION);
            response.setTs(getResponseTimestamp());
            ResponseParams params = response.getParams();
            if (null == params)
                params = new ResponseParams();
            if (StringUtils.isNotBlank(msgId))
                params.setMsgid(msgId);
            params.setResmsgid(getUUID());
            if (StringUtils.equalsIgnoreCase(ResponseParams.StatusType.successful.name(), params.getStatus())) {
                params.setErr(null);
                params.setErrmsg(null);
            }
            response.setParams(params);
        }
    }

    private String getResponseTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'XXX");
        return sdf.format(new Date());
    }

    private String getUUID() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }

}
