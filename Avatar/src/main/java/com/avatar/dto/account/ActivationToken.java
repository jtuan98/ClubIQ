package com.avatar.dto.account;

import java.io.Serializable;
import java.util.Date;

public class ActivationToken implements Serializable {

	protected Integer id; //PK

	private String token;

	private Date expirationDate;

	public Date getExpirationDate() {
		return expirationDate;
	}

	public Integer getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public void setExpirationDate(final Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setToken(final String token) {
		this.token = token;
	}
}
