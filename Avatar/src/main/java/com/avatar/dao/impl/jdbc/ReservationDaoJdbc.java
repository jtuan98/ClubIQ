package com.avatar.dao.impl.jdbc;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

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

	private static final String SEL_RESERVATION_BY_AVAILID = "SELECT UR.*, CSA.SUBAMENITYID, CSA.NAME SUBAMENITY_NAME, c.CLUBID FROM USER_RESERVATIONS UR, CLUB_SUB_AMENITIES csa, CLUBS c WHERE c.id = UR.CLUB_ID and UR.SUBAMENITY_ID = CSA.ID AND USER_ID = ? AND RESERVATION_NUMBER = ?";

	private static final String SEL_BLACKOUT_DAYS_BY_MONTH_AMENITYID = "SELECT DAY(BLACKOUT_DATE) BLACKOUT_DAY FROM AMENITY_BLACKOUT where club_id = ? and SUBAMENITY_ID = ? and YEAR(BLACKOUT_DATE) = YEAR(NOW()) and MONTH(BLACKOUT_DATE) = ? ORDER BY 1";

	private static final String SEL_BLACKOUT_TIMES_BY_MONTH_DAY_AMENITYID = "SELECT BLACKOUT_HOURS FROM AMENITY_BLACKOUT where club_id = ? and SUBAMENITY_ID = ? and YEAR(BLACKOUT_DATE) = YEAR(NOW()) and MONTH(BLACKOUT_DATE) = ? and DAY(BLACKOUT_DATE) = ? LIMIT 1";

	private final ReservationMapper reservationMapper = new ReservationMapper();

	private final BlackoutDateMapper blackoutDateMapper = new BlackoutDateMapper();

	private final BlackoutTimesMapper blackoutTimesMapper = new BlackoutTimesMapper();

	@Override
	public List<BlackoutDate> fetchBlackoutDates(final int clubIdPk, final int subAmenityIdPk,
			final String month) {
		final List<BlackoutDate> retVal = getJdbcTemplate().query(SEL_BLACKOUT_DAYS_BY_MONTH_AMENITYID,
				blackoutDateMapper, clubIdPk, subAmenityIdPk, month);
		return retVal;
	}
	@Override
	public List<BlackoutTime> fetchBlackoutTimes(final int clubIdPk, final int subAmenityIdPk,
			final String month, final String day) {
		List<BlackoutTime> retVal = null;
		try {
			retVal = getJdbcTemplate().queryForObject(SEL_BLACKOUT_TIMES_BY_MONTH_DAY_AMENITYID,
					blackoutTimesMapper, clubIdPk, subAmenityIdPk, Integer.parseInt(month), Integer.parseInt(day));
		} catch(final EmptyResultDataAccessException e) {

		}
		return retVal;
	}

	@Override
	public CheckInfo getReservation(final int userIdPk, final String availId)
			throws NotFoundException {
		CheckInfo retVal = null;
		try {
			retVal = getJdbcTemplate().queryForObject(SEL_RESERVATION_BY_AVAILID,
					reservationMapper, userIdPk, availId);
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
	@Resource(name = "avatarDataSource")
	public void setDataSource(final DataSource ds) {
		initTemplate(ds);
	}

}
