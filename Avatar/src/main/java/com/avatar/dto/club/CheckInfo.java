package com.avatar.dto.club;

import java.io.Serializable;
import java.util.Date;

public class CheckInfo implements Serializable {
	private Integer id;
	private String availId;
	private String requestedClubId;
	private String amenityId;
	private String amenityName;
	private int personNumber;
	private Date requestedDateTime;// in yyyymmddhh24mi

	public String getAmenityId() {
		return amenityId;
	}

	public String getAmenityName() {
		return amenityName;
	}

	public String getAvailId() {
		return availId;
	}

	public Integer getId() {
		return id;
	}

	public int getPersonNumber() {
		return personNumber;
	}

	public String getRequestedClubId() {
		return requestedClubId;
	}

	public Date getRequestedDateTime() {
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

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setPersonNumber(final int personNumber) {
		this.personNumber = personNumber;
	}

	public void setRequestedClubId(final String requestedClubId) {
		this.requestedClubId = requestedClubId;
	}

	public void setRequestedDateTime(final Date requestedDateTime) {
		this.requestedDateTime = requestedDateTime;
	}

}
