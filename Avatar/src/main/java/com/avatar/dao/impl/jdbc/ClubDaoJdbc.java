package com.avatar.dao.impl.jdbc;

import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Repository;

import com.avatar.dao.AccountDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.impl.jdbc.mapper.AmenityMapper;
import com.avatar.dao.impl.jdbc.mapper.ClubDtoMapper;
import com.avatar.dao.impl.jdbc.mapper.StringMapper;
import com.avatar.dto.ImagePic;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.ClubListingSortBy;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.NotFoundException;

@Repository
public class ClubDaoJdbc extends BaseJdbcDao implements ClubDao {

	private static String INS_USER_TO_CLUB_PK = "INSERT INTO USER_CLUBS (ID, USER_ID, CLUB_ID) VALUES (?,?,?)";

	private static String GET_CLUB_FROM_PK = "SELECT * FROM CLUBS where ID = ?";

	private static String GET_CLUB_FROM_CLUBID = "SELECT * FROM CLUBS WHERE CLUBID = ?";
	private static String GET_IMAGE_ID = "SELECT IMAGE_ID FROM CLUBS WHERE ID=?";

	private static String GET_CLUBIDPK = "SELECT ID FROM CLUBS WHERE CLUBID=?";
	private static String GET_CLUBIDPK_BYKEYCODE = "SELECT CLUB_ID FROM CLUB_KEYS WHERE KEYCODE = ?";

	private static String SEL_AMENITY_PK_BY_AMENITYID = "SELECT ID FROM CLUB_AMENITIES WHERE AMENITYID = ? ";
	private static String SEL_AMENITY_BY_PK = "SELECT CA.*, AT.NAME FROM CLUB_AMENITIES CA, AMENITY_TYPES AT WHERE CA.ID = ? and AT.ID = CA.AMENITY_TYPE_ID ";

	private static String SEL_AMENITIES_BY_CLUBID = "SELECT CA.*, AT.NAME FROM CLUB_AMENITIES CA, AMENITY_TYPES AT WHERE CLUB_ID = ? and AT.ID = CA.AMENITY_TYPE_ID ";

	private static String SEL_AMENITIES_BY_CLUBID_AMENITY_TYPE = SEL_AMENITIES_BY_CLUBID
			+ " AND UPPER(AT.NAME)=UPPER(?)";

	private static String UPD_CLUB_INFO = "update CLUBS set NAME=?, ADDRESS=?, ZIPCODE=?,"
			+ "CITY=?, STATE_ABBR=?, PHONE_NUMBER=?, HZRESTRICTION=?, CLUB_TYPE=?, CLUB_WEBSITE=?, TIME_ZONE=?, X_COORD=?, Y_COORD=? "
			+ "WHERE ID=? ";

	private static String GET_EMPLOYEES_FOR_AMENITY_PK = "select USER_ID from AMENITY_EMPLOYEE where CLUB_AMENITY_ID = ? ";

	static private String SEL_CLUBS_BY_USER_IDPK = "SELECT CLUBS.* FROM CLUBS, USERS, USER_ROLES where USERS.ID = ? and HOME_CLUB_ID = CLUBS.ID AND USER_ROLES.USER_ID = USERS.ID and ROLE != '"
			+ Privilege.superUser.name() + "'";

	static private String SEL_SUPER_USER_CLUBS_BY_USER_IDPK = "SELECT CLUBS.* FROM CLUBS where exists (select 1 from USERS, USER_ROLES where USERS.ID = ? AND USER_ROLES.USER_ID = USERS.ID and ROLE = '"
			+ Privilege.superUser.name() + "')";

	private static String COUNT_CLUB_PIN = "select count(*) from CLUBS where club_pin = ? ";

	private final static String GET_CLUB_BODY_TEXT = "select BODY_TEXT FROM CLUBS where ID = ? ";

	private final static String GET_CLUB_HEADLINE_TEXT = "select HEADER_TEXT FROM CLUBS where ID = ? ";

	private static final String GET_CLUBS_BY_STATE = "select C.*, RS.STATE_NAME FROM CLUBS C, REF_STATES RS where C.STATE_ABBR = RS.STATE_ABBR AND (UPPER(RS.STATE_NAME) = ? OR RS.STATE_ABBR=?)  ORDER BY ";
	private static final String GET_ALL_CLUBS = "select C.*, RS.STATE_NAME FROM CLUBS C, REF_STATES RS where C.STATE_ABBR = RS.STATE_ABBR ORDER BY ";

	private static final String UPD_CLUB_AMENITY = "UPDATE CLUB_AMENITIES set COLUMN=? where CLUB_ID = ? and ID = ?";
	private static final String UPD_CLUB_AMENITY_BODY = UPD_CLUB_AMENITY.replace("COLUMN", "BODY_TEXT");
	private static final String UPD_CLUB_AMENITY_HEADER = UPD_CLUB_AMENITY.replace("COLUMN", "HEADER_TEXT");
	private static final String UPD_CLUB_AMENITY_SECONDARY_HEADER = UPD_CLUB_AMENITY.replace("COLUMN", "SECONDARY_HEADER_TEXT");
	private static final String UPD_CLUB = "UPDATE CLUBS SET COLUMN = ? where ID = ?";
	private static final String UPD_CLUB_BODY = UPD_CLUB.replace("COLUMN", "BODY_TEXT");
	private static final String UPD_CLUB_HEADER = UPD_CLUB.replace("COLUMN", "HEADER_TEXT");

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	private final ClubDtoMapper clubDtoMapper = new ClubDtoMapper();

	private final AmenityMapper amenityMapper = new AmenityMapper();

	private final StringMapper stringMapper = new StringMapper();

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

	private String buildClubsByStateSql(final String state,
			final ClubListingSortBy orderByClause) {
		String retVal = GET_CLUBS_BY_STATE;
		if (StringUtils.isEmpty(state)) {
			retVal = GET_ALL_CLUBS;
		}
		switch(orderByClause) {
		case clubName:
			retVal = retVal + " NAME ";
			break;
		case state:
		default:
			retVal = retVal + " STATE_NAME ";
			break;
		}
		retVal = retVal + " DESC ";
		return retVal;
	}

	@Override
	public ClubDto get(final Integer clubIdPk, final boolean includePicture)
			throws NotFoundException {
		ClubDto retVal = null;

		if (clubIdPk != null) {
			retVal = getJdbcTemplate().queryForObject(GET_CLUB_FROM_PK,
					clubDtoMapper, clubIdPk);
			if (includePicture) {
				try {
					final Integer imageIdPk = getJdbcTemplate().queryForObject(
							GET_IMAGE_ID, Integer.class, retVal.getId());
					final ImagePic image = getImage(imageIdPk);
					retVal.setImage(image);
				} catch (final EmptyResultDataAccessException e1) {
				}
			}
		}
		return retVal;
	}

	@Override
	public ClubDto get(final String clubId) throws NotFoundException {
		try {
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
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Club " + clubId + " not found");
		}
	}

	@Override
	public List<AmenityDto> getAmenities(final Integer clubIdPk)
			throws NotFoundException {
		final List<AmenityDto> amenities = getJdbcTemplate().query(
				SEL_AMENITIES_BY_CLUBID, amenityMapper, clubIdPk);
		return amenities;
	}

	@Override
	public List<AmenityDto> getAmenities(final Integer clubIdPk,
			final String amenityType) throws NotFoundException {
		final List<AmenityDto> amenities = getJdbcTemplate().query(
				SEL_AMENITIES_BY_CLUBID_AMENITY_TYPE, amenityMapper, clubIdPk,
				amenityType);
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
	public String getBodyText(final int clubIdPk) {
		String retVal = "";
		try {
			retVal = getJdbcTemplate().queryForObject(GET_CLUB_BODY_TEXT,
					stringMapper, clubIdPk);
		} catch (final EmptyResultDataAccessException e) {
		}
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
	public ClubDto getClubByKeyCode(final String clubKeycode)
			throws NotFoundException {
		final Integer clubIdPk = getClubIdPkByKeyCode(clubKeycode);
		return get(clubIdPk, true);
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

	private Integer getClubIdPkByKeyCode(final String clubKeycode) throws NotFoundException {
		try {
			final Integer clubIdPk = getJdbcTemplate().queryForObject(
					GET_CLUBIDPK_BYKEYCODE, Integer.class, clubKeycode);
			return clubIdPk;
		} catch (final IncorrectResultSizeDataAccessException e) {
			throw new NotFoundException("Club Keycode " + clubKeycode + " not found!");
		}
	}

	@Override
	public List<ClubDto> getClubs(final Integer userIdPk)
			throws NotFoundException {
		List<ClubDto> clubs = getJdbcTemplate().query(SEL_CLUBS_BY_USER_IDPK,
				clubDtoMapper, userIdPk);
		if (CollectionUtils.isEmpty(clubs)) {
			clubs = getJdbcTemplate().query(SEL_SUPER_USER_CLUBS_BY_USER_IDPK,
					clubDtoMapper, userIdPk);
		}
		populateClubDetails(clubs);
		return clubs;
	}

	@Override
	public List<ClubDto> getClubsByState(final String state,
			final ClubListingSortBy orderByClause) {
		List<ClubDto> clubs = null;
		if (StringUtils.isEmpty(state)) {
			clubs = getJdbcTemplate().query(
					buildClubsByStateSql(state, orderByClause), clubDtoMapper);
		} else {
			clubs = getJdbcTemplate().query(
					buildClubsByStateSql(state, orderByClause), clubDtoMapper,
					state, state);
		}
		populateClubDetails(clubs);
		return clubs;
	}

	@Override
	public String getHeadlineText(final int clubIdPk) {
		String retVal = "";
		try {
			retVal = getJdbcTemplate().queryForObject(GET_CLUB_HEADLINE_TEXT,
					stringMapper, clubIdPk);
		} catch (final EmptyResultDataAccessException e) {
		}
		return retVal;
	}

	private void populateClubDetails(final List<ClubDto> clubs) {
		if (CollectionUtils.isNotEmpty(clubs)) {
			for (final ClubDto clubDto : clubs) {
				try {
					final Integer imageIdPk = getJdbcTemplate().queryForObject(
							GET_IMAGE_ID, Integer.class, clubDto.getId());
					final ImagePic image = getImage(imageIdPk);
					clubDto.setImage(image);
				} catch (final EmptyResultDataAccessException e1) {
				}
			}

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
				club.getTimeZone().getDbSetting(), club.getXcoord(), club.getYcoord(), club.getId());
	}

	@Override
	public void updateAmenityBody(final Integer clubIdPk, final Integer amenityIdPk,
			final String bodyText) throws NotFoundException {
		getJdbcTemplate().update(UPD_CLUB_AMENITY_BODY, bodyText, clubIdPk, amenityIdPk);
	}

	@Override
	public void updateAmenityHeaderText(final Integer clubIdPk, final Integer amenityIdPk,
			final String headerText) throws NotFoundException {
		getJdbcTemplate().update(UPD_CLUB_AMENITY_HEADER, headerText, clubIdPk, amenityIdPk);
	}

	@Override
	public void updateAmenitySecondaryHeaderText(final Integer clubIdPk, final Integer amenityIdPk,
			final String headerText) throws NotFoundException {
		getJdbcTemplate().update(UPD_CLUB_AMENITY_SECONDARY_HEADER, headerText, clubIdPk, amenityIdPk);
	}

	@Override
	public void updateBodyText(final Integer clubIdPk, final String bodyText)
			throws NotFoundException {
		getJdbcTemplate().update(UPD_CLUB_BODY, bodyText, clubIdPk);
	}

	@Override
	public void updateHeaderText(final Integer clubIdPk, final String headerText)
			throws NotFoundException {
		getJdbcTemplate().update(UPD_CLUB_HEADER, headerText, clubIdPk);
	}

	@Override
	public boolean verifyClubPin(final String clubPin) {
		final Integer counter = getJdbcTemplate().queryForObject(
				COUNT_CLUB_PIN, Integer.class, clubPin);
		return counter > 0;
	}

}
