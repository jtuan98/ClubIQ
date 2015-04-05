package com.avatar.exception;

public class NotificationException extends Exception {

	public NotificationException() {
	}

	public NotificationException(final String message) {
		super(message);
	}

	public NotificationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public NotificationException(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NotificationException(final Throwable cause) {
		super(cause);
	}

}
