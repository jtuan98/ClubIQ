package com.avatar.dto.account;

public class MobileAccountDto extends AccountDto {

	protected String mobileNumber;

	protected String deviceId;

	protected String tangerineHandsetId;

	public String getDeviceId() {
		return deviceId;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public String getTangerineHandsetId() {
		return tangerineHandsetId;
	}

	public void setDeviceId(final String deviceId) {
		this.deviceId = deviceId;
	}

	public void setMobileNumber(final String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public void setTangerineHandsetId(final String tangerineHandsetId) {
		this.tangerineHandsetId = tangerineHandsetId;
	}
}
