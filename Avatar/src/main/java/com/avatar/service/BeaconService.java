package com.avatar.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.avatar.business.BeaconBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dao.BeaconDao;
import com.avatar.dao.ClubDao;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.BeaconDto;
import com.avatar.exception.NotFoundException;

@Service
public class BeaconService implements BeaconBusiness {

	@Resource(name = "beaconDaoJdbc")
	private BeaconDao beaconDao;

	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDao;

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	@Override
	public void addUserIdToBeacon(final String beaconId, final String userId)
			throws NotFoundException {
		beaconDao.addUserIdToBeaconMapping(beaconId, userId);
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
