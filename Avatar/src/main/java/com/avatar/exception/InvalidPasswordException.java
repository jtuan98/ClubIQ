package com.avatar.exception;

public class InvalidPasswordException extends Exception {

	public InvalidPasswordException(final String message) {
		super(message);
	}

	public InvalidPasswordException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InvalidPasswordException(final String message,
			final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidPasswordException(final Throwable cause) {
		super(cause);
	}

}
