package com.avatar.service.mock;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.avatar.business.NotificationBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dto.account.AccountDto;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.NotificationException;

@Service
public class SnsNotificationMock implements NotificationBusiness {

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	@Override
	public boolean sendAlert(final AccountDto staffAccount,
			final AccountDto memberAccount) throws NotificationException {
		// TODO Auto-generated method stub
		return false;
	}

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
	public boolean sendNotification(final String deviceId, final String msg,
			final boolean staff) throws NotificationException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean testAlert(final String deviceId, final boolean staff)
			throws NotificationException {
		// TODO Auto-generated method stub
		return false;
	}

}
