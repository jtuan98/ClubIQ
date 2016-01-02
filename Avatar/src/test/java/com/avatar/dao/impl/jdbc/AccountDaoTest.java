package com.avatar.dao.impl.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.annotation.Resource;

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
import com.avatar.dao.TruncateDao;
import com.avatar.dto.AccountDtoBuilder;
import com.avatar.dto.account.AccountDto;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;

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

	@Resource(name = "accountDaoJdbc")
	private TruncateDao accountTruncateDaoJdbc;

	@Before
	public void cleanup() {
		accountTruncateDaoJdbc.truncate();
	}

	@Test(expected = NotFoundException.class)
	public void testActivateCase01NonExistentAccountId()
			throws NotFoundException {
		accountDaoJdbc.activate("junk", "activationToken", new Date());
	}

	@Test(expected = NotFoundException.class)
	public void testActivateCase02ExistentMemberAccountIdInvalidToken()
			throws NotFoundException {
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

	@Test (expected = NotFoundException.class)
	public void testAddNote01NonExistentuserPkId() throws NotFoundException {
		accountDaoJdbc.addNote(-1, "noteText", new DateTime());
	}

	@Test
	public void testAddNote02ExistentUser() throws NotFoundException, InvalidParameterException {
		final AccountDto account = new AccountDtoBuilder(true)
		.withUserId("theUserId").withName("Unit Test")
		.withDefaultToken(false).getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		final int userIdPk = accountDaoJdbc.getUserIdPkByUserId(account.getUserId());
		accountDaoJdbc.addNote(userIdPk, "noteText", new DateTime());
		//Verify if note is added
		final AccountDto accountFromDb = accountDaoJdbc.fetch(userIdPk);
		assertNotNull(accountFromDb);
		assertNotNull(accountFromDb.getNoteHistory());
		assertTrue(CollectionUtils.isNotEmpty(accountFromDb.getNoteHistory()));
		assertEquals(1, accountFromDb.getNoteHistory().size());
		assertEquals("noteText", accountFromDb.getNoteHistory().get(0).getNoteText());
	}
}