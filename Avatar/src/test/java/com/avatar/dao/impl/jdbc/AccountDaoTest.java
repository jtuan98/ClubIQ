package com.avatar.dao.impl.jdbc;

import java.util.Date;

import javax.annotation.Resource;

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
import com.avatar.exception.NotFoundException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
@Transactional
@TransactionConfiguration(defaultRollback=true)
public class AccountDaoTest {
	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = true)
	static class MySqlContextConfiguration extends MySqlLocalJdbcContextConfiguration {
	}

	@Resource(name="accountDaoJdbc")
	private AccountDao accountDaoJdbc;

	@Resource(name="accountDaoJdbc")
	private TruncateDao accountTruncateDaoJdbc;

	@Before
	public void cleanup() {
		accountTruncateDaoJdbc.truncate();
	}

	@Test(expected=NotFoundException.class)
	public void testActivateExistentMemberAccountIdInvalidToken() throws NotFoundException {
		final AccountDto account = new AccountDtoBuilder(true).withUserId("theUserId")
				.withName("Unit Test")
				.withDefaultToken(false)
				.getBuiltInstance();
		accountDaoJdbc.newAccount(account, account.getToken());
		accountDaoJdbc.activate(account.getUserId(), "activationToken", new Date());
	}

	@Test(expected=NotFoundException.class)
	public void testActivateNonExistentAccountId() throws NotFoundException {
		accountDaoJdbc.activate("junk", "activationToken", new Date());
	}

}