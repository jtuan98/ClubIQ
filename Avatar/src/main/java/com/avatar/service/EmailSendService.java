package com.avatar.service;

import java.util.Properties;

import javax.annotation.Resource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.Validate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.avatar.business.NotificationBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.exception.NotificationException;

@Service
public class EmailSendService implements NotificationBusiness {
	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	@Resource(name = "mailSender")
	private MailSender mailSender;

	@Resource(name = "mailSmtpHost")
	private String mailSmtpHost;

	@Resource(name = "mailSmtpPort")
	private String mailSmtpPort;

	@Resource(name = "mailSmtpUserId")
	private String mailSmtpUserId;

	@Resource(name = "mailSmtpPassword")
	private String mailSmtpPassword;

	@Resource(name = "mailSenderFrom")
	private String fromAddress;

	@Resource(name = "ec2Host")
	private String ec2Host;

	@Resource(name = "accountActivationLink")
	private String accountActivationLink;

	private Session mailServerSession = null;

	private final String emailText = "<!DOCTYPE html>" + "<html><body><p>"
			+ "Please click the link below for account registration:<P>"
			+ "<a href=\":THELINK:\" target=\"_top\">Account Activation</a>"
			+ "</p>" + "</body></html>";

	private final String emailTextPlain = "Please click the link below for account registration:\r\n"
			+ ":THELINK:";

	private String buildLink(final ActivationToken token) {
		final String retVal = ec2Host + accountActivationLink
				+ token.getToken();
		return retVal;
	}

	private void init() {
		if (mailServerSession == null) {
			final Properties props = new Properties();
			props.put("mail.smtp.host", mailSmtpHost);
			props.put("mail.smtp.port", mailSmtpPort);
			// props.put("mail.debug", "true");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			mailServerSession = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
	            @Override
				protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication(mailSmtpUserId, mailSmtpPassword);
	             }
	          });
		}
	}

	@Override
	public boolean sendAlert(final AccountDto staffAccount,
			final AccountDto memberAccount) throws NotificationException {
		return false;
	}

	@Override
	public boolean sendNotification(final AccountDto account)
			throws NotificationException {
		init();
		boolean retVal = false;
		try {
			Validate.notBlank(account.getEmail());
			// // Create a default MimeMessage object.
			final MimeMessage message = new MimeMessage(mailServerSession);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(fromAddress));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					account.getEmail()));

			// Set Subject: header field
			message.setSubject("ClubIQ Account Registration");

			// Now set the actual message
			final BodyPart msgBody = new MimeBodyPart();
			msgBody.setText(emailText.replaceAll(":THELINK:",
					buildLink(account.getToken())));
			msgBody.setHeader("Content-Type", "text/html");
			message.setText(emailTextPlain.replaceAll(":THELINK:",
					buildLink(account.getToken())));

			// Send message
			Transport.send(message);

			accountDao.markStatusAsNotified(account.getUserId());
			retVal = true;
		} catch (final Exception e) {
			retVal = false;
			throw new NotificationException(e.getMessage());
		}
		return retVal;
	}

	@Override
	public boolean sendNotification(final String emailId, final String msg,
			final boolean staff) throws NotificationException {
		final boolean retVal = false;
		try {
			final SimpleMailMessage message = new SimpleMailMessage();
			Validate.notBlank(emailId);
			message.setFrom(fromAddress);
			message.setTo(emailId);
			message.setSubject("Simple mailSender Testing email");

			message.setText(msg );
			mailSender.send(message);
		} catch (final Exception e) {
			throw new NotificationException(e.getMessage());
		}
		testAlert(emailId, false);
		return retVal;
	}

	public void setMailSender(final MailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public boolean testAlert(final String emailId, final boolean staff)
			throws NotificationException {
		init();
		boolean retVal = false;
		try {
			Validate.notBlank(emailId);
			// // Create a default MimeMessage object.
			final MimeMessage message = new MimeMessage(mailServerSession);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(fromAddress));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					emailId));

			// Set Subject: header field
			message.setSubject("ClubIQ Account Registration");

			// Now set the actual message
			final MimeMultipart multipart = new MimeMultipart();
			final BodyPart msgBody = new MimeBodyPart();
			msgBody.setText(emailText
					.replaceAll(":THELINK:", ec2Host + accountActivationLink+ "123-123-123-123"));
			msgBody.setHeader("Content-Type", "text/html");
			multipart.addBodyPart(msgBody);
			message.setContent(multipart);

			// Send message
			Transport.send(message);

			retVal = true;
		} catch (final Exception e) {
			retVal = false;
			throw new NotificationException(e.getMessage());
		}
		return retVal;
	}
}
