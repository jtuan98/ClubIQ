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
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

@Controller
@RequestMapping(value = { "/AcctMgr", "/AccountMgr" })
public class AccountManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = { Privilege.clubAdmin,
		Privilege.staff, Privilege.superUser };

	@RequestMapping(value = { "/AddClubAmenityToAccount" })
	public ModelAndView addClubAmenityToAccount(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "userId") final String userId,
			@RequestParam(required = true, value = "clubAmenityId") final String clubAmenityId)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		WsResponse<String> apiResponse = null;
		try {
			accountService.addAmenityToUser(userId, clubAmenityId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/GetAcctInfo", "/GetAccountInfo" })
	public ModelAndView getAcctInfo(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "mobileNumber") final String mobileNumber)
					throws Exception {
		init();
		WsResponse<AccountDto> apiResponse = null;
		try {
			final AccountDto account = accountService.get(mobileNumber);
			apiResponse = new WsResponse<AccountDto>(ResponseStatus.success,
					"", account);
		} catch (final Exception e) {
			apiResponse = new WsResponse<AccountDto>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

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

	@RequestMapping(value = { "/Mobile/SetAccountInfo", // This will be
			// deprecated
			"/Member/SetAccountInfo", "/SetAccountInfo" })
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
	@RequestMapping(value = { "/Employee/SetAccountInfo",
	"/SetEmployeeAccountInfo" })
	public ModelAndView updateAccountEmployee(
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

	@RequestMapping(value = { "/MapTangerineHandsetIDwithUser" })
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
