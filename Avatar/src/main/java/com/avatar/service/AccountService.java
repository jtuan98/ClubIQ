package com.avatar.service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avatar.business.AccountBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dao.ClubDao;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.account.MemberAccountDto;
import com.avatar.dto.account.MobileActivationPin;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.CheckInfo;
import com.avatar.dto.enums.AccountStatus;
import com.avatar.dto.enums.DbTimeZone;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.AccountCreationException;
import com.avatar.exception.AccountExistedException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;

@Service
@Transactional("transactionManager")
public class AccountService extends BaseService implements AccountBusiness {
	private static long KEY_VALID_FOR_IN_MINUTES = 60 * 7 * 24;

	public static void main(final String[] args) {
		// final AccountService service = new AccountService();
		// final ActivationToken token = service.generateActivationToken(true);
		// final Gson gson = new Gson();
		// System.out.println(gson.toJson(token));
		final Date test = new Date(System.currentTimeMillis()
				+ (KEY_VALID_FOR_IN_MINUTES * 60 * 1000));
		System.out.println(test);
	}

	private final LoadingCache<String, AccountDto> activationCache = CacheBuilder
			.newBuilder().maximumSize(1000)
			.expireAfterWrite(KEY_VALID_FOR_IN_MINUTES, TimeUnit.MINUTES)
			.build(new CacheLoader<String, AccountDto>() {
				@Override
				public AccountDto load(final String activationTokenParam)
						throws NotFoundException, InvalidParameterException {
					final String[] activationToken = activationTokenParam
							.split("_");
					String token, userId, deviceId;
					if (activationToken.length > 1) {
						// Mobile
						userId = activationToken[0];
						deviceId = activationToken[1];
						token = activationToken[2];
					} else {
						token = activationTokenParam;
						userId = null;
						deviceId = null;
					}
					return accountDao.fetchByToken(token, userId, deviceId);
				}
			});

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDao;

	@Override
	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	public boolean activateAccount(final String activationToken)
			throws InvalidParameterException {
		if (StringUtils.isEmpty(activationToken)) {
			throw new InvalidParameterException(
					"Activation Token cannot be null");
		}
		boolean retVal = false;
		try {
			final AccountDto activationAccount = activationCache
					.get(activationToken);
			Validate.notNull(activationAccount);
			if (!(activationAccount instanceof EmployeeAccountDto)) {
				throw new InvalidParameterException(
						"Activation Token is not for EmployeeAccountDto");
			}
			// Update database as activated!
			accountDao.activate(activationAccount.getUserId(), activationToken);
			retVal = true;
		} catch (InvalidCacheLoadException | ExecutionException
				| NotFoundException e) {
			throw new InvalidParameterException(
					"Activation Token Not Found!  May have expired.["
							+ activationToken + "]");
		}
		return retVal;
	}

	@Override
	public boolean activateMobileAccount(final String mobileNumber,
			final String deviceId, final String activationToken)
					throws InvalidParameterException {
		boolean retVal = false;
		if (StringUtils.isEmpty(mobileNumber)) {
			throw new InvalidParameterException("mobileNumber cannot be null");
		}
		if (StringUtils.isEmpty(activationToken)) {
			throw new InvalidParameterException(
					"Activation Token cannot be null");
		}
		try {
			final AccountDto activationAccount = activationCache
					.get(generateKey(true, mobileNumber, deviceId,
							activationToken));
			Validate.notNull(activationAccount);
			if (!(activationAccount instanceof MemberAccountDto)) {
				throw new InvalidParameterException(
						"Activation Token is not for MemberAccountDto");
			}
			// Update database as activated!
			accountDao.activate(activationAccount.getUserId(), activationToken);
			retVal = true;
		} catch (InvalidCacheLoadException | ExecutionException
				| NotFoundException e) {
			throw new InvalidParameterException(
					"Activation Token Not Found!  May have expired.["
							+ activationToken + "]");
		}
		return retVal;
	}

	@Override
	public void addAmenityToUser(final String userId, final String clubAmenityId)
			throws NotFoundException, InvalidParameterException {
		if (StringUtils.isEmpty(userId)) {
			throw new InvalidParameterException("UserId cannot be null");
		}
		if (StringUtils.isEmpty(clubAmenityId)) {
			throw new InvalidParameterException("ClubAmenityId cannot be null");
		}

		final Integer userIdPk = accountDao.getUserIdPkByUserId(userId);
		final Integer clubAmenityIdPk = clubDao
				.getClubAmenityIdPk(clubAmenityId);
		accountDao.addAmenityToUser(userIdPk, clubAmenityIdPk);
	}

	@Override
	public void addNote(final String memberId, final String noteText, final DateTime parseDateTime)
			throws NotFoundException {
		// TODO Phase2

	}

	@Override
	public void cancelMembership(final String userId, final Date currentDate)
			throws NotFoundException {
		// TODO Phase 2
	}

	@Override
	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	public ActivationToken createAccount(final AccountDto accountInfo)
			throws NotFoundException, AccountCreationException,
			InvalidParameterException {
		if (accountInfo == null) {
			throw new InvalidParameterException("AccountInfo cannot be null");
		}
		if (accountInfo.getStatus() == null) {
			throw new InvalidParameterException(
					"AccountInfo Status cannot be null");
		}
		final DbTimeZone timezone = accountInfo.getHomeClub() != null ? accountInfo
				.getHomeClub().getTimeZone() : null;
				final Date now = getNow(timezone);
				final boolean mobile = accountInfo instanceof MemberAccountDto;
				final ActivationToken activationToken = generateActivationToken(mobile,
						timezone);

				final String deviceId = accountInfo.getDeviceId();

				// Check if account already exists...
				try {
					final AccountDto checkAcct = accountDao.fetch(accountInfo
							.getUserId());
					// if Found
					switch (checkAcct.getStatus()) {
					case Activated:
						throw new AccountExistedException("Account "
								+ accountInfo.getUserId() + " is already active!");
					case Cancelled:
					case Terminated:
						throw new AccountCreationException("Account "
								+ accountInfo.getUserId()
								+ " is already cancelled or terminated!");
					case New:
					case TokenSent:
						if ((checkAcct.getToken() != null)
								&& checkAcct.getToken().getExpirationDate().before(now)) {
							// New token, token expire
							checkAcct.getToken().setToken(activationToken.getToken());
							checkAcct.getToken().setExpirationDate(
									activationToken.getExpirationDate());
							accountDao.updateNewToken(checkAcct.getToken());
						}
						return checkAcct.getToken();
					}
				} catch (final NotFoundException | InvalidParameterException e) {
				}

				// Verify amenity id
				if (!mobile) {
					final EmployeeAccountDto employeeAccountInfo = (EmployeeAccountDto) accountInfo;
					if (employeeAccountInfo.getAmenity() != null) {
						final Integer amenityIdPk = clubDao
								.getClubAmenityIdPk(employeeAccountInfo.getAmenity()
										.getAmenityId());
						final AmenityDto amenityFromDb = clubDao
								.getAmenity(amenityIdPk);
						employeeAccountInfo.getAmenity().makeCopy(amenityFromDb);
					}
				}
				// Persist account info
				accountDao.newAccount(accountInfo, activationToken);
				activationCache.put(
						generateKey(true, accountInfo.getUserId(), deviceId,
								activationToken.getToken()), accountInfo);
				return activationToken;
	}

	@Override
	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	public boolean deactivateAccount(final String userId)
			throws NotFoundException {
		accountDao.deactivate(userId);
		return true;
	}

	@Override
	public boolean exists(final String userId) throws InvalidParameterException {
		if (StringUtils.isEmpty(userId)) {
			throw new InvalidParameterException("UserId cannot be null");
		}
		boolean retVal = false;
		try {
			accountDao.getUserIdPkByUserId(userId);
			retVal = true;
		} catch (final Exception e) {
		}
		return retVal;
	}

	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	private ActivationToken generateActivationToken(final boolean mobile,
			final DbTimeZone timezone) {
		final ActivationToken retVal = mobile ? new MobileActivationPin()
		: new ActivationToken();
		retVal.setToken(UUID.randomUUID().toString());
		final Date now = getNow(timezone);
		retVal.setExpirationDate(new Date(now.getTime()
				+ (KEY_VALID_FOR_IN_MINUTES * 60 * 1000)));
		return retVal;
	}

	private String generateKey(final boolean mobile, final String userId,
			final String deviceId, final String token) {
		String retVal = token;
		if (mobile) {
			retVal = userId + "_" + deviceId + "_" + token;
		}
		System.out.println(retVal);
		return retVal;
	}

	@Override
	public AccountDto get(final String userId) throws NotFoundException,
	InvalidParameterException {
		if (StringUtils.isEmpty(userId)) {
			throw new InvalidParameterException("UserId cannot be null");
		}
		final AccountDto retVal = accountDao.fetch(userId);

		return retVal;
	}

	// Phase2 TODO
	@Override
	public CheckInfo getCheckInfo(final String userId, final String availId) {
		final CheckInfo retVal = new CheckInfo();
		retVal.setAvailId(availId);
		retVal.setAmenityId("myBar1");
		retVal.setAmenityName("My Bar One");
		retVal.setPersonNumber(5);
		retVal.setRequestedClubId("Gentlemens club");
		retVal.setRequestedDateTime("201512251700");

		return retVal;
	}

	@Override
	public List<AccountDto> getMembers(final String clubId) throws NotFoundException {
		// TODO Phase 2
		final List<AccountDto>  retVal = new LinkedList<AccountDto>();
		for (int i=0;i<5;i++) {
			final AccountDto mockAccount = new MemberAccountDto();
			mockAccount.add(Privilege.user);
			mockAccount.setAddress("123"+i+" whatever rd");
			mockAccount.setDeviceId("whatever deviceid");
			mockAccount.setEmail("123"+i+"@whatever.com");
			mockAccount.setId(i);
			mockAccount.setMobileNumber(getRandomPhoneNumber());
			mockAccount.setLinkMobileNumber(getRandomPhoneNumber());
			mockAccount.setName("whatever name " + i);
			mockAccount.setStatus(AccountStatus.Activated);
			mockAccount.setUserId(mockAccount.getMobileNumber());
			retVal.add(mockAccount);
		}
		return retVal;
	}

	private String getRandomPhoneNumber() {
		int pre = (int) (Math.floor(Math.random()*1000) % 1000);
		if (pre < 200) {
			pre += 200;
		}
		final int post = (int) (Math.floor(Math.random()*10000) % 10000);
		return String.format("%03d-%04d", pre, post);
	}

	@Override
	public void setLinkNumber(final String userId, final String linkNumber, final Date currentDate)
			throws NotFoundException {
		// TODO Phase 2

	}

	@Override
	public void suspend(final String memberId, final DateTime suspendDate)
			throws NotFoundException {
		// TODO Phase2

	}

	@Override
	public void unsuspend(final String memberId) throws NotFoundException {
		// TODO Phase2

	}

	@Override
	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	public void updateAccountInfo(final String userId, final String deviceId,
			final String fullName, final String email,
			final String pictureBase64) throws NotFoundException,
			InvalidParameterException {
		if (StringUtils.isEmpty(userId)) {
			throw new InvalidParameterException("UserId cannot be null");
		}
		if (StringUtils.isNoneEmpty(deviceId)) {
			accountDao.updateUserDeviceId(userId, deviceId);
		}
		if (StringUtils.isNoneEmpty(fullName)) {
			accountDao.updateAccountInfoName(userId, fullName);
		}
		if (StringUtils.isNoneEmpty(email)) {
			accountDao.updateAccountInfoEmail(userId, email);
		}
		if (StringUtils.isNoneEmpty(pictureBase64)) {
			accountDao.updateAccountInfoPicture(userId, pictureBase64);
		}
	}

	// Phase 2
	@Override
	public String updateCheckInfo(final String userId,
			final String requestedClubId, final String amenityId,
			final int numOfPerson, final String requestedDateTime) {
		// TODO Phase2
		return UUID.randomUUID().toString();
	}

	@Override
	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	public void updateUserTangerineHandSetId(final String userId,
			final String deviceId, final String tangerineHandSetId)
					throws NotFoundException, InvalidParameterException {
		if (StringUtils.isEmpty(userId)) {
			throw new InvalidParameterException("UserId cannot be null");
		}
		accountDao.updateUserTangerineHandSetId(userId, deviceId,
				tangerineHandSetId);
	}
}
