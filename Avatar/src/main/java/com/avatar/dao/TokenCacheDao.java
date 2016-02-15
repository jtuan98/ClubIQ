package com.avatar.dao;

import java.util.Date;

import com.avatar.dto.account.AccountDto;
import com.avatar.exception.NotFoundException;

public interface TokenCacheDao {
	int fetchAccountIdPk (String token) throws NotFoundException;

	void persist (String token, Date validTill, AccountDto account) throws NotFoundException;

	void removeAll();
}
