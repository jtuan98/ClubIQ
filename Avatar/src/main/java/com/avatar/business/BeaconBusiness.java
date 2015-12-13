package com.avatar.business;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.BeaconDto;
import com.avatar.dto.club.BlackoutDate;
import com.avatar.dto.club.BlackoutTime;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.club.SubAmenityDto;
import com.avatar.dto.enums.ClubListingSortBy;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

public interface BeaconBusiness {
	void addUserIdToBeacon(String beaconId, String userId)
			throws NotFoundException, InvalidParameterException;

	void deleteBeacon(BeaconDto beacon) throws NotFoundException, PermissionDeniedException;

	List<AmenityDto> getAmenities(String clubId) throws NotFoundException;

	String getAmenityHeaderText(String clubId, String amenityId) throws NotFoundException;

	BeaconDto getBeacon(String beaconActionId) throws NotFoundException;

	List<BeaconDto> getBeacons(String clubId, String subAmenityId)
			throws NotFoundException;

	List<BlackoutDate> getBlackoutDates(String clubId, String subAmenityId,
			String month) throws NotFoundException;

	List<BlackoutTime> getBlackoutTimes(String clubId, String subAmenityId,
			String requestedDateMMDD) throws NotFoundException;

	ClubDto getClub(String clubId) throws NotFoundException;

	String getClubBodyText(String clubId) throws NotFoundException;

	ClubDto getClubByKeycode(String clubKeycode)throws NotFoundException;

	ClubDto getClubDetails(String clubId) throws NotFoundException;

	String getClubHeadlineText(String clubId) throws NotFoundException;

	List<ClubDto> getClubs(Integer userIdPk) throws NotFoundException;

	List<ClubDto> getClubs(String state, ClubListingSortBy clubname) throws NotFoundException;

	String getSubAmenityBodyText(String clubId, String amenityId) throws NotFoundException;

	List<String> getSubAmenityDeptName(String clubId) throws NotFoundException;

	String getSubAmenityHeaderText(String clubId, String subAmenityId)throws NotFoundException;

	List<SubAmenityDto> getSubAmenityList(String clubId, String amenityType) throws NotFoundException;

	String getSubAmenitySecondaryHeaderText(String clubId, String amenityId)throws NotFoundException;

	List<ImmutablePair<AccountDto, Date>> getUsers(String amenityId, Date onDate)
			throws NotFoundException;

	void setSubAmenityId(String clubId, String apnsToken,
			String amenityId, String subAmenityId) throws NotFoundException;

	void setAmenityHeaderText(String clubId, String amenityId, String headerText) throws NotFoundException;

	void setClubBodyText(String clubId, String bodyText) throws NotFoundException;

	void setClubHeaderText(String clubId, String headerText) throws NotFoundException;

	void setSubAmenityBodyText(String clubId, String subAmenityId, String bodyText) throws NotFoundException;

	void setSubAmenityHeaderText(String clubId, String subAmenityId,
			String headerText) throws NotFoundException;

	void setSubAmenitySecondaryHeaderText(String clubId, String amenityId,
			String headerText) throws NotFoundException;

	void update(ClubDto club) throws NotFoundException;

	BeaconDto updateBeacon(BeaconDto beacon) throws NotFoundException;

	boolean verifyClubPin(String clubPin);

}
