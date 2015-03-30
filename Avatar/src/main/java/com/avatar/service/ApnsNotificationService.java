package com.avatar.service;

import java.io.InputStream;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.avatar.business.NotificationBusiness;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.MobileAccountDto;
import com.avatar.exception.AccountNotificationException;
import com.avatar.exception.InvalidDeviceId;

@Service
public class ApnsNotificationService implements NotificationBusiness {

	@Resource(name = "apnsCertificateP12")
	private String apnsServerCertificate;

	@Resource(name = "apnsCertificateP12Password")
	private String apnsServerCertificatePassword;

	@Override
	public boolean sendNotification(final AccountDto account)
			throws AccountNotificationException {
		boolean retVal = true;
		try {
			final InputStream p12is = this.getClass().getClassLoader()
					.getResourceAsStream(apnsServerCertificate);
			final MobileAccountDto mobileAccount = (MobileAccountDto) account;
			final PushedNotifications notifications = Push.alert(
					"Your activation pin is "
							+ mobileAccount.getToken().getToken(), p12is,
					apnsServerCertificatePassword, true,
					mobileAccount.getDeviceId());
			for (final PushedNotification notification : notifications) {
				if (notification.isSuccessful()) {
					/* Apple accepted the notification and should deliver it */
					System.out
							.println("Apple accepted the notification and should deliver it");
				} else {
					final String invalidToken = notification.getDevice().getToken();
					/* Add code here to remove invalidToken from your database */
					System.out.println("invalidDeviceId: " + invalidToken);
					throw new InvalidDeviceId("InvalidDeviceId: " + invalidToken);
				}
			}
		} catch (CommunicationException | KeystoreException e) {
			retVal = false;
			throw new AccountNotificationException(e.getMessage());
		}
		return retVal;
	}

}
