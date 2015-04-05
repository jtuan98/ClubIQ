package com.avatar.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.avatar.business.BeaconBusiness;
import com.avatar.business.NotificationBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dao.BeaconDao;
import com.avatar.dao.ClubDao;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.BeaconDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.NotificationException;

@Service
public class BeaconService implements BeaconBusiness {

	@Resource(name = "beaconDaoJdbc")
	private BeaconDao beaconDao;

	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDao;

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	@Resource(name = "apnsNotificationService")
	private NotificationBusiness apnsNotificationService;

	@Override
	public void addUserIdToBeacon(final String beaconId, final String userId)
			throws NotFoundException {
		final Integer beaconIdPk = beaconDao.getBeaconIdPk(beaconId);
		beaconDao.addUserIdToBeaconMapping(beaconId, userId);

		final AccountDto member = accountDao.fetch(userId);

		//Find the amenity associated to beacon
		final Integer clubAmenityIdPk = beaconDao.getAmenityIdPk(beaconIdPk);

		//Find the staff associated to amenity
		final List<Integer> amenityEmployeeIdsPk = clubDao.getAmenityEmployees(clubAmenityIdPk);

		//Send alert to staff
		if (CollectionUtils.isNotEmpty(amenityEmployeeIdsPk)) {
			for (final Integer employeeIdPk : amenityEmployeeIdsPk) {
				final AccountDto empoyee = accountDao.fetch(employeeIdPk);
				try {
					apnsNotificationService.sendAlert(empoyee, member);
				} catch (final NotificationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public List<AmenityDto> getAmenities(final String clubId)
			throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		return clubDao.getAmenities(clubIdPk);
	}

	@Override
	public List<String> getAmenityDeptName(final String clubId)
			throws NotFoundException {
		return beaconDao.getAmenityDeptName(clubId);
	}

	@Override
	public List<AccountDto> getUsers(final String beaconId,
			final String amenityDepartment) {
		return beaconDao.getUsers(beaconId, amenityDepartment);
	}

	@Override
	public void setAmenityDeptName(final String clubId, final String apnsToken,
			final String amenityDepartment) throws NotFoundException {
		if (StringUtils.isEmpty(amenityDepartment)) {
			beaconDao.setApnsToken(clubId, apnsToken);
		} else {
			beaconDao.setAmenityDeptName(clubId, apnsToken, amenityDepartment);
		}
	}

	@Override
	public void update(final ClubDto club) throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(club.getClubId());
		final ClubDto clubFromDb = clubDao.get(clubIdPk);
		if (StringUtils.isNotEmpty(club.getAddress())) {
			clubFromDb.setAddress(club.getAddress());
		}
		if (StringUtils.isNotEmpty(club.getCity())) {
			clubFromDb.setCity(club.getCity());
		}
		if (StringUtils.isNotEmpty(club.getClubName())) {
			clubFromDb.setClubName(club.getClubName());
		}
		if (StringUtils.isNotEmpty(club.getClubType())) {
			clubFromDb.setClubType(club.getClubType());
		}
		if (StringUtils.isNotEmpty(club.getHzRestriction())) {
			clubFromDb.setHzRestriction(club.getHzRestriction());
		}
		if (StringUtils.isNotEmpty(club.getPhoneNumber())) {
			clubFromDb.setPhoneNumber(club.getPhoneNumber());
		}
		if (StringUtils.isNotEmpty(club.getState())) {
			clubFromDb.setState(club.getState());
		}
		if (StringUtils.isNotEmpty(club.getWebSite())) {
			clubFromDb.setWebSite(club.getWebSite());
		}
		if (StringUtils.isNotEmpty(club.getZipCode())) {
			clubFromDb.setZipCode(club.getZipCode());
		}
		//TODO Must handle images...

		clubDao.update(clubFromDb);

	}

	@Override
	public BeaconDto updateBeacon(final BeaconDto beacon)
			throws NotFoundException {
		Assert.notNull(beacon, "Checking beacon");
		Assert.notNull(beacon.getClub(), "Checking club");
		Assert.notNull(beacon.getAmenity(), "Checking amenity");
		Assert.notNull(beacon.getInstallerStaff(), "Checking installer");
		Assert.notNull(beacon.getClub().getClubId(), "Checking club id");
		Assert.notNull(beacon.getAmenity().getAmenityId(),
				"Checking amenity id");
		Assert.notNull(beacon.getInstallerStaff().getUserId(),
				"Checking installer userid ");
		// Find the pk
		final Integer clubIdPk = clubDao.getClubIdPk(beacon.getClub()
				.getClubId());
		beacon.getClub().setId(clubIdPk);
		final Integer amenityIdPk = clubDao.getClubAmenityIdPk(beacon
				.getAmenity().getAmenityId());
		beacon.getAmenity().setId(amenityIdPk);
		final Integer installerIdPk = accountDao.getUserIdPkByUserId(beacon
				.getInstallerStaff().getUserId());
		beacon.getInstallerStaff().setId(installerIdPk);

		beaconDao.updateBeaconInfo(beacon);
		return beacon;
	}

}
