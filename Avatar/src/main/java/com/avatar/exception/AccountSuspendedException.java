package com.avatar.exception;

public class AccountSuspendedException extends AccountExistedException {

	public AccountSuspendedException(final String message) {
		super(message);
	}

}
