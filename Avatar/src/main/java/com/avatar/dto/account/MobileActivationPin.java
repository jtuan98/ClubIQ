package com.avatar.dto.account;

public class MobileActivationPin extends ActivationToken {
	public String getPin() {
		return getToken();
	}

	public void setPin(final String pin) {
		setToken(pin);
	}

	@Override
	public void setToken(final String token) {
		super.setToken(token.substring(token.length()-4));
	}

}
