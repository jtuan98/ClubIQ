package com.avatar.business;

import java.util.List;

import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.BeaconDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.exception.NotFoundException;

public interface BeaconBusiness {
	void addUserIdToBeacon(String beaconId, String userId)
			throws NotFoundException;

	List<AmenityDto> getAmenities(String clubId) throws NotFoundException;

	List<String> getAmenityDeptName(String clubId) throws NotFoundException;

	List<AccountDto> getUsers(String beaconId, String amenityDepartment)
			throws NotFoundException;

	void setAmenityDeptName(String clubId, String apnsToken,
			String amenityDepartment) throws NotFoundException;

	void update(ClubDto club) throws NotFoundException;

	BeaconDto updateBeacon(BeaconDto beacon) throws NotFoundException;
}
