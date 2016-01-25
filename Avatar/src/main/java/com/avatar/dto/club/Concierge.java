package com.avatar.dto.club;

import java.io.Serializable;

public class Concierge implements Serializable {
	private String firstName;
	private String lastName;
	private String notifEmail;
	private String adminEmail;

	public Concierge(final String firstName, final String lastName,
			final String notifEmail, final String adminEmail) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.notifEmail = notifEmail;
		this.adminEmail = adminEmail;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getNotifEmail() {
		return notifEmail;
	}

	public void setAdminEmail(final String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	public void setNotifEmail(final String notifEmail) {
		this.notifEmail = notifEmail;
	}

}
