package com.avatar.dto.club;

import java.io.Serializable;

import com.avatar.dto.account.AccountDto;
import com.avatar.dto.enums.Location;

public class BeaconDto implements Serializable {
	protected Integer id; // primary key.
	protected String beaconid;
	protected ClubDto club;
	protected AmenityDto amenity;
	protected Location location;
	protected String description;
	protected AccountDto installerStaff;

	public AmenityDto getAmenity() {
		return amenity;
	}

	public String getBeaconid() {
		return beaconid;
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

	public AccountDto getInstallerStaff() {
		return installerStaff;
	}

	public Location getLocation() {
		return location;
	}

	public void setAmenity(final AmenityDto amenity) {
		this.amenity = amenity;
	}

	public AmenityDto setAmenityId(final String amenityId) {
		if (amenity == null) {
			amenity = new AmenityDto();
		}
		amenity.setAmenityId(amenityId);
		return amenity;
	}

	public void setBeaconid(final String beaconid) {
		this.beaconid = beaconid;
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

	public void setInstallerStaff(final AccountDto installerStaff) {
		this.installerStaff = installerStaff;
	}

	public AccountDto setInstallerStaffId(final String userId) {
		if (installerStaff == null) {
			installerStaff = new AccountDto();
		}
		installerStaff.setUserId(userId);
		return installerStaff;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}
