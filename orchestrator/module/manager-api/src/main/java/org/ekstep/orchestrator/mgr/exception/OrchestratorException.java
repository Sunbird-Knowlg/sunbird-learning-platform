package org.ekstep.orchestrator.mgr.exception;

import java.text.MessageFormat;

public class OrchestratorException extends RuntimeException {

	private static final long serialVersionUID = -2063800290989853701L;

	private String errCode;

	/**
	 * Initializes the orchestrator exception with a given error code and
	 * message.
	 * 
	 * @param errCode
	 *            Error code from the service
	 * @param message
	 *            Error message (static). For parameter substitution use the
	 *            other constructor
	 */
	public OrchestratorException(String errCode, String message) {
		super(message);
		this.errCode = errCode;
	}

	/**
	 * Initializes the orchestrator exception with a template message
	 * 
	 * @param errCode
	 *            Error code from the service
	 * @param message
	 *            Error message, which conforms to the java.text.MessageFormat
	 *            spec
	 * @param params
	 *            Parameters substituted in the message string
	 */
	public OrchestratorException(String errCode, String message, Object... params) {
		super(MessageFormat.format(message, params));
		this.errCode = errCode;
	}

	/**
	 * Initializes the orchestrator exception with a template message
	 * 
	 * @param errCode
	 *            Error code from the service
	 * @param message
	 *            Error message
	 * @param root
	 *            Root cause exception
	 */
	public OrchestratorException(String errCode, String message, Throwable root) {
		super(message, root);
		this.errCode = errCode;
	}

	/**
	 * Initializes the orchestrator exception with a template message
	 * 
	 * @param errCode
	 *            Error code from the service
	 * @param root
	 *            Root cause of the exception
	 * @param message
	 *            Error message, which conforms to the java.text.MessageFormat
	 *            spec
	 * @param params
	 *            Parameters substituted in the message string
	 */
	public OrchestratorException(String errCode, String message, Throwable root, Object... params) {
		super(MessageFormat.format(message, params), root);
		this.errCode = errCode;
	}

	/**
	 * @return the errCode
	 */
	public String getErrCode() {
		return errCode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(errCode).append(": ");
		builder.append(super.getMessage());
		return builder.toString();
	}

}
