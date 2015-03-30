package com.avatar.exception;

public class InvalidParameterException extends Exception {

	public InvalidParameterException() {
	}

	public InvalidParameterException(final String message) {
		super(message);
	}

	public InvalidParameterException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InvalidParameterException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidParameterException(final Throwable cause) {
		super(cause);
	}

}
