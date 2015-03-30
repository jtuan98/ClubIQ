package com.avatar.exception;

public class InvalidDeviceId extends AccountNotificationException {

	public InvalidDeviceId() {
	}

	public InvalidDeviceId(final String message) {
		super(message);
	}

	public InvalidDeviceId(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InvalidDeviceId(final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidDeviceId(final Throwable cause) {
		super(cause);
	}

}
