package com.avatar.exception;

public class AccountNotificationException extends Exception {

	public AccountNotificationException() {
	}

	public AccountNotificationException(final String message) {
		super(message);
	}

	public AccountNotificationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AccountNotificationException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AccountNotificationException(final Throwable cause) {
		super(cause);
	}

}
