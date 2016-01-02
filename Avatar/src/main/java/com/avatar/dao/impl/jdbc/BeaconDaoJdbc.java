package com.avatar.dao.impl.jdbc;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.avatar.dao.AccountDao;
import com.avatar.dao.BeaconDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.impl.jdbc.mapper.AccountDtoCheckInDateMapper;
import com.avatar.dao.impl.jdbc.mapper.BeaconDtoMapper;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.BeaconDto;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

@Repository
public class BeaconDaoJdbc extends BaseJdbcDao implements BeaconDao {

	private static final String GET_BEACON_ID = "SELECT ID FROM BEACONS WHERE BEACONID = ? ";
	private static final String GET_COUNT_BEACON_ID_USERID = "SELECT COUNT(*) FROM BEACON_USERS BU, USERS U WHERE USER_ID = U.ID AND BEACON_ID = ? and U.USERID=? "
			+ " and DATE(BU.CREATE_DATE) = DATE(NOW()) ";
	private static final String INS_BEACON_ID_USERID = "INSERT INTO BEACON_USERS (ID, BEACON_ID, USER_ID, CREATE_DATE) VALUES (?, ?, ?, NOW())";

	private static final String INS_CLUB_APNS_TOKEN = "INSERT INTO CLUB_APNS_TOKEN(ID, CLUB_SUBAMENITY_ID, APNS_TOKEN, CREATE_DATE) VALUES(?,?,?,NOW())";
	private static final String UPD_CLUB_APNS_TOKEN = "UPDATE CLUB_APNS_TOKEN SET APNS_TOKEN=? WHERE CLUB_SUBAMENITY_ID = ?";
	private static final String UPD_CLUB_APNS_TOKEN_USING_CLUBID = "UPDATE CLUB_APNS_TOKEN SET APNS_TOKEN=? WHERE CLUB_SUBAMENITY_ID IN(SELECT ID FROM CLUBS WHERE CLUBID=?)";

	private static String GET_CLUB_ID = "SELECT ID FROM CLUBS WHERE CLUBID=?";

	private static String GET_SUBAMENITY_ID_BY_NAME_CLUBID = "SELECT ID FROM CLUB_SUB_AMENITIES WHERE SUBAMENITYID=? AND CLUB_ID=? ";

	private static String CHECK_SUBAMENITY_DEPT_NAME = "SELECT COUNT(*) FROM CLUB_SUB_AMENITIES WHERE SUBAMENITYID=? AND CLUB_ID=? ";
	private static String INS_SUBAMENITY_DEPT_NAME = "INSERT INTO CLUB_SUB_AMENITIES (ID, CLUB_ID, SUBAMENITYID, IMAGE_ID, DESCRIPTION, AVAILABLE_DATE_TIME, CREATE_DATE, AMENITY_ID) VALUES (?,?,?,?,?,NOW(), NOW(),?)";
	private static String SEL_USER = "select distinct USERS.*, BU.CREATE_DATE CHECKIN_DATE FROM USERS, BEACONS B, BEACON_USERS BU, CLUB_SUB_AMENITIES CA WHERE USER_ID=USERS.ID "
			+ " AND B.CLUB_ID=CA.CLUB_ID AND CA.SUBAMENITYID=? AND BU.BEACON_ID=B.ID AND CA.ID = B.SUBAMENITY_ID AND DATE(BU.CREATE_DATE) =  ";

	private static String GET_BEACON_PK = "SELECT ID FROM BEACONS WHERE BEACONID=?";

	private static String GET_CLUBIDPK_FROM_BEACONIDPK = "SELECT CLUB_ID FROM BEACONS WHERE ID=?";

	private static final String GET_SUBAMENITY_IDPK_BY_BEACON_ID_PK = "SELECT SUBAMENITY_ID FROM BEACONS WHERE ID = ? ";

	private static final String GET_AMENITY_IDPK_BY_BEACON_ID_PK = "SELECT AMENITY_ID FROM BEACONS B, CLUB_SUB_AMENITIES CSA WHERE B.ID = ? and B.SUBAMENITY_ID = CSA.ID ";

	private static String INS_BEACON = "INSERT INTO BEACONS ("
			+ "ID, BEACONID, CLUB_ID, SUBAMENITY_ID, LOCATION, DESCRIPTION, INSTALLATION_STAFF_ID, INSTALLATION_DATE, CREATE_DATE) "
			+ "VALUES (?,?,?,?,?,?,?,?,NOW())";

	private static String UPD_BEACON = "UPDATE BEACONS SET CLUB_ID=?, SUBAMENITY_ID=?, LOCATION=?, DESCRIPTION=?, INSTALLATION_STAFF_ID=? "
			+ "WHERE ID=?";

	private static final String GET_BEACONS_BYCLUBID_SUBAMENITYID = "SELECT * FROM BEACONS WHERE CLUB_ID = ? and SUBAMENITY_ID = ? ";

	private static final String DEL_BEACON = "DELETE FROM BEACONS where ID = ?";

	private static final String GET_BEACON_BY_PKID = "SELECT * FROM BEACONS where ID = ? ";

	private static final String DEL_BEACON_BY_MEMBERID = "delete from BEACON_USERS where USER_ID=? and CREATE_DATE >=? and CREATE_DATE < ? ";

	@Resource(name = "accountDaoJdbc")
	AccountDao accountDao;

	private final AccountDtoCheckInDateMapper accountDtoCheckInDateMapper = new AccountDtoCheckInDateMapper();

	private final BeaconDtoMapper beaconDtoMapper = new BeaconDtoMapper();

	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDao;

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
			final int userIdPk = accountDao.getUserIdPkByUserId(userId);
			if (counter == 0) {
				// Insert
				final int id = sequencer.nextVal("ID_SEQ");
				getJdbcTemplate().update(INS_BEACON_ID_USERID, id, beaconIdPk,
						userIdPk);
			}
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Beacon " + beaconId + " not found!");
		}

	}

	@Override
	public void delete(final BeaconDto beacon) throws NotFoundException,
	PermissionDeniedException {
		Assert.notNull(beacon, "Checking beacon");
		Assert.notNull(beacon.getId(), "Checking beacon id");
		try {
			getJdbcTemplate().update(DEL_BEACON, beacon.getId());
		} catch (final DataAccessException e) {
			throw new PermissionDeniedException(e.getMessage());
		}
	}

	@Override
	public void deleteBeaconInfoByUserId(final Integer userIdPk,
			final Date fromDate, final Date toDate) {
		getJdbcTemplate().update(DEL_BEACON_BY_MEMBERID, userIdPk, fromDate,
				toDate);

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
	public BeaconDto getBeacon(final Integer beaconIdPk) {
		final BeaconDto beacon = getJdbcTemplate().queryForObject(
				GET_BEACON_BY_PKID, beaconDtoMapper, beaconIdPk);
		try {
			beacon.setClub(clubDao.get(beacon.getClub().getId(), false));
		} catch (final NotFoundException e) {
		}
		try {
			beacon.setSubAmenity(clubDao.getSubAmenity(beacon.getSubAmenity().getId()));
		} catch (final NotFoundException e) {
		}
		if ((beacon.getInstallerStaff() != null)
				&& (beacon.getInstallerStaff().getId() != null)) {
			try {
				beacon.setInstallerStaff(accountDao.fetch(beacon
						.getInstallerStaff().getId()));
			} catch (final NotFoundException | InvalidParameterException e) {
			}
		}
		return beacon;
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
	public List<BeaconDto> getBeacons(final Integer clubIdPk,
			final Integer subAmenityIdPk) {
		final List<BeaconDto> beacons = getJdbcTemplate().query(
				GET_BEACONS_BYCLUBID_SUBAMENITYID, beaconDtoMapper, clubIdPk,
				subAmenityIdPk);
		if (CollectionUtils.isNotEmpty(beacons)) {
			for (final BeaconDto beacon : beacons) {
				try {
					beacon.setClub(clubDao.get(clubIdPk, false));
				} catch (final NotFoundException e) {
				}
				try {
					beacon.setSubAmenity(clubDao.getSubAmenity(subAmenityIdPk));
				} catch (final NotFoundException e) {
				}
				if ((beacon.getInstallerStaff() != null)
						&& (beacon.getInstallerStaff().getId() != null)) {
					try {
						beacon.setInstallerStaff(accountDao.fetch(beacon
								.getInstallerStaff().getId()));
					} catch (final NotFoundException | InvalidParameterException e) {
					}
				}
			}
		}
		return beacons;
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
	public Integer getSubAmenityIdPk(final Integer beaconIdPk)
			throws NotFoundException {
		try {
			final Integer clubSubAmenityIdPk = getJdbcTemplate()
					.queryForObject(GET_SUBAMENITY_IDPK_BY_BEACON_ID_PK,
							Integer.class, beaconIdPk);
			return clubSubAmenityIdPk;
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("BeaconIdPk : " + beaconIdPk
					+ " not found!");
		}
	}

	@Override
	public List<AccountDto> getUsers(
			final String subAmenityId, final Date onDate) {
		List<AccountDto> users = null;
		final String orderBy = " ORDER BY BU.CREATE_DATE DESC ";
		if (onDate == null) {
			users = getJdbcTemplate().query(
					SEL_USER + "DATE(NOW()) " + orderBy,
					accountDtoCheckInDateMapper, subAmenityId);
		} else {
			users = getJdbcTemplate().query(SEL_USER + "DATE(?) " + orderBy,
					accountDtoCheckInDateMapper, subAmenityId, onDate);
		}
		if (CollectionUtils.isNotEmpty(users)) {
			for (final AccountDto user : users) {
				accountDao.populateAccountInfo(user, true);
			}
		}
		return users;
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
	public void setSubAmenityId(final String clubId, final String apnsToken,
			final String amenityId, final String subAmenityId) throws NotFoundException {
		try {
			final Integer clubIdPk = getJdbcTemplate().queryForObject(
					GET_CLUB_ID, Integer.class, clubId);

			final Integer amenityPkId = StringUtils.isNotEmpty(amenityId)? clubDao.getClubAmenityIdPk(clubIdPk, amenityId): null;
			final int counter = getJdbcTemplate().queryForObject(
					CHECK_SUBAMENITY_DEPT_NAME, Integer.class, subAmenityId,
					clubIdPk);
			if (counter == 0) {
				final Integer subAmenityIdPk = sequencer.nextVal("ID_SEQ");
				getJdbcTemplate().update(INS_SUBAMENITY_DEPT_NAME,
						// ID
						subAmenityIdPk,
						// CLUB_ID
						clubIdPk,
						// SUBAMENITYID
						subAmenityId,
						// IMAGE_ID
						null,
						// DESCRIPTION
						"",
						//AMENITY_ID
						amenityPkId);
				final Integer mapAmenityIdPk = sequencer.nextVal("ID_SEQ");
				getJdbcTemplate().update(INS_CLUB_APNS_TOKEN,
						// ID,
						mapAmenityIdPk,
						// CLUB_SUBAMENITY_ID,
						subAmenityIdPk,
						// APNS_TOKEN,
						apnsToken);

			} else {
				final Integer subAmenityIdPk = getJdbcTemplate().queryForObject(
						GET_SUBAMENITY_ID_BY_NAME_CLUBID, Integer.class,
						subAmenityId, clubIdPk);
				final int updated = getJdbcTemplate().update(
						UPD_CLUB_APNS_TOKEN,
						// APNS_TOKEN,
						apnsToken,
						// CLUB_SUBAMENITY_ID,
						subAmenityIdPk);
				if (updated == 0) {
					// Insert
					final Integer mapAmenityIdPk = sequencer.nextVal("ID_SEQ");
					getJdbcTemplate().update(INS_CLUB_APNS_TOKEN,
							// ID,
							mapAmenityIdPk,
							// CLUB_SUBAMENITY_ID,
							subAmenityIdPk,
							// APNS_TOKEN,
							apnsToken);
				}
			}

		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Club " + clubId + " not found!");
		}

	}

	@Override
	public void updateBeaconInfo(final BeaconDto beacon)
			throws NotFoundException {
		Assert.notNull(beacon, "Checking beacon");
		Assert.notNull(beacon.getBeaconActionId(), "Checking beaconid");
		Assert.notNull(beacon.getLocation(), "Checking beacon location");
		Assert.notNull(beacon.getClub(), "Checking club");
		Assert.notNull(beacon.getSubAmenity(), "Checking sub amenity");
		Assert.notNull(beacon.getInstallerStaff(), "Checking installer");
		Assert.notNull(beacon.getClub().getId(), "Checking club idpk");
		Assert.notNull(beacon.getSubAmenity().getId(), "Checking sub amenity id pk");
		Assert.notNull(beacon.getInstallerStaff().getId(),
				"Checking installer id pk");

		try {
			final Integer beaconIdPk = getJdbcTemplate().queryForObject(
					GET_BEACON_ID, Integer.class, beacon.getBeaconActionId());
			beacon.setId(beaconIdPk);
		} catch (final EmptyResultDataAccessException e) {
		}

		if (beacon.getId() == null) {
			// Insert
			final int beaconIdPk = sequencer.nextVal("ID_SEQ");
			beacon.setId(beaconIdPk);

			getJdbcTemplate().update(INS_BEACON, beaconIdPk,
					beacon.getBeaconActionId(), beacon.getClub().getId(),
					beacon.getSubAmenity().getId(), beacon.getLocation(),
					beacon.getDescription(),
					beacon.getInstallerStaff().getId(),
					beacon.getInstallationDate());
		} else {
			// update
			getJdbcTemplate().update(UPD_BEACON, beacon.getClub().getId(),
					beacon.getSubAmenity().getId(), beacon.getLocation(),
					beacon.getDescription(),
					beacon.getInstallerStaff().getId(), beacon.getId());
		}
	}

}
