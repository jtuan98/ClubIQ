package com.avatar.dto.club;

import java.io.Serializable;

public class BlackoutDate implements Serializable {
	private String date;

	public String getDate() {
		return date;
	}

	public void setDate(final String date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return date;
	}
}
