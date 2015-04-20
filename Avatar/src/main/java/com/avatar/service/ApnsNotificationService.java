package com.avatar.service;

import java.io.IOException;
import java.io.InputStream;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;
import javapns.notification.transmission.PushQueue;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.springframework.stereotype.Service;

import com.avatar.business.NotificationBusiness;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.exception.InvalidDeviceId;
import com.avatar.exception.NotificationException;

@Service
public class ApnsNotificationService extends BaseService implements
		NotificationBusiness {
	@Resource(name = "apnsCertificateP12Staff")
	private String apnsServerCertificateStaff;

	@Resource(name = "apnsCertificateP12Member")
	private String apnsServerCertificateMember;

	@Resource(name = "apnsCertificateP12Password")
	private String apnsServerCertificatePassword;

	private byte[] staffP12Bytes = null;
	private byte[] memberP12Bytes = null;
	private PushQueue queueNotificationMember = null;
	private PushQueue queueNotificationEmployee = null;
	private PushQueue queueAlert = null;

	@Resource(name = "apnsPushThreads")
	private int pushThreads;

	@Resource(name = "apnsAlertMsg")
	private String alertMsg;

	private static final String ALERT_JSON = "{'alert':'USER_INFO', 'sound':'default', 'ads':'%s', 'user_define':'%s'}";
	private final DateTimeFormatter dtf = DateTimeFormat
			.forPattern("MM/dd/yyyy HH:mm:ss");

	private PushNotificationPayload buildAlertPayload(
			final AccountDto memberAccount) throws JSONException {
		final PushNotificationPayload payload = new PushNotificationPayload();
		payload.addCustomAlertBody(String.format(alertMsg,
				memberAccount.getName()));
		payload.addBadge(1);
		payload.addSound("default");

		payload.addCustomDictionary("phoneNumber",
				memberAccount.getMobileNumber());
		payload.addCustomDictionary("checkIn", dtf.print(getNow(
				memberAccount.getHomeClub().getTimeZone()).getTime())); // System.currentTimeMillis()));
		System.out.println("DEBUG: json apns alrt=>"
				+ payload.getPayload().toString());
		return payload;
	}

	private void init() throws NotificationException {
		if (staffP12Bytes == null) {
			staffP12Bytes = readP12(apnsServerCertificateStaff);
		}
		if (memberP12Bytes == null) {
			memberP12Bytes = readP12(apnsServerCertificateMember);
		}
		try {
			if (queueNotificationMember == null) {
				/* Create the queue */
				queueNotificationMember = Push.queue(memberP12Bytes,
						apnsServerCertificatePassword, true /* production */,
						pushThreads);
				/* Start the queue (all threads and connections and initiated) */
				queueNotificationMember.start();
			}
		} catch (final KeystoreException e) {
			throw new NotificationException(
					"Member Keystore initialization error.");
		}
		try {
			if (queueNotificationEmployee == null) {
				queueNotificationEmployee = Push.queue(staffP12Bytes,
						apnsServerCertificatePassword, true /* production */,
						pushThreads);
				/* Start the queue (all threads and connections and initiated) */
				queueNotificationEmployee.start();
			}
			if (queueAlert == null) {
				queueAlert = Push.queue(staffP12Bytes,
						apnsServerCertificatePassword, true /* production */,
						pushThreads);
				/* Start the queue (all threads and connections and initiated) */
				queueAlert.start();
			}
		} catch (final KeystoreException e) {
			throw new NotificationException(
					"Staff Keystore initialization error.");
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
	public boolean sendAlert(final AccountDto staffAccount,
			final AccountDto memberAccount) throws NotificationException {
		boolean retVal = true;
		init();

		if (StringUtils.isNotEmpty(staffAccount.getDeviceId())) {
			try {
				final PushNotificationPayload payload = buildAlertPayload(memberAccount);

				/* Prepare a simple payload to push */
				queueNotificationEmployee.add(payload,
						staffAccount.getDeviceId());
				System.out.println("DEBUG: SENT alert to "
						+ staffAccount.getDeviceId());
			} catch (final InvalidDeviceTokenFormatException | JSONException e) {
				final String name = StringUtils
						.isEmpty(memberAccount.getName()) ? "" : memberAccount
						.getName();
				final String mobileNumber = StringUtils.isEmpty(memberAccount
						.getMobileNumber()) ? "" : memberAccount
						.getMobileNumber();
				final String msg = String.format(ALERT_JSON,
						name.replaceAll("'", ""),
						mobileNumber.replaceAll("'", ""));

				retVal = sendNotification(staffAccount.getDeviceId(), msg,
						staffAccount.isStaff());
			}
		} else {
			System.out
					.println("INFO: skip Alert... Staff does not have any deviceId");
			retVal = false;
		}
		return retVal;
	}

	@Override
	public boolean sendNotification(final AccountDto account)
			throws NotificationException {
		boolean retVal = true;
		init();
		final String msg = "Your activation pin is "
				+ account.getToken().getToken();
		try {
			/* Prepare a simple payload to push */
			final PushNotificationPayload payload = PushNotificationPayload
					.alert(msg);
			if (account.isStaff()) {
				queueNotificationEmployee.add(payload, account.getDeviceId());
			} else {
				queueNotificationMember.add(payload, account.getDeviceId());
			}
		} catch (final InvalidDeviceTokenFormatException e) {
			retVal = sendNotification(account.getDeviceId(), msg,
					account.isStaff());
		}
		return retVal;
	}

	@Override
	public boolean sendNotification(final String deviceId, final String msg,
			final boolean staff) throws NotificationException {
		System.out.println("WARNING: Using nonconnection pool APNS");
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
			throw new NotificationException(e.getMessage());
		}
		return retVal;
	}

	@Override
	public boolean testAlert(final String deviceId, final boolean staff)
			throws NotificationException {
		final boolean retVal = true;
		init();
		try {
			final AccountDto memberAccount = new EmployeeAccountDto();
			memberAccount.setName("John Doe");
			memberAccount.setMobileNumber("123-456-1234");

			/* Prepare a simple payload to push */
			final PushNotificationPayload payload = buildAlertPayload(memberAccount);

			if (staff) {
				queueNotificationEmployee.add(payload, deviceId);
			} else {
				queueNotificationMember.add(payload, deviceId);
			}
		} catch (final JSONException | InvalidDeviceTokenFormatException e) {
			throw new NotificationException(
					"InvalidDeviceTokenFormatException was thrown: "
							+ e.getMessage());
		}
		return retVal;
	}

}
