package com.avatar.dto.club;

import java.io.Serializable;
import java.util.Date;

import com.avatar.dto.account.MemberAccountDto;

public class CheckInfo implements Serializable {
	private Integer id;
	private String availId;
	private String requestedClubId;
	private String subAmenityId;
	private String subAmenityName;
	private int personNumber;
	private Date requestedDateTime;// in yyyymmddhh24mi
	private Date requestedToDate;// in yyyymmdd
	private MemberAccountDto member;

	public String getAvailId() {
		return availId;
	}

	public Integer getId() {
		return id;
	}

	public MemberAccountDto getMember() {
		return member;
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

	public Date getRequestedToDate() {
		return requestedToDate;
	}

	public String getSubAmenityId() {
		return subAmenityId;
	}

	public String getSubAmenityName() {
		return subAmenityName;
	}

	public void setAvailId(final String availId) {
		this.availId = availId;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setMember(final MemberAccountDto member) {
		this.member = member;
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

	public void setRequestedToDate(final Date requestedToDate) {
		this.requestedToDate = requestedToDate;
	}

	public void setSubAmenityId(final String subAmenityId) {
		this.subAmenityId = subAmenityId;
	}

	public void setSubAmenityName(final String subAmenityName) {
		this.subAmenityName = subAmenityName;
	}
}
