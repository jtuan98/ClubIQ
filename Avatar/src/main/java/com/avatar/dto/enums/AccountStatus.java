package com.avatar.dto.enums;

public enum AccountStatus {
	New(0), TokenSent(1), Activated(2), Terminated(-1), Cancelled(-2);
	public static AccountStatus convert(final int status) {
		for (final AccountStatus val : values()) {
			if (val.getStatusNo() == status) {
				return val;
			}
		}
		return null;
	}

	private int statusNo;

	private AccountStatus(final int statusNo) {
		this.statusNo = statusNo;
	}

	public int getStatusNo() {
		return statusNo;
	}
}
