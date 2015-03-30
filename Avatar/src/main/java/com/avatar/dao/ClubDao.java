package com.avatar.dao;

import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.exception.NotFoundException;

public interface ClubDao {
	void addUserToClub(int clubIdPk, int userIdPk) throws NotFoundException;
	void addUserToClub(String clubId, String userId) throws NotFoundException;

	ClubDto get(Integer clubIdPk) throws NotFoundException;
	ClubDto get(String clubId) throws NotFoundException;

	AmenityDto getAmenity(Integer amenityIdPk) throws NotFoundException;
	Integer getClubAmenityIdPk(String clubAmenityId) throws NotFoundException;
	Integer getClubIdPk(String clubId) throws NotFoundException;
}
