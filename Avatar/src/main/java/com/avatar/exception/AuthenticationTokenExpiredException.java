package com.avatar.exception;

public class AuthenticationTokenExpiredException extends Exception{

	public AuthenticationTokenExpiredException(final String message) {
		super(message);
	}

	public AuthenticationTokenExpiredException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AuthenticationTokenExpiredException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AuthenticationTokenExpiredException(final Throwable cause) {
		super(cause);
	}

}
