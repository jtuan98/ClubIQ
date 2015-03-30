package com.avatar.business;

import com.avatar.dto.account.AccountDto;
import com.avatar.exception.AccountNotificationException;

public interface NotificationBusiness {
	boolean sendNotification (AccountDto account) throws AccountNotificationException;
}
