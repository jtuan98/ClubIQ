package com.avatar.dto.club;

import java.io.Serializable;

public class CheckInfo implements Serializable {
	private String availId;
	private String requestedClubId;
	private String amenityId;
	private String amenityName;
	private int personNumber;
	private String requestedDateTime;// in yyyymmddhh24mi

	public String getAmenityId() {
		return amenityId;
	}

	public String getAmenityName() {
		return amenityName;
	}

	public String getAvailId() {
		return availId;
	}

	public int getPersonNumber() {
		return personNumber;
	}

	public String getRequestedClubId() {
		return requestedClubId;
	}

	public String getRequestedDateTime() {
		return requestedDateTime;
	}

	public void setAmenityId(final String amenityId) {
		this.amenityId = amenityId;
	}

	public void setAmenityName(final String amenityName) {
		this.amenityName = amenityName;
	}

	public void setAvailId(final String availId) {
		this.availId = availId;
	}

	public void setPersonNumber(final int personNumber) {
		this.personNumber = personNumber;
	}

	public void setRequestedClubId(final String requestedClubId) {
		this.requestedClubId = requestedClubId;
	}

	public void setRequestedDateTime(final String requestedDateTime) {
		this.requestedDateTime = requestedDateTime;
	}

}
