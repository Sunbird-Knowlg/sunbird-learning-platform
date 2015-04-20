package com.ilimi.taxonomy.controller;

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

    protected ResponseEntity<Response> getExceptionResponseEntity(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (e instanceof ClientException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (e instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
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

}
