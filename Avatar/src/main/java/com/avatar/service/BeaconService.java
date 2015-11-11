package com.avatar.service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import com.avatar.dto.club.BlackoutDate;
import com.avatar.dto.club.BlackoutTime;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.ClubListingSortBy;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.NotificationException;
import com.avatar.exception.PermissionDeniedException;

@Service
public class BeaconService extends BaseService implements BeaconBusiness {

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
			throws NotFoundException, InvalidParameterException {
		final Integer beaconIdPk = beaconDao.getBeaconIdPk(beaconId);
		beaconDao.addUserIdToBeaconMapping(beaconId, userId);

		final AccountDto member = accountDao.fetch(userId);

		// Find the amenity associated to beacon
		final Integer clubAmenityIdPk = beaconDao.getAmenityIdPk(beaconIdPk);

		// Find the staff associated to amenity
		final List<Integer> amenityEmployeeIdsPk = clubDao
				.getAmenityEmployees(clubAmenityIdPk);
		// Send alert to staff
		if (CollectionUtils.isNotEmpty(amenityEmployeeIdsPk)) {
			System.out.println("DEBUG: amenityEmployeeIdsPk=>"
					+ amenityEmployeeIdsPk.size());
			for (final Integer employeeIdPk : amenityEmployeeIdsPk) {
				final AccountDto empoyee = accountDao.fetch(employeeIdPk);
				try {
					System.out.println("DEBUG: Sending APNS to empoyee=>"
							+ empoyee.getDeviceId() + " [id=" + empoyee.getId()
							+ "]");
					apnsNotificationService.sendAlert(empoyee, member);
				} catch (final NotificationException e) {
					System.out
					.println("DEBUG: ERROR in Sending APNS to empoyee=>"
							+ empoyee.getDeviceId()
							+ " [id="
							+ empoyee.getId() + "]");
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("DEBUG: amenityEmployeeIdsPk is EMPTY");
		}
	}

	private ClubDto createMockClubData(final String state,
			final ClubListingSortBy orderByClause, final int i) {
		final ClubDto val = new ClubDto();
		val.setAddress(Math.round(Math.random()) + " St.");
		val.setCity(i + " city");
		val.setClubId(i + "");
		val.setClubName(orderByClause.equals(ClubListingSortBy.clubName) ? i
				+ " Club"
				: "club " + Math.round(Math.random()));
		val.setClubType("nightclub");
		val.setId(i);
		val.setState(StringUtils.isEmpty(state) ? (orderByClause
				.equals(ClubListingSortBy.state) ? i + " state" : Math
						.round(Math.random()) + " state") : state);
		val.setXcoord(i + "2345");
		val.setYcoord(i + "5672345");
		return val;
	}

	@Override
	public void deleteBeacon(final BeaconDto beacon) throws NotFoundException,
	PermissionDeniedException {
		Assert.notNull(beacon, "Checking beacon");
		beaconDao.delete(beacon);
	}

	@Override
	public List<AmenityDto> getAmenities(final String clubId)
			throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		return clubDao.getAmenities(clubIdPk);
	}

	@Override
	public String getAmenityBodyText(final String clubId, final String amenityId)
			throws NotFoundException {
		// TODO Phase 2
		final String retVal = "This is mock data for Amenity Body Text";
		return retVal;
	}

	@Override
	public List<String> getAmenityDeptName(final String clubId)
			throws NotFoundException {
		return beaconDao.getAmenityDeptName(clubId);
	}

	@Override
	public String getAmenityHeaderText(final String clubId, final String amenityId)
			throws NotFoundException {
		// TODO Phase 2
		final String retVal = "This is mock data for Amenity Header Text";
		return retVal;
	}

	@Override
	public String getAmenitySecondaryHeaderText(final String clubId, final String amenityId)
			throws NotFoundException {
		// TODO Phase 2
		final String retVal = "This is mock data for Amenity Secondary Header Text";
		return retVal;
	}

	@Override
	public BeaconDto getBeacon(final String beaconActionId)
			throws NotFoundException {
		final Integer beaconPkId = beaconDao.getBeaconIdPk(beaconActionId);
		return beaconDao.getBeacon(beaconPkId);
	}

	@Override
	public List<BeaconDto> getBeacons(final String clubId,
			final String amenityId) throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		final Integer amenityIdPk = clubDao.getClubAmenityIdPk(amenityId);
		return beaconDao.getBeacons(clubIdPk, amenityIdPk);
	}

	@Override
	public List<BlackoutDate> getBlackoutDates(final String clubId,
			final String amenityId, final String month)
					throws NotFoundException {
		// TODO Mocking only. PHASE 2
		final List<BlackoutDate> retVal = new LinkedList<BlackoutDate>();
		final Set<Integer> dates = new TreeSet<Integer>();
		for (int i = 0; i < 15; i++) {
			dates.add((int) (Math.round(Math.random()*100) % 30) + 1);
		}
		for (final Integer date : dates) {
			final BlackoutDate d = new BlackoutDate();
			d.setDate(date.toString());
			retVal.add(d);
		}
		return retVal;
	}

	@Override
	public List<BlackoutTime> getBlackoutTimes(final String clubId, final String amenityId,
			final String requestedDateMMDD) {
		// TODO Mocking only. PHASE 2
		final List<BlackoutTime> retVal = new LinkedList<BlackoutTime>();
		final Set<Integer> times = new TreeSet<Integer>();
		for (int i = 0; i < 5; i++) {
			final int hr = (int) (Math.round(Math.random()*100) % 24) ;
			final int mi = (int) (Math.round(Math.random()*100) % 2) * 30 ;
			times.add(hr*100 + mi);
		}
		for (final Integer time : times) {
			final BlackoutTime t = new BlackoutTime();
			t.setTime(String.format("%04d", time));
			retVal.add(t);
		}
		return retVal;
	}

	@Override
	public ClubDto getClub(final String clubId) throws NotFoundException {
		return clubDao.get(clubId);
	}

	@Override
	public ClubDto getClubByKeycode(final String clubKeycode)
			throws NotFoundException {
		// TODO Phase 2
		final ClubDto retVal = createMockClubData("CA", ClubListingSortBy.clubName, 1);
		return retVal;
	}

	@Override
	public List<ClubDto> getClubs(final Integer userIdPk)
			throws NotFoundException {
		final List<ClubDto> retVal = clubDao.getClubs(userIdPk);
		if (CollectionUtils.isNotEmpty(retVal)) {
			for (final ClubDto club : retVal) {
				final List<AmenityDto> amenities = clubDao.getAmenities(club
						.getId());
				club.setAmenities(amenities);
			}
		}
		return retVal;
	}

	@Override
	public List<ClubDto> getClubs(final String state,
			final ClubListingSortBy orderByClause) {
		// TODO Mocking Phase 2
		final List<ClubDto> retVal = new LinkedList<ClubDto>();
		for (int i = 0; i < 10; i++) {
			final ClubDto val = createMockClubData(state, orderByClause, i);
			retVal.add(val);
		}
		return retVal;
	}

	@Override
	public List<AmenityDto> getSubAmenityList(final String clubId, final String amenityType)
			throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		return clubDao.getAmenities(clubIdPk, amenityType);
	}

	@Override
	public List<ImmutablePair<AccountDto, Date>> getUsers(
			final String amenityId, final Date onDate) {
		return beaconDao.getUsers(amenityId, onDate);
	}

	@Override
	public void setAmenityBodyText(final String clubId, final String amenityId,
			final String bodyText) throws NotFoundException {
		// TODO Phase 2

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
	public void setAmenityHeaderText(final String clubId, final String amenityId,
			final String headerText) throws NotFoundException {
		// TODO Phase 2

	}

	@Override
	public void setAmenitySecondaryHeaderText(final String clubId, final String amenityId,
			final String headerText) throws NotFoundException {
		// TODO Phase 2

	}

	@Override
	public void setClubBodyText(final String clubId, final String bodyText)
			throws NotFoundException {
		// TODO Phase 2

	}

	@Override
	public void setClubHeaderText(final String clubId, final String headerText)
			throws NotFoundException {
		// TODO Phase 2

	}

	@Override
	public void update(final ClubDto club) throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(club.getClubId());
		final ClubDto clubFromDb = clubDao.get(clubIdPk, true);
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
		// TODO Must handle images...

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

	@Override
	public boolean verifyClubPin(final String clubPin) {
		return clubDao.verifyClubPin(clubPin);
	}
}