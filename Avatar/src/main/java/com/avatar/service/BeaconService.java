package com.avatar.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.avatar.dao.ReservationDao;
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

	@Resource(name = "reservationDaoJdbc")
	private ReservationDao reservationDao;

	private final boolean mockingBlackoutDates = false;

	@Override
	public void addUserIdToBeacon(final String beaconId, final String userId)
			throws NotFoundException, InvalidParameterException {
		final Integer beaconIdPk = beaconDao.getBeaconIdPk(beaconId);
		beaconDao.addUserIdToBeaconMapping(beaconId, userId);

		final AccountDto member = accountDao.fetch(userId);

		// Find the amenity associated to beacon
		final Integer clubAmenityIdPk = beaconDao.getSubAmenityIdPk(beaconIdPk);

		// Find the staff associated to amenity
		final List<Integer> amenityEmployeeIdsPk = clubDao
				.getSubAmenityEmployees(clubAmenityIdPk);
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
				+ " Club" : "club " + Math.round(Math.random()));
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
	PermissionDeniedException, InvalidParameterException {
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
	public AmenityDto getAmenity(final String clubId, final String amenityId)
			throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		final Integer amenityIdPk = clubDao.getClubAmenityIdPk(clubIdPk, amenityId);
		return clubDao.getAmenity(clubIdPk, amenityIdPk);
	}

	@Override
	public String getAmenityHeaderText(final String clubId,
			final String amenityId) throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		final int amenityIdPk = clubDao.getClubAmenityIdPk(clubIdPk, amenityId);
		return clubDao.getAmenityHeaderText(clubIdPk, amenityIdPk);
	}

	@Override
	public BeaconDto getBeacon(final String beaconActionId)
			throws NotFoundException {
		final Integer beaconPkId = beaconDao.getBeaconIdPk(beaconActionId);
		return beaconDao.getBeacon(beaconPkId);
	}

	@Override
	public List<BeaconDto> getBeacons(final String clubId,
			final String subAmenityId) throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		final Integer subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);
		return beaconDao.getBeacons(clubIdPk, subAmenityIdPk);
	}

	@Override
	public List<BlackoutDate> getBlackoutDates(final String clubId,
			final String subAmenityId, final String year, final String month)
					throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		final int subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);
		List<BlackoutDate> retVal = null;
		if (!mockingBlackoutDates) {
			retVal = reservationDao.fetchBlackoutDates(clubIdPk,
					subAmenityIdPk, year, month);
		}
		return retVal;
	}

	@Override
	public Map<String, List<BlackoutTime>> getBlackoutTimes(final String clubId,
			final String subAmenityId, final Date requestedDateFrom, final Date requestedDateTo)
					throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		final int subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);
		Map<String, List<BlackoutTime>> retVal = null;
		retVal = reservationDao.fetchBlackoutTimes(clubIdPk, subAmenityIdPk,
				requestedDateFrom, requestedDateTo);
		return retVal;
	}

	@Override
	public List<BlackoutTime> getBlackoutTimes(final String clubId,
			final String subAmenityId, final String requestedDateYear,
			final String requestedDateMonth, final String requestedDateDay)
					throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		final int subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);
		List<BlackoutTime> retVal = null;
		retVal = reservationDao.fetchBlackoutTimes(clubIdPk, subAmenityIdPk,
				requestedDateYear, requestedDateMonth, requestedDateDay);
		return retVal;
	}

	@Override
	public ClubDto getClub(final String clubId) throws NotFoundException {
		return clubDao.get(clubId);
	}

	@Override
	public String getClubBodyText(final String clubId) throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		return clubDao.getBodyText(clubIdPk);
	}

	@Override
	public ClubDto getClubByKeycode(final String clubKeycode)
			throws NotFoundException {
		final ClubDto retVal = clubDao.getClubByKeyCode(clubKeycode);
		return retVal;
	}

	@Override
	public ClubDto getClubDetails(final String clubId) throws NotFoundException {
		final ClubDto retVal = getClub(clubId);
		final List<AmenityDto> amenities = clubDao.getAmenities(retVal.getId());
		retVal.setAmenities(amenities);
		return retVal;
	}

	@Override
	public String getClubHeadlineText(final String clubId)
			throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		return clubDao.getHeadlineText(clubIdPk);
	}

	@Override
	public List<ClubDto> getClubs(final Integer userIdPk)
			throws NotFoundException {
		final List<ClubDto> retVal = clubDao.getClubs(userIdPk);
		if (CollectionUtils.isNotEmpty(retVal)) {
			for (final ClubDto club : retVal) {
				final List<AmenityDto> amenities = clubDao.getAmenities(club
						.getId(), false);
				club.setAmenities(amenities);
			}
		}
		return retVal;
	}

	@Override
	public List<ClubDto> getClubs(final String state,
			final ClubListingSortBy orderByClause) throws NotFoundException {
		return getClubs(state, orderByClause, true);
	}

	@Override
	public List<ClubDto> getClubs(final String state, final ClubListingSortBy orderByClause, final boolean retrieveImagesFlag) throws NotFoundException{
		final List<ClubDto> retVal = clubDao.getClubsByState(state,
				orderByClause, retrieveImagesFlag);
		return retVal;

	}


	@Override
	public List<SubAmenityDto> getSubAmenities(final String clubId)
			throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		final List<SubAmenityDto> retVal = clubDao.getSubAmenities(clubIdPk);
		return retVal;
	}

	@Override
	public String getSubAmenityBodyText(final String clubId,
			final String subAmenityId) throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		final int subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);

		return clubDao.getSubAmenityBodyText(clubIdPk, subAmenityIdPk);
	}

	@Override
	public List<String> getSubAmenityDeptName(final String clubId)
			throws NotFoundException {
		return clubDao.getSubAmenityDeptName(clubId);
	}

	@Override
	public String getSubAmenityHeaderText(final String clubId,
			final String subAmenityId) throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		final int subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);
		return clubDao.getSubAmenityHeaderText(clubIdPk, subAmenityIdPk);
	}

	@Override
	public List<SubAmenityDto> getSubAmenityList(final String clubId,
			final String amenityId) throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		return clubDao.getSubAmenities(clubIdPk, amenityId);
	}

	@Override
	public String getSubAmenitySecondaryHeaderText(final String clubId,
			final String subAmenityId) throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		final int subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);
		return clubDao
				.getSubAmenitySecondayHeaderText(clubIdPk, subAmenityIdPk);
	}

	@Override
	public List<AccountDto> getUsers(final String subAmenityId,
			final Date onDate) throws InvalidParameterException {
		return beaconDao.getUsers(subAmenityId, onDate);
	}

	@Override
	public void setAmenityHeaderText(final String clubId,
			final String amenityId, final String headerText)
					throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		final Integer amenityIdPk = clubDao.getClubAmenityIdPk(clubIdPk, amenityId);
		clubDao.updateAmenityHeaderText(clubIdPk, amenityIdPk, headerText);
	}

	@Override
	public void setAmenityPhoto(final String clubId, final String amenityId,
			final String pictureBase64) throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		final Integer amenityIdPk = clubDao.getClubAmenityIdPk(clubIdPk, amenityId);
		clubDao.updateAmenityPhoto(clubIdPk, amenityIdPk, pictureBase64);
	}

	@Override
	public void setBlackoutTimes(final String clubId,
			final String subAmenityId, final Date blackoutDate,
			final String blackoutTimes) throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		final int subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);
		reservationDao.setBlackoutTimes(clubIdPk, subAmenityIdPk, blackoutDate,
				blackoutTimes);
	}

	@Override
	public void setClubBodyText(final String clubId, final String bodyText)
			throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		clubDao.updateBodyText(clubIdPk, bodyText);
	}

	@Override
	public void setClubHeaderText(final String clubId, final String headerText)
			throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		clubDao.updateHeaderText(clubIdPk, headerText);
	}

	@Override
	public void setClubPhoto(final String clubId, final String pictureBase64)
			throws NotFoundException {
		final int clubIdPk = clubDao.getClubIdPk(clubId);
		clubDao.updateClubPhoto(clubIdPk, pictureBase64);
	}

	@Override
	public void setSubAmenityBodyText(final String clubId,
			final String subAmenityId, final String bodyText)
					throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		final Integer subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);
		clubDao.updateSubAmenityBody(clubIdPk, subAmenityIdPk, bodyText);
	}

	@Override
	public void setSubAmenityHeaderText(final String clubId,
			final String subAmenityId, final String headerText)
					throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		final Integer subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);
		clubDao.updateSubAmenityHeaderText(clubIdPk, subAmenityIdPk, headerText);
	}

	@Override
	public void setSubAmenityId(final String clubId, final String apnsToken,
			final String amenityId, final String subAmenityId)
					throws NotFoundException {
		if (StringUtils.isEmpty(subAmenityId)) {
			beaconDao.setApnsToken(clubId, apnsToken);
		} else {
			beaconDao.setSubAmenityId(clubId, apnsToken, amenityId,
					subAmenityId);
		}
	}

	@Override
	public void setSubAmenitySecondaryHeaderText(final String clubId,
			final String subAmenityId, final String headerText)
					throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		final Integer subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);
		clubDao.updateSubAmenitySecondaryHeaderText(clubIdPk, subAmenityIdPk,
				headerText);
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
		if (StringUtils.isNotEmpty(club.getXcoord())) {
			clubFromDb.setXcoord(club.getXcoord());
		}
		if (StringUtils.isNotEmpty(club.getYcoord())) {
			clubFromDb.setYcoord(club.getZipCode());
		}
		// TODO Must handle images...

		clubDao.update(clubFromDb);

	}

	@Override
	public BeaconDto updateBeacon(final BeaconDto beacon)
			throws NotFoundException {
		Assert.notNull(beacon, "Checking beacon");
		Assert.notNull(beacon.getClub(), "Checking club");
		Assert.notNull(beacon.getSubAmenity(), "Checking sub amenity");
		Assert.notNull(beacon.getInstallerStaff(), "Checking installer");
		Assert.notNull(beacon.getClub().getClubId(), "Checking club id");
		Assert.notNull(beacon.getSubAmenity().getSubAmenityId(),
				"Checking sub amenity id");
		Assert.notNull(beacon.getInstallerStaff().getUserId(),
				"Checking installer userid ");
		// Find the pk
		final Integer clubIdPk = clubDao.getClubIdPk(beacon.getClub()
				.getClubId());
		beacon.getClub().setId(clubIdPk);
		final Integer subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				beacon.getSubAmenity().getSubAmenityId());
		beacon.getSubAmenity().setId(subAmenityIdPk);
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