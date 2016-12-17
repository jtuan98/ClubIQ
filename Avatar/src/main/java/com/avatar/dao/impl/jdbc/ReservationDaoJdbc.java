package com.avatar.dao.impl.jdbc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.avatar.dao.ReservationDao;
import com.avatar.dao.impl.jdbc.mapper.BlackoutDateMapper;
import com.avatar.dao.impl.jdbc.mapper.BlackoutTimesMapper;
import com.avatar.dao.impl.jdbc.mapper.ReservationMapper;
import com.avatar.dto.club.BlackoutDate;
import com.avatar.dto.club.BlackoutTime;
import com.avatar.dto.club.CheckInfo;
import com.avatar.exception.NotFoundException;

@Repository
public class ReservationDaoJdbc extends BaseJdbcDao implements ReservationDao {

	private static final String INS_RESERVATION = "INSERT INTO USER_RESERVATIONS (ID, RESERVATION_NUMBER, "
			+ "USER_ID,"
			+ "CLUB_ID,"
			+ "SUBAMENITY_ID,"
			+ "NO_PERSONS,"
			+ "RESERVATION_DATE) VALUES (?,?,?,?,?,?,?) ";

	private static final String SEL_RESERVATION_BY_AVAILID = "SELECT UR.*, CSA.SUBAMENITYID, c.CLUBID, CSA.DESCRIPTION SUBAMENITY_NAME FROM USER_RESERVATIONS UR, CLUB_SUB_AMENITIES CSA, CLUBS c WHERE c.id = UR.CLUB_ID and UR.SUBAMENITY_ID = CSA.ID AND USER_ID = ? AND RESERVATION_NUMBER = ?";

	private static final String SEL_BLACKOUT_DAYS_BY_MONTH_AMENITYID = "SELECT DAY(BLACKOUT_DATE) BLACKOUT_DAY FROM AMENITY_BLACKOUT where club_id = ? and SUBAMENITY_ID = ? and YEAR(BLACKOUT_DATE) = ? and MONTH(BLACKOUT_DATE) = ? ORDER BY 1";

	private static final String SEL_BLACKOUT_TIMES_BY_MONTH_DAY_AMENITYID = "SELECT BLACKOUT_HOURS FROM AMENITY_BLACKOUT where club_id = ? and SUBAMENITY_ID = ? and YEAR(BLACKOUT_DATE) = ? and MONTH(BLACKOUT_DATE) = ? and DAY(BLACKOUT_DATE) = ? LIMIT 1";
	private static final String SEL_BLACKOUT_TIMES_BY_FROMDAY_AMENITYID= "SELECT BLACKOUT_HOURS FROM AMENITY_BLACKOUT where club_id = ? and SUBAMENITY_ID = ? and BLACKOUT_DATE = ?  LIMIT 1";

	private static final String INS_AMENITY_BLACKOUT_TABLE = "INSERT INTO AMENITY_BLACKOUT (ID, CLUB_ID, SUBAMENITY_ID, BLACKOUT_DATE, BLACKOUT_HOURS) values (?,?,?,?,?)";

	private static final String UPDATE_AMENITY_BLACKOUT_TABLE = "UPDATE AMENITY_BLACKOUT set BLACKOUT_HOURS = ? where club_id = ? and subamenity_id = ? and BLACKOUT_DATE = ?";

	private static final String CHK_AMENITY_BLACKOUT_EXISTS = "select count(*) from AMENITY_BLACKOUT where club_id = ? and subamenity_id = ? and BLACKOUT_DATE = ?";

	private final ReservationMapper reservationMapper = new ReservationMapper();

	private final BlackoutDateMapper blackoutDateMapper = new BlackoutDateMapper();

	private final BlackoutTimesMapper blackoutTimesMapper = new BlackoutTimesMapper();

	SimpleDateFormat yyyymmddFormatter = new SimpleDateFormat("yyyyMMdd");

	@Override
	public List<BlackoutDate> fetchBlackoutDates(final int clubIdPk,
			final int subAmenityIdPk, final String year, final String month) {
		final List<BlackoutDate> retVal = getJdbcTemplate().query(
				SEL_BLACKOUT_DAYS_BY_MONTH_AMENITYID, blackoutDateMapper,
				clubIdPk, subAmenityIdPk, year, month);
		return retVal;
	}

	@Override
	public Map<String, List<BlackoutTime>> fetchBlackoutTimes(final int clubIdPk,
			final int subAmenityIdPk, final Date requestedDateFrom, final Date requestedDateTo) {
		final Map<String, List<BlackoutTime>> retVal = new LinkedHashMap<String, List<BlackoutTime>>();
		for(Date requestDate=requestedDateFrom;requestDate.getTime()<=requestedDateTo.getTime();requestDate=DateUtils.addDays(requestDate, 1)) {
			try {
				retVal.put(yyyymmddFormatter.format(requestDate), getJdbcTemplate().queryForObject(
						SEL_BLACKOUT_TIMES_BY_FROMDAY_AMENITYID,
						blackoutTimesMapper, clubIdPk, subAmenityIdPk,
						yyyymmddFormatter.format(requestDate)));
			} catch (final EmptyResultDataAccessException e) {

			}
		}
		return retVal;
	}

	@Override
	public List<BlackoutTime> fetchBlackoutTimes(final int clubIdPk,
			final int subAmenityIdPk, final String year, final String month,
			final String day) {
		List<BlackoutTime> retVal = null;
		try {
			retVal = getJdbcTemplate().queryForObject(
					SEL_BLACKOUT_TIMES_BY_MONTH_DAY_AMENITYID,
					blackoutTimesMapper, clubIdPk, subAmenityIdPk,
					Integer.parseInt(year), Integer.parseInt(month),
					Integer.parseInt(day));
		} catch (final EmptyResultDataAccessException e) {

		}
		return retVal;
	}

	@Override
	public CheckInfo getReservation(final int userIdPk, final String availId)
			throws NotFoundException {
		CheckInfo retVal = null;
		try {
			retVal = getJdbcTemplate().queryForObject(
					SEL_RESERVATION_BY_AVAILID, reservationMapper, userIdPk,
					availId);
		} catch (final EmptyResultDataAccessException e) {
		}
		return retVal;
	}

	@Override
	public Number reserve(final int clubIdPk, final int subAmenityIdPk,
			final int userIdPk, final int numberOfPeople,
			final Date reservationDate, final String reservationNumber)
					throws NotFoundException {
		final int idReservationAdded = sequencer.nextVal("ID_SEQ");
		getJdbcTemplate().update(INS_RESERVATION, idReservationAdded,
				reservationNumber, userIdPk, clubIdPk, subAmenityIdPk,
				numberOfPeople, reservationDate);
		return idReservationAdded;
	}

	@Override
	public void setBlackoutTimes(final int clubIdPk, final int subAmenityIdPk,
			final Date blackoutDate, final String blackoutTimes) {
		final int counter = getJdbcTemplate().queryForObject(
				CHK_AMENITY_BLACKOUT_EXISTS, Integer.class, clubIdPk,
				subAmenityIdPk, blackoutDate);
		if (counter == 0) {
			final int idBlackoutPk = sequencer.nextVal("ID_SEQ");
			getJdbcTemplate().update(INS_AMENITY_BLACKOUT_TABLE, idBlackoutPk,
					clubIdPk, subAmenityIdPk, blackoutDate, blackoutTimes);
		} else {
			// update
			getJdbcTemplate().update(UPDATE_AMENITY_BLACKOUT_TABLE, blackoutTimes, clubIdPk,
					subAmenityIdPk, blackoutDate );
		}
	}
	@Override
	@Resource(name = "avatarDataSource")
	public void setDataSource(final DataSource ds) {
		initTemplate(ds);
	}

}
