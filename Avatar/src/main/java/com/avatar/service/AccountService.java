package com.avatar.service;

import java.security.InvalidParameterException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avatar.business.AccountBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.account.MobileAccountDto;
import com.avatar.dto.account.MobileActivationPin;
import com.avatar.exception.AccountCreationException;
import com.avatar.exception.AccountExistedException;
import com.avatar.exception.NotFoundException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;

@Service
@Transactional("transactionManager")
public class AccountService implements AccountBusiness {
	public static void main(final String[] args) {
		// final AccountService service = new AccountService();
		// final ActivationToken token = service.generateActivationToken(true);
		// final Gson gson = new Gson();
		// System.out.println(gson.toJson(token));
		final String activationTokenParam = "123";
		final String[] activationToken = activationTokenParam.split("_");
		System.out.println(activationToken.length);
	}

	private static long KEY_VALID_FOR_IN_MINUTES = 2000;

	private final LoadingCache<String, AccountDto> activationCache = CacheBuilder
			.newBuilder().maximumSize(1000)
			.expireAfterWrite(KEY_VALID_FOR_IN_MINUTES, TimeUnit.MINUTES)
			.build(new CacheLoader<String, AccountDto>() {
				@Override
				public AccountDto load(final String activationTokenParam)
						throws NotFoundException {
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

	@Override
	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	public boolean activateAccount(final String activationToken)
			throws InvalidParameterException {
		boolean retVal = false;
		try {
			final AccountDto activationAccount = activationCache
					.get(activationToken);
			Validate.notNull(activationAccount);
			// Update database as activated!
			accountDao.activate(activationAccount.getUserId(), activationToken);
			retVal = true;
		} catch (InvalidCacheLoadException | ExecutionException
				| NotFoundException e) {
			e.printStackTrace();
			throw new InvalidParameterException(
					"Activation Token Not Found!  May have expired.["
							+ activationToken + "]");
		}
		return retVal;
	}

	@Override
	public boolean activateMobileAccount(final String mobileNumber,
			final String deviceId, final String activationToken) {
		boolean retVal = false;
		try {
			final AccountDto activationAccount = activationCache
					.get(generateKey(true, mobileNumber, deviceId,
							activationToken));
			Validate.notNull(activationAccount);
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
	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	public ActivationToken createAccount(final AccountDto accountInfo)
			throws NotFoundException, AccountCreationException {
		final Date now = new Date();
		final boolean mobile = accountInfo instanceof MobileAccountDto;
		final ActivationToken activationToken = generateActivationToken(mobile);

		String deviceId = null;
		if (mobile) {
			final MobileAccountDto mobileAccount = (MobileAccountDto) accountInfo;
			deviceId = mobileAccount.getDeviceId();
		}
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
		} catch (final NotFoundException e) {
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
	public boolean exists(final String userId) {
		boolean retVal = false;
		try {
			accountDao.getUserIdPkByUserId(userId);
			retVal = true;
		} catch (final NotFoundException e) {
		}
		return retVal;
	}

	@Override
	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	public ActivationToken generateActivationToken(final boolean mobile) {
		final ActivationToken retVal = mobile ? new MobileActivationPin()
				: new ActivationToken();
		retVal.setToken(UUID.randomUUID().toString());
		retVal.setExpirationDate(new Date(System.currentTimeMillis()
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
	public AccountDto get(final String userId) throws NotFoundException {
		final AccountDto retVal = accountDao.fetch(userId);
		return retVal;
	}

	@Override
	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	public void updateAccountInfo(final String userId, final String deviceId,
			final String fullName, final String email,
			final String pictureBase64) throws NotFoundException {
		Validate.isTrue(StringUtils.isNotEmpty(userId));
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

	@Override
	@Transactional(rollbackFor = Throwable.class, readOnly = false)
	public void updateUserTangerineHandSetId(final String userId,
			final String deviceId, final String tangerineHandSetId)
			throws NotFoundException {
		accountDao.updateUserTangerineHandSetId(userId, deviceId,
				tangerineHandSetId);
	}
}
