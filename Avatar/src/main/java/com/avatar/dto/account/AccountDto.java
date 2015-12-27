package com.avatar.dto.account;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.avatar.dto.ImagePic;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.AccountStatus;
import com.avatar.dto.enums.Privilege;

public abstract class AccountDto implements Serializable {

	protected Integer id; // primary key.

	protected String userId;

	protected ActivationToken token;

	protected Set<Privilege> priviledges;

	protected ImagePic picture;

	protected String password;

	protected String name;

	protected String address;

	protected String email;

	protected AccountStatus status;

	protected ClubDto homeClub;

	protected String mobileNumber;

	protected String linkMobileNumber;

	protected String deviceId;

	protected String tangerineHandsetId;

	protected Date actDate;

	protected Date susDate;

	protected boolean training = false;

	protected List<AccountNotes> noteHistory;

	protected Date lastCheckInDate;

	public AccountDto add(final AccountNotes note) {
		if (noteHistory == null) {
			noteHistory = new LinkedList<AccountNotes>();
		}
		noteHistory.add(note);
		return this;
	}

	public AccountDto add(final Privilege priviledge) {
		if (priviledge != null) {
			if (priviledges == null) {
				priviledges = new HashSet<Privilege>();
			}
			priviledges.add(priviledge);
		}
		return this;
	}

	public Date getActDate() {
		return actDate;
	}

	public String getAddress() {
		return address;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getEmail() {
		return email;
	}

	public ClubDto getHomeClub() {
		return homeClub;
	}

	public Integer getId() {
		return id;
	}

	public Date getLastCheckInDate() {
		return lastCheckInDate;
	}

	public String getLinkMobileNumber() {
		return linkMobileNumber;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public String getName() {
		return name;
	}

	public List<AccountNotes> getNoteHistory() {
		return noteHistory;
	}

	public String getPassword() {
		return password;
	}

	public ImagePic getPicture() {
		return picture;
	}

	public Set<Privilege> getPriviledges() {
		return priviledges;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public Date getSusDate() {
		return susDate;
	}

	public String getTangerineHandsetId() {
		return tangerineHandsetId;
	}

	public ActivationToken getToken() {
		return token;
	}

	public String getUserId() {
		return userId;
	}

	public boolean isStaff() {
		return (this instanceof EmployeeAccountDto);
	}

	public boolean isTraining() {
		return training;
	}

	public void setActDate(final Date activationDate) {
		this.actDate = activationDate;
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public void setDeviceId(final String deviceId) {
		this.deviceId = deviceId;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public void setHomeClub(final ClubDto homeClub) {
		this.homeClub = homeClub;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setLastCheckInDate(final Date lastCheckInDate) {
		this.lastCheckInDate = lastCheckInDate;
	}

	public void setLinkMobileNumber(final String linkMobileNumber) {
		this.linkMobileNumber = linkMobileNumber;
	}

	public void setMobileNumber(final String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setNoteHistory(final List<AccountNotes> noteHistory) {
		this.noteHistory = noteHistory;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public void setPicture(final ImagePic picture) {
		this.picture = picture;
	}

	public void setPriviledges(final Set<Privilege> priviledges) {
		this.priviledges = priviledges;
	}

	public void setStatus(final AccountStatus status) {
		this.status = status;
	}

	public void setSusDate(final Date suspendedDate) {
		this.susDate = suspendedDate;
	}

	public void setTangerineHandsetId(final String tangerineHandsetId) {
		this.tangerineHandsetId = tangerineHandsetId;
	}

	public void setToken(final ActivationToken token) {
		this.token = token;
	}

	public void setTraining(final boolean training) {
		this.training = training;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}
}
