package com.avatar.mvc.controller;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.MemberAccountDto;
import com.avatar.dto.club.CheckInfo;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.dto.serializer.AccountDtoWithHzRestrictionInfoSerializer;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;
import com.avatar.mvc.view.JsonView;
import com.avatar.mvc.view.RenderingImageView;

@Controller
@RequestMapping(value = { "/AcctMgr", "/AccountMgr" })
public class AccountManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = { Privilege.clubAdmin,
		Privilege.staff, Privilege.superUser };

	private static final String YYYYMMDDHH24MISS = "yyyyMMddHHmmss";

	private final DateFormat df = new SimpleDateFormat(YYYYMMDDHH24MISS);

	protected JsonView jsonAccoutWithHzRestrictionView = null;
	private final RenderingImageView imageRenderer = new RenderingImageView();

	@RequestMapping(value = { "/AddClubSubAmenityToAccount" })
	public ModelAndView addClubAmenityToAccount(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "userId") final String userId,
			@RequestParam(required = true, value = "clubSubAmenityId") final String clubSubAmenityId)
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
			accountService.addSubAmenityToUser(userId, clubSubAmenityId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/Mobile/cancelMembership",
	"/Mobile/CancelMembership" })
	public ModelAndView cancelMembership(
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "currentDate") final String currentDateyyyymmddhh24miss)
					throws Exception {
		init();
		WsResponse<CheckInfo> apiResponse = null;
		final AccountDto account = authenticationService.getAccount(authToken);
		final String userId = account.getUserId();
		try {
			final Date currentDate = df.parse(currentDateyyyymmddhh24miss);
			accountService.cancelMembership(userId, currentDate);
			apiResponse = new WsResponse<CheckInfo>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<CheckInfo>(ResponseStatus.failure,
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
		return new ModelAndView(jsonAccoutWithHzRestrictionView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/Mobile/getLinkPhone", "/Mobile/GetLinkPhone" })
	public ModelAndView getLinkPhone(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "mobileNumber") final String mobileNumber)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, Privilege.values());
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}

		WsResponse<String> apiResponse = null;
		try {
			final AccountDto account = accountService.get(mobileNumber);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					StringUtils.isEmpty(account.getLinkMobileNumber()) ? ""
							: account.getLinkMobileNumber(), "2ndLinkPhone");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@Override
	protected void init() {
		super.init();
		jsonAccoutWithHzRestrictionView = init(jsonAccoutWithHzRestrictionView);
		jsonAccoutWithHzRestrictionView.register(MemberAccountDto.class, new AccountDtoWithHzRestrictionInfoSerializer());
	}

	// @RequestMapping(value = "/GetMemberAcct")
	// public ModelAndView getMemberAcct(
	// final Principal principal,
	// final HttpServletRequest req,
	// @RequestParam(required = true, value = "deviceId") final String deviceId)
	// throws Exception {
	// init();
	// WsResponse<AccountDto> apiResponse = null;
	// final String userId = principal.getName();
	// try {
	// final AccountDto account = accountService.get(userId);
	// apiResponse = new WsResponse<AccountDto>(ResponseStatus.success,
	// "", account);
	// } catch (final Exception e) {
	// apiResponse = new WsResponse<AccountDto>(ResponseStatus.failure,
	// e.getMessage(), null);
	// }
	// return new ModelAndView(jsonView, toModel(apiResponse));
	// }

	@RequestMapping(value = { "/Mobile/setLinkPhone", "/Mobile/SetLinkPhone" })
	public ModelAndView setLinkPhone(
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "linkNumber") final String linkNumber,
			@RequestParam(required = true, value = "currentDate") final String currentDateyyyymmddhh24miss)
					throws Exception {
		init();
		WsResponse<CheckInfo> apiResponse = null;
		try {
			final AccountDto account = authenticationService
					.getAccount(authToken);
			final String userId = account.getUserId();
			try {
				final Date currentDate = df.parse(currentDateyyyymmddhh24miss);
				accountService.setLinkNumber(userId, linkNumber, currentDate);
				apiResponse = new WsResponse<CheckInfo>(ResponseStatus.success,
						"", null);
			} catch (final Exception e) {
				apiResponse = new WsResponse<CheckInfo>(ResponseStatus.failure,
						e.getMessage(), null);
			}
		} catch (final AuthenticationTokenExpiredException e) {
			apiResponse = new WsResponse<CheckInfo>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/Mobile/SetAccountInfo",
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

	@RequestMapping(value = { "/setNoticeInfo", "/SetNoticeInfo" })
	public ModelAndView updateNoticeInfo(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "dateTime") final String dateTimeyyyymmddhh24miss,
			@RequestParam(required = true, value = "agreed") final boolean agreed)
					throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			final AccountDto account = authenticationService
					.getAccount(authToken);
			final String userId = account.getUserId();
			final Date currentDate = df.parse(dateTimeyyyymmddhh24miss);
			accountService.setNoticeInfo(userId, currentDate, agreed);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
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
