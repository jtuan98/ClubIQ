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
	List<AmenityDto> getAmenities(Integer clubIdPk, boolean includeImages) throws NotFoundException;

	AmenityDto getAmenity(Integer clubIdPk, Integer amenityIdPk)throws NotFoundException;

	String getAmenityHeaderText(int clubIdPk, int amenityIdPk) throws NotFoundException;

	String getBodyText(int clubIdPk);

	Integer getClubAmenityIdPk(int clubIdPk, String amenityId) throws NotFoundException;

	ClubDto getClubByKeyCode(String clubKeycode) throws NotFoundException;

	Integer getClubIdPk(String clubId) throws NotFoundException;

	List<ClubDto> getClubs(Integer userIdPk) throws NotFoundException;

	List<ClubDto> getClubsByState(String state, ClubListingSortBy orderByClause);

	List<ClubDto> getClubsByState(String state, ClubListingSortBy orderByClause, boolean retrieveImagesFlag);

	Integer getClubSubAmenityIdPk(int clubIdPk, String clubSubAmenityId) throws NotFoundException;

	String getHeadlineText(int clubIdPk);

	List<SubAmenityDto> getSubAmenities(Integer clubIdPk) throws NotFoundException;
	List<SubAmenityDto> getSubAmenities(Integer clubIdPk, String amenityId) throws NotFoundException;

	SubAmenityDto getSubAmenity(Integer subAmenityIdPk) throws NotFoundException;

	String getSubAmenityBodyText(int clubIdPk, int amenityIdPk);

	// Return Amenity Names
	List<String> getSubAmenityDeptName(String clubId) throws NotFoundException;

	List<Integer> getSubAmenityEmployees(Integer clubSubAmenityId)
			throws NotFoundException;

	String getSubAmenityHeaderText(int clubIdPk, int subAmenityIdPk);

	String getSubAmenitySecondayHeaderText(int clubIdPk, int subAmenityIdPk);

	void update(ClubDto club) throws NotFoundException;

	void updateAmenityHeaderText(Integer clubIdPk, Integer amenityIdPk,
			String headerText) throws NotFoundException;

	void updateAmenityPhoto(int clubIdPk, Integer amenityIdPk,
			String pictureBase64);

	void updateBodyText(Integer clubIdPk, String bodyText) throws NotFoundException;

	void updateClubPhoto(int clubIdPk, String pictureBase64);

	void updateHeaderText(Integer clubIdPk, String headerText) throws NotFoundException;

	void updateSubAmenityBody(Integer clubIdPk, Integer subAmenityIdPk, String bodyText)throws NotFoundException;

	void updateSubAmenityHeaderText(Integer clubIdPk, Integer subAmenityIdPk,
			String headerText) throws NotFoundException;

	void updateSubAmenitySecondaryHeaderText(Integer clubIdPk,
			Integer subAmenityIdPk, String headerText) throws NotFoundException;

	boolean verifyClubPin(String clubPin);
}
