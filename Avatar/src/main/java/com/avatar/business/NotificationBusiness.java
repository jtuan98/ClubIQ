package com.avatar.business;

import com.avatar.dto.account.AccountDto;
import com.avatar.exception.NotificationException;

public interface NotificationBusiness {
	boolean sendAlert(AccountDto staffAccount, AccountDto memberAccount)
			throws NotificationException;

	boolean sendNotification(AccountDto account) throws NotificationException;

	boolean sendNotification(String deviceId, String msg, boolean staff)
			throws NotificationException;

	public boolean testAlert(final String deviceId, final boolean staff)
			throws NotificationException;
}
