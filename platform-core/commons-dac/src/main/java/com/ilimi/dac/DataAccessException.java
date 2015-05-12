/*
 * Copyright(c) 2013-2014 Canopus Consutling. All rights reserved.
 *
 * This code is intellectual property of Canopus Consutling. The intellectual and technical concepts contained herein
 * may be covered by patents, patents in process, and are protected by trade secret or copyright law. Any unauthorized
 * use of this code without prior approval from Canopus Consutling is prohibited.
 */
package com.ilimi.dac;

import com.ilimi.graph.common.exception.MiddlewareException;



// TODO: Auto-generated Javadoc
/**
 * An exception thrown during data access. Extends runtime exception so that it
 * is unchecked. But the caller needs to carefully catch the exception.
 *
 * @author Feroz
 */
public class DataAccessException extends MiddlewareException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7034846525956736642L;

    /**
     * Initializes the DAC exception with a given error code and message.
     *
     * @param errCode
     *            Error code from the service
     * @param message
     *            Error message(static). For parameter substitution use the
     *            other constructor
     */
    public DataAccessException(String errCode, String message) {
        super(errCode, message);
    }

    /**
     * Initializes the DAC exception with a template message .
     *
     * @param errCode
     *            Error code from the service
     * @param message
     *            Error message, which conforms to the java.text.MessageFormat
     *            spec
     * @param params
     *            Parameters substituted in the message string
     */
    public DataAccessException(String errCode, String message, Object... params) {
        super(errCode, message, params);
    }

    /**
     * Initializes the DAC exception with a template message .
     *
     * @param errCode
     *            Error code from the service
     * @param message
     *            Error message
     * @param root
     *            Root cause exception
     */
    public DataAccessException(String errCode, String message, Throwable root) {
        super(errCode, message, root);
    }

    /**
     * Initializes the DAC exception with a template message .
     *
     * @param errCode
     *            Error code from the service
     * @param message
     *            Error message, which conforms to the java.text.MessageFormat
     *            spec
     * @param root
     *            Root cause of the exception
     * @param params
     *            Parameters substituted in the message string
     */
    public DataAccessException(String errCode, String message, Throwable root,
            Object... params) {
        super(errCode, message, root, params);
    }
}
