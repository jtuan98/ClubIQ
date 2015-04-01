package com.avatar.dao.impl.jdbc;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.avatar.dao.AccountDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.impl.jdbc.mapper.AccountDtoMapper;
import com.avatar.dao.impl.jdbc.mapper.ActivationTokenMapper;
import com.avatar.dao.impl.jdbc.mapper.RolesMapper;
import com.avatar.dto.ImagePic;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.account.MobileAccountDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.AccountStatus;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.NotFoundException;

@Repository
public class AccountDaoJdbc extends BaseJdbcDao implements AccountDao {

	private static String INS_ACCOUNT = "INSERT INTO USERS (ID, "
			+ "USERID, MOBILE_IND, MOBILE_NUMBER, HOME_CLUB_ID, "
			+ "EMAIL, PASSWORD, REALNAME, ADDRESS, IMAGE_ID, STATUS, "
			+ "CREATE_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static String INS_TOKEN = "INSERT INTO USER_ACTIVATION_TOKEN (ID, "
			+ "USER_ID, TOKEN, MOBILE_PIN_FLAG, VALID_TILL, "
			+ "CREATE_DATE) VALUES (?, ?, ?, ?, ?, ?)";

	private static String UPD_TOKEN = "UPDATE USER_ACTIVATION_TOKEN SET TOKEN=?, VALID_TILL=?, CREATE_DATE=NOW() "
			+ " WHERE ID = ?";

	private static String INS_DEVICES = "INSERT INTO USER_DEVICES (ID, "
			+ "USER_ID, DEVICE_ID, TANGERINE_HANDSET_ID, "
			+ "CREATE_DATE) VALUES (?, ?, ?, ?, ?)";

	private static String INS_ROLES = "INSERT INTO USER_ROLES (ID, "
			+ "USER_ID, ROLE, CREATE_DATE) VALUES (?, ?, ?, ?)";

	private static String UPD_ACCOUNT_ACTIVATION = "update USERS set STATUS='"
			+ AccountStatus.Activated.name()
			+ "' WHERE ID = (SELECT USER_ID FROM USER_ACTIVATION_TOKEN WHERE TOKEN=? AND USER_ID = USERS.ID) AND USERID=? "
			+ "AND STATUS in ('" + AccountStatus.TokenSent.name() + "', '"
			+ AccountStatus.Activated.name() + "')";

	private static String UPD_ACCOUNT_STATUS_NOTIFIED = "update USERS set STATUS='"
			+ AccountStatus.TokenSent.name()
			+ "' WHERE USERID = ? AND STATUS='"
			+ AccountStatus.New.name()
			+ "'";

	private static String UPD_USER_DEVICEID = "update USER_DEVICES set DEVICE_ID=? "
			+ "WHERE USER_ID = (SELECT ID FROM USERS WHERE USERID=?)";

	private static String UPD_USER_TANGERINE_HANDSET_ID = "update USER_DEVICES set TANGERINE_HANDSET_ID=? "
			+ "WHERE USER_ID = (SELECT ID FROM USERS WHERE USERID=?) AND DEVICE_ID = ?";


	private static final String GET_USER_ID_PK = "select ID from USERS where USERID=?";

	private static final String GET_IMAGE_ID_BYUSERID = "select IMAGE_ID from USERS where USERID=?";
	private static final String GET_IMAGE_ID = "select IMAGE_ID from USERS where ID=?";
	private static final String GET_HOME_CLUB_ID = "select HOME_CLUB_ID from USERS where ID=?";

	private static final String UPD_USER_EMAIL = "UPDATE USERS set EMAIL=? where USERID=?";

	private static final String UPD_USER_FULLNAME = "UPDATE USERS set REALNAME=? WHERE USERID=?";

	private static final String SEL_USER = "select * from USERS where USERID = ? ";

	private static final String SEL_USER_BY_PK = "select * from USERS where ID = ? ";

	private final AccountDtoMapper accountDtoMapper = new AccountDtoMapper();

	private static final String SEL_ROLES_BY_USER_ID = "SELECT ROLE from USER_ROLES where USER_ID = ? ";

	private final RolesMapper rolesMapper = new RolesMapper();

	private static final String SEL_DEVICE_TANGERINE_HANDSET_ID_BY_USER_ID = "SELECT DEVICE_ID, TANGERINE_HANDSET_ID FROM USER_DEVICES WHERE USER_ID = ? ";

	// SELECT TOKEN FROM USER_DEVICES UD, USER_ACTIVATION_TOKEN UAT, USERS U
	// WHERE VALID_TILL > NOW() AND UAT.USER_ID = U.ID AND UD.USER_ID = U.ID AND
	// USERID = '1234' AND DEVICE_ID = 'deviceId1234'
	private static final String SEL_USERIDPK_BY_USER_ID_DEVICE_ID_TOKEN = "SELECT U.ID FROM USER_DEVICES UD, USER_ACTIVATION_TOKEN UAT, USERS U WHERE VALID_TILL > NOW() AND UAT.USER_ID = U.ID AND UD.USER_ID = U.ID AND USERID = ? AND DEVICE_ID = ? AND UAT.TOKEN=?";
	private static final String SEL_USERIDPK_BY_TOKEN = "SELECT USER_ID FROM USER_ACTIVATION_TOKEN UAT WHERE VALID_TILL > NOW() AND UAT.TOKEN=?";
	private static final String SEL_TOKEN_BY_USERIDPK = "SELECT * FROM USER_ACTIVATION_TOKEN UAT WHERE UAT.USER_ID=?";

	private static String UPD_USER_IMAGE_ID_LINK = "UPDATE USERS SET IMAGE_ID = ? WHERE USERID = ?";

	private static final String GET_USER_ID_BY_DEVICE_ID = "SELECT USERID FROM USERS U, USER_DEVICES UD WHERE USER_ID = U.ID and DEVICE_ID = ? ";

	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDao;

	private final ActivationTokenMapper activationTokenMapper = new ActivationTokenMapper();

	@Override
	public void activate(final String userId, final String activationToken)
			throws NotFoundException {
		final int updated = getJdbcTemplate().update(UPD_ACCOUNT_ACTIVATION,
				activationToken, userId);
		if (updated == 0) {
			throw new NotFoundException();
		}
	}

	@Override
	public void deactivate(final String userId) throws NotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public AccountDto fetch(final Integer userIdPk) throws NotFoundException {
		return fetch(SEL_USER_BY_PK, userIdPk);
	}

	@Override
	public AccountDto fetch(final String userId) throws NotFoundException {
		return fetch(SEL_USER, userId);
	}

	private AccountDto fetch(final String sql, final Object paramUserId)
			throws NotFoundException {
		AccountDto account = null;

		if (paramUserId != null) {
			try {
				account = getJdbcTemplate().queryForObject(sql,
						accountDtoMapper, paramUserId);
				populateAccountInfo(account);
				final List<Privilege> roles = getJdbcTemplate().query(
						SEL_ROLES_BY_USER_ID, rolesMapper, account.getId());
				account.setPriviledges(new HashSet<Privilege>(roles));
				if (account instanceof MobileAccountDto) {
					final Map<String, Object> result = getJdbcTemplate()
							.queryForMap(
									SEL_DEVICE_TANGERINE_HANDSET_ID_BY_USER_ID,
									account.getId());
					final MobileAccountDto mobileAccount = (MobileAccountDto) account;
					mobileAccount.setDeviceId((String) result.get("DEVICE_ID"));
					mobileAccount.setTangerineHandsetId((String) result
							.get("TANGERINE_HANDSET_ID"));
				}
				if ((account.getPicture() != null) && (account.getPicture().getId() != null)) {
					System.out.println("Getting the picture!!!");
					final ImagePic image = getImage(account.getPicture().getId());
					account.setPicture(image);
				}
			} catch (final EmptyResultDataAccessException e) {
				throw new NotFoundException();
			}
		}
		return account;
	}

	@Override
	public AccountDto fetchByToken(final String token, final String userId,
			final String deviceId) throws NotFoundException {
		Integer userIdPk = null;
		try {
			if (StringUtils.isNotEmpty(userId)) {
				// Mobile
				userIdPk = getJdbcTemplate().queryForObject(
						SEL_USERIDPK_BY_USER_ID_DEVICE_ID_TOKEN, Integer.class,
						userId, deviceId, token);
			} else {
				userIdPk = getJdbcTemplate().queryForObject(
						SEL_USERIDPK_BY_TOKEN, Integer.class, token);
			}
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException();
		}
		return fetch(userIdPk);
	}

	@Override
	public String getUserIdByDeviceId(final String deviceId)
			throws NotFoundException {
		final String userId = getJdbcTemplate().queryForObject(
				GET_USER_ID_BY_DEVICE_ID, String.class, deviceId);
		return userId;
	}

	@Override
	public int getUserIdPkByUserId(final String userId)
			throws NotFoundException {
		try {
			final Integer userIdPk = getJdbcTemplate().queryForObject(
					GET_USER_ID_PK, Integer.class, userId);
			return userIdPk;
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Account " + userId + " not found!");
		}
	}

	@Override
	public void markStatusAsNotified(final String userId)
			throws NotFoundException {
		final int updated = getJdbcTemplate().update(
				UPD_ACCOUNT_STATUS_NOTIFIED, userId);
		if (updated == 0) {
			throw new NotFoundException();
		}
	}

	@Override
	public void newAccount(final AccountDto account,
			final ActivationToken activationToken) throws NotFoundException {
		final Date entry = new Date();
		final int id = sequencer.nextVal("ID_SEQ");
		account.setId(id);
		final int idToken = sequencer.nextVal("ID_SEQ");
		String mobileNumber = "";
		final boolean mobile = (account instanceof MobileAccountDto);
		final Integer idImage = persistImage(account.getPicture());
		Validate.notEmpty(this.getClass().getName() + " USER_ID",
				account.getUserId());
		// Get mobile stuff
		if (mobile) {
			final MobileAccountDto accountMobile = (MobileAccountDto) account;
			mobileNumber = accountMobile.getMobileNumber();
			Validate.notEmpty(this.getClass().getName() + " MOBILE_NUMBER",
					mobileNumber);
			Validate.notEmpty(this.getClass().getName() + " DEVICE_ID",
					accountMobile.getDeviceId());
		}
		// Add in the ClubDao
		Integer clubIdPk = null;
		if ((account.getHomeClub() != null)
				&& StringUtils.isNotEmpty(account.getHomeClub().getClubId())) {
			clubIdPk = clubDao.getClubIdPk(account.getHomeClub().getClubId());
		}

		getJdbcTemplate().update(INS_ACCOUNT,
		// ID
				account.getId(),
				// USERID
				account.getUserId(),
				// MOBILE_IND
				(mobile ? "Y" : "N"),
				// MOBILE_NUMBER
				mobileNumber,
				// HOME_CLUB_ID
				clubIdPk,
				// EMAIL
				account.getEmail(),
				// PASSWORD,
				account.getPassword(),
				// REALNAME,"
				account.getName(),
				// "ADDRESS
				account.getAddress(),
				// IMAGE_ID
				idImage,
				// STATUS
				AccountStatus.New.name(),
				// "CREATE_DATE";
				entry);
		if (clubIdPk != null) {
			clubDao.addUserToClub(clubIdPk, account.getId());
		}
		getJdbcTemplate().update(INS_TOKEN,
		// ID
				idToken,
				// "USER_ID,
				account.getId(),
				// TOKEN,
				activationToken.getToken(),
				//MOBILE_PIN_FLAG,
				(mobile ? "Y" : "N"),
				// VALID_TILL, "
				activationToken.getExpirationDate(),
				// CREATE_DATE
				entry);

		// Roles
		if (CollectionUtils.isNotEmpty(account.getPriviledges())) {
			for (final Privilege role : account.getPriviledges()) {
				final int idRole = sequencer.nextVal("ID_SEQ");
				getJdbcTemplate().update(INS_ROLES,
				// ID
						idRole,
						// "USER_ID,
						account.getId(),
						// ROLE
						role.name(),
						// CREATE_DATE
						entry);
			}
		}

		if (mobile) {
			final MobileAccountDto accountMobile = (MobileAccountDto) account;
			final int idDevice = sequencer.nextVal("ID_SEQ");
			getJdbcTemplate().update(INS_DEVICES,
			// IDcom.avatar.dao.impl.jdbc.AccountDaoJdbc.updateUserTangerineHandSetId
					idDevice,
					// USER_ID,
					account.getId(),
					// DEVICE_ID,
					accountMobile.getDeviceId(),
					// TANGERINE_HANDSET_ID
					accountMobile.getTangerineHandsetId(),
					// CREATE_DATEcom.avatar.dao.impl.jdbc.AccountDaoJdbc.updateUserTangerineHandSetId
					entry);
		}

	}

	@Override
	public void populateAccountInfo(final AccountDto account) {

		try {
			final Integer imageIdPk = getJdbcTemplate().queryForObject(
					GET_IMAGE_ID, Integer.class, account.getId());
			final ImagePic image = getImage(imageIdPk);
			account.setPicture(image);
		} catch (final EmptyResultDataAccessException e1) {
		}

		// Fetch homeClubID
		try {
			final Integer homeClubIdPk = getJdbcTemplate().queryForObject(
					GET_HOME_CLUB_ID, Integer.class, account.getId());

			final ClubDto homeClub = clubDao.get(homeClubIdPk);
			account.setHomeClub(homeClub);
		} catch (final NotFoundException e) {
			// NP.
		}

		if (AccountStatus.New.equals(account.getStatus()) || AccountStatus.TokenSent.equals(account.getStatus())) {
			// Get the account token
			try {
				final ActivationToken token = getJdbcTemplate().queryForObject(
						SEL_TOKEN_BY_USERIDPK, activationTokenMapper,
						account.getId());
				account.setToken(token);
			} catch (final EmptyResultDataAccessException e) {
				// NP.
			}
		}
	}

	@Override
	@Resource(name = "avatarDataSource")
	public void setDataSource(final DataSource ds) {
		initTemplate(ds);
	}

	@Override
	public void updateAccountInfoEmail(final String userId, final String email)
			throws NotFoundException {
		final int updated = getJdbcTemplate().update(UPD_USER_EMAIL, email,
				userId);
		if (updated == 0) {
			throw new NotFoundException();
		}
	}

	@Override
	public void updateAccountInfoName(final String userId, final String fullName)
			throws NotFoundException {
		final int updated = getJdbcTemplate().update(UPD_USER_FULLNAME,
				fullName, userId);
		if (updated == 0) {
			throw new NotFoundException();
		}
	}

	@Override
	public void updateAccountInfoPicture(final String userId,
			final String pictureBase64) throws NotFoundException {
		final byte[] picture = Base64.decodeBase64(pictureBase64);
		try {
			final Integer imageIdPk = getJdbcTemplate().queryForObject(
					GET_IMAGE_ID_BYUSERID, Integer.class, userId);
			final Integer updateImageIdPk = updateImage(imageIdPk, picture);
			if (updateImageIdPk != imageIdPk) {
				// Update user image_id link
				getJdbcTemplate().update(UPD_USER_IMAGE_ID_LINK,
						updateImageIdPk, userId);
			}
		} catch (final EmptyResultDataAccessException e) {
			// Not found!, so insert one in.
			final ImagePic pic = new ImagePic(pictureBase64);
			final Integer idImagePk = persistImage(pic);
			System.out.println("updateAccountInfoPicture: Not found!, so insert one in. idImagePk="+ idImagePk);
			getJdbcTemplate().update(UPD_USER_IMAGE_ID_LINK, idImagePk, userId);
		}
	}

	@Override
	public void updateNewToken(final ActivationToken token)
			throws NotFoundException {
		getJdbcTemplate().update(UPD_TOKEN,
				token.getToken(), token.getExpirationDate(), token.getId());
	}

	@Override
	public void updateUserDeviceId(final String userId, final String deviceId)
			throws NotFoundException {
		final int updated = getJdbcTemplate().update(UPD_USER_DEVICEID,
				deviceId, userId);
		if (updated == 0) {
			throw new NotFoundException();
		}
	}

	@Override
	public void updateUserTangerineHandSetId(final String userId,
			final String deviceId, final String tangerineHandSetId)
			throws NotFoundException {
		final int updated = getJdbcTemplate().update(
				UPD_USER_TANGERINE_HANDSET_ID, tangerineHandSetId, userId,
				deviceId);
		if (updated == 0) {
			throw new NotFoundException("Mobile " + userId + "/" + deviceId
					+ " not found!");
		}
	}
}
