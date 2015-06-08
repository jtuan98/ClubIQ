package com.avatar.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Date;

import com.avatar.dao.AccountDao;
import com.avatar.dao.BeaconDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.DbDateDao;
import com.avatar.dao.SurveyDao;
import com.avatar.dto.AccountDtoBuilder;
import com.avatar.dto.club.AmenityDto;

public abstract class BaseServiceTest {
	protected AccountDao accountDao = mock(AccountDao.class);

	protected ClubDao clubDao = mock(ClubDao.class);

	protected final DbDateDao dbDateDao = mock(DbDateDao.class);

	protected SurveyDao surveyDao = mock(SurveyDao.class);

	protected BeaconDao beaconDao = mock(BeaconDao.class);

	protected AccountDtoBuilder builder;

	protected AmenityDto getAmenityInstance(final Integer amenityIdPk,
			final String amenityId) {
		AmenityDto amenity = null;
		if (amenityId != null) {
			amenity = new AmenityDto(1);
			amenity.setAmenityId(amenityId);
			amenity.setAmenityType("type");
			amenity.setDescription("Test");
			amenity.setHoursOfOperation("1");
		}
		return amenity;
	}

	protected void setup() {
		given(dbDateDao.getNow()).willReturn(new Date());
	}

}
