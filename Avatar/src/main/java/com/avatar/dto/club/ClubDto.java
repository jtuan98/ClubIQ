package com.avatar.dto.club;

import java.io.Serializable;

import com.avatar.dto.ImagePic;

public class ClubDto implements Serializable {

	protected Integer id; // primary key.
	protected String clubId;
	protected String clubName;
	protected ImagePic image;
	protected String address;
	protected String zipCode;
	protected String city;
	protected String state;
	protected String phoneNumber;
	protected String clubType;
	protected String webSite;
	protected String hzRestriction;

	public ClubDto() {
	}

	public ClubDto(final Integer id) {
		this.id = id;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public String getClubId() {
		return clubId;
	}

	public String getClubName() {
		return clubName;
	}

	public String getClubType() {
		return clubType;
	}

	public String getHzRestriction() {
		return hzRestriction;
	}

	public Integer getId() {
		return id;
	}

	public ImagePic getImage() {
		return image;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getState() {
		return state;
	}

	public String getWebSite() {
		return webSite;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public void setCity(final String city) {
		this.city = city;
	}

	public void setClubId(final String clubId) {
		this.clubId = clubId;
	}

	public void setClubName(final String clubName) {
		this.clubName = clubName;
	}

	public void setClubType(final String clubType) {
		this.clubType = clubType;
	}

	public void setHzRestriction(final String hzRestriction) {
		this.hzRestriction = hzRestriction;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setImage(final ImagePic image) {
		this.image = image;
	}

	public void setPhoneNumber(final String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setState(final String state) {
		this.state = state;
	}

	public void setWebSite(final String webSite) {
		this.webSite = webSite;
	}

	public void setZipCode(final String zipCode) {
		this.zipCode = zipCode;
	}
}
