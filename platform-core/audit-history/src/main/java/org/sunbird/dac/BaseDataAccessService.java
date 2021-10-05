/*
 * Copyright(c) 2013-2014 Canopus Consutling. All rights reserved.
 *
 * This code is intellectual property of Canopus Consutling. The intellectual and technical concepts contained herein
 * may be covered by patents, patents in process, and are protected by trade secret or copyright law. Any unauthorized
 * use of this code without prior approval from Canopus Consutling is prohibited.
 */
package org.sunbird.dac;

import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.dto.ResponseParams.StatusType;

/**
 * Base class for DAC services. Provides the implementation of commonly used
 * service methods.
 *
 * @author Feroz
 * 
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
    public Response OK(String responseIdentifier, Object vo) {
        Response response = new Response();
        response.setParams(getSucessStatus());
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
        response.setParams(getSucessStatus());
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
    
    private ResponseParams getSucessStatus() {
        ResponseParams params = new ResponseParams();
        params.setErr("0");
        params.setStatus(StatusType.successful.name());
        params.setErrmsg("Operation successful");
        return params;
    }
}
