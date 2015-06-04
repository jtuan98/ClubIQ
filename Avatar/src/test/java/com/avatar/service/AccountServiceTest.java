package com.avatar.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.avatar.dao.DbDateDao;
import com.avatar.dto.AccountDtoBuilder;
import com.avatar.dto.ClubDtoBuilder;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.AccountStatus;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.AccountCreationException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;

public class AccountServiceTest extends BaseServiceTest {
	// Case 2: employee account already exists...
	static int KEY_VALID_FOR_IN_MINUTES = 1;

	private AccountService service;

	private AccountDtoBuilder builder;

	private AccountDto createAccount(final boolean employeeAccount,
			final AccountStatus status, final String deviceId,
			final Privilege role, final boolean expiredToken,
			final String userIdOrMobileNumber) {
		return createAccount(employeeAccount, status, deviceId, role,
				expiredToken, userIdOrMobileNumber, null);
	}

	private AccountDto createAccount(final boolean employeeAccount,
			final AccountStatus status, final String deviceId,
			final Privilege role, final boolean expiredToken,
			final String userIdOrMobileNumber, final String amenityId) {
		final Set<Privilege> privileges = new HashSet<>();
		privileges.add(Privilege.user);
		final ClubDto club = new ClubDtoBuilder().withId(1).getBuiltInstance();
		final ActivationToken token = new ActivationToken();
		token.setToken("whatever");
		token.setExpirationDate(new Date(System.currentTimeMillis()
				+ (KEY_VALID_FOR_IN_MINUTES * 60 * 1000 * (expiredToken ? -1
						: 1))));
		AmenityDto amenity = null;
		if (amenityId != null) {
			amenity = new AmenityDto(1);
			amenity.setAmenityId(amenityId);
			amenity.setAmenityType("type");
			amenity.setDescription("Test");
			amenity.setHoursOfOperation("1");
		}
		final AccountDtoBuilder builder = new AccountDtoBuilder(employeeAccount);
		builder.withId(1).withStatus(status).withToken(token)
		.withDeviceId(deviceId).withHomeClub(club)
		.withPrivilege(privileges).withAmenity(amenity);
		builder.withUserId(userIdOrMobileNumber);
		final AccountDto account = builder.getBuiltInstance();
		return account;
	}

	@Before
	public void setUp() {
		service = new AccountService();
		ReflectionTestUtils.setField(service, "clubDao", clubDao);
		ReflectionTestUtils.setField(service, "accountDao", accountDao);
		final DbDateDao dbDateDao = mock(DbDateDao.class);
		ReflectionTestUtils.setField(service, "dbDateDao", dbDateDao);
		given(dbDateDao.getNow()).willReturn(new Date());
	}

	private void setupActivateAccountTest(final String activationToken,
			final String userId, final String deviceId,
			final AccountDto account, final boolean throwNotFound)
					throws NotFoundException, InvalidParameterException {
		if (!throwNotFound) {
			given(
					accountDao.fetchByToken(eq(activationToken), eq(userId),
							eq(deviceId))).willReturn(account);
		} else {
			given(
					accountDao.fetchByToken(eq(activationToken), eq(userId),
							eq(deviceId))).willThrow(NotFoundException.class);
		}
	}

	private void setupAddAmenityToUserTest(final String userId,
			final String clubAmenityId,
			final boolean throwNotFoundByAccountDao,
			final boolean throwNotFoundByClubDao, final Integer userPkId,
			final Integer clubAmenityPkId) throws NotFoundException {
		if (throwNotFoundByAccountDao) {
			given(accountDao.getUserIdPkByUserId(userId)).willThrow(
					NotFoundException.class);
		} else if (throwNotFoundByClubDao) {
			given(clubDao.getClubAmenityIdPk(clubAmenityId)).willThrow(
					NotFoundException.class);
		} else {
			if (userPkId != null) {
				given(accountDao.getUserIdPkByUserId(eq(userId))).willReturn(
						userPkId);
			}
			if (clubAmenityPkId != null) {
				given(clubDao.getClubAmenityIdPk(clubAmenityId)).willReturn(
						clubAmenityPkId);
			}

		}
	}

	private void setupCreateAccountTest(final AccountDto account,
			final boolean accountExists) throws NotFoundException,
			InvalidParameterException {
		setupCreateAccountTest(account, accountExists, false);
	}

	private void setupCreateAccountTest(final AccountDto account,
			final boolean accountExists, final boolean throwAmenityNotFound)
					throws NotFoundException, InvalidParameterException {
		if (accountExists) {
			given(accountDao.fetch(account.getUserId())).willReturn(account);
		} else {
			given(accountDao.fetch(account.getUserId())).willThrow(
					NotFoundException.class);
		}
		if (account instanceof EmployeeAccountDto) {
			final EmployeeAccountDto employeeAccountInfo = (EmployeeAccountDto) account;
			if (throwAmenityNotFound) {
				given(
						clubDao.getClubAmenityIdPk(employeeAccountInfo
								.getAmenity().getAmenityId())).willThrow(
										NotFoundException.class);
			} else if (employeeAccountInfo.getAmenity() != null) {
				final String amenityId = employeeAccountInfo.getAmenity()
						.getAmenityId();
				final Integer amenityPkId = employeeAccountInfo.getAmenity().getId();
				given(clubDao.getClubAmenityIdPk(amenityId)).willReturn(
						amenityPkId);
				final AmenityDto amenity = employeeAccountInfo.getAmenity();
				when(clubDao.getAmenity(amenityPkId)).thenReturn(amenity);
			}

		}
	}

	// //////activateAccount
	@Test(expected = InvalidParameterException.class)
	public void test001ActivateAccount_tokenNull() throws NotFoundException,
	InvalidParameterException {
		final String activationToken = null;
		service.activateAccount(activationToken);
	}

	@Test(expected = InvalidParameterException.class)
	public void test002ActivateAccount_tokenInvalid()
			throws InvalidParameterException {
		final String activationToken = "not valid";
		service.activateAccount(activationToken);
	}

	@Test
	public void test003ActivateAccount_memberTokenValid()
			throws NotFoundException, InvalidParameterException {
		final String deviceId = "device";
		final String mobileNumber = null;
		final boolean employeeAccount = true;
		final boolean expiredToken = false;
		final AccountDto account = createAccount(employeeAccount,
				AccountStatus.New, deviceId, Privilege.user, expiredToken,
				mobileNumber);
		final String activationToken = account.getToken().getToken();

		setupActivateAccountTest(activationToken, null, null, account, false);
		service.activateAccount(activationToken);
		verify(accountDao, times(1)).activate(eq(account.getUserId()),
				eq(activationToken));

	}

	@Test(expected = InvalidParameterException.class)
	public void test003ActivateAccount_mobileTokenValid()
			throws NotFoundException, InvalidParameterException {
		final String deviceId = "device";
		final String mobileNumber = null;

		final boolean employeeAccount = false;
		final boolean expiredToken = false;
		final AccountDto account = createAccount(employeeAccount,
				AccountStatus.New, deviceId, Privilege.user, expiredToken,
				mobileNumber);
		final String activationToken = account.getToken().getToken();
		setupActivateAccountTest(activationToken, account.getId().toString(),
				deviceId, account, false);
		service.activateAccount(activationToken);
	}

	@Test(expected = InvalidParameterException.class)
	public void test004ActivateAccount_daoThrowsNotFound()
			throws NotFoundException, InvalidParameterException {
		final String deviceId = "device";
		final String mobileNumber = null;

		final boolean employeeAccount = false;
		final boolean expiredToken = false;
		final AccountDto account = createAccount(employeeAccount,
				AccountStatus.New, deviceId, Privilege.user, expiredToken,
				mobileNumber);
		final String activationToken = account.getToken().getToken();
		setupActivateAccountTest(activationToken, null, null, account, true);
		service.activateAccount(activationToken);
	}

	@Test(expected = InvalidParameterException.class)
	public void test011ActivateMobileAccount_deviceIdNull()
			throws NotFoundException, InvalidParameterException {
		final String mobileNumber = "12345";
		final String deviceId = null;
		final String activationToken = "token";
		service.activateMobileAccount(mobileNumber, deviceId, activationToken);
	}

	@Test(expected = InvalidParameterException.class)
	public void test011ActivateMobileAccount_mobileNumberNull()
			throws NotFoundException, InvalidParameterException {
		final String mobileNumber = null;
		final String deviceId = "device";
		final String activationToken = "token";

		service.activateMobileAccount(mobileNumber, deviceId, activationToken);
	}

	// //////activateMobileAccount
	// Case 1: Null token
	@Test(expected = InvalidParameterException.class)
	public void test011ActivateMobileAccount_tokenNull()
			throws NotFoundException, InvalidParameterException {
		final String mobileNumber = "12345";
		final String deviceId = "device";
		final String activationToken = null;

		service.activateMobileAccount(mobileNumber, deviceId, activationToken);
	}

	// Case 2: Invalid token
	@Test(expected = InvalidParameterException.class)
	public void test012ActivateMobileAccount_tokenInvalid()
			throws NotFoundException, InvalidParameterException {
		final String mobileNumber = "12345";
		final String deviceId = "device";
		final String activationToken = "not valid";

		service.activateMobileAccount(mobileNumber, deviceId, activationToken);
	}

	// Case 3: Member account valid token (Good case)
	@Test
	public void test013ActivateMobileAccount_memberTokenValid()
			throws NotFoundException, InvalidParameterException {
		final String mobileNumber = "12345";
		final String deviceId = "device";
		final boolean employeeAccount = false;
		final boolean expiredToken = false;
		final AccountDto account = createAccount(employeeAccount,
				AccountStatus.New, deviceId, Privilege.user, expiredToken,
				mobileNumber);
		final String activationToken = account.getToken().getToken();
		setupActivateAccountTest(activationToken, mobileNumber, deviceId,
				account, false);
		service.activateMobileAccount(mobileNumber, deviceId, activationToken);
		verify(accountDao, times(1)).activate(eq(account.getUserId()),
				eq(activationToken));

	}

	// Case 3a: valid token, member account is already activated
	@Test
	public void test013ActivateMobileAccount_mobileTokenValid()
			throws NotFoundException, InvalidParameterException {
		final String mobileNumber = "12345";
		final String deviceId = "device";
		final boolean employeeAccount = false;
		final boolean expiredToken = false;
		final AccountDto account = createAccount(employeeAccount,
				AccountStatus.New, deviceId, Privilege.user, expiredToken,
				mobileNumber);
		final String activationToken = account.getToken().getToken();
		setupActivateAccountTest(activationToken, mobileNumber, deviceId,
				account, false);
		service.activateMobileAccount(mobileNumber, deviceId, activationToken);
		verify(accountDao, times(1)).activate(eq(mobileNumber),
				eq(activationToken));
	}

	// Case 4: Dao throws not found (fetchByToken)
	@Test(expected = InvalidParameterException.class)
	public void test014ActivateMobileAccount_daoThrowsNotFound()
			throws NotFoundException, InvalidParameterException {
		final String mobileNumber = "12345";
		final String deviceId = "device";
		final boolean employeeAccount = false;
		final boolean expiredToken = false;
		final AccountDto account = createAccount(employeeAccount,
				AccountStatus.New, deviceId, Privilege.user, expiredToken,
				mobileNumber);
		final String activationToken = account.getToken().getToken();
		setupActivateAccountTest(activationToken, null, null, account, true);
		service.activateMobileAccount(mobileNumber, deviceId, activationToken);
	}

	// Case 5: Trying to call activeMobileAccount when an account is an employee
	@Test(expected = InvalidParameterException.class)
	public void test015ActivateMobileAccount_employeeTokenValid()
			throws NotFoundException, InvalidParameterException {
		final String mobileNumber = "12345";
		final String deviceId = "device";
		final boolean employeeAccount = true;
		final boolean expiredToken = false;
		final AccountDto account = createAccount(employeeAccount,
				AccountStatus.New, deviceId, Privilege.user, expiredToken,
				mobileNumber);
		final String activationToken = account.getToken().getToken();

		setupActivateAccountTest(activationToken, mobileNumber, deviceId,
				account, false);
		service.activateMobileAccount(mobileNumber, deviceId, activationToken);
	}

	// ***** addAmenityToUser
	// Case 1: Invalid USER ID, null
	@Test(expected = InvalidParameterException.class)
	public void test016AddAmenityToUser_InvalidNullUserId()
			throws NotFoundException, InvalidParameterException {
		final String userId = null;
		final Integer userPkId = null;
		final String clubAmenityId = "whatever";
		final Integer clubAmenityPkId = 1;

		setupAddAmenityToUserTest(userId, clubAmenityId, false, false,
				userPkId, clubAmenityPkId);
		service.addAmenityToUser(userId, clubAmenityId);
		verify(accountDao, never()).addAmenityToUser(eq(userPkId),
				eq(clubAmenityPkId));
	}

	@Test(expected = NotFoundException.class)
	public void test016AddAmenityToUser_InvalidUserId()
			throws NotFoundException, InvalidParameterException {
		final String userId = "whatever";
		final Integer userPkId = null;
		final String clubAmenityId = "whatever";
		final Integer clubAmenityPkId = 1;
		setupAddAmenityToUserTest(userId, clubAmenityId, true, false, userPkId,
				clubAmenityPkId);
		service.addAmenityToUser(userId, clubAmenityId);
		verify(accountDao, never()).addAmenityToUser(eq(userPkId),
				eq(clubAmenityPkId));
	}

	// Case 2: Invalid clubAmenityId
	@Test(expected = NotFoundException.class)
	public void test017AddAmenityToUser_InvalidClubAmenityId()
			throws NotFoundException, InvalidParameterException {
		final String userId = "whatever";
		final Integer userPkId = 1;
		final String clubAmenityId = "whatever";
		final Integer clubAmenityPkId = 1;

		setupAddAmenityToUserTest(userId, clubAmenityId, false, true, userPkId,
				clubAmenityPkId);
		service.addAmenityToUser(userId, clubAmenityId);
		verify(accountDao, never()).addAmenityToUser(eq(userPkId),
				eq(clubAmenityPkId));
	}

	@Test(expected = InvalidParameterException.class)
	public void test017AddAmenityToUser_InvalidNullClubAmenityId()
			throws NotFoundException, InvalidParameterException {
		final String userId = "whatever";
		final Integer userPkId = 1;
		final String clubAmenityId = null;
		final Integer clubAmenityPkId = 1;
		setupAddAmenityToUserTest(userId, clubAmenityId, false, false,
				userPkId, clubAmenityPkId);
		service.addAmenityToUser(userId, clubAmenityId);
		verify(accountDao, never()).addAmenityToUser(eq(userPkId),
				eq(clubAmenityPkId));
	}

	// Case 3: Good case, verify addLinkAmenityUserId is called.
	@Test
	public void test018AddAmenityToUser_GoodCase() throws NotFoundException,
	InvalidParameterException {
		final String userId = "whatever";
		final Integer userPkId = 1;
		final String clubAmenityId = "whatever";
		final Integer clubAmenityPkId = 1;
		setupAddAmenityToUserTest(userId, clubAmenityId, false, false,
				userPkId, clubAmenityPkId);
		service.addAmenityToUser(userId, clubAmenityId);
		verify(accountDao, times(1)).addAmenityToUser(eq(userPkId),
				eq(clubAmenityPkId));
	}

	// ***** createAccount
	// Case 1: accountInfo param is null
	@Test(expected = InvalidParameterException.class)
	public void test019CreateAccount_employeeAccountInfoNull()
			throws NotFoundException, AccountCreationException,
			InvalidParameterException {
		final EmployeeAccountDto accountInfo = null;
		service.createAccount(accountInfo);
	}

	// Case 2a: employee account exists with Activated status
	@Test(expected = AccountCreationException.class)
	public void test020CreateAccount_04_employeeAccountInfoExistedActivatedStatusExpired_AccountCreationExceptionThrown()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.Activated;
		final boolean expiredToken = true;
		final boolean accountExists = true;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");

		setupCreateAccountTest(account, accountExists);
		final ActivationToken returnedToken = service.createAccount(account);
	}

	@Test(expected = AccountCreationException.class)
	public void test020CreateAccount_04_employeeAccountInfoExistedActivatedStatusNotExpired_AccountCreationExceptionThrown()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.Activated;
		final boolean expiredToken = false;
		final boolean accountExists = true;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");

		setupCreateAccountTest(account, accountExists);
		final ActivationToken returnedToken = service.createAccount(account);
	}

	// Case 2b: employee account exists with Terminated status
	@Test(expected = AccountCreationException.class)
	public void test020CreateAccount_05_employeeAccountInfoExistedTerminatedStatusExpired_AccountCreationExceptionThrown()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.Terminated;
		final boolean expiredToken = true;
		final boolean accountExists = true;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");

		setupCreateAccountTest(account, accountExists);
		final ActivationToken returnedToken = service.createAccount(account);
	}

	@Test(expected = AccountCreationException.class)
	public void test020CreateAccount_05_employeeAccountInfoExistedTerminatedStatusNotExpired_AccountCreationExceptionThrown()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.Terminated;
		final boolean expiredToken = false;
		final boolean accountExists = true;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");

		setupCreateAccountTest(account, accountExists);
		final ActivationToken returnedToken = service.createAccount(account);
	}

	// Case 2c: employee account exists with Cancelled status
	@Test(expected = AccountCreationException.class)
	public void test020CreateAccount_06_employeeAccountInfoExistedCancelledStatusExpired_AccountCreationExceptionThrown()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.Cancelled;
		final boolean expiredToken = true;
		final boolean accountExists = true;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");

		setupCreateAccountTest(account, accountExists);
		final ActivationToken returnedToken = service.createAccount(account);
	}

	@Test(expected = AccountCreationException.class)
	public void test020CreateAccount_06_employeeAccountInfoExistedCancelledStatusNotExpired_AccountCreationExceptionThrown()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.Cancelled;
		final boolean expiredToken = false;
		final boolean accountExists = true;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");

		setupCreateAccountTest(account, accountExists);
		final ActivationToken returnedToken = service.createAccount(account);
	}

	@Test
	public void test020CreateAccount_07_employeeAccountInfoExistedNewStatusExpired()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.New;
		final boolean expiredToken = true;
		final boolean accountExists = true;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");

		setupCreateAccountTest(account, accountExists);
		final ActivationToken returnedToken = service.createAccount(account);
		verify(accountDao, never()).newAccount(eq(account),
				any(ActivationToken.class));
		verify(accountDao, atLeastOnce()).fetch(account.getUserId());
		verify(accountDao, times(1)).updateNewToken(returnedToken);
		Assert.assertNotEquals("whatever", returnedToken.getToken());
	}

	// Case 2d: employee account exists with New status
	@Test
	public void test020CreateAccount_07_employeeAccountInfoExistedNewStatusNotExpired()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.New;
		final boolean expiredToken = false;
		final boolean accountExists = true;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");

		setupCreateAccountTest(account, accountExists);
		final ActivationToken returnedToken = service.createAccount(account);
		verify(accountDao, never()).newAccount(eq(account),
				any(ActivationToken.class));
		verify(accountDao, atLeastOnce()).fetch(account.getUserId());
		Assert.assertEquals("whatever", returnedToken.getToken());
	}

	// Case 2e: employee account exists with TokenSent status
	@Test
	public void test020CreateAccount_08_employeeAccountInfoExistedTokenSentStatusExpired()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.TokenSent;
		final boolean expiredToken = true;
		final boolean accountExists = true;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");

		setupCreateAccountTest(account, accountExists);
		final ActivationToken returnedToken = service.createAccount(account);
		verify(accountDao, never()).newAccount(eq(account),
				any(ActivationToken.class));
		verify(accountDao, atLeastOnce()).fetch(account.getUserId());
		verify(accountDao, times(1)).updateNewToken(returnedToken);
		Assert.assertNotEquals("whatever", returnedToken.getToken());
	}

	@Test
	public void test020CreateAccount_08_employeeAccountInfoExistedTokenSentStatusNotExpired()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.TokenSent;
		final boolean expiredToken = false;
		final boolean accountExists = true;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");

		setupCreateAccountTest(account, accountExists);
		final ActivationToken returnedToken = service.createAccount(account);
		verify(accountDao, never()).newAccount(account, account.getToken());
		verify(accountDao, atLeastOnce()).fetch(account.getUserId());
		Assert.assertEquals("whatever", returnedToken.getToken());
	}

	private void test020CreateAccount_employeeAccountInfoExistedStatus(
			final AccountStatus status,
			final boolean expectAccountCreateException,
			final boolean expiredToken, final boolean employeeAccount,
			final boolean accountExists) {
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");
		try {
			setupCreateAccountTest(account, accountExists);
			final ActivationToken returnedToken = service
					.createAccount(account);
			if (expectAccountCreateException) {
				Assert.fail("AccountCreationException should have throw!");
			} else {
				verify(accountDao, never()).newAccount(account,
						account.getToken());
				verify(accountDao, atLeastOnce()).fetch(account.getUserId());
				if (expiredToken) {
					verify(accountDao, times(1)).updateNewToken(returnedToken);
					Assert.assertNotEquals("whatever", returnedToken.getToken());
				} else {
					if (accountExists) {
						Assert.assertEquals("whatever",
								returnedToken.getToken());
					}
				}
			}
		} catch (final InvalidParameterException e) {
			Assert.fail("InvalidParameterException should not have throw!");
		} catch (final NotFoundException e) {
			Assert.fail("NotFoundException should not have throw!");
		} catch (final AccountCreationException e) {
			if (!expectAccountCreateException) {
				Assert.fail("AccountCreationException should not have throw!");
			}
		}
	}

	// Case 3: employee account does not exists...
	@Test
	public void test021CreateAccount_01_employeeAccountInfoNotExisted()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.New;
		final boolean expiredToken = false;
		final boolean accountExists = false;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid");

		setupCreateAccountTest(account, accountExists);
		final ActivationToken returnedToken = service.createAccount(account);
		verify(accountDao, times(1)).newAccount(account, returnedToken);
		verify(accountDao, atLeastOnce()).fetch(account.getUserId());
		Assert.assertNotEquals("whatever", returnedToken.getToken());
	}

	// Case 3a: employee account with amenity (invalid/non existent amenity)
	@Test(expected = NotFoundException.class)
	public void test021CreateAccount_02_employeeAccountInfoNotExisted_invalidAmenityId()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.New;
		final boolean expiredToken = false;
		final boolean accountExists = false;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid", "junkamenity");

		setupCreateAccountTest(account, accountExists, true);
		final ActivationToken returnedToken = service.createAccount(account);
	}

	// Case 3b: employee account with amenity (valid amenity)
	@Test
	public void test021CreateAccount_02_employeeAccountInfoNotExisted_validAmenityId()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.New;
		final boolean expiredToken = false;
		final boolean accountExists = false;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid", "junkamenity");

		setupCreateAccountTest(account, accountExists, false);
		final ActivationToken returnedToken = service.createAccount(account);
		verify(clubDao, times(1)).getAmenity(
				eq(((EmployeeAccountDto) account).getAmenity().getId()));
	}
	// Case 3c: employee account with no amenity
	@Test
	public void test021CreateAccount_03_employeeAccountInfoNotExisted_noAmenity()
			throws NotFoundException, InvalidParameterException,
			AccountCreationException {
		final boolean employeeAccount = true;
		final AccountStatus status = AccountStatus.New;
		final boolean expiredToken = false;
		final boolean accountExists = false;
		final AccountDto account = createAccount(employeeAccount, status,
				"device", Privilege.user, expiredToken, "userid", null);

		setupCreateAccountTest(account, accountExists, false);
		final ActivationToken returnedToken = service.createAccount(account);
		verify(clubDao, never()).getAmenity(anyInt());
	}

	// Case 4: member account already exists...
	// Case 4a: member account exists with Activated status
	// Case 4b: member account exists with Terminated status
	// Case 4c: member account exists with Cancelled status
	// Case 4d: member account exists with New status
	// Case 4e: member account exists with TokenSent status
	// Case 5: member account does not exists...

	// ***** deactivateAccount
	// case 1: uerId is null
	// case 2: non existent userId
	// case 3: valid userId

	// ***** exists
	// case 1: userId null
	// case 2: non existent userId
	// case 3: valid userId employee account
	// case 4: valid userId member account

	// ***** get
	// case 1: userId is null
	// case 2: userId of a mobile account
	// case 3: userId of an employee account

	// ***** updateAccountInfo
	// case 1: userId is null
	// case 2: non existent userId
	// case 3: userId valid employee, update full name
	// case 4: userId valid employee, update email
	// case 5: userId valid employee, update base64
	// case 6: userId valid employee, update deviceId
	// case 7: userId valid member, update full name
	// case 8: userId valid member, update email
	// case 9: userId valid member, update base64
	// case 10: userId valid member, update deviceId

	// ***** updateUserTangerineHandSetId
	// case 1: userId is null
	// case 2: deviceId is null
	// case 3: tangerineHandsetId is null

}