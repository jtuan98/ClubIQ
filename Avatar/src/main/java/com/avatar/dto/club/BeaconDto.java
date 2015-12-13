package com.avatar.dto.club;

import java.io.Serializable;
import java.util.Date;

import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.EmployeeAccountDto;

public class BeaconDto implements Serializable {
	protected Integer id; // primary key.
	protected String beaconActionId;
	protected ClubDto club;
	protected SubAmenityDto subAmenity;
	protected String location;
	protected String description;
	protected AccountDto installerStaff;
	protected Date installationDate;

	public String getBeaconActionId() {
		return beaconActionId;
	}

	public ClubDto getClub() {
		return club;
	}

	public String getDescription() {
		return description;
	}

	public Integer getId() {
		return id;
	}

	public Date getInstallationDate() {
		return installationDate;
	}

	public AccountDto getInstallerStaff() {
		return installerStaff;
	}

	public String getLocation() {
		return location;
	}

	public SubAmenityDto getSubAmenity() {
		return subAmenity;
	}

	public void setBeaconActionId(final String beaconid) {
		beaconActionId = beaconid;
	}

	public void setClub(final ClubDto club) {
		this.club = club;
	}

	public ClubDto setClubId(final String clubId) {
		if (club == null) {
			club = new ClubDto();
		}
		club.setClubId(clubId);
		return club;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setInstallationDate(final Date installationDate) {
		this.installationDate = installationDate;
	}

	public void setInstallerStaff(final AccountDto installerStaff) {
		this.installerStaff = installerStaff;
	}

	public AccountDto setInstallerStaffId(final String userId) {
		if (installerStaff == null) {
			installerStaff = new EmployeeAccountDto();
		}
		installerStaff.setUserId(userId);
		return installerStaff;
	}

	public void setLocation(final String location) {
		this.location = location;
	}

	public void setSubAmenity(final SubAmenityDto subAmenity) {
		this.subAmenity = subAmenity;
	}

	public SubAmenityDto setSubAmenityId(final String subAmenityId) {
		if (subAmenity == null) {
			subAmenity = new SubAmenityDto();
		}
		subAmenity.setSubAmenityId(subAmenityId);
		return subAmenity;
	}
}
