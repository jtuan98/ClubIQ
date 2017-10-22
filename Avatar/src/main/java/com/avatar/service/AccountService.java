package com.avatar.service;

import java.util.Date;
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
import org.springframework.util.Assert;

import com.avatar.business.AccountBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.ReservationDao;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.account.MemberAccountDto;
import com.avatar.dto.account.MobileActivationPin;
import com.avatar.dto.club.CheckInfo;
import com.avatar.dto.club.SubAmenityDto;
import com.avatar.dto.enums.AccountStatus;
import com.avatar.dto.enums.DbTimeZone;
import com.avatar.exception.AccountCreationException;
import com.avatar.exception.AccountExistedException;
import com.avatar.exception.AccountSuspendedException;
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

	@Resource(name = "reservationDaoJdbc")
	private ReservationDao reservationDao;

	@Override
	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	public boolean activateAccount(final String activationToken,
			final Date activatedDate) throws InvalidParameterException {
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
			accountDao.activate(activationAccount.getUserId(), activationToken,
					activatedDate);
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
			final String deviceId, final String activationToken,
			final Date activatedDate) throws InvalidParameterException {
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
			accountDao.activate(activationAccount.getUserId(), activationToken,
					activatedDate);
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
	public void addNote(final String memberId, final String noteText,
			final DateTime parseDateTime) throws NotFoundException {
		final Integer userPkId = accountDao.getUserIdPkByUserId(memberId);
		accountDao.addNote(userPkId, noteText, parseDateTime);
	}

	@Override
	public void addSubAmenityToUser(final String userId,
			final String clubSubAmenityId) throws NotFoundException,
			InvalidParameterException {
		if (StringUtils.isEmpty(userId)) {
			throw new InvalidParameterException("UserId cannot be null");
		}
		if (StringUtils.isEmpty(clubSubAmenityId)) {
			throw new InvalidParameterException(
					"ClubSubAmenityId cannot be null");
		}

		final Integer userIdPk = accountDao.getUserIdPkByUserId(userId);
		final AccountDto account = get(userId);
		if (account.getHomeClub() == null
				|| account.getHomeClub().getId() == null) {
			throw new InvalidParameterException("Account " + userId
					+ " is missing home club association");
		}
		final Integer clubSubAmenityIdPk = clubDao.getClubSubAmenityIdPk(
				account.getHomeClub().getId(), clubSubAmenityId);
		accountDao.addSubAmenityToUser(userIdPk, clubSubAmenityIdPk);
	}

	@Override
	public void cancelMembership(final String userId, final Date currentDate)
			throws NotFoundException {
		accountDao.deactivate(userId, currentDate, true);
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

				// Verify sub amenity id
				if (!mobile) {
					final EmployeeAccountDto employeeAccountInfo = (EmployeeAccountDto) accountInfo;
					if (employeeAccountInfo.getSubAmenity() != null) {
						final Integer subAmenityIdPk = clubDao.getClubSubAmenityIdPk(
								employeeAccountInfo.getHomeClub().getId(),
								employeeAccountInfo.getSubAmenity().getSubAmenityId());
						final SubAmenityDto subAmenityFromDb = clubDao
								.getSubAmenity(subAmenityIdPk);
						employeeAccountInfo.getSubAmenity().makeCopy(subAmenityFromDb);
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
		accountDao.deactivate(userId, null);
		return true;
	}

	@Override
	public boolean exists(final String userId)
			throws InvalidParameterException, AccountSuspendedException {
		if (StringUtils.isEmpty(userId)) {
			throw new InvalidParameterException("UserId cannot be null");
		}
		boolean retVal = false;
		try {
			final int userIdPk = accountDao.getUserIdPkByUserId(userId);
			final AccountStatus status = accountDao.getStatus(userIdPk);
			if (AccountStatus.Cancelled.equals(status)
					|| AccountStatus.Terminated.equals(status)) {
				throw new AccountSuspendedException("Account " + userId
						+ " is " + status.name());
			}
			retVal = true;
		} catch (final AccountSuspendedException e) {
			throw e;
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

	// Phase2
	@Override
	public CheckInfo getCheckInfo(final String availId)
			throws NotFoundException {
		final CheckInfo retVal = reservationDao.getReservation(availId);
		if (retVal == null) {
			throw new NotFoundException();
		}
		return retVal;
	}

	@Override
	public List<AccountDto> getMembers(final String clubId)
			throws NotFoundException, InvalidParameterException {
		return getMembers(clubId, null, false);
	}


	@Override
	public List<AccountDto> getMembers(final String clubId, final DateTime fromDate, final boolean populatePicture)
			throws NotFoundException, InvalidParameterException {
		final int clubIdPk = StringUtils.isEmpty(clubId)? 0: clubDao.getClubIdPk(clubId);
		final List<AccountDto> retVal = accountDao.getMembers(clubIdPk, fromDate, populatePicture);
		return retVal;
	}

	@Override
	public void markStatusAsNotified(final String userId) throws NotFoundException {
		accountDao.markStatusAsNotified(userId);
	}

	@Override
	public void setLinkNumber(final String userId, final String linkNumber,
			final Date currentDate) throws NotFoundException,
			InvalidParameterException, AccountSuspendedException {
		exists(userId);
		exists(linkNumber);
		accountDao.linkNumbers(userId, linkNumber, currentDate);
	}

	@Override
	public void setNoticeInfo(final String userId, final Date currentDate, final boolean agreed)
			throws NotFoundException, InvalidParameterException {
		final int userIdPk = accountDao.getUserIdPkByUserId(userId);
		accountDao.updateNoticeInfo(userIdPk, currentDate, agreed);
	}

	@Override
	public void suspend(final String memberId, final DateTime suspendDate)
			throws NotFoundException, InvalidParameterException {
		Assert.notNull(suspendDate);
		accountDao.deactivate(memberId, suspendDate.toDate());
	}

	@Override
	public void unsuspend(final String memberId) throws NotFoundException, InvalidParameterException {
		accountDao.undeactivate(memberId);
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
			final String requestedClubId, final String subAmenityId,
			final int numOfPerson, final Date requestedDateTime, final Date requestedToDateTime)
					throws NotFoundException {
		final int userIdPk = accountDao.getUserIdPkByUserId(userId);
		final int clubIdPk = clubDao.getClubIdPk(requestedClubId);
		final int subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk,
				subAmenityId);
		final String reservationId = UUID.randomUUID().toString();
		reservationDao.reserve(clubIdPk, subAmenityIdPk, userIdPk, numOfPerson,
				requestedDateTime, reservationId, requestedToDateTime);
		return reservationId;
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
