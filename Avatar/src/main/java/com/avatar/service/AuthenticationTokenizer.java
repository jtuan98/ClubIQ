package com.avatar.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.avatar.business.AuthenticationTokenizerBusiness;
import com.avatar.business.CacheBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dto.AuthenticationTokenPrincipal;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.InvalidPasswordException;
import com.avatar.exception.NotFoundException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;

@Service
public class AuthenticationTokenizer implements AuthenticationTokenizerBusiness {

	private static final long KEY_VALID_FOR_IN_MINUTES = 60;

	@Resource(name = "accountDaoJdbc")
	AccountDao accountDao;

	private final LoadingCache<String, AuthenticationTokenPrincipal> tokenCache = CacheBuilder
			.newBuilder().maximumSize(1000)
			.expireAfterWrite(KEY_VALID_FOR_IN_MINUTES, TimeUnit.MINUTES)
			.build(new CacheLoader<String, AuthenticationTokenPrincipal>() {
				@Override
				public AuthenticationTokenPrincipal load(final String uuidToken) {
					return null;
				}
			});

	@Resource(name="tokenCache")
	private CacheBusiness<String, AccountDto> accountCache;

	@Override
	public AccountDto getAccount(final String token) throws NotFoundException,
	AuthenticationTokenExpiredException, InvalidParameterException {
		if (token == null) {
			throw new InvalidParameterException("Token is null");
		}
		final AccountDto retVal = accountCache.get(token);
		return retVal;
	}

	@Override
	public Set<Privilege> getRoles(final String token)
			throws NotFoundException, AuthenticationTokenExpiredException, InvalidParameterException {
		if (StringUtils.isEmpty(token)) {
			throw new InvalidParameterException();
		}
		Set<Privilege> retVal = null;
		try {
			final AuthenticationTokenPrincipal principal = tokenCache
					.get(token);
			retVal = principal.getRoles();
		} catch (InvalidCacheLoadException | ExecutionException e) {
			final AccountDto account = getAccount(token);
			final AuthenticationTokenPrincipal principal = new AuthenticationTokenPrincipal(new HashSet<Privilege>(
					accountDao.fetchRoles(account.getUserId())));
			tokenCache.put(token, principal);
			retVal = principal.getRoles();
		}
		return retVal;
	}

	@Override
	public AuthenticationTokenPrincipal getToken(final String userId,
			final String password) throws InvalidPasswordException,
			NotFoundException, InvalidParameterException
	{
		if (StringUtils.isEmpty(userId)) {
			throw new InvalidParameterException();
		}
		AuthenticationTokenPrincipal retVal = null;
		final boolean validated = accountDao.validateUserIdPasswd(userId,
				password);

		if (validated) {
			retVal = new AuthenticationTokenPrincipal(new HashSet<Privilege>(
					accountDao.fetchRoles(userId)));
			tokenCache.put(retVal.getToken().toString(), retVal);
			final AccountDto account = accountDao.fetch(userId);
			accountCache.put(retVal.getToken().toString(), account);
		}
		return retVal;
	}

}
