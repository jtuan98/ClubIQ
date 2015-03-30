package com.avatar.mvc.controller;

import java.security.InvalidParameterException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.dto.ImagePic;
import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.account.MobileAccountDto;
import com.avatar.dto.account.MobileActivationPin;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.exception.InvalidDeviceId;

@Controller
public class RegistrationController extends BaseController {
	@RequestMapping(value = { "/Registration/ActivateAccount",
			"/Registration/ActivateAccountMobile",
			"/open/Registration/ActivateAccount",
			"/open/Registration/ActivateAccountMobile" })
	public ModelAndView activateAccount(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "activationToken") final String activationToken,
			@RequestParam(required = true, value = "mobileNumber") final String mobileNumber,
			@RequestParam(required = true, value = "deviceId") final String deviceId)
			throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		String msg = "";
		boolean activated = false;
		try {
			activated = accountService.activateMobileAccount(mobileNumber,
					deviceId, activationToken);
		} catch (final InvalidParameterException e) {
			msg = "Error:  Please check activation token, might have expired ["
					+ activationToken + "]";
			apiResponse = new WsResponse<String>(ResponseStatus.failure, msg,
					null);
		}
		apiResponse = new WsResponse<String>(activated ? ResponseStatus.success
				: ResponseStatus.failure, msg, null);
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/Registration/NonMobile/ActivateAccount",
			"/open/Registration/NonMobile/ActivateAccount" })
	public ModelAndView activateNonMobileAccount(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "activationToken") final String activationToken)
			throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		String msg = "";
		boolean activated = false;
		try {
			activated = accountService.activateAccount(activationToken);
		} catch (final InvalidParameterException e) {
			msg = "Error:  Please check activation token, might have expired ["
					+ activationToken + "]";
			apiResponse = new WsResponse<String>(ResponseStatus.failure, msg,
					null);
		}
		apiResponse = new WsResponse<String>(activated ? ResponseStatus.success
				: ResponseStatus.failure, msg, null);
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/Registration/CreateAccountMobile",
			"/Registration/CreateAccount",
			"/open/Registration/CreateAccountMobile",
			"/open/Registration/CreateAccount" })
	public ModelAndView createAccountMobile(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "mobileNumber") final String mobileNumber,
			@RequestParam(required = true, value = "deviceId") final String deviceId,
			@RequestParam(required = true, value = "homeClubId") final String homeClubId)
			throws Exception {
		init();
		System.out.println("In................createAccountMobile");
		WsResponse<ActivationToken> apiResponse = null;
		try {
			final String userid = mobileNumber;
			final MobileAccountDto accountInfo = (MobileAccountDto) createActivationAccountDto(
					true, null, mobileNumber, userid, homeClubId, null, null,
					null, null, Privilege.user.name());
			accountInfo.setDeviceId(deviceId);
			final MobileActivationPin activationToken = (MobileActivationPin) accountService
					.createAccount(accountInfo);
			accountInfo.setToken(activationToken);
			String msg = "";
			try {
				mobileNotificationService.sendNotification(accountInfo);
			} catch( final InvalidDeviceId e) {
				msg = "Account created but unable to send notification: " + e.getMessage();
			}
			apiResponse = new WsResponse<ActivationToken>(
					ResponseStatus.success, msg, activationToken);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<ActivationToken>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/Registration/NonMobile/CreateAccount",
			"/open/Registration/NonMobile/CreateAccount" })
	public ModelAndView createAccountNonMobile(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "email") final String email,
			@RequestParam(required = true, value = "password") final String password,
			@RequestParam(required = false, value = "homeClubId") final String homeClubId,
			@RequestParam(required = false, value = "realname") final String name,
			@RequestParam(required = false, value = "pictureBase64") final String pictureBase64Encoded,
			@RequestParam(required = false, value = "address") final String address,
			@RequestParam(required = true, value = "privilege", defaultValue = "user") final String privilege)
			throws Exception {
		init();
		WsResponse<ActivationToken> apiResponse = null;
		try {
			final String userid = email;
			System.out.println("In................createAccount");
			final AccountDto accountInfo = createActivationAccountDto(false,
					email, null, userid, homeClubId, password, name,
					pictureBase64Encoded, address, privilege);
			final ActivationToken activationToken = accountService
					.createAccount(accountInfo);
			System.out.println("In................activationToken=>"
					+ activationToken.getToken());
			accountInfo.setToken(activationToken);
			emailNotificationService.sendNotification(accountInfo);
			apiResponse = new WsResponse<ActivationToken>(
					ResponseStatus.success, "Token Sent", null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<ActivationToken>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	private AccountDto createActivationAccountDto(final boolean mobileFlag,
			final String email, final String mobile, final String userid,
			final String homeClubId, final String password, final String name,
			final String pictureBase64Encoded, final String address,
			final String privilege) {
		init();
		AccountDto retVal = null;
		if (mobileFlag) {
			retVal = new MobileAccountDto();
			((MobileAccountDto) retVal).setMobileNumber(mobile);
		} else {
			retVal = new AccountDto();
		}
		if (StringUtils.isNotEmpty(homeClubId)) {
			final ClubDto homeClub = new ClubDto();
			homeClub.setClubId(homeClubId);
			retVal.setHomeClub(homeClub);
		}

		retVal.setEmail(email);
		retVal.add(Privilege.valueOf(privilege));
		retVal.setUserId(userid);
		retVal.setPassword(password);
		if (pictureBase64Encoded != null) {
			final ImagePic pic = new ImagePic(pictureBase64Encoded);
			retVal.setPicture(pic);
		}
		retVal.setAddress(address);
		retVal.setName(name);
		return retVal;
	}

	@RequestMapping(value = "/open/test")
	public ModelAndView openTest(final Principal principal,
			final HttpServletRequest req) throws Exception {
		init();
		return new ModelAndView(jsonView, toModel("Testing.."));
	}

	@RequestMapping(value = "/restricted/test")
	public @ResponseBody String restrictedTest(final Principal principal,
			final HttpServletRequest req) throws Exception {
		init();
		return "User is [" + principal.getName() + "]";
	}

	@RequestMapping(value = { "/Registration/VerifyAcctExist",
			"/open/Registration/VerifyAcctExist" })
	public ModelAndView verifyAccount(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "mobileNumber") final String userid)
			throws Exception {
		init();
		final String msg = "";
		final boolean exists = accountService.exists(userid);
		final WsResponse<String> apiResponse = new WsResponse<String>(
				ResponseStatus.success, "", exists ? "true" : "false", "exist");
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

}
