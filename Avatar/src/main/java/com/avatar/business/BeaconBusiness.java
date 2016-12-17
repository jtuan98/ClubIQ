package com.avatar.business;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

	void deleteBeacon(BeaconDto beacon) throws NotFoundException, PermissionDeniedException, InvalidParameterException;

	List<AmenityDto> getAmenities(String clubId) throws NotFoundException;

	AmenityDto getAmenity(String clubId, String amenityId) throws NotFoundException;

	String getAmenityHeaderText(String clubId, String amenityId) throws NotFoundException;

	BeaconDto getBeacon(String beaconActionId) throws NotFoundException;

	List<BeaconDto> getBeacons(String clubId, String subAmenityId)
			throws NotFoundException;

	List<BlackoutDate> getBlackoutDates(String clubId, String subAmenityId,
			String year, String month) throws NotFoundException;

	Map<String, List<BlackoutTime>> getBlackoutTimes(String clubId, String subAmenityId,
			Date requestedDateFrom, Date requestedDateTo) throws NotFoundException;

	List<BlackoutTime> getBlackoutTimes(String clubId, String subAmenityId,
			String requestedDateYear, String requestedDateMonth, String requestedDateDay) throws NotFoundException;

	ClubDto getClub(String clubId) throws NotFoundException;

	String getClubBodyText(String clubId) throws NotFoundException;

	ClubDto getClubByKeycode(String clubKeycode)throws NotFoundException;

	ClubDto getClubDetails(String clubId) throws NotFoundException;

	String getClubHeadlineText(String clubId) throws NotFoundException;

	List<ClubDto> getClubs(Integer userIdPk) throws NotFoundException;

	List<ClubDto> getClubs(String state, ClubListingSortBy clubname) throws NotFoundException;

	List<SubAmenityDto> getSubAmenities(String clubId) throws NotFoundException;

	String getSubAmenityBodyText(String clubId, String amenityId) throws NotFoundException;

	List<String> getSubAmenityDeptName(String clubId) throws NotFoundException;

	String getSubAmenityHeaderText(String clubId, String subAmenityId)throws NotFoundException;

	List<SubAmenityDto> getSubAmenityList(String clubId, String amenityType) throws NotFoundException;

	String getSubAmenitySecondaryHeaderText(String clubId, String amenityId)throws NotFoundException;

	List<AccountDto> getUsers(String amenityId, Date onDate)
			throws NotFoundException, InvalidParameterException;

	void setAmenityHeaderText(String clubId, String amenityId, String headerText) throws NotFoundException;

	void setAmenityPhoto(String clubId, String amenityId, String pictureBase64) throws NotFoundException;

	void setBlackoutTimes(String clubId, String subAmenityId,
			Date requestedDate, String blackoutTimes /* blocks of 30 min */) throws NotFoundException;

	void setClubBodyText(String clubId, String bodyText) throws NotFoundException;

	void setClubHeaderText(String clubId, String headerText) throws NotFoundException;

	void setClubPhoto(String clubId, String pictureBase64) throws NotFoundException;

	void setSubAmenityBodyText(String clubId, String subAmenityId, String bodyText) throws NotFoundException;

	void setSubAmenityHeaderText(String clubId, String subAmenityId,
			String headerText) throws NotFoundException;

	void setSubAmenityId(String clubId, String apnsToken,
			String amenityId, String subAmenityId) throws NotFoundException;

	void setSubAmenitySecondaryHeaderText(String clubId, String amenityId,
			String headerText) throws NotFoundException;

	void update(ClubDto club) throws NotFoundException;

	BeaconDto updateBeacon(BeaconDto beacon) throws NotFoundException;

	boolean verifyClubPin(String clubPin);

}
