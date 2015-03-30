package com.avatar.dto.enums;

public enum ResponseStatus {
	success(200), failure(500);
	int status;

	private ResponseStatus(final int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
