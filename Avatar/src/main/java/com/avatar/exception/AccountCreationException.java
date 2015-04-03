package com.avatar.exception;

public class AccountCreationException extends Exception{

	public AccountCreationException(final String message) {
		super(message);
	}

	public AccountCreationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AccountCreationException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AccountCreationException(final Throwable cause) {
		super(cause);
	}

}
