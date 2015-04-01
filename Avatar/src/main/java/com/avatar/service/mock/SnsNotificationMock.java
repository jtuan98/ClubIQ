package com.avatar.service.mock;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.avatar.business.NotificationBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dto.account.AccountDto;
import com.avatar.exception.AccountNotificationException;
import com.avatar.exception.NotFoundException;

@Service
public class SnsNotificationMock implements NotificationBusiness {

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	@Override
	public boolean sendNotification(final AccountDto account) {
		try {
			accountDao.markStatusAsNotified(account.getUserId());
		} catch (final NotFoundException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean sendNotification(final String deviceId, final String msg)
			throws AccountNotificationException {
		// TODO Auto-generated method stub
		return false;
	}

}
