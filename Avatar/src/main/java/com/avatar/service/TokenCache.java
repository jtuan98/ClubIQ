package com.avatar.service;

import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import com.avatar.business.CacheBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dao.TokenCacheDao;
import com.avatar.dto.account.AccountDto;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;

@Service
public class TokenCache implements CacheBusiness<String, AccountDto> {

	@Resource(name = "tokenCacheDaoJdbc")
	private TokenCacheDao tokenCacheDao;

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	@Override
	public void clear() {
		tokenCacheDao.removeAll();
	}

	@Override
	public AccountDto get(final String guid) throws NotFoundException,
	InvalidParameterException {
		final int userIdPk = tokenCacheDao.fetchAccountIdPk(guid);
		final AccountDto retVal = accountDao.fetch(userIdPk);
		return retVal;
	}

	@Override
	public void put(final String token, final AccountDto account) throws NotFoundException {
		tokenCacheDao.persist(token, DateUtils.addHours(new Date(), 24), account);
	}

}
