package org.sunbird.common.exception;

import java.text.MessageFormat;

/**
 * Base class for middleware exceptions. Provides the support for returning error
 * code and error message. Each middleware operation should define appropriate 
 * error codes to convey the meaning of the exception. This class mandates the
 * error code parameter in all its constructors. 
 * 
 * @author Feroz
 */
public class MiddlewareException extends RuntimeException {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -3866941321932941766L;
	private String errCode;
    
    /**
     * Initializes the middleware exception with a given error code and message.
     * @param errCode Error code from the service
     * @param message Error message (static). For parameter substitution use the other constructor
     */
    public MiddlewareException(String errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    /**
     * Initializes the middleware exception with a template message 
     * @param errCode Error code from the service
     * @param message Error message, which conforms to the java.text.MessageFormat spec
     * @param params Parameters substituted in the message string
     */
    public MiddlewareException(String errCode, String message, Object... params) {
        super(MessageFormat.format(message, params));
        this.errCode = errCode;
    }

    /**
     * Initializes the middleware exception with a template message 
     * @param errCode Error code from the service
     * @param message Error message 
     * @param root Root cause exception
     */
    public MiddlewareException(String errCode, String message, Throwable root) {
        super(message, root);
        this.errCode = errCode;
    }
    
    /**
     * Initializes the middleware exception with a template message 
     * @param errCode Error code from the service
     * @param root Root cause of the exception
     * @param message Error message, which conforms to the java.text.MessageFormat spec
     * @param params Parameters substituted in the message string
     */
    public MiddlewareException(String errCode, String message, Throwable root, Object... params) {
        super(MessageFormat.format(message, params), root);
        this.errCode = errCode;
    }

    /**
     * @return the errCode
     */
    public String getErrCode() {
        return errCode;
    }
    
    public ResponseCode getResponseCode() {
        return ResponseCode.SERVER_ERROR;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(errCode).append(": ");
        builder.append(super.getMessage());
        return builder.toString();
    }
}
