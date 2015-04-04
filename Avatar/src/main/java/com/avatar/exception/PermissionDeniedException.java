package com.avatar.exception;

public class PermissionDeniedException extends Exception{

	public PermissionDeniedException(final String message) {
		super(message);
	}

	public PermissionDeniedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public PermissionDeniedException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PermissionDeniedException(final Throwable cause) {
		super(cause);
	}

}
