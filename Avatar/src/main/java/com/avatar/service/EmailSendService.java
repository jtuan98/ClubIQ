package com.avatar.service;

import javax.annotation.Resource;

import org.apache.commons.lang3.Validate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import com.avatar.business.NotificationBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.exception.AccountNotificationException;

@Service
public class EmailSendService implements NotificationBusiness {
	public static void main(final String[] args) throws AccountNotificationException {
		final org.springframework.mail.javamail.JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.gmail.com");
		mailSender.setPort(587);
		mailSender.setUsername("clubiq2015@gmail.com");
		mailSender.setPassword("avatar2015");
		mailSender.getJavaMailProperties().put("mail.smtp.auth", true);
		mailSender.getJavaMailProperties().put("mail.smtp.starttls.enable",
				true);

		final EmailSendService testService = new EmailSendService();
		testService.setMailSender(mailSender);
		testService.sendNotification(null);
	}

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	@Resource(name = "mailSender")
	private MailSender mailSender;

	@Resource(name = "mailSenderFrom")
	private String fromAddress;

	@Resource(name = "ec2Host")
	private String ec2Host;

	@Resource(name = "accountActivationLink")
	private String accountActivationLink;

	private String buildLink(final ActivationToken token) {
		final String retVal = ec2Host + accountActivationLink
				+ token.getToken();
		return retVal;
	}

	@Override
	public boolean sendNotification(final AccountDto account)
			throws AccountNotificationException {
		boolean retVal = false;
		try {
			final SimpleMailMessage message = new SimpleMailMessage();
			Validate.notBlank(account.getEmail());
			message.setFrom(fromAddress);
			message.setTo(account.getEmail());
			message.setSubject("ClubIQ Account Registration");
			final String emailText = "<!DOCTYPE html>"
					+ "<html><body><p>"
					+ "Please click the link below for account registration:<P>"
					+ "<a href=\":THELINK:\" target=\"_top\">Account Activation</a>"
					+ "</p>" + "</body></html>";
			final String emailTextPlain = "Please click the link below for account registration:\r\n"
					+ ":THELINK:";

			message.setText(emailTextPlain.replaceAll(":THELINK:",
					buildLink(account.getToken())));
			mailSender.send(message);
			accountDao.markStatusAsNotified(account.getUserId());
			retVal = true;
		} catch (final Exception e) {
			retVal = false;
			throw new AccountNotificationException(e.getMessage());
		}
		return retVal;
	}

	@Override
	public boolean sendNotification(final String deviceId, final String msg)
			throws AccountNotificationException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setMailSender(final MailSender mailSender) {
		this.mailSender = mailSender;
	}
}
