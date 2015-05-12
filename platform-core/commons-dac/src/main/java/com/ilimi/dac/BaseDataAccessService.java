/*
 * Copyright(c) 2013-2014 Canopus Consutling. All rights reserved.
 *
 * This code is intellectual property of Canopus Consutling. The intellectual and technical concepts contained herein
 * may be covered by patents, patents in process, and are protected by trade secret or copyright law. Any unauthorized
 * use of this code without prior approval from Canopus Consutling is prohibited.
 */
package com.ilimi.dac;

import com.ilimi.graph.common.Response;
//import com.canopus.command.Response;
import com.ilimi.graph.common.dto.BaseValueObject;
//import com.canopus.command.dto.BaseValueObject;
import com.ilimi.graph.common.dto.Status;
import com.ilimi.graph.common.dto.Status.StatusType;



// TODO: Auto-generated Javadoc
/**
 * Base class for DAC services. Provides the implementation of commonly used
 * service methods.
 *
 * @author Feroz
 * 
 * TODO: update all these methods
 * 
 */
public abstract class BaseDataAccessService {

    /**
     * Ok.
     *
     * @param responseIdentifier
     *            the response identifier
     * @param vo
     *            the vo
     * @return the response
     */
    public Response OK(String responseIdentifier, BaseValueObject vo) {
        Response response = new Response();
        response.setStatus(getSucessStatus());
        response.put(responseIdentifier, vo);
        return response;
    }

    /**
     * Ok.
     *
     * @return the response
     */
    public Response OK() {
        Response response = new Response();
        response.setStatus(getSucessStatus());
        return response;
    }

    /**
     * Error.
     *
     * @param e
     *            the e
     * @return the response
     */
    public Response ERROR(Exception e) {
        Response response = new Response();
//        response.addError(e);
        return response;
    }
    
    private Status getSucessStatus() {
        Status status = new Status();
        status.setCode("0");
        status.setStatus(StatusType.SUCCESS.name());
        status.setMessage("Operation successful");
        return status;
    }
}
