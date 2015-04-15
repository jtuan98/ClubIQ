package com.avatar.dao;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.BeaconDto;
import com.avatar.exception.NotFoundException;

public interface BeaconDao {

	void addUserIdToBeaconMapping(String beaconId, String userId)
			throws NotFoundException;

	// Return Amenity Names
	List<String> getAmenityDeptName(String clubId) throws NotFoundException;

	Integer getAmenityIdPk(Integer beaconIdPk) throws NotFoundException;

	Integer getBeaconIdPk(String beaconId) throws NotFoundException;

	Integer getClubIdPkByBeaconIdPk(Integer beaconIdPk)
			throws NotFoundException;

	List<ImmutablePair<AccountDto, Date>> getUsers(String amenityDepartment, Date onDate);

	void setAmenityDeptName(String clubId, String apnsToken,
			String amenityDepartment) throws NotFoundException;

	void setApnsToken(final String clubId, final String apnsToken)
			throws NotFoundException;

	void updateBeaconInfo(BeaconDto beacon) throws NotFoundException;
}
