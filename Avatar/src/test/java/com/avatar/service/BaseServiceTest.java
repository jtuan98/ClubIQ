package com.avatar.service;

import static org.mockito.Mockito.mock;

import com.avatar.dao.AccountDao;
import com.avatar.dao.ClubDao;

public abstract class BaseServiceTest {
	protected AccountDao accountDao = mock(AccountDao.class);

	protected ClubDao clubDao = mock(ClubDao.class);

}
