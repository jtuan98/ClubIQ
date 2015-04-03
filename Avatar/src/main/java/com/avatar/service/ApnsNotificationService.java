package com.avatar.service;

import java.io.IOException;
import java.io.InputStream;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import com.avatar.business.NotificationBusiness;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.MobileAccountDto;
import com.avatar.exception.AccountNotificationException;
import com.avatar.exception.InvalidDeviceId;

@Service
public class ApnsNotificationService implements NotificationBusiness {

	@Resource(name = "apnsCertificateP12Staff")
	private String apnsServerCertificateStaff;

	@Resource(name = "apnsCertificateP12Member")
	private String apnsServerCertificateMember;

	@Resource(name = "apnsCertificateP12Password")
	private String apnsServerCertificatePassword;

	private byte[] staffP12Bytes = null;
	private byte[] memberP12Bytes = null;

	private void init() {
		if (staffP12Bytes == null) {
			staffP12Bytes = readP12(apnsServerCertificateStaff);
		}
		if (memberP12Bytes == null) {
			memberP12Bytes = readP12(apnsServerCertificateMember);
		}
	}

	private byte[] readP12(final String file) {
		byte[] retVal = null;
		final InputStream p12is = this.getClass().getClassLoader()
				.getResourceAsStream(file);
		try {
			retVal = IOUtils.toByteArray(p12is);
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				p12is.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return retVal;
	}

	@Override
	public boolean sendNotification(final AccountDto account)
			throws AccountNotificationException {
		boolean retVal = true;
		init();
		final MobileAccountDto mobileAccount = (MobileAccountDto) account;
		final String msg = "Your activation pin is "
				+ mobileAccount.getToken().getToken();
		try {
			final PushedNotifications notifications = Push.alert(msg,
					account.isStaff() ? staffP12Bytes : memberP12Bytes,
					apnsServerCertificatePassword, true,
					mobileAccount.getDeviceId());
			for (final PushedNotification notification : notifications) {
				if (notification.isSuccessful()) {
					/* Apple accepted the notification and should deliver it */
					System.out
							.println("Apple accepted the notification and should deliver it: "
									+ msg
									+ "[staff?"
									+ account.isStaff()
									+ "]");
				} else {
					final String invalidToken = notification.getDevice()
							.getToken();
					/* Add code here to remove invalidToken from your database */
					System.out.println("invalidDeviceId: " + invalidToken);
					throw new InvalidDeviceId("InvalidDeviceId: "
							+ invalidToken);
				}
			}
		} catch (CommunicationException | KeystoreException e) {
			e.printStackTrace();
			retVal = false;
			throw new AccountNotificationException(e.getMessage());
		}
		return retVal;
	}

	@Override
	public boolean sendNotification(final String deviceId, final String msg,
			final boolean staff) throws AccountNotificationException {
		boolean retVal = true;
		init();
		try {
			final PushedNotifications notifications = Push.alert(msg,
					staff ? staffP12Bytes : memberP12Bytes,
					apnsServerCertificatePassword, true, deviceId);
			for (final PushedNotification notification : notifications) {
				if (notification.isSuccessful()) {
					/* Apple accepted the notification and should deliver it */
					System.out
							.println("Apple accepted the notification and should deliver it: "
									+ msg);
				} else {
					final String invalidToken = notification.getDevice()
							.getToken();
					/* Add code here to remove invalidToken from your database */
					System.out.println("invalidDeviceId: " + invalidToken);
					throw new InvalidDeviceId("InvalidDeviceId: "
							+ invalidToken);
				}
			}
		} catch (CommunicationException | KeystoreException e) {
			e.printStackTrace();
			retVal = false;
			throw new AccountNotificationException(e.getMessage());
		}
		return retVal;
	}

}
