package org.sunbird.jobs.samza.exception;

import org.sunbird.common.exception.MiddlewareException;

public class PlatformException extends MiddlewareException {

	private static final long serialVersionUID = -8708641286413033915L;

	public PlatformException(String errCode, String message) {
        super(errCode, message);
    }

    public PlatformException(String errCode, String message, Object... params) {
        super(errCode, message, params);
    }

    public PlatformException(String errCode, String message, Throwable root) {
        super(errCode, message, root);
    }

    public PlatformException(String errCode, String message, Throwable root, Object... params) {
        super(errCode, message, root, params);
    }
}
