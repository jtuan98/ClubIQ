package com.avatar.dao.impl.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.avatar.dao.AccountDao;
import com.avatar.dao.BeaconDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.TruncateDao;
import com.avatar.dto.AccountDtoBuilder;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.account.MemberAccountDto;
import com.avatar.dto.enums.AccountStatus;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.InvalidPasswordException;
import com.avatar.exception.NotFoundException;
import com.avatar.util.Md5Sum;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class AccountDaoTest {
	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = true)
	static class MySqlContextConfiguration extends
	MySqlLocalJdbcContextConfiguration {
	}

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDaoJdbc;

	@Resource(name = "beaconDaoJdbc")
	private BeaconDao beaconDaoJdbc;

	@Resource(name = "accountDaoJdbc")
	private TruncateDao accountTruncateDaoJdbc;

	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDaoJdbc;

	@Before
	public void cleanup() throws NotFoundException {
		accountTruncateDaoJdbc.truncate();

		beaconDaoJdbc.setSubAmenityId("unittest club", "unittest apns toke",
				"Bar", "unittest subamenity");
	}

	@Test(expected = NotFoundException.class)
	public void testActivateCase01NonExistentAccountId()
			throws NotFoundException {
		accountDaoJdbc.activate("junk", "activationToken", new Date());
	}

	@Test(expected = NotFoundException.class)
	public void testActivateCase02ExistentMemberAccountIdInvalidToken()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(true)
		.withUserId("theUserId").withName("Unit Test")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.activate(account.getUserId(), "activationToken",
				new Date());
	}

	@Test
	public void testActivateCase03ExistentMemberAccountIdActivatedAccount()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(true)
		.withUserId("theUserId").withName("Unit Test")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.activate(account.getUserId(), account.getToken()
				.getToken(), new Date());
		accountDaoJdbc.activate(account.getUserId(), account.getToken()
				.getToken(), new Date());
	}

	@Test
	public void testActivateCase04ExistentMemberAccountId()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(true)
		.withUserId("theUserId").withName("Unit Test")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.activate(account.getUserId(), account.getToken()
				.getToken(), new Date());
		final AccountDto accountFroDb = accountDaoJdbc.fetch(account
				.getUserId());
		assertNotNull(accountFroDb);
		assertNotNull("Checking PK", accountFroDb.getId());
		assertEquals("Checking User id", account.getUserId(),
				accountFroDb.getUserId());
	}

	@Test(expected = NotFoundException.class)
	public void testAddNote01NonExistentuserPkId() throws NotFoundException {
		accountDaoJdbc.addNote(-1, "noteText", new DateTime());
	}

	@Test
	public void testAddNote02ExistentUser() throws NotFoundException,
	InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(true)
		.withUserId("theUserId").withName("Unit Test")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final int userIdPk = accountDaoJdbc.getUserIdPkByUserId(account
				.getUserId());
		accountDaoJdbc.addNote(userIdPk, "noteText", new DateTime());
		// Verify if note is added
		final AccountDto accountFromDb = accountDaoJdbc.fetch(userIdPk);
		assertNotNull(accountFromDb);
		assertNotNull(accountFromDb.getNoteHistory());
		assertTrue(CollectionUtils.isNotEmpty(accountFromDb.getNoteHistory()));
		assertEquals(1, accountFromDb.getNoteHistory().size());
		assertEquals("noteText", accountFromDb.getNoteHistory().get(0)
				.getNoteText());
	}

	@Test(expected = NotFoundException.class)
	public void testAddSubAmenityToUser01NonExistentUser()
			throws NotFoundException, InvalidParameterException {
		final Integer clubSubAmenityIdPk = 1;
		accountDaoJdbc.addSubAmenityToUser(-1, clubSubAmenityIdPk);
	}

	@Test(expected = NotFoundException.class)
	public void testAddSubAmenityToUser02NonExistentSubAmenityId()
			throws NotFoundException, InvalidParameterException {
		final Integer clubSubAmenityIdPk = -1;
		final AccountDto account = new AccountDtoBuilder(true)
		.withUserId("theUserId").withName("Unit Test")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final int userIdPk = accountDaoJdbc.getUserIdPkByUserId(account
				.getUserId());
		accountDaoJdbc.addSubAmenityToUser(userIdPk, clubSubAmenityIdPk);
	}

	@Test
	public void testAddSubAmenityToUser03() throws NotFoundException,
	InvalidParameterException {
		final Integer clubSubAmenityIdPk = clubDaoJdbc.getClubSubAmenityIdPk(1,
				"unittest subamenity");
		final AccountDto account = new AccountDtoBuilder(true)
		.withUserId("theUserId").withName("Unit Test")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final int userIdPk = accountDaoJdbc.getUserIdPkByUserId(account
				.getUserId());
		accountDaoJdbc.addSubAmenityToUser(userIdPk, clubSubAmenityIdPk);
		final EmployeeAccountDto acct = (EmployeeAccountDto) accountDaoJdbc
				.fetch(userIdPk);
		assertNotNull(acct);
		assertNotNull(acct.getSubAmenity());
		assertEquals(clubSubAmenityIdPk, acct.getSubAmenity().getId());
	}

	@Test(expected = NotFoundException.class)
	public void testDeactivate01NonExistentAccount() throws NotFoundException {
		accountDaoJdbc.deactivate("junk", new Date());
	}

	@Test
	public void testDeactivate02DeactivatedAccount() throws NotFoundException,
	InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(true)
		.withUserId("theUserId").withName("Unit Test")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.deactivate(account.getUserId(), new Date());
		accountDaoJdbc.deactivate(account.getUserId(), new Date());
		final AccountDto acctFromDb = accountDaoJdbc.fetch(account.getUserId());
		assertNotNull(acctFromDb);
		assertEquals(AccountStatus.Cancelled, acctFromDb.getStatus());
	}

	@Test(expected = NotFoundException.class)
	public void testFetch01aNonExistingUserId() throws NotFoundException,
	InvalidParameterException {
		accountDaoJdbc.fetch("whatever");
	}

	@Test(expected = NotFoundException.class)
	public void testFetch01NonExistingUserId() throws NotFoundException,
	InvalidParameterException {
		accountDaoJdbc.fetch(-1);
	}

	@Test(expected = InvalidParameterException.class)
	public void testFetch02aNullUserId() throws NotFoundException,
	InvalidParameterException {
		accountDaoJdbc.fetch((String) null);
	}

	@Test(expected = InvalidParameterException.class)
	public void testFetch02NullUserId() throws NotFoundException,
	InvalidParameterException {
		accountDaoJdbc.fetch((Integer) null);
	}

	@Test
	public void testFetch03ValidEmployeeUserId() throws NotFoundException,
	InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(true)
		.withUserId("theUserId").withName("Unit Test")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final int userIdPk = accountDaoJdbc.getUserIdPkByUserId(account
				.getUserId());

		final AccountDto acctFromDb = accountDaoJdbc.fetch(userIdPk);
		assertNotNull(acctFromDb);
		assertEquals(userIdPk, acctFromDb.getId().intValue());
		assertTrue(acctFromDb instanceof EmployeeAccountDto);
		final AccountDto acct2FromDb = accountDaoJdbc
				.fetch(account.getUserId());
		assertNotNull(acct2FromDb);
		assertEquals(userIdPk, acct2FromDb.getId().intValue());
		assertTrue(acct2FromDb instanceof EmployeeAccountDto);
	}

	@Test
	public void testFetch04MemberUserId() throws NotFoundException,
	InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("device123")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final int userIdPk = accountDaoJdbc.getUserIdPkByUserId(account
				.getUserId());

		final AccountDto acctFromDb = accountDaoJdbc.fetch(userIdPk);
		assertNotNull(acctFromDb);
		assertEquals(userIdPk, acctFromDb.getId().intValue());
		assertTrue(acctFromDb instanceof MemberAccountDto);
		final AccountDto acct2FromDb = accountDaoJdbc
				.fetch(account.getUserId());
		assertNotNull(acct2FromDb);
		assertEquals(userIdPk, acct2FromDb.getId().intValue());
		assertTrue(acct2FromDb instanceof MemberAccountDto);
	}

	@Test(expected = InvalidParameterException.class)
	public void testNewAccount01MemberUserIdNoMobileNumber()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
	}

	@Test(expected = InvalidParameterException.class)
	public void testNewAccount02MemberUserIdNoDeviceId()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDefaultToken(false)
		.getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
	}

	@Test(expected = InvalidParameterException.class)
	public void testPopulateAccountInfo01NullAccount()
			throws NotFoundException, InvalidParameterException {
		accountDaoJdbc.populateAccountInfo(null, true);
	}

	@Test(expected = NotFoundException.class)
	public void testPopulateAccountInfo02AccountNoId()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345")
		.withPicture(1, "12345", "picture".getBytes())
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.populateAccountInfo(account, true);
	}

	@Test
	public void testPopulateAccountInfo03Picture() throws NotFoundException,
	InvalidParameterException, IOException {
		final byte[] pic = com.avatar.util.FileUtils.readAndClose(ClassLoader
				.getSystemResourceAsStream("test/Clubhouse.png"));
		final String hash = Md5Sum.hashString(pic);

		final String pictureBase64 = Base64.encodeBase64String(pic);
		final String hashBase64Decoded = Md5Sum
				.hashStringBase64Data(pictureBase64);
		assertEquals(hash, hashBase64Decoded);

		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withPicture(1, "12345", pic)
		.withDeviceId("deviceId").withDefaultToken(false)
		.getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		accountDaoJdbc.populateAccountInfo(accountFromDb, true);
		assertNotNull(accountFromDb.getPicture());
		final String hashFromDb = Md5Sum.hashString(accountFromDb.getPicture()
				.getPicture());
		assertEquals(hash, hashFromDb);
	}

	@Test(expected = InvalidParameterException.class)
	public void testUndeactivate01NullAccount() throws NotFoundException,
	InvalidParameterException {
		accountDaoJdbc.undeactivate(null);
	}

	@Test(expected = NotFoundException.class)
	public void testUndeactivate02NonExistentAccount()
			throws NotFoundException, InvalidParameterException {
		accountDaoJdbc.undeactivate("whatever");
	}

	@Test
	public void testUndeactivate03ExistingAccountNew()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		accountDaoJdbc.undeactivate(account.getUserId());
		final AccountStatus prevStatus = accountDaoJdbc
				.getPreviousStatus(accountFromDb.getId());
		assertNull(prevStatus);
		final AccountStatus status = accountDaoJdbc.getStatus(accountFromDb
				.getId());
		assertNotNull(status);
		assertEquals(AccountStatus.New, status);
	}

	@Test
	public void testUndeactivate04ExistingAccountActive()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.activate(account.getUserId(), account.getToken()
				.getToken(), new Date());

		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		accountDaoJdbc.undeactivate(account.getUserId());
		final AccountStatus prevStatus = accountDaoJdbc
				.getPreviousStatus(accountFromDb.getId());
		assertNull(prevStatus);
		final AccountStatus status = accountDaoJdbc.getStatus(accountFromDb
				.getId());
		assertNotNull(status);
		assertEquals(AccountStatus.Activated, status);
	}

	@Test
	public void testUndeactivate05ExistingAccountDeactive()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.activate(account.getUserId(), account.getToken()
				.getToken(), new Date());
		accountDaoJdbc.deactivate(account.getUserId(), new Date());
		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		assertNotNull(accountFromDb.getSusDate());
		AccountStatus prevStatus = accountDaoJdbc
				.getPreviousStatus(accountFromDb.getId());
		assertEquals(AccountStatus.Activated, prevStatus);
		AccountStatus status = accountDaoJdbc.getStatus(accountFromDb.getId());
		assertNotNull(status);
		assertEquals(AccountStatus.Cancelled, status);

		accountDaoJdbc.undeactivate(account.getUserId());
		prevStatus = accountDaoJdbc.getPreviousStatus(accountFromDb.getId());
		assertEquals(AccountStatus.Activated, prevStatus);
		status = accountDaoJdbc.getStatus(accountFromDb.getId());
		assertNotNull(status);
		assertEquals(AccountStatus.Activated, status);
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateAccountInfoEmail01aNullUserId()
			throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateAccountInfoEmail(null, "email");
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateAccountInfoEmail01bNullEmail()
			throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateAccountInfoEmail("junk", null);
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateAccountInfoEmail02NonExistentUserId()
			throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateAccountInfoEmail("junk", "email");
	}

	@Test
	public void testUpdateAccountInfoEmail03ExistentUserId()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.updateAccountInfoEmail(account.getUserId(),
				"email@whatever.com");
		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		assertNotNull(accountFromDb.getEmail());
		assertEquals("email@whatever.com", accountFromDb.getEmail());
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateAccountInfoName01aNullUserId()
			throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateAccountInfoName(null, "fullName");
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateAccountInfoName01bNullFullName()
			throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateAccountInfoName("junk", null);
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateAccountInfoName02NonExistentUserId()
			throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateAccountInfoName("junk", "email");
	}

	@Test
	public void testUpdateAccountInfoName03ExistentUserId()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.updateAccountInfoName(account.getUserId(),
				"whatever name");
		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		assertNotNull(accountFromDb.getName());
		assertEquals("whatever name", accountFromDb.getName());
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateAccountInfoPicture01aNullUserId()
			throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateAccountInfoPicture(null, "image base 64");
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateAccountInfoPicture01bNullImage()
			throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateAccountInfoPicture("junk", null);
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateAccountInfoPicture02NonExistentUserId()
			throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateAccountInfoPicture("junk", "image base 64");
	}

	@Test
	public void testUpdateAccountInfoPicture03ExistentUserIdNonExistImage()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.updateAccountInfoPicture(account.getUserId(),
				"whateverimagebase64");
		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		assertNotNull(accountFromDb.getPicture());
		assertEquals("whateverimagebase64", accountFromDb.getPicture()
				.getPictureAsBase64String());
	}

	@Test
	public void testUpdateAccountInfoPicture04ExistentUserIdExistedImage()
			throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withPicture(1, "12345", "picture".getBytes())
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.updateAccountInfoPicture(account.getUserId(),
				"whateverimagebase64");
		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		assertNotNull(accountFromDb.getPicture());
		assertEquals("whateverimagebase64", accountFromDb.getPicture()
				.getPictureAsBase64String());
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateNewToken01aNullToken()
			throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateNewToken(null);
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateNewToken01bNullTokenId()
			throws InvalidParameterException, NotFoundException {
		final ActivationToken token = new ActivationToken();
		accountDaoJdbc.updateNewToken(token);
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateNewToken01cNullTokenString()
			throws InvalidParameterException, NotFoundException {
		final ActivationToken token = new ActivationToken();
		token.setId(1);
		accountDaoJdbc.updateNewToken(token);
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateNewToken01dNullTokenExpirationDate()
			throws InvalidParameterException, NotFoundException {
		final ActivationToken token = new ActivationToken();
		token.setId(1);
		token.setToken("token");
		accountDaoJdbc.updateNewToken(token);
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateNewToken01eNonExistentToken()
			throws InvalidParameterException, NotFoundException {
		final ActivationToken token = new ActivationToken();
		token.setId(1);
		token.setToken("token");
		token.setExpirationDate(new Date());
		accountDaoJdbc.updateNewToken(token);
	}

	@Test
	public void testUpdateNewToken02ExistentToken() throws NotFoundException,
	InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withPicture(1, "12345", "picture".getBytes())
		.withDefaultToken(true).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final AccountDto accountFromDbBeforeUpdate = accountDaoJdbc
				.fetch(account.getUserId());
		final ActivationToken token = accountFromDbBeforeUpdate.getToken();
		assertEquals("ever", token.getToken());
		token.setToken("1234");
		accountDaoJdbc.updateNewToken(token);
		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		assertNotNull(accountFromDb.getToken());
		assertEquals("1234", accountFromDb.getToken().getToken());
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateNoticeInfo01NullDate() throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateNoticeInfo(-1, null, true);
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateNoticeInfo02NonExistentUserIdPk() throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateNoticeInfo(-1, new Date(), true);
	}

	@Test
	public void testUpdateNoticeInfo03ExistentUserId() throws NotFoundException,
	InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withPicture(1, "12345", "picture".getBytes())
		.withDefaultToken(true).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final AccountDto accountFromDbBeforeUpdate = accountDaoJdbc
				.fetch(account.getUserId());
		accountDaoJdbc.updateNoticeInfo(accountFromDbBeforeUpdate.getId(), new Date(), true);
		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		assertEquals(true, accountFromDb.isNoticedFlag());
		accountDaoJdbc.updateNoticeInfo(accountFromDbBeforeUpdate.getId(), new Date(), false);
		final AccountDto accountFromDb2 = accountDaoJdbc.fetch(account
				.getUserId());
		assertEquals(false, accountFromDb2.isNoticedFlag());
	}


	@Test(expected = InvalidParameterException.class)
	public void testUpdateUserDeviceId01aNullUserId() throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateUserDeviceId(null, "deviceId");
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateUserDeviceId01bNullDeviceId() throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateUserDeviceId("junk", null);
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateUserDeviceId02NonExistentUserId() throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateUserDeviceId("junk", "deviceId");
	}

	@Test
	public void testUpdateUserDeviceId03ExistentUserId() throws NotFoundException,
	InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withPicture(1, "12345", "picture".getBytes())
		.withDefaultToken(true).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final AccountDto accountFromDbBeforeUpdate = accountDaoJdbc
				.fetch(account.getUserId());
		accountDaoJdbc.updateUserDeviceId(accountFromDbBeforeUpdate.getUserId(), "deviceId1234");
		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		assertEquals("deviceId1234", accountFromDb.getDeviceId());
	}

	@Test(expected = InvalidParameterException.class)
	public void testUpdateUserTangerineHandSetId01aNullUserId() throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateUserTangerineHandSetId(null, "deviceId", "tangerineHandSetId");
	}

	@Test(expected = NotFoundException.class)
	public void testUpdateUserTangerineHandSetId02NonExistentUserId() throws InvalidParameterException, NotFoundException {
		accountDaoJdbc.updateUserTangerineHandSetId("junk", "deviceId", "tangerineHandSetId");
	}

	@Test
	public void testUpdateUserTangerineHandSetId03ExistentUserId() throws NotFoundException,
	InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withPicture(1, "12345", "picture".getBytes())
		.withDefaultToken(true).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final AccountDto accountFromDbBeforeUpdate = accountDaoJdbc
				.fetch(account.getUserId());
		accountDaoJdbc.updateUserTangerineHandSetId(accountFromDbBeforeUpdate.getUserId(), "deviceId1234", "tangerineHandSetId");
		final AccountDto accountFromDb = accountDaoJdbc.fetch(account
				.getUserId());
		assertEquals("deviceId1234", accountFromDb.getDeviceId());
		assertEquals("tangerineHandSetId", accountFromDb.getTangerineHandsetId());
		accountDaoJdbc.updateUserTangerineHandSetId(accountFromDbBeforeUpdate.getUserId(), "deviceId1234", "tangerineHandSetId2");
		final AccountDto accountFromDb2 = accountDaoJdbc.fetch(account
				.getUserId());
		assertEquals("deviceId1234", accountFromDb2.getDeviceId());
		assertEquals("tangerineHandSetId2", accountFromDb2.getTangerineHandsetId());
		accountDaoJdbc.updateUserTangerineHandSetId(accountFromDbBeforeUpdate.getUserId(), "deviceId1235", null);
		final AccountDto accountFromDb3 = accountDaoJdbc.fetch(account
				.getUserId());
		assertEquals("deviceId1235", accountFromDb3.getDeviceId());
		assertNull(accountFromDb3.getTangerineHandsetId());
	}


	@Test(expected = InvalidParameterException.class)
	public void testValidateUserIdPasswd01aNullUserId() throws InvalidParameterException, NotFoundException, InvalidPasswordException {
		accountDaoJdbc.validateUserIdPasswd(null, "passwd");
	}

	@Test(expected = InvalidParameterException.class)
	public void testValidateUserIdPasswd01bNullPassword() throws InvalidParameterException, NotFoundException, InvalidPasswordException {
		accountDaoJdbc.validateUserIdPasswd("userId", null);
	}

	@Test(expected = NotFoundException.class)
	public void testValidateUserIdPasswd02NonExistentUserId() throws InvalidParameterException, NotFoundException, InvalidPasswordException {
		accountDaoJdbc.validateUserIdPasswd("junk", "password");
	}

	@Test(expected=InvalidPasswordException.class)
	public void testValidateUserIdPasswd03ExistentUserIdInvalidPassword() throws NotFoundException,
	InvalidParameterException, InvalidPasswordException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withPicture(1, "12345", "picture".getBytes())
		.withDefaultToken(true).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final AccountDto accountFromDbBeforeUpdate = accountDaoJdbc
				.fetch(account.getUserId());
		accountDaoJdbc.validateUserIdPasswd(accountFromDbBeforeUpdate.getUserId(), "junk");
	}

	@Test(expected=InvalidPasswordException.class)
	public void testValidateUserIdPasswd04ExistentUserIdValidPasswordButAccountStatusNotActive() throws NotFoundException,
	InvalidParameterException, InvalidPasswordException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withPassword("pa22w0rd")
		.withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withPicture(1, "12345", "picture".getBytes())
		.withDefaultToken(true).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final AccountDto accountFromDbBeforeUpdate = accountDaoJdbc
				.fetch(account.getUserId());
		accountDaoJdbc.validateUserIdPasswd(accountFromDbBeforeUpdate.getUserId(), "pa22w0rd");
	}

	@Test
	public void testValidateUserIdPasswd05ExistentUserIdValidPasswordAccountStatusActive() throws NotFoundException,
	InvalidParameterException, InvalidPasswordException {
		final AccountDto account = new AccountDtoBuilder(false)
		.withUserId("theUserId").withPassword("pa22w0rd")
		.withName("Unit Test")
		.withMobileNumber("12345").withDeviceId("deviceId")
		.withPicture(1, "12345", "picture".getBytes())
		.withDefaultToken(true).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.activate(account.getUserId(), account.getToken().getToken(), new Date());
		final AccountDto accountFromDbBeforeUpdate = accountDaoJdbc
				.fetch(account.getUserId());
		accountDaoJdbc.validateUserIdPasswd(accountFromDbBeforeUpdate.getUserId(), "pa22w0rd");
	}
}
