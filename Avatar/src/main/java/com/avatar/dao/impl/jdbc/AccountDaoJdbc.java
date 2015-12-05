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
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.avatar.dao.AccountDao;
import com.avatar.dao.ClubDao;
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
public class AccountDaoJdbc extends BaseJdbcDao implements AccountDao {

	private final AccountDtoMapper accountDtoMapper = new AccountDtoMapper();
	private final RolesMapper rolesMapper = new RolesMapper();

	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDao;

	private final ActivationTokenMapper activationTokenMapper = new ActivationTokenMapper();

	private final AccountNotesDtoMapper accountNotesDtoMapper = new AccountNotesDtoMapper();

	@Override
	public void activate(final String userId, final String activationToken, final Date activated)
			throws NotFoundException {
		final int updated = getJdbcTemplate().update(
				AccountDaoSql.UPD_ACCOUNT_ACTIVATION, activated, activationToken, userId);
		if (updated == 0) {
			throw new NotFoundException();
		}
	}

	@Override
	public void addAmenityToUser(final Integer userIdPk,
			final Integer clubAmenityIdPk) throws InvalidParameterException {
		if (userIdPk == null || clubAmenityIdPk == null) {
			throw new InvalidParameterException("Keys cannot be null");
		}
		addLinkAmenityUserId(clubAmenityIdPk, userIdPk);
	}

	private void addLinkAmenityUserId(final Integer amenityIdPk,
			final Integer userIdPk) {

		final int updated = getJdbcTemplate().update(
				AccountDaoSql.UPD_AMENITY_EMPLOYEE, amenityIdPk, userIdPk);
		if (updated == 0) {
			final int idAmenityEmployee = sequencer.nextVal("ID_SEQ");
			getJdbcTemplate().update(AccountDaoSql.INS_AMENITY_EMPLOYEE,
					idAmenityEmployee, amenityIdPk, userIdPk);
		}
	}

	@Override
	public Number addNote(final Integer userIdPk, final String noteText,
			final DateTime noteDateTime) {

		final int idNoteAdded = sequencer.nextVal("ID_SEQ");
		getJdbcTemplate().update(AccountDaoSql.INS_NOTES, idNoteAdded,
				userIdPk, noteText, noteDateTime);
		return idNoteAdded;
	}

	@Override
	public void deactivate(final String userId, final Date deactivateDate)
			throws NotFoundException {
		final int userIdPk = getUserIdPkByUserId(userId);
		getJdbcTemplate().update(AccountDaoSql.UPD_ACCOUNT_DEACTIVATION,
				deactivateDate, userIdPk);
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
				final List<Privilege> roles = fetchRoles(account.getId());
				account.setPriviledges(new HashSet<Privilege>(roles));
				try {
					final Map<String, Object> result = getJdbcTemplate()
							.queryForMap(
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
						final Integer amenityIdPk = getJdbcTemplate()
								.queryForObject(
										AccountDaoSql.SEL_AMENITY_ID_BY_USERID,
										Integer.class, account.getId());
						final EmployeeAccountDto employeeAccount = (EmployeeAccountDto) account;
						employeeAccount.setAmenity(clubDao
								.getAmenity(amenityIdPk));
					} catch (final EmptyResultDataAccessException e) {
					}
				}
				if ((account.getPicture() != null)
						&& (account.getPicture().getId() != null)) {
					System.out.println("Getting the picture!!!");
					final ImagePic image = getImage(account.getPicture()
							.getId());
					account.setPicture(image);
				}

				// Phase 2: fetch from USER_NOTES
				final List<AccountNotes> notes = fetchNoteHistory(account.getId());
				account.setNoteHistory(notes);

			} catch (final EmptyResultDataAccessException e) {
				throw new NotFoundException();
			}
		} else {
			throw new InvalidParameterException("Param cannot be null");
		}

		// Phase 2: Mocking...
		if (account != null && CollectionUtils.isEmpty(account.getNoteHistory())) {
			account.setActDate(getNow());
			final AccountNotes note1 = new AccountNotes();
			note1.setNoteDate(getNow());
			note1.setNoteText("Mock data only.");
			account.add(note1);
			final AccountNotes note2 = new AccountNotes();
			note2.setNoteDate(getNow());
			note2.setNoteText("Mock data only.");
			account.add(note2);
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
		return getJdbcTemplate().query(AccountDaoSql.SEL_NOTESHISTORY_BY_USER_ID,
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

	@Override
	public List<AccountDto> getMembers(final int clubIdPk) throws NotFoundException {
		// TODO Phase 2
		return null;
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
	public void linkNumbers(final String userId, final String linkNumber, final Date currentDate)
			throws NotFoundException {
		final int linkNumberIdPk = getUserIdPkByUserId(linkNumber);
		final int userIdPk = getUserIdPkByUserId(userId);
		getJdbcTemplate().update(
				AccountDaoSql.UPD_ACCOUNT_LINK, linkNumberIdPk, userIdPk);
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
			final ActivationToken activationToken) throws NotFoundException {
		final int id = sequencer.nextVal("ID_SEQ");
		account.setId(id);
		final int idToken = sequencer.nextVal("ID_SEQ");
		String mobileNumber = "";
		final boolean mobile = (account instanceof MemberAccountDto);
		final Integer idImage = persistImage(account.getPicture());
		Validate.notEmpty(this.getClass().getName() + " USER_ID",
				account.getUserId());
		// Get mobile stuff
		if (mobile) {
			final MemberAccountDto accountMobile = (MemberAccountDto) account;
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

		getJdbcTemplate().update(AccountDaoSql.INS_ACCOUNT,
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
				AccountStatus.New.name());
		if (clubIdPk != null) {
			clubDao.addUserToClub(clubIdPk, account.getId());
		}
		if (!mobile) {
			final EmployeeAccountDto employeeAccount = (EmployeeAccountDto) account;
			if (employeeAccount.getAmenity() != null) {
				addLinkAmenityUserId(employeeAccount.getAmenity().getId(),
						account.getId());
			}
		}
		getJdbcTemplate().update(AccountDaoSql.INS_TOKEN,
				// ID
				idToken,
				// "USER_ID,
				account.getId(),
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
						account.getId(),
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
					account.getId(),
					// DEVICE_ID,
					accountMobile.getDeviceId(),
					// TANGERINE_HANDSET_ID
					accountMobile.getTangerineHandsetId());
		}

	}

	@Override
	public void populateAccountInfo(final AccountDto account,
			final boolean includePicture) {

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

	@Override
	@Resource(name = "avatarDataSource")
	public void setDataSource(final DataSource ds) {
		initTemplate(ds);
	}

	@Override
	public void undeactivate(final String userId) throws NotFoundException {
		// TODO Phase 2

	}

	@Override
	public void updateAccountInfoEmail(final String userId, final String email)
			throws NotFoundException {
		final int updated = getJdbcTemplate().update(
				AccountDaoSql.UPD_USER_EMAIL, email, userId);
		if (updated == 0) {
			throw new NotFoundException();
		}
	}

	@Override
	public void updateAccountInfoName(final String userId, final String fullName)
			throws NotFoundException {
		final int updated = getJdbcTemplate().update(
				AccountDaoSql.UPD_USER_FULLNAME, fullName, userId);
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
			throws NotFoundException {
		getJdbcTemplate().update(AccountDaoSql.UPD_TOKEN, token.getToken(),
				token.getExpirationDate(), token.getId());
	}

	@Override
	public void updateUserDeviceId(final String userId, final String deviceId)
			throws NotFoundException {
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
					throws NotFoundException {
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
			InvalidPasswordException {
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
