package com.avatar.dao.impl.jdbc;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.avatar.dao.AccountDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.TruncateDao;
import com.avatar.dao.impl.jdbc.mapper.AccountDtoMapper;
import com.avatar.dao.impl.jdbc.mapper.AccountNotesDtoMapper;
import com.avatar.dao.impl.jdbc.mapper.ActivationTokenMapper;
import com.avatar.dao.impl.jdbc.mapper.RolesMapper;
import com.avatar.dao.impl.jdbc.sql.AccountDaoSql;
import com.avatar.dto.ImagePic;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.AccountNotes;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.account.MemberAccountDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.AccountStatus;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.InvalidPasswordException;
import com.avatar.exception.NotFoundException;

@Repository
public class AccountDaoJdbc extends BaseJdbcDao implements AccountDao,
TruncateDao {

	private final AccountDtoMapper accountDtoMapper = new AccountDtoMapper();
	private final RolesMapper rolesMapper = new RolesMapper();

	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDao;

	private final ActivationTokenMapper activationTokenMapper = new ActivationTokenMapper();

	private final AccountNotesDtoMapper accountNotesDtoMapper = new AccountNotesDtoMapper();

	protected final DateTimeFormatter yyyyMMdd_hh24missDtf = DateTimeFormat
			.forPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public void activate(final String userId, final String activationToken,
			final Date activated) throws NotFoundException {
		final int updated = getJdbcTemplate().update(
				AccountDaoSql.UPD_ACCOUNT_ACTIVATION, activated,
				activationToken, userId);
		if (updated == 0) {
			throw new NotFoundException();
		}
	}

	private void addLinkSubAmenityUserId(final Integer subAmenityIdPk,
			final Integer userIdPk) throws NotFoundException {
		try {
			final int updated = getJdbcTemplate().update(
					AccountDaoSql.UPD_SUBAMENITY_EMPLOYEE, subAmenityIdPk,
					userIdPk);
			if (updated == 0) {
				final int idSubAmenityEmployee = sequencer.nextVal("ID_SEQ");
				getJdbcTemplate().update(AccountDaoSql.INS_SUBAMENITY_EMPLOYEE,
						idSubAmenityEmployee, subAmenityIdPk, userIdPk);
			}
		} catch (final DataIntegrityViolationException e) {
			throw new NotFoundException("user id or subamenityId not found.");
		}
	}

	@Override
	public Number addNote(final Integer userIdPk, final String noteText,
			final DateTime noteDateTime) throws NotFoundException {
		System.out.println("adding note date: "
				+ yyyyMMdd_hh24missDtf.print(noteDateTime.getMillis()));
		final int idNoteAdded = sequencer.nextVal("ID_SEQ");
		try {
			getJdbcTemplate().update(AccountDaoSql.INS_NOTES, idNoteAdded,
					userIdPk, noteText,
					yyyyMMdd_hh24missDtf.print(noteDateTime.getMillis()));
		} catch (final DataIntegrityViolationException e) {
			throw new NotFoundException("User ID not found");
		}
		return idNoteAdded;
	}

	@Override
	public void addSubAmenityToUser(final Integer userIdPk,
			final Integer clubSubAmenityIdPk) throws InvalidParameterException,
			NotFoundException {
		if (userIdPk == null || clubSubAmenityIdPk == null) {
			throw new InvalidParameterException("Keys cannot be null");
		}
		addLinkSubAmenityUserId(clubSubAmenityIdPk, userIdPk);
	}

	@Override
	public void deactivate(final String userId, final Date deactivateDate)
			throws NotFoundException {
		deactivate(userId, deactivateDate, false);
	}

	@Override
	public void deactivate(final String userId, final Date deactivateDate, final boolean cancelNoteFlag)
			throws NotFoundException {
		final int userIdPk = getUserIdPkByUserId(userId);
		getJdbcTemplate().update(AccountDaoSql.UPD_ACCOUNT_DEACTIVATION,
				yyyyMMdd_hh24missDtf.print(deactivateDate.getTime()), userIdPk);
		if (cancelNoteFlag) {
			addNote(userIdPk, "Member cancel subscription", new DateTime(
					deactivateDate));
		}
	}

	@Override
	public AccountDto fetch(final Integer userIdPk) throws NotFoundException,
	InvalidParameterException {
		return fetch(AccountDaoSql.SEL_USER_BY_PK, userIdPk);
	}

	@Override
	public AccountDto fetch(final String userId) throws NotFoundException,
	InvalidParameterException {
		return fetch(AccountDaoSql.SEL_USER, userId);
	}

	private AccountDto fetch(final String sql, final Object paramUserId)
			throws NotFoundException, InvalidParameterException {
		AccountDto account = null;

		if (paramUserId != null) {
			try {
				account = getJdbcTemplate().queryForObject(sql,
						accountDtoMapper, paramUserId);
				if (account == null) {
					throw new NotFoundException(paramUserId + " not found!");
				}
				populateAccountInfo(account, true);
				populateOtherAccountInfo(account);
			} catch (final EmptyResultDataAccessException e) {
				throw new NotFoundException();
			}
		} else {
			throw new InvalidParameterException("Param cannot be null");
		}
		return account;
	}

	@Override
	public AccountDto fetchByToken(final String token, final String userId,
			final String deviceId) throws NotFoundException,
			InvalidParameterException {
		Integer userIdPk = null;
		try {
			if (StringUtils.isNotEmpty(userId)) {
				// Mobile
				userIdPk = getJdbcTemplate().queryForObject(
						AccountDaoSql.SEL_USERIDPK_BY_USER_ID_DEVICE_ID_TOKEN,
						Integer.class, userId, deviceId, token);
			} else {
				userIdPk = getJdbcTemplate().queryForObject(
						AccountDaoSql.SEL_USERIDPK_BY_TOKEN, Integer.class,
						token);
			}
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException();
		}
		return fetch(userIdPk);
	}

	private List<AccountNotes> fetchNoteHistory(final Integer userIdPk) {
		return getJdbcTemplate().query(
				AccountDaoSql.SEL_NOTESHISTORY_BY_USER_ID,
				accountNotesDtoMapper, userIdPk);
	}

	@Override
	public List<Privilege> fetchRoles(final Integer userIdPk)
			throws NotFoundException {
		return getJdbcTemplate().query(AccountDaoSql.SEL_ROLES_BY_USER_ID,
				rolesMapper, userIdPk);
	}

	@Override
	public List<Privilege> fetchRoles(final String userId)
			throws NotFoundException {
		return fetchRoles(getUserIdPkByUserId(userId));
	}

	private String getLinkPhoneNumber(final Integer id) {
		String linkNumber = null;

		try {
			linkNumber = getJdbcTemplate().queryForObject(
					AccountDaoSql.SEL_LINKPHONE_BY_USERID, String.class, id);
		} catch (final EmptyResultDataAccessException e) {
		}
		return linkNumber;
	}

	@Override
	public List<AccountDto> getMembers(final int clubIdPk)
			throws NotFoundException, InvalidParameterException {
		return getMembers(clubIdPk, null, false);
	}

	@Override
	public List<AccountDto> getMembers(final int clubIdPk,
			final DateTime fromDate, final boolean populatePicture)
					throws NotFoundException, InvalidParameterException {
		List<AccountDto> accounts = null;
		if (fromDate != null) {
			if (clubIdPk > 0) {
				accounts = getJdbcTemplate().query(
						AccountDaoSql.SEL_USERS_BY_CLUBID_BY_DATE,
						accountDtoMapper, clubIdPk,
						yyyyMMdd_hh24missDtf.print(fromDate));
			} else {
				accounts = getJdbcTemplate().query(
						AccountDaoSql.SEL_USERS_BY_DATE, accountDtoMapper,
						yyyyMMdd_hh24missDtf.print(fromDate));
			}
		} else {
			if (clubIdPk > 0) {
				accounts = getJdbcTemplate().query(
						AccountDaoSql.SEL_USERS_BY_CLUBID, accountDtoMapper,
						clubIdPk);
			} else {
				accounts = getJdbcTemplate().query(AccountDaoSql.SEL_USERS,
						accountDtoMapper);
			}
		}
		if (accounts == null) {
			throw new NotFoundException(clubIdPk + " not found!");
		}
		for (final AccountDto account : accounts) {
			populateAccountInfo(account, populatePicture);
			populateOtherAccountInfo(account);
		}
		return accounts;
	}

	@Override
	public AccountStatus getPreviousStatus(final int userIdPk) {
		final String status = getJdbcTemplate().queryForObject(
				AccountDaoSql.GET_PREV_STATUS_BY_IDPK, String.class, userIdPk);
		return StringUtils.isNotEmpty(status) ? AccountStatus.valueOf(status)
				: null;
	}

	@Override
	public AccountStatus getStatus(final int userIdPk) {
		final String status = getJdbcTemplate().queryForObject(
				AccountDaoSql.GET_STATUS_BY_IDPK, String.class, userIdPk);
		return AccountStatus.valueOf(status);
	}

	@Override
	public String getUserIdByDeviceId(final String deviceId)
			throws NotFoundException {
		final String userId = getJdbcTemplate().queryForObject(
				AccountDaoSql.GET_USER_ID_BY_DEVICE_ID, String.class, deviceId);
		return userId;
	}

	@Override
	public int getUserIdPkByUserId(final String userId)
			throws NotFoundException {
		try {
			final Integer userIdPk = getJdbcTemplate().queryForObject(
					AccountDaoSql.GET_USER_ID_PK, Integer.class, userId);
			return userIdPk;
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Account " + userId + " not found!");
		}
	}

	@Override
	public void linkNumbers(final String userId, final String linkNumber,
			final Date currentDate) throws NotFoundException {
		final int linkNumberIdPk = getUserIdPkByUserId(linkNumber);
		final int userIdPk = getUserIdPkByUserId(userId);
		getJdbcTemplate().update(AccountDaoSql.UPD_ACCOUNT_LINK,
				linkNumberIdPk, userIdPk);
	}

	@Override
	public void markStatusAsNotified(final String userId)
			throws NotFoundException {
		final int updated = getJdbcTemplate().update(
				AccountDaoSql.UPD_ACCOUNT_STATUS_NOTIFIED, userId);
		if (updated == 0) {
			throw new NotFoundException();
		}
	}

	@Override
	public void newAccount(final AccountDto account,
			final ActivationToken activationToken) throws NotFoundException,
			InvalidParameterException {
		final boolean mobile = (account instanceof MemberAccountDto);
		verify(account.getUserId(), this.getClass().getName() + " USER_ID");
		String mobileNumber = "";
		// Get mobile stuff
		if (mobile) {
			final MemberAccountDto accountMobile = (MemberAccountDto) account;
			mobileNumber = accountMobile.getMobileNumber();
			verify(mobileNumber, this.getClass().getName() + " MOBILE_NUMBER");
			verify(accountMobile.getDeviceId(), this.getClass().getName()
					+ " DEVICE_ID");

		}
		final int id = sequencer.nextVal("ID_SEQ");
		account.setId(id);
		final int idToken = sequencer.nextVal("ID_SEQ");
		final Integer idImage = persistImage(account.getPicture());
		// Add in the ClubDao
		Integer clubIdPk = null;
		if ((account.getHomeClub() != null)
				&& StringUtils.isNotEmpty(account.getHomeClub().getClubId())) {
			clubIdPk = clubDao.getClubIdPk(account.getHomeClub().getClubId());
		}

		getJdbcTemplate().update(AccountDaoSql.INS_ACCOUNT,
				// ID
				id,
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
				AccountStatus.New.name());
		if (clubIdPk != null) {
			clubDao.addUserToClub(clubIdPk, account.getId());
		}
		if (!mobile) {
			final EmployeeAccountDto employeeAccount = (EmployeeAccountDto) account;
			if (employeeAccount.getSubAmenity() != null) {
				addLinkSubAmenityUserId(
						employeeAccount.getSubAmenity().getId(), id);
			}
		}
		getJdbcTemplate().update(AccountDaoSql.INS_TOKEN,
				// ID
				idToken,
				// "USER_ID,
				id,
				// TOKEN,
				activationToken.getToken(),
				// MOBILE_PIN_FLAG,
				(mobile ? "Y" : "N"),
				// VALID_TILL, "
				activationToken.getExpirationDate());

		// Roles
		if (CollectionUtils.isNotEmpty(account.getPriviledges())) {
			for (final Privilege role : account.getPriviledges()) {
				final int idRole = sequencer.nextVal("ID_SEQ");
				getJdbcTemplate().update(AccountDaoSql.INS_ROLES,
						// ID
						idRole,
						// "USER_ID,
						id,
						// ROLE
						role.name());
			}
		}

		if (mobile) {
			final MemberAccountDto accountMobile = (MemberAccountDto) account;
			final int idDevice = sequencer.nextVal("ID_SEQ");
			getJdbcTemplate().update(AccountDaoSql.INS_DEVICES,
					// IDcom.avatar.dao.impl.jdbc.AccountDaoJdbc.updateUserTangerineHandSetId
					idDevice,
					// USER_ID,
					id,
					// DEVICE_ID,
					accountMobile.getDeviceId(),
					// TANGERINE_HANDSET_ID
					accountMobile.getTangerineHandsetId());
		}

	}

	@Override
	public void populateAccountInfo(final AccountDto account,
			final boolean includePicture) throws InvalidParameterException,
			NotFoundException {
		verify(account, "Checking account");
		// Fetch link phone number
		account.setLinkMobileNumber(getLinkPhoneNumber(account.getId()));

		if (includePicture) {
			try {
				final Integer imageIdPk = getJdbcTemplate().queryForObject(
						AccountDaoSql.GET_IMAGE_ID, Integer.class,
						account.getId());
				final ImagePic image = getImage(imageIdPk);
				account.setPicture(image);
			} catch (final EmptyResultDataAccessException e1) {
			}
		}
		// Fetch homeClubID
		try {
			final Integer homeClubIdPk = getJdbcTemplate().queryForObject(
					AccountDaoSql.GET_HOME_CLUB_ID, Integer.class,
					account.getId());

			final ClubDto homeClub = clubDao.get(homeClubIdPk, includePicture);
			account.setHomeClub(homeClub);
		} catch (final EmptyResultDataAccessException e1) {
			throw new NotFoundException("Account not found");
		} catch (final NotFoundException e) {
			// NP.
		}

		if (AccountStatus.New.equals(account.getStatus())
				|| AccountStatus.TokenSent.equals(account.getStatus())) {
			// Get the account token
			try {
				final ActivationToken token = getJdbcTemplate().queryForObject(
						AccountDaoSql.SEL_TOKEN_BY_USERIDPK,
						activationTokenMapper, account.getId());
				account.setToken(token);
			} catch (final EmptyResultDataAccessException e) {
				// NP.
			}
		}
	}

	private void populateOtherAccountInfo(final AccountDto account)
			throws NotFoundException {
		final List<Privilege> roles = fetchRoles(account.getId());
		account.setPriviledges(new HashSet<Privilege>(roles));
		try {
			final Map<String, Object> result = getJdbcTemplate().queryForMap(
					AccountDaoSql.SEL_DEVICE_TANGERINE_HANDSET_ID_BY_USER_ID,
					account.getId());
			if (MapUtils.isNotEmpty(result)) {
				account.setDeviceId((String) result.get("DEVICE_ID"));
				account.setTangerineHandsetId((String) result
						.get("TANGERINE_HANDSET_ID"));
			}
		} catch (final EmptyResultDataAccessException e) {

		}
		if (account instanceof EmployeeAccountDto) {
			try {
				final Integer subAmenityIdPk = getJdbcTemplate()
						.queryForObject(
								AccountDaoSql.SEL_SUBAMENITY_ID_BY_USERID,
								Integer.class, account.getId());
				final EmployeeAccountDto employeeAccount = (EmployeeAccountDto) account;
				employeeAccount.setSubAmenity(clubDao
						.getSubAmenity(subAmenityIdPk));
			} catch (final EmptyResultDataAccessException e) {
			}
		}

		// Phase 2: fetch from USER_NOTES
		final List<AccountNotes> notes = fetchNoteHistory(account.getId());
		account.setNoteHistory(notes);
	}

	@Override
	@Resource(name = "avatarDataSource")
	public void setDataSource(final DataSource ds) {
		initTemplate(ds);
	}

	@Override
	public void truncate() {
		truncate("USER_DEVICES");
		truncate("AMENITY_EMPLOYEE");
		truncate("USER_NOTES");
		truncate("USER_ACTIVATION_TOKEN");
		truncate("USERS");
	}

	@Override
	public void undeactivate(final String userId) throws NotFoundException,
	InvalidParameterException {
		verify(userId, "User ID cannot be null");
		final int userIdPk = getUserIdPkByUserId(userId);
		getJdbcTemplate().update(AccountDaoSql.UPD_ACCOUNT_UNDEACTIVATION,
				userIdPk);
	}

	@Override
	public void updateAccountInfoEmail(final String userId, final String email)
			throws InvalidParameterException, NotFoundException {
		verify(userId, "User ID cannot be null");
		verify(email, "email cannot be null");
		final int updated = getJdbcTemplate().update(
				AccountDaoSql.UPD_USER_EMAIL, email, userId);
		if (updated == 0) {
			throw new NotFoundException();
		}
	}

	@Override
	public void updateAccountInfoName(final String userId, final String fullName)
			throws NotFoundException, InvalidParameterException {
		verify(userId, "User ID cannot be null");
		verify(fullName, "fullName cannot be null");
		final int updated = getJdbcTemplate().update(
				AccountDaoSql.UPD_USER_FULLNAME, fullName, userId);
		if (updated == 0) {
			throw new NotFoundException();
		}
	}

	@Override
	public void updateAccountInfoPicture(final String userId,
			final String pictureBase64) throws NotFoundException,
			InvalidParameterException {
		verify(userId, "User ID cannot be null");
		verify(pictureBase64, "pictureBase64 cannot be null");
		// Must verify that user id exists.
		final int userIdPk = getUserIdPkByUserId(userId);
		final byte[] picture = Base64.decodeBase64(pictureBase64);
		try {
			final Integer imageIdPk = getJdbcTemplate().queryForObject(
					AccountDaoSql.GET_IMAGE_ID_BYUSERID, Integer.class, userId);
			final Integer updateImageIdPk = updateImage(imageIdPk, picture);
			if (updateImageIdPk != imageIdPk) {
				// Update user image_id link
				getJdbcTemplate().update(AccountDaoSql.UPD_USER_IMAGE_ID_LINK,
						updateImageIdPk, userId);
			}
		} catch (final EmptyResultDataAccessException e) {
			// Not found!, so insert one in.
			final ImagePic pic = new ImagePic(pictureBase64);
			final Integer idImagePk = persistImage(pic);
			System.out
			.println("updateAccountInfoPicture: Not found!, so insert one in. idImagePk="
					+ idImagePk);
			getJdbcTemplate().update(AccountDaoSql.UPD_USER_IMAGE_ID_LINK,
					idImagePk, userId);
		}
	}

	@Override
	public void updateNewToken(final ActivationToken token)
			throws NotFoundException, InvalidParameterException {
		verify(token, "Token cannot be null");
		verify(token.getId(), "Token ID cannot be null");
		verify(token.getToken(), "Token string cannot be null");
		verify(token.getExpirationDate(),
				"Token expiration date cannot be null");
		final int rowUpdated = getJdbcTemplate().update(
				AccountDaoSql.UPD_TOKEN, token.getToken(),
				token.getExpirationDate(), token.getId());
		if (rowUpdated == 0) {
			throw new NotFoundException("Token id " + token.getId()
					+ " not found.");
		}
	}

	@Override
	public void updateNoticeInfo(final int userIdPk, final Date currentDate,
			final boolean agreed) throws NotFoundException,
			InvalidParameterException {
		verify(currentDate, "currentDate cannot be null");
		final int rowUpdated = getJdbcTemplate().update(
				AccountDaoSql.UPDATE_NOTICE_INFO, currentDate,
				agreed ? "Y" : "N", userIdPk);
		if (rowUpdated == 0) {
			throw new NotFoundException("id " + userIdPk + " not found.");
		}
	}

	@Override
	public void updateUserDeviceId(final String userId, final String deviceId)
			throws NotFoundException, InvalidParameterException {
		verify(userId, "userId cannot be null");
		verify(deviceId, "deviceId cannot be null");
		final int userIdPk = getUserIdPkByUserId(userId);
		final int updated = getJdbcTemplate().update(
				AccountDaoSql.UPD_USER_DEVICEID, deviceId, userId);
		if (updated == 0) {
			final int idDevice = sequencer.nextVal("ID_SEQ");
			getJdbcTemplate().update(AccountDaoSql.INS_DEVICES,
					// IDcom.avatar.dao.impl.jdbc.AccountDaoJdbc.updateUserTangerineHandSetId
					idDevice,
					// USER_ID,
					userIdPk,
					// DEVICE_ID,
					deviceId,
					// TANGERINE_HANDSET_ID
					null);
		}
	}

	@Override
	public void updateUserTangerineHandSetId(final String userId,
			final String deviceId, final String tangerineHandSetId)
					throws NotFoundException, InvalidParameterException {
		verify(userId, "userId cannot be null");
		verify(deviceId, "deviceId cannot be null");
		final int userIdPk = getUserIdPkByUserId(userId);
		final int updated = getJdbcTemplate().update(
				AccountDaoSql.UPD_USER_TANGERINE_HANDSET_ID,
				tangerineHandSetId, deviceId, userId);
		if (updated == 0) {
			final int idDevice = sequencer.nextVal("ID_SEQ");
			getJdbcTemplate().update(AccountDaoSql.INS_DEVICES,
					// IDcom.avatar.dao.impl.jdbc.AccountDaoJdbc.updateUserTangerineHandSetId
					idDevice,
					// USER_ID,
					userIdPk,
					// DEVICE_ID,
					deviceId,
					// TANGERINE_HANDSET_ID
					tangerineHandSetId);
		}
	}

	@Override
	public boolean validateUserIdPasswd(final String userId,
			final String password) throws NotFoundException,
			InvalidPasswordException, InvalidParameterException {
		verify(userId, "userId cannot be null");
		final Integer userIdPk = getUserIdPkByUserId(userId);
		int validate = 0;
		if (StringUtils.isNotEmpty(password)) {
			validate = getJdbcTemplate().queryForObject(
					AccountDaoSql.VALIDATE_USERID_PASSWD, Integer.class,
					userIdPk, password);
		} else {
			validate = getJdbcTemplate().queryForObject(
					AccountDaoSql.VALIDATE_USERID_NOPASSWD, Integer.class,
					userIdPk);
		}
		if (validate == 0) {
			throw new InvalidPasswordException(
					"Incorrect Password or Account Status Not Activated");
		}
		return true;
	}
}
