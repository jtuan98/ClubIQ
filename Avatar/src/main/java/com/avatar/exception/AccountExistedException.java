package com.avatar.exception;

public class AccountExistedException extends AccountCreationException {

	public AccountExistedException() {
		super();
	}

	public AccountExistedException(final String message) {
		super(message);
	}

	public AccountExistedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AccountExistedException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AccountExistedException(final Throwable cause) {
		super(cause);
	}

}
