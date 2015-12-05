package com.avatar.dao;

import java.util.List;

import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.ClubListingSortBy;
import com.avatar.exception.NotFoundException;

public interface ClubDao {
	void addUserToClub(int clubIdPk, int userIdPk) throws NotFoundException;

	void addUserToClub(String clubId, String userId) throws NotFoundException;

	ClubDto get(Integer clubIdPk, boolean includePicture)
			throws NotFoundException;

	ClubDto get(String clubId) throws NotFoundException;

	List<AmenityDto> getAmenities(Integer clubIdPk) throws NotFoundException;

	List<AmenityDto> getAmenities(Integer clubIdPk, String amenityType) throws NotFoundException;

	AmenityDto getAmenity(Integer amenityIdPk) throws NotFoundException;

	List<Integer> getAmenityEmployees(Integer clubAmenityId)
			throws NotFoundException;

	String getBodyText(int clubIdPk);

	Integer getClubAmenityIdPk(String clubAmenityId) throws NotFoundException;

	ClubDto getClubByKeyCode(String clubKeycode) throws NotFoundException;

	Integer getClubIdPk(String clubId) throws NotFoundException;

	List<ClubDto> getClubs(Integer userIdPk) throws NotFoundException;

	List<ClubDto> getClubsByState(String state, ClubListingSortBy orderByClause);

	String getHeadlineText(int clubIdPk);

	void update(ClubDto club) throws NotFoundException;

	void updateAmenityBody(Integer clubIdPk, Integer amenityIdPk, String bodyText)throws NotFoundException;

	void updateAmenityHeaderText(Integer clubIdPk, Integer amenityIdPk,
			String headerText) throws NotFoundException;

	void updateAmenitySecondaryHeaderText(Integer clubIdPk,
			Integer amenityIdPk, String headerText) throws NotFoundException;

	void updateBodyText(Integer clubIdPk, String bodyText) throws NotFoundException;

	void updateHeaderText(Integer clubIdPk, String headerText) throws NotFoundException;

	boolean verifyClubPin(String clubPin);
}
