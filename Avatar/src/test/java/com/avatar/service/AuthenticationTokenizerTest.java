package com.avatar.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.avatar.dto.AccountDtoBuilder;
import com.avatar.dto.AuthenticationTokenPrincipal;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.InvalidPasswordException;
import com.avatar.exception.NotFoundException;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;

public class AuthenticationTokenizerTest extends BaseServiceTest {
	private AuthenticationTokenizer service;

	@Before
	public void setUp() {
		service = new AuthenticationTokenizer();
		ReflectionTestUtils.setField(service, "accountDao", accountDao);
		super.setup();
	}

	// -----------GetAccount--------------
	// token is null
	@Test(expected = InvalidParameterException.class)
	public void test001GetAccount_NullParam() throws NotFoundException,
	AuthenticationTokenExpiredException {
		final String token = null;
		service.getAccount(token);
	}

	// token is invalid
	@Test(expected = AuthenticationTokenExpiredException.class)
	public void test002GetAccount_001InvalidToken() throws NotFoundException,
	AuthenticationTokenExpiredException, ExecutionException {
		final String token = "123";
		final LoadingCache<String, AccountDto> accountCacheMock = mock(LoadingCache.class);
		ReflectionTestUtils.setField(service, "accountCache", accountCacheMock);
		given(accountCacheMock.get(token)).willThrow(
				InvalidCacheLoadException.class);
		service.getAccount(token);
	}

	@Test(expected = AuthenticationTokenExpiredException.class)
	public void test002GetAccount_002InvalidToken() throws NotFoundException,
	AuthenticationTokenExpiredException, ExecutionException {
		final String token = "123";
		final LoadingCache<String, AccountDto> accountCacheMock = mock(LoadingCache.class);
		ReflectionTestUtils.setField(service, "accountCache", accountCacheMock);
		given(accountCacheMock.get(token)).willThrow(ExecutionException.class);
		service.getAccount(token);
	}

	// token is valid
	@Test
	public void test003GetAccount_001ValidToken() throws NotFoundException,
	AuthenticationTokenExpiredException, ExecutionException {
		// Set up
		final String token = "123";
		final LoadingCache<String, AccountDto> accountCacheMock = mock(LoadingCache.class);
		ReflectionTestUtils.setField(service, "accountCache", accountCacheMock);
		final AccountDto account = new AccountDtoBuilder(true).withId(1)
				.getBuiltInstance();
		given(accountCacheMock.get(token)).willReturn(account);

		// Call
		final AccountDto returnedAccount = service.getAccount(token);

		// Verify
		Assert.assertEquals("Check account id", account.getId(),
				returnedAccount.getId());
	}

	// ----------------GetRoles---------------------------
	// token is null
	@Test(expected = InvalidParameterException.class)
	public void test004GetRoles_NullParam() throws NotFoundException,
	AuthenticationTokenExpiredException {
		final String token = null;
		service.getRoles(token);
	}

	// token is invalid
	@Test(expected = AuthenticationTokenExpiredException.class)
	public void test005GetRoles_001InvalidToken() throws NotFoundException,
	AuthenticationTokenExpiredException, ExecutionException {
		final String token = "123";
		final LoadingCache<String, AccountDto> accountCacheMock = mock(LoadingCache.class);
		ReflectionTestUtils.setField(service, "accountCache", accountCacheMock);
		given(accountCacheMock.get(token)).willThrow(
				InvalidCacheLoadException.class);
		service.getRoles(token);
	}

	@Test(expected = ExecutionException.class)
	public void test005GetRoles_002InvalidToken() throws NotFoundException,
	AuthenticationTokenExpiredException, ExecutionException {
		final String token = "123";
		final LoadingCache<String, AccountDto> accountCacheMock = mock(LoadingCache.class);
		ReflectionTestUtils.setField(service, "accountCache", accountCacheMock);
		given(accountCacheMock.get(token)).willThrow(ExecutionException.class);
		service.getRoles(token);
	}

	// token is valid
	@Test
	public void test006GetRoles_ValidToken() throws NotFoundException,
	AuthenticationTokenExpiredException, ExecutionException {
		// set up
		final String token = "123";
		final LoadingCache<String, AuthenticationTokenPrincipal> tokenCacheMock = mock(LoadingCache.class);
		ReflectionTestUtils.setField(service, "tokenCache", tokenCacheMock);
		final Set<Privilege> roles = new HashSet<Privilege>();
		roles.add(Privilege.avatarStaff);
		final AuthenticationTokenPrincipal tokenPrincipal = new AuthenticationTokenPrincipal(roles);
		given(tokenCacheMock.get(token)).willReturn(tokenPrincipal);

		// call
		final Set<Privilege> returnedRole = service.getRoles(token);

		// verify
		Assert.assertEquals("Checking Roles", roles, returnedRole);
	}

	// ----------------GetToken---------------------------
	// userId and password are null
	@Test(expected = InvalidParameterException.class)
	public void test007GetToken_001NullParam() throws NotFoundException,
	AuthenticationTokenExpiredException, InvalidPasswordException,
	InvalidParameterException {
		final String userId = null;
		final String password = null;
		service.getToken(userId, password);
	}

	// userId is null but password is acceptable
	@Test(expected = InvalidParameterException.class)
	public void test007GetToken_002NullParam() throws NotFoundException,
	AuthenticationTokenExpiredException, InvalidPasswordException,
	InvalidParameterException {
		final String userId = null;
		final String password = "password";
		service.getToken(userId, password);
	}

	// userId is acceptable but password is null
	@Test(expected = InvalidParameterException.class)
	public void test007GetToken_003NullParam() throws NotFoundException,
	AuthenticationTokenExpiredException, InvalidPasswordException,
	InvalidParameterException {
		final String userId = "user";
		final String password = null;
		service.getToken(userId, password);
	}

	// userId and password are acceptable
	@Test
	public void test008GetToken_Valid() throws NotFoundException,
	AuthenticationTokenExpiredException, InvalidPasswordException,
	InvalidParameterException {

		// set up
		final String userId = "user";
		final String password = "password";
		final LoadingCache<String, AuthenticationTokenPrincipal> tokenCacheMock = mock(LoadingCache.class);
		ReflectionTestUtils.setField(service, "tokencache", tokenCacheMock);

		final AuthenticationTokenPrincipal token = new AuthenticationTokenPrincipal(new HashSet<Privilege>());

		given(service.getToken(userId, password)).willReturn(token);

		// call
		final AuthenticationTokenPrincipal returnedToken = service.getToken(userId, password);

		// verify
		Assert.assertEquals("Checking token", token, returnedToken);
	}
}
