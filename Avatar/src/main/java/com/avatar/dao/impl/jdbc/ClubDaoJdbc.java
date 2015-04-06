package com.avatar.dao.impl.jdbc;

import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Repository;

import com.avatar.dao.AccountDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.impl.jdbc.mapper.AmenityMapper;
import com.avatar.dao.impl.jdbc.mapper.ClubDtoMapper;
import com.avatar.dto.ImagePic;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.exception.NotFoundException;

@Repository
public class ClubDaoJdbc extends BaseJdbcDao implements ClubDao {

	private static String INS_USER_TO_CLUB_PK = "INSERT INTO USER_CLUBS (ID, USER_ID, CLUB_ID) VALUES (?,?,?)";

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	private static String GET_CLUB_FROM_PK = "SELECT * FROM CLUBS WHERE ID = ?";
	private static String GET_CLUB_FROM_CLUBID = "SELECT * FROM CLUBS WHERE CLUBID = ?";

	private final ClubDtoMapper clubDtoMapper = new ClubDtoMapper();

	private static String GET_IMAGE_ID = "SELECT IMAGE_ID FROM CLUBS WHERE ID=?";
	private static String GET_CLUBIDPK = "SELECT ID FROM CLUBS WHERE CLUBID=?";

	private static String SEL_AMENITY_PK_BY_AMENITYID = "SELECT ID FROM CLUB_AMENITIES WHERE AMENITYID = ? ";

	private static String SEL_AMENITY_BY_PK = "SELECT CA.*, AT.NAME FROM CLUB_AMENITIES CA, AMENITY_TYPES AT WHERE CA.ID = ? and AT.ID = CA.AMENITY_TYPE_ID ";

	private final AmenityMapper amenityMapper = new AmenityMapper();

	private static String SEL_AMENITIES_BY_CLUBID = "SELECT CA.*, AT.NAME FROM CLUB_AMENITIES CA, AMENITY_TYPES AT WHERE CLUB_ID = ? and AT.ID = CA.AMENITY_TYPE_ID ";

	private static String UPD_CLUB_INFO = "update CLUBS set NAME=?, ADDRESS=?, ZIPCODE=?,"
			+ "CITY=?, STATE=?, PHONE_NUMBER=?, HZRESTRICTION=?, CLUB_TYPE=?, CLUB_WEBSITE=? "
			+ "WHERE ID=? ";

	private static String GET_EMPLOYEES_FOR_AMENITY_PK = "select USER_ID from AMENITY_EMPLOYEE where CLUB_AMENITY_ID = ? ";

	@Override
	public void addUserToClub(final int clubIdPk, final int userIdPk)
			throws NotFoundException {
		final int idUserClubMappingIdPk = sequencer.nextVal("ID_SEQ");
		getJdbcTemplate().update(INS_USER_TO_CLUB_PK, idUserClubMappingIdPk,
				userIdPk, clubIdPk);
	}

	@Override
	public void addUserToClub(final String clubId, final String userId)
			throws NotFoundException {
		final int userIdPk = accountDao.getUserIdPkByUserId(userId);
		final int clubIdPk = getClubIdPk(clubId);
		addUserToClub(clubIdPk, userIdPk);
	}

	@Override
	public ClubDto get(final Integer clubIdPk) throws NotFoundException {
		ClubDto retVal = null;

		if (clubIdPk != null) {
			retVal = getJdbcTemplate().queryForObject(GET_CLUB_FROM_PK,
					clubDtoMapper, clubIdPk);
			try {
				final Integer imageIdPk = getJdbcTemplate().queryForObject(
						GET_IMAGE_ID, Integer.class, retVal.getId());
				final ImagePic image = getImage(imageIdPk);
				retVal.setImage(image);
			} catch (final EmptyResultDataAccessException e1) {
			}
		}
		return retVal;
	}

	@Override
	public ClubDto get(final String clubId) throws NotFoundException {
		final ClubDto retVal = getJdbcTemplate().queryForObject(
				GET_CLUB_FROM_CLUBID, clubDtoMapper, clubId);
		try {
			final Integer imageIdPk = getJdbcTemplate().queryForObject(
					GET_IMAGE_ID, Integer.class, retVal.getId());
			final ImagePic image = getImage(imageIdPk);
			retVal.setImage(image);
		} catch (final EmptyResultDataAccessException e1) {
		}
		return retVal;
	}

	@Override
	public List<AmenityDto> getAmenities(final Integer clubIdPk)
			throws NotFoundException {
		final List<AmenityDto> amenities = getJdbcTemplate().query(
				SEL_AMENITIES_BY_CLUBID, amenityMapper, clubIdPk);
		return amenities;
	}

	@Override
	public AmenityDto getAmenity(final Integer amenityIdPk)
			throws NotFoundException {
		try {
			final AmenityDto amenity = getJdbcTemplate().queryForObject(
					SEL_AMENITY_BY_PK, amenityMapper, amenityIdPk);
			return amenity;
		} catch (final IncorrectResultSizeDataAccessException e) {
			throw new NotFoundException("Club Amenity " + amenityIdPk
					+ " not found!");
		}
	}

	@Override
	public List<Integer> getAmenityEmployees(final Integer clubAmenityId)
			throws NotFoundException {
		final List<Integer> retVal = getJdbcTemplate().queryForList(
				GET_EMPLOYEES_FOR_AMENITY_PK, Integer.class, clubAmenityId);
		return retVal;
	}

	@Override
	public Integer getClubAmenityIdPk(final String clubAmenityId)
			throws NotFoundException {
		try {
			final Integer amenityIdPk = getJdbcTemplate().queryForObject(
					SEL_AMENITY_PK_BY_AMENITYID, Integer.class, clubAmenityId);
			return amenityIdPk;
		} catch (final IncorrectResultSizeDataAccessException e) {
			throw new NotFoundException("Club Amenity " + clubAmenityId
					+ " not found!");
		}
	}

	@Override
	public Integer getClubIdPk(final String clubId) throws NotFoundException {
		try {
			final Integer clubIdPk = getJdbcTemplate().queryForObject(
					GET_CLUBIDPK, Integer.class, clubId);
			return clubIdPk;
		} catch (final IncorrectResultSizeDataAccessException e) {
			throw new NotFoundException("Club " + clubId + " not found!");
		}
	}

	@Override
	@Resource(name = "avatarDataSource")
	public void setDataSource(final DataSource ds) {
		initTemplate(ds);
	}

	@Override
	public void update(final ClubDto club) throws NotFoundException {
		final Integer clubIdPk = getClubIdPk(club.getClubId());
		club.setId(clubIdPk);
		getJdbcTemplate().update(UPD_CLUB_INFO, club.getClubName(),
				club.getAddress(), club.getZipCode(), club.getCity(),
				club.getState(), club.getPhoneNumber(),
				club.getHzRestriction(), club.getClubType(), club.getWebSite(),
				club.getId());
	}

}
