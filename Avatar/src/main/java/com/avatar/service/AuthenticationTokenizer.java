package com.avatar.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.avatar.business.AuthenticationTokenizerBusiness;
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

	private final LoadingCache<String, AccountDto> accountCache = CacheBuilder
			.newBuilder().maximumSize(1000)
			.expireAfterWrite(KEY_VALID_FOR_IN_MINUTES, TimeUnit.MINUTES)
			.build(new CacheLoader<String, AccountDto>() {
				@Override
				public AccountDto load(final String uuidToken) {
					return null;
				}
			});

	@Override
	public AccountDto getAccount(final String token) throws NotFoundException,
			AuthenticationTokenExpiredException {
		AccountDto retVal = null;
		try {
			retVal = accountCache.get(token);
		} catch (InvalidCacheLoadException | ExecutionException e) {
			throw new AuthenticationTokenExpiredException(
					"Token not found or expired");
		}
		return retVal;
	}

	@Override
	public Set<Privilege> getRoles(final String token)
			throws NotFoundException, AuthenticationTokenExpiredException {
		Set<Privilege> retVal = null;
		try {
			final AuthenticationTokenPrincipal principal = tokenCache
					.get(token);
			retVal = principal.getRoles();
		} catch (InvalidCacheLoadException | ExecutionException e) {
			throw new AuthenticationTokenExpiredException(
					"Token not found or expired");
		}
		return retVal;
	}

	@Override
	public AuthenticationTokenPrincipal getToken(final String userId,
			final String password) throws InvalidPasswordException,
			NotFoundException, InvalidParameterException 
	{
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
