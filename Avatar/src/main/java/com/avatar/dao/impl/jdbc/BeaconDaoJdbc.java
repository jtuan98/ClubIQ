package com.avatar.dao.impl.jdbc;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.avatar.dao.AccountDao;
import com.avatar.dao.BeaconDao;
import com.avatar.dao.impl.jdbc.mapper.AccountDtoMapper;
import com.avatar.dao.impl.jdbc.mapper.StringMapper;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.BeaconDto;
import com.avatar.exception.NotFoundException;

@Repository
public class BeaconDaoJdbc extends BaseJdbcDao implements BeaconDao {

	private static final String GET_BEACON_ID = "SELECT ID FROM BEACONS WHERE BEACONID = ? ";
	private static final String GET_COUNT_BEACON_ID_USERID = "SELECT COUNT(*) FROM BEACON_USERS BU, USERS U WHERE USER_ID = U.ID AND BEACON_ID = ? and U.USERID=?";
	private static final String INS_BEACON_ID_USERID = "INSERT INTO BEACON_USERS (ID, BEACON_ID, USER_ID, CREATE_DATE) VALUES (?, ?, ?, ?)";
	private static final String INS_CLUB_APNS_TOKEN = "INSERT INTO CLUB_APNS_TOKEN(ID, CLUB_AMENITY_ID, APNS_TOKEN, CREATE_DATE) VALUES(?,?,?,NOW())";
	private static final String UPD_CLUB_APNS_TOKEN = "UPDATE CLUB_APNS_TOKEN SET APNS_TOKEN=? WHERE CLUB_AMENITY_ID = ?";
	private static final String UPD_CLUB_APNS_TOKEN_USING_CLUBID = "UPDATE CLUB_APNS_TOKEN SET APNS_TOKEN=? WHERE CLUB_AMENITY_ID IN(SELECT ID FROM CLUBS WHERE CLUBID=?)";

	@Resource(name = "accountDaoJdbc")
	AccountDao accountDao;

	private static String GET_CLUB_ID = "SELECT ID FROM CLUBS WHERE CLUBID=?";

	private static String GET_AMENITY_DEPT_NAMES = "SELECT CA.NAME FROM CLUB_AMENITIES CA WHERE CLUB_ID=? ORDER BY 1";

	private static String GET_AMENITY_ID_BY_NAME_CLUBID = "SELECT ID FROM CLUB_AMENITIES WHERE NAME=? AND CLUB_ID=? ";
	private static String CHECK_AMENITY_DEPT_NAME = "SELECT COUNT(*) FROM CLUB_AMENITIES WHERE NAME=? AND CLUB_ID=? ";
	private static String INS_AMENITY_DEPT_NAME = "INSERT INTO CLUB_AMENITIES (ID, CLUB_ID, NAME, IMAGE_ID, DESCRIPTION, AVAILABLE_DATE_TIME, CREATE_DATE) VALUES (?,?,?,?,?,NOW(), NOW())";

	private static String SEL_USER = "select USERS.* FROM USERS, BEACONS, BEACON_USERS BU, CLUB_AMENITIES CA WHERE USER_ID=USERS.ID AND BEACONID=? AND CA.NAME=? AND BU.BEACON_ID=BEACONS.ID AND CA.ID = BEACONS.AMENITY_ID ORDER BY USERID";

	private final AccountDtoMapper accountDtoMapper = new AccountDtoMapper();

	private final StringMapper stringMapper = new StringMapper();

	private static String GET_BEACON_PK = "SELECT ID FROM BEACONS WHERE BEACONID=?";

	private static String GET_CLUBIDPK_FROM_BEACONIDPK = "SELECT CLUB_ID FROM BEACONS WHERE ID=?";

	private static final String GET_AMENITY_IDPK_BY_BEACON_ID_PK = "SELECT AMENITY_ID FROM BEACONS WHERE ID = ? ";

	private static String INS_BEACON = "INSERT INTO BEACONS ("
			+ "ID, BEACONID, CLUB_ID, AMENITY_ID, LOCATION, DESCRIPTION, INSTALLATION_STAFF_ID, CREATE_DATE) "
			+ "VALUES (?,?,?,?,?,?,?,NOW())";

	private static String UPD_BEACON = "UPDATE BEACONS SET CLUB_ID=?, AMENITY_ID=?, LOCATION=?, DESCRIPTION=?, INSTALLATION_STAFF_ID=? "
			+ "WHERE ID=?";

	@Override
	public void addUserIdToBeaconMapping(final String beaconId,
			final String userId) throws NotFoundException {
		// Check if beacon id exists
		try {
			final Integer beaconIdPk = getJdbcTemplate().queryForObject(
					GET_BEACON_ID, Integer.class, beaconId);
			final int counter = getJdbcTemplate().queryForObject(
					GET_COUNT_BEACON_ID_USERID, Integer.class, beaconIdPk,
					userId);
			if (counter == 0) {
				// Insert
				final int userIdPk = accountDao.getUserIdPkByUserId(userId);
				final Date entry = new Date();
				final int id = sequencer.nextVal("ID_SEQ");
				getJdbcTemplate().update(INS_BEACON_ID_USERID, id, beaconIdPk,
						userIdPk, entry);
			}
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Beacon " + beaconId + " not found!");
		}

	}

	@Override
	public List<String> getAmenityDeptName(final String clubId)
			throws NotFoundException {
		try {
			final Integer clubIdPk = getJdbcTemplate().queryForObject(
					GET_CLUB_ID, Integer.class, clubId);
			final List<String> deptName = getJdbcTemplate().query(
					GET_AMENITY_DEPT_NAMES, stringMapper, clubIdPk);
			return deptName;
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Club : " + clubId + " not found!");
		}
	}

	@Override
	public Integer getAmenityIdPk(final Integer beaconIdPk)
			throws NotFoundException {
		try {
			final Integer clubAmenityIdPk = getJdbcTemplate()
					.queryForObject(GET_AMENITY_IDPK_BY_BEACON_ID_PK,
							Integer.class, beaconIdPk);
			return clubAmenityIdPk;
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("BeaconIdPk : " + beaconIdPk
					+ " not found!");
		}
	}

	@Override
	public Integer getBeaconIdPk(final String beaconId)
			throws NotFoundException {
		try {
			final Integer beaconIdPk = getJdbcTemplate().queryForObject(
					GET_BEACON_PK, Integer.class, beaconId);
			return beaconIdPk;
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Beacon: " + beaconId + " not found!");
		}
	}

	@Override
	public Integer getClubIdPkByBeaconIdPk(final Integer beaconIdPk)
			throws NotFoundException {
		try {
			final Integer clubIdPk = getJdbcTemplate().queryForObject(
					GET_CLUBIDPK_FROM_BEACONIDPK, Integer.class, beaconIdPk);
			return clubIdPk;
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("BeaconIdPk : " + beaconIdPk
					+ " not found!");
		}
	}

	@Override
	public List<AccountDto> getUsers(final String beaconId,
			final String amenityDepartment) {
		final List<AccountDto> users = getJdbcTemplate().query(SEL_USER,
				accountDtoMapper, beaconId, amenityDepartment);
		if (CollectionUtils.isNotEmpty(users)) {
			for (final AccountDto user : users) {
				accountDao.populateAccountInfo(user);
			}
		}
		return users;
	}

	@Override
	public void setAmenityDeptName(final String clubId, final String apnsToken,
			final String amenityDepartment) throws NotFoundException {
		try {
			final Integer clubIdPk = getJdbcTemplate().queryForObject(
					GET_CLUB_ID, Integer.class, clubId);

			final int counter = getJdbcTemplate().queryForObject(
					CHECK_AMENITY_DEPT_NAME, Integer.class, amenityDepartment,
					clubIdPk);
			if (counter == 0) {
				final Integer amenityIdPk = sequencer.nextVal("ID_SEQ");
				getJdbcTemplate().update(INS_AMENITY_DEPT_NAME,
				// ID
						amenityIdPk,
						// CLUB_ID
						clubIdPk,
						// NAME
						amenityDepartment,
						// IMAGE_ID
						null,
						// DESCRIPTION
						"");
				final Integer mapAmenityIdPk = sequencer.nextVal("ID_SEQ");
				getJdbcTemplate().update(INS_CLUB_APNS_TOKEN,
				// ID,
						mapAmenityIdPk,
						// CLUB_AMENITY_ID,
						amenityIdPk,
						// APNS_TOKEN,
						apnsToken);

			} else {
				final Integer amenityIdPk = getJdbcTemplate().queryForObject(
						GET_AMENITY_ID_BY_NAME_CLUBID, Integer.class,
						amenityDepartment, clubIdPk);
				final int updated = getJdbcTemplate().update(
						UPD_CLUB_APNS_TOKEN,
						// APNS_TOKEN,
						apnsToken,
						// CLUB_AMENITY_ID,
						amenityIdPk);
				if (updated == 0) {
					// Insert
					final Integer mapAmenityIdPk = sequencer.nextVal("ID_SEQ");
					getJdbcTemplate().update(INS_CLUB_APNS_TOKEN,
					// ID,
							mapAmenityIdPk,
							// CLUB_AMENITY_ID,
							amenityIdPk,
							// APNS_TOKEN,
							apnsToken);
				}
			}

		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Club " + clubId + " not found!");
		}

	}

	@Override
	public void setApnsToken(final String clubId, final String apnsToken)
			throws NotFoundException {
		final Integer clubIdPk = getJdbcTemplate().queryForObject(GET_CLUB_ID,
				Integer.class, clubId);

		final int updated = getJdbcTemplate().update(
				UPD_CLUB_APNS_TOKEN_USING_CLUBID,
				// APNS_TOKEN,
				apnsToken,
				// CLUB_ID,
				clubIdPk);
	}

	@Override
	@Resource(name = "avatarDataSource")
	public void setDataSource(final DataSource ds) {
		initTemplate(ds);
	}

	@Override
	public void updateBeaconInfo(final BeaconDto beacon)
			throws NotFoundException {
		Assert.notNull(beacon, "Checking beacon");
		Assert.notNull(beacon.getBeaconid(), "Checking beaconid");
		Assert.notNull(beacon.getLocation(), "Checking beacon location");
		Assert.notNull(beacon.getClub(), "Checking club");
		Assert.notNull(beacon.getAmenity(), "Checking amenity");
		Assert.notNull(beacon.getInstallerStaff(), "Checking installer");
		Assert.notNull(beacon.getClub().getId(), "Checking club idpk");
		Assert.notNull(beacon.getAmenity().getId(), "Checking amenity id pk");
		Assert.notNull(beacon.getInstallerStaff().getId(),
				"Checking installer id pk");

		try {
			final Integer beaconIdPk = getJdbcTemplate().queryForObject(
					GET_BEACON_ID, Integer.class, beacon.getBeaconid());
			beacon.setId(beaconIdPk);
		} catch (final EmptyResultDataAccessException e) {
		}

		if (beacon.getId() == null) {
			// Insert
			final Date entry = new Date();
			final int beaconIdPk = sequencer.nextVal("ID_SEQ");
			beacon.setId(beaconIdPk);

			getJdbcTemplate()
					.update(INS_BEACON, beaconIdPk, beacon.getBeaconid(),
							beacon.getClub().getId(),
							beacon.getAmenity().getId(),
							beacon.getLocation().name(),
							beacon.getDescription(),
							beacon.getInstallerStaff().getId());
		} else {
			// update
			getJdbcTemplate().update(UPD_BEACON, beacon.getClub().getId(),
					beacon.getAmenity().getId(), beacon.getLocation().name(),
					beacon.getDescription(),
					beacon.getInstallerStaff().getId(), beacon.getId());
		}
	}

}
