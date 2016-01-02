package com.avatar.dao.impl.jdbc;

import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
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
import com.avatar.dao.impl.jdbc.mapper.SubAmenityDtoMapper;
import com.avatar.dto.ImagePic;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.club.SubAmenityDto;
import com.avatar.dto.enums.ClubListingSortBy;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.NotFoundException;

@Repository
public class ClubDaoJdbc extends BaseJdbcDao implements ClubDao {

	private static String INS_USER_TO_CLUB_PK = "INSERT INTO USER_CLUBS (ID, USER_ID, CLUB_ID) VALUES (?,?,?)";

	private static String GET_CLUB_FROM_PK = "SELECT * FROM CLUBS where ID = ?";

	private static String GET_CLUB_FROM_CLUBID = "SELECT * FROM CLUBS WHERE CLUBID = ?";
	private static String GET_CLUB_IMAGE_ID = "SELECT IMAGE_ID FROM CLUBS WHERE ID=?";

	private static String GET_CLUBIDPK = "SELECT ID FROM CLUBS WHERE CLUBID=?";
	private static String GET_CLUBIDPK_BYKEYCODE = "SELECT CLUB_ID FROM CLUB_KEYS WHERE KEYCODE = ?";
	private static String GET_AMENITY_IMAGE_ID = "SELECT IMAGE_ID FROM CLUB_AMENITIES WHERE ID=?";
	private static String SEL_AMENITY_PK_BY_AMENITYID = "SELECT ID FROM CLUB_AMENITIES WHERE CLUB_ID = ? AND AMENITYID = ? ";
	private static String SEL_AMENITY_BY_CLUBID_IDPK = "SELECT * FROM CLUB_AMENITIES WHERE CLUB_ID = ? and ID = ? ORDER BY ORDERING";
	private static String SEL_AMENITIES_BY_CLUBID = "SELECT * FROM CLUB_AMENITIES WHERE CLUB_ID = ? ORDER BY ORDERING";
	private static String SEL_SUBAMENITY_PK_BY_SUBAMENITYID = "SELECT ID FROM CLUB_SUB_AMENITIES WHERE CLUB_ID= ? AND SUBAMENITYID = ? ";
	private static String SEL_SUBAMENITY_BY_PK = "SELECT CA.*, AT.AMENITYID, AT.DESCRIPTION AMENITY_NAME FROM CLUB_SUB_AMENITIES CA, CLUB_AMENITIES AT WHERE CA.ID = ? and AT.ID = CA.AMENITY_ID ";
	private static String GET_SUBAMENITY_IMAGE_ID = "SELECT IMAGE_ID FROM CLUB_SUB_AMENITIES WHERE ID=?";

	private static String SEL_SUBAMENITIES_BY_CLUBID = "SELECT CA.*, AT.AMENITYID, AT.DESCRIPTION AMENITY_NAME FROM CLUB_SUB_AMENITIES CA, CLUB_AMENITIES AT WHERE CA.CLUB_ID = ? and AT.ID = CA.AMENITY_ID ";

	private static String SEL_SUBAMENITIES_BY_CLUBID_AMENITYID = SEL_SUBAMENITIES_BY_CLUBID
			+ " AND UPPER(AT.AMENITYID)=UPPER(?) ORDER BY ORDERING";

	private static String UPD_CLUB_INFO = "update CLUBS set NAME=?, ADDRESS=?, ZIPCODE=?,"
			+ "CITY=?, STATE_ABBR=?, PHONE_NUMBER=?, HZRESTRICTION=?, CLUB_TYPE=?, CLUB_WEBSITE=?, TIME_ZONE=?, X_COORD=?, Y_COORD=? "
			+ "WHERE ID=? ";

	private static String GET_EMPLOYEES_FOR_SUBAMENITY_PK = "select USER_ID from AMENITY_EMPLOYEE where CLUB_SUBAMENITY_ID = ? ";

	static private String SEL_CLUBS_BY_USER_IDPK = "SELECT CLUBS.* FROM CLUBS, USERS, USER_ROLES where USERS.ID = ? and HOME_CLUB_ID = CLUBS.ID AND USER_ROLES.USER_ID = USERS.ID and ROLE != '"
			+ Privilege.superUser.name() + "'";

	static private String SEL_SUPER_USER_CLUBS_BY_USER_IDPK = "SELECT CLUBS.* FROM CLUBS where exists (select 1 from USERS, USER_ROLES where USERS.ID = ? AND USER_ROLES.USER_ID = USERS.ID and ROLE = '"
			+ Privilege.superUser.name() + "')";

	private static String COUNT_CLUB_PIN = "select count(*) from CLUBS where club_pin = ? ";

	private final static String GET_CLUB_BODY_TEXT = "select BODY_TEXT FROM CLUBS where ID = ? ";

	private final static String GET_CLUB_HEADLINE_TEXT = "select HEADER_TEXT FROM CLUBS where ID = ? ";

	private static final String GET_CLUBS_BY_STATE = "select C.*, RS.STATE_NAME FROM CLUBS C, REF_STATES RS where C.STATE_ABBR = RS.STATE_ABBR AND (UPPER(RS.STATE_NAME) = ? OR RS.STATE_ABBR=?)  ORDER BY ";
	private static final String GET_ALL_CLUBS = "select C.*, RS.STATE_NAME FROM CLUBS C, REF_STATES RS where C.STATE_ABBR = RS.STATE_ABBR ORDER BY ";

	private static final String UPD_CLUB_AMENITY_HEADER = "UPDATE CLUB_AMENITIES set HEADER = ? where CLUB_ID = ? and ID = ?";
	private static final String UPD_CLUB_SUBAMENITY = "UPDATE CLUB_SUB_AMENITIES set COLUMN=? where CLUB_ID = ? and ID = ?";
	private static final String UPD_CLUB_SUBAMENITY_BODY = UPD_CLUB_SUBAMENITY
			.replace("COLUMN", "BODY_TEXT");
	private static final String UPD_CLUB_SUBAMENITY_HEADER = UPD_CLUB_SUBAMENITY
			.replace("COLUMN", "HEADER_TEXT");
	private static final String UPD_CLUB_SUBAMENITY_SECONDARY_HEADER = UPD_CLUB_SUBAMENITY
			.replace("COLUMN", "SECONDARY_HEADER_TEXT");
	private static final String UPD_CLUB = "UPDATE CLUBS SET COLUMN = ? where ID = ?";
	private static final String UPD_CLUB_BODY = UPD_CLUB.replace("COLUMN",
			"BODY_TEXT");
	private static final String UPD_CLUB_HEADER = UPD_CLUB.replace("COLUMN",
			"HEADER_TEXT");

	private final static String GET_AMENITY_HEADER_TEXT = "select HEADER FROM CLUB_AMENITIES where CLUB_ID = ? and ID = ? ";

	private final static String GET_SUBAMENITY_BODY_TEXT = "select BODY_TEXT FROM CLUB_SUB_AMENITIES where CLUB_ID = ? and ID = ? ";
	private final static String GET_SUBAMENITY_HEADER_TEXT = "select HEADER_TEXT FROM CLUB_SUB_AMENITIES where CLUB_ID = ? and ID = ? ";

	private final static String GET_SUBAMENITY_SECONDARY_HEADER_TEXT = "select SECONDARY_HEADER_TEXT FROM CLUB_SUB_AMENITIES where CLUB_ID = ? and ID = ? ";
	private static String GET_SUBAMENITY_DEPT_NAMES = "SELECT CA.SUBAMENITYID FROM CLUB_SUB_AMENITIES CA WHERE CLUB_ID=? ORDER BY 1";

	public static final String GET_IMAGE_ID_BYCLUBIDPK = "select IMAGE_ID from CLUBS where ID=?";
	public static final String GET_IMAGE_ID_BYAMENITYIDPK = "select IMAGE_ID from CLUB_AMENITIES where ID=?";

	public static String UPD_CLUB_IMAGE_ID_LINK = "UPDATE CLUBS SET IMAGE_ID = ? WHERE ID = ?";
	public static String UPD_AMENITY_IMAGE_ID_LINK = "UPDATE CLUB_AMENITIES SET IMAGE_ID = ? WHERE ID = ?";

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	private final ClubDtoMapper clubDtoMapper = new ClubDtoMapper();

	private final AmenityMapper amenityMapper = new AmenityMapper();

	private final SubAmenityDtoMapper subAmenityMapper = new SubAmenityDtoMapper();

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
		switch (orderByClause) {
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
							GET_CLUB_IMAGE_ID, Integer.class, retVal.getId());
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
						GET_CLUB_IMAGE_ID, Integer.class, retVal.getId());
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
		if (CollectionUtils.isNotEmpty(amenities)) {
			for (final AmenityDto amenity : amenities) {
				final Integer imageIdPk = getJdbcTemplate().queryForObject(
						GET_AMENITY_IMAGE_ID, Integer.class, amenity.getId());
				final ImagePic image = getImage(imageIdPk);
				amenity.setImage(image);
			}
		}
		return amenities;
	}

	@Override
	public AmenityDto getAmenity(final Integer clubIdPk,
			final Integer amenityIdPk) throws NotFoundException {
		try {
			final AmenityDto amenity = getJdbcTemplate().queryForObject(
					SEL_AMENITY_BY_CLUBID_IDPK, amenityMapper, clubIdPk,
					amenityIdPk);
			// Populate Photos
			final Integer imageIdPk = getJdbcTemplate().queryForObject(
					GET_AMENITY_IMAGE_ID, Integer.class, amenity.getId());
			final ImagePic image = getImage(imageIdPk);
			amenity.setImage(image);
			return amenity;
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException();
		}
	}

	@Override
	public String getAmenityHeaderText(final int clubIdPk, final int amenityIdPk)
			throws NotFoundException {
		String retVal = "";
		try {
			retVal = getJdbcTemplate().queryForObject(GET_AMENITY_HEADER_TEXT,
					stringMapper, clubIdPk, amenityIdPk);
		} catch (final EmptyResultDataAccessException e) {
		}
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
	public Integer getClubAmenityIdPk(final int clubIdPk, final String amenityId)
			throws NotFoundException {
		try {
			final Integer amenityIdPk = getJdbcTemplate().queryForObject(
					SEL_AMENITY_PK_BY_AMENITYID, Integer.class, clubIdPk, amenityId);
			return amenityIdPk;
		} catch (final IncorrectResultSizeDataAccessException e) {
			throw new NotFoundException("Club Amenity " + amenityId
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

	private Integer getClubIdPkByKeyCode(final String clubKeycode)
			throws NotFoundException {
		try {
			final Integer clubIdPk = getJdbcTemplate().queryForObject(
					GET_CLUBIDPK_BYKEYCODE, Integer.class, clubKeycode);
			return clubIdPk;
		} catch (final IncorrectResultSizeDataAccessException e) {
			throw new NotFoundException("Club Keycode " + clubKeycode
					+ " not found!");
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
	public Integer getClubSubAmenityIdPk(final int clubIdPk,
			final String clubSubAmenityId) throws NotFoundException {
		try {
			final Integer subAmenityIdPk = getJdbcTemplate().queryForObject(
					SEL_SUBAMENITY_PK_BY_SUBAMENITYID, Integer.class, clubIdPk,
					clubSubAmenityId);
			return subAmenityIdPk;
		} catch (final IncorrectResultSizeDataAccessException e) {
			throw new NotFoundException("Club Sub Amenity " + clubSubAmenityId
					+ " not found!");
		}
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

	@Override
	public List<SubAmenityDto> getSubAmenities(final Integer clubIdPk)
			throws NotFoundException {
		final List<SubAmenityDto> subAmenities = getJdbcTemplate().query(
				SEL_SUBAMENITIES_BY_CLUBID + " ORDER BY AT.AMENITYID, AT.DESCRIPTION", subAmenityMapper,
				clubIdPk);
		// Populate Photos
		if (CollectionUtils.isNotEmpty(subAmenities)) {
			for (final SubAmenityDto subAmenityDto : subAmenities) {
				final Integer imageIdPk = getJdbcTemplate().queryForObject(
						GET_SUBAMENITY_IMAGE_ID, Integer.class,
						subAmenityDto.getId());
				final ImagePic image = getImage(imageIdPk);
				subAmenityDto.setImage(image);
			}
		}
		return subAmenities;
	}

	@Override
	public List<SubAmenityDto> getSubAmenities(final Integer clubIdPk,
			final String amenityId) throws NotFoundException {
		final List<SubAmenityDto> amenities = getJdbcTemplate().query(
				SEL_SUBAMENITIES_BY_CLUBID_AMENITYID, subAmenityMapper,
				clubIdPk, amenityId);
		// Populate Photos
		if (CollectionUtils.isNotEmpty(amenities)) {
			for (final SubAmenityDto subAmenityDto : amenities) {
				final Integer imageIdPk = getJdbcTemplate().queryForObject(
						GET_SUBAMENITY_IMAGE_ID, Integer.class,
						subAmenityDto.getId());
				final ImagePic image = getImage(imageIdPk);
				subAmenityDto.setImage(image);
			}
		}
		return amenities;
	}

	@Override
	public SubAmenityDto getSubAmenity(final Integer subAmenityIdPk)
			throws NotFoundException {
		try {
			final SubAmenityDto subAmenity = getJdbcTemplate().queryForObject(
					SEL_SUBAMENITY_BY_PK, subAmenityMapper, subAmenityIdPk);
			return subAmenity;
		} catch (final IncorrectResultSizeDataAccessException e) {
			throw new NotFoundException("Club SUB Amenity " + subAmenityIdPk
					+ " not found!");
		}
	}

	@Override
	public String getSubAmenityBodyText(final int clubIdPk,
			final int subAmenityIdPk) {
		String retVal = "";
		try {
			retVal = getJdbcTemplate().queryForObject(GET_SUBAMENITY_BODY_TEXT,
					stringMapper, clubIdPk, subAmenityIdPk);
		} catch (final EmptyResultDataAccessException e) {
		}
		return retVal;
	}

	@Override
	public List<String> getSubAmenityDeptName(final String clubId)
			throws NotFoundException {
		try {
			final Integer clubIdPk = getClubIdPk(clubId);
			final List<String> deptName = getJdbcTemplate().query(
					GET_SUBAMENITY_DEPT_NAMES, stringMapper, clubIdPk);
			return deptName;
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Club : " + clubId + " not found!");
		}
	}

	@Override
	public List<Integer> getSubAmenityEmployees(final Integer clubSubAmenityId)
			throws NotFoundException {
		final List<Integer> retVal = getJdbcTemplate().queryForList(
				GET_EMPLOYEES_FOR_SUBAMENITY_PK, Integer.class,
				clubSubAmenityId);
		return retVal;
	}

	@Override
	public String getSubAmenityHeaderText(final int clubIdPk,
			final int subAmenityIdPk) {
		String retVal = "";
		try {
			retVal = getJdbcTemplate().queryForObject(
					GET_SUBAMENITY_HEADER_TEXT, stringMapper, clubIdPk,
					subAmenityIdPk);
		} catch (final EmptyResultDataAccessException e) {
		}
		return retVal;
	}

	@Override
	public String getSubAmenitySecondayHeaderText(final int clubIdPk,
			final int subAmenityIdPk) {
		String retVal = "";
		try {
			retVal = getJdbcTemplate().queryForObject(
					GET_SUBAMENITY_SECONDARY_HEADER_TEXT, stringMapper,
					clubIdPk, subAmenityIdPk);
		} catch (final EmptyResultDataAccessException e) {
		}
		return retVal;
	}

	private void populateClubDetails(final List<ClubDto> clubs) {
		if (CollectionUtils.isNotEmpty(clubs)) {
			for (final ClubDto clubDto : clubs) {
				try {
					final Integer imageIdPk = getJdbcTemplate().queryForObject(
							GET_CLUB_IMAGE_ID, Integer.class, clubDto.getId());
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
				club.getTimeZone().getDbSetting(), club.getXcoord(),
				club.getYcoord(), club.getId());
	}

	@Override
	public void updateAmenityHeaderText(final Integer clubIdPk,
			final Integer amenityIdPk, final String headerText)
					throws NotFoundException {
		getJdbcTemplate().update(UPD_CLUB_AMENITY_HEADER, headerText, clubIdPk,
				amenityIdPk);
	}

	@Override
	public void updateAmenityPhoto(final int clubIdPk, final Integer amenityIdPk,
			final String pictureBase64) {
		final byte[] picture = Base64.decodeBase64(pictureBase64);
		try {
			final Integer imageIdPk = getJdbcTemplate().queryForObject(
					GET_IMAGE_ID_BYAMENITYIDPK, Integer.class, amenityIdPk);
			final Integer updateImageIdPk = updateImage(imageIdPk, picture);
			if (updateImageIdPk != imageIdPk) {
				// Update user image_id link
				getJdbcTemplate().update(UPD_AMENITY_IMAGE_ID_LINK,
						updateImageIdPk, amenityIdPk);
			}
		} catch (final EmptyResultDataAccessException e) {
			// Not found!, so insert one in.
			final ImagePic pic = new ImagePic(pictureBase64);
			final Integer idImagePk = persistImage(pic);
			System.out
			.println("updateClubPhoto: Not found!, so insert one in. idImagePk="
					+ idImagePk);
			getJdbcTemplate().update(UPD_AMENITY_IMAGE_ID_LINK, idImagePk,
					amenityIdPk);
		}
	}

	@Override
	public void updateBodyText(final Integer clubIdPk, final String bodyText)
			throws NotFoundException {
		getJdbcTemplate().update(UPD_CLUB_BODY, bodyText, clubIdPk);
	}

	@Override
	public void updateClubPhoto(final int clubIdPk, final String pictureBase64) {
		final byte[] picture = Base64.decodeBase64(pictureBase64);
		try {
			final Integer imageIdPk = getJdbcTemplate().queryForObject(
					GET_IMAGE_ID_BYCLUBIDPK, Integer.class, clubIdPk);
			final Integer updateImageIdPk = updateImage(imageIdPk, picture);
			if (updateImageIdPk != imageIdPk) {
				// Update user image_id link
				getJdbcTemplate().update(UPD_CLUB_IMAGE_ID_LINK,
						updateImageIdPk, clubIdPk);
			}
		} catch (final EmptyResultDataAccessException e) {
			// Not found!, so insert one in.
			final ImagePic pic = new ImagePic(pictureBase64);
			final Integer idImagePk = persistImage(pic);
			System.out
			.println("updateClubPhoto: Not found!, so insert one in. idImagePk="
					+ idImagePk);
			getJdbcTemplate().update(UPD_CLUB_IMAGE_ID_LINK, idImagePk,
					clubIdPk);
		}
	}

	@Override
	public void updateHeaderText(final Integer clubIdPk, final String headerText)
			throws NotFoundException {
		getJdbcTemplate().update(UPD_CLUB_HEADER, headerText, clubIdPk);
	}

	@Override
	public void updateSubAmenityBody(final Integer clubIdPk,
			final Integer subAmenityIdPk, final String bodyText)
					throws NotFoundException {
		getJdbcTemplate().update(UPD_CLUB_SUBAMENITY_BODY, bodyText, clubIdPk,
				subAmenityIdPk);
	}

	@Override
	public void updateSubAmenityHeaderText(final Integer clubIdPk,
			final Integer subAmenityIdPk, final String headerText)
					throws NotFoundException {
		getJdbcTemplate().update(UPD_CLUB_SUBAMENITY_HEADER, headerText,
				clubIdPk, subAmenityIdPk);
	}

	@Override
	public void updateSubAmenitySecondaryHeaderText(final Integer clubIdPk,
			final Integer subAmenityIdPk, final String headerText)
					throws NotFoundException {
		getJdbcTemplate().update(UPD_CLUB_SUBAMENITY_SECONDARY_HEADER,
				headerText, clubIdPk, subAmenityIdPk);
	}

	@Override
	public boolean verifyClubPin(final String clubPin) {
		final Integer counter = getJdbcTemplate().queryForObject(
				COUNT_CLUB_PIN, Integer.class, clubPin);
		return counter > 0;
	}

}
