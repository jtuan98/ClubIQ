package com.avatar.mvc.controller;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.exception.NotFoundException;

@Controller
@RequestMapping(value = "/AcctMgr")
public class AccountManagerController extends BaseController {

	@RequestMapping(value = "/GetMemberAcct")
	public ModelAndView getMemberAcct(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "deviceId") final String deviceId)
			throws Exception {
		init();
		WsResponse<AccountDto> apiResponse = null;
		final String userId = principal.getName();
		try {
			final AccountDto account = accountService.get(userId);
			apiResponse = new WsResponse<AccountDto>(ResponseStatus.success,
					"", account);
		} catch (final Exception e) {
			apiResponse = new WsResponse<AccountDto>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/Mobile/SetUserAccount", "/SetUserAccount" })
	public ModelAndView updateAccount(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "mobileNumber") final String userId,
			@RequestParam(required = false, value = "deviceId") final String deviceId,
			@RequestParam(required = false, value = "realname") final String realName,
			@RequestParam(required = false, value = "email") final String email,
			@RequestParam(required = false, value = "pictureBase64") final String pictureBase64)
			throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			if (StringUtils.isNotEmpty(deviceId)
					|| StringUtils.isNotEmpty(realName)
					|| StringUtils.isNotEmpty(email)
					|| StringUtils.isNotEmpty(pictureBase64)) {
				accountService.updateAccountInfo(userId, deviceId, realName,
						email, pictureBase64);
			}
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final NotFoundException e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					"Mobile Number not found!", null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	// Update the email is not allowed since email is the USERID
	@RequestMapping(value = { "/NonMobile/SetUserAccount",
			"/SetUserNonMobileAccount" })
	public ModelAndView updateAccountNonMobile(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "email") final String userId,
			@RequestParam(required = false, value = "deviceId") final String deviceId,
			@RequestParam(required = false, value = "realname") final String realName,
			@RequestParam(required = false, value = "pictureBase64") final String pictureBase64)
			throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			if (StringUtils.isNotEmpty(deviceId)
					|| StringUtils.isNotEmpty(realName)
					|| StringUtils.isNotEmpty(pictureBase64)) {
				accountService.updateAccountInfo(userId, deviceId, realName,
						null, pictureBase64);
			}
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final NotFoundException e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					"Account not found!", null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/MapTangerineHandsetIDwithUser",
			"/Mobile/MapTangerineHandsetIDwithUser" })
	public ModelAndView updateTangerineHandsetIDwithUser(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "mobileNumber") final String userId,
			@RequestParam(required = true, value = "deviceId") final String deviceId,
			@RequestParam(required = true, value = "tangerineHandsetId") final String tangerineHandsetId)
			throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			accountService.updateUserTangerineHandSetId(userId, deviceId,
					tangerineHandsetId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

}
