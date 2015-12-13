package com.avatar.dao;

import java.util.List;

import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.club.SubAmenityDto;
import com.avatar.dto.enums.ClubListingSortBy;
import com.avatar.exception.NotFoundException;

public interface ClubDao {
	void addUserToClub(int clubIdPk, int userIdPk) throws NotFoundException;

	void addUserToClub(String clubId, String userId) throws NotFoundException;

	ClubDto get(Integer clubIdPk, boolean includePicture)
			throws NotFoundException;

	ClubDto get(String clubId) throws NotFoundException;

	List<AmenityDto> getAmenities(Integer clubIdPk) throws NotFoundException;

	String getAmenityHeaderText(int clubIdPk, int amenityIdPk) throws NotFoundException;

	String getBodyText(int clubIdPk);

	Integer getClubAmenityIdPk(String amenityId) throws NotFoundException;

	ClubDto getClubByKeyCode(String clubKeycode) throws NotFoundException;

	Integer getClubIdPk(String clubId) throws NotFoundException;

	List<ClubDto> getClubs(Integer userIdPk) throws NotFoundException;

	List<ClubDto> getClubsByState(String state, ClubListingSortBy orderByClause);

	Integer getClubSubAmenityIdPk(String clubSubAmenityId) throws NotFoundException;

	String getHeadlineText(int clubIdPk);

	List<SubAmenityDto> getSubAmenities(Integer clubIdPk, String amenity) throws NotFoundException;

	SubAmenityDto getSubAmenity(Integer subAmenityIdPk) throws NotFoundException;

	String getSubAmenityBodyText(int clubIdPk, int amenityIdPk);

	// Return Amenity Names
	List<String> getSubAmenityDeptName(String clubId) throws NotFoundException;

	List<Integer> getSubAmenityEmployees(Integer clubAmenityId)
			throws NotFoundException;

	String getSubAmenityHeaderText(int clubIdPk, int amenityIdPk);

	String getSubAmenitySecondayHeaderText(int clubIdPk, int amenityIdPk);

	void update(ClubDto club) throws NotFoundException;

	void updateAmenityHeaderText(Integer clubIdPk, Integer amenityIdPk,
			String headerText) throws NotFoundException;

	void updateBodyText(Integer clubIdPk, String bodyText) throws NotFoundException;

	void updateHeaderText(Integer clubIdPk, String headerText) throws NotFoundException;

	void updateSubAmenityBody(Integer clubIdPk, Integer subAmenityIdPk, String bodyText)throws NotFoundException;

	void updateSubAmenityHeaderText(Integer clubIdPk, Integer subAmenityIdPk,
			String headerText) throws NotFoundException;

	void updateSubAmenitySecondaryHeaderText(Integer clubIdPk,
			Integer subAmenityIdPk, String headerText) throws NotFoundException;

	boolean verifyClubPin(String clubPin);
}
