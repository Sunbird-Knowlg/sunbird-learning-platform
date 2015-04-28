package com.ilimi.taxonomy.controller;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.Status;
import com.ilimi.graph.common.dto.Status.StatusType;
import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.MiddlewareException;
import com.ilimi.graph.common.exception.ResourceNotFoundException;
import com.ilimi.taxonomy.enums.TaxonomyErrorCodes;

public abstract class BaseController {

    protected ResponseEntity<Response> getResponseEntity(Response response) {
        int statusCode = response.getResponseCode().code();
        HttpStatus status = getStatus(statusCode);
        return new ResponseEntity<Response>(response, status);
    }

    protected Response getErrorResponse(Exception e) {
        Response response = new Response();
        Status resStatus = new Status();
        resStatus.setMessage(e.getMessage());
        resStatus.setStatus(StatusType.ERROR.name());
        if (e instanceof MiddlewareException) {
            MiddlewareException me = (MiddlewareException) e;
            resStatus.setCode(me.getErrCode());
        } else {
            resStatus.setCode(TaxonomyErrorCodes.SYSTEM_ERROR.name());
        }
        response.setStatus(resStatus);
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

    protected ResponseEntity<Response> getExceptionResponseEntity(Exception e) {
        HttpStatus status = getHttpStatus(e);
        Response response = getErrorResponse(e);
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

    protected void writeToResponse(Status status, String content, String contentType, HttpServletResponse response) throws Exception {
        response.setContentType(contentType);
        OutputStream resOs = response.getOutputStream();
        OutputStream buffOs = new BufferedOutputStream(resOs);
        OutputStreamWriter outputwriter = new OutputStreamWriter(buffOs);
        outputwriter.write(status.toString() + "\n");
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
            writeToResponse(appResponse.getStatus(), e.getMessage(), "text/csv;charset=utf-8", response);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
