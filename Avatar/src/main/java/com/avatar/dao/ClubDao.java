package com.avatar.dao;

import java.util.List;

import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.exception.NotFoundException;

public interface ClubDao {
	void addUserToClub(int clubIdPk, int userIdPk) throws NotFoundException;

	void addUserToClub(String clubId, String userId) throws NotFoundException;

	ClubDto get(Integer clubIdPk) throws NotFoundException;

	ClubDto get(String clubId) throws NotFoundException;

	List<AmenityDto> getAmenities(Integer clubIdPk) throws NotFoundException;

	AmenityDto getAmenity(Integer amenityIdPk) throws NotFoundException;

	List<Integer> getAmenityEmployees(Integer clubAmenityId)
			throws NotFoundException;

	Integer getClubAmenityIdPk(String clubAmenityId) throws NotFoundException;

	Integer getClubIdPk(String clubId) throws NotFoundException;

	void update(ClubDto club) throws NotFoundException;
}
