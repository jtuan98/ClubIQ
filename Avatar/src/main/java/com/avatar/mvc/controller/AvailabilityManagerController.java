package com.avatar.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.CheckInfo;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

@Controller
@RequestMapping(value = "/AvailabilityMgr")
public class AvailabilityManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = { Privilege.user };

	@RequestMapping(value = { "/getAvailInfo", "/GetAvailInfo" })
	public ModelAndView getCheckInfoByAvailId(
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "availId") final String availId)
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
		WsResponse<CheckInfo> apiResponse = null;
		final AccountDto account = authenticationService.getAccount(authToken);
		final String userId = account.getUserId();
		try {
			final CheckInfo checkInfo = accountService.getCheckInfo(userId,
					availId);
			apiResponse = new WsResponse<CheckInfo>(ResponseStatus.success, "",
					checkInfo);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<CheckInfo>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/setCheckInfo", "/SetCheckInfo" })
	public ModelAndView setCheckInfo(
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "requestedClubId") final String requestedClubId,
			@RequestParam(required = true, value = "amenityId") final String amenityId,
			@RequestParam(required = true, value = "numOfPerson", defaultValue = "1") final int numOfPerson,
			@RequestParam(required = true, value = "requestedDateTime") final String requestedDateTimeyyyymmddhh24mi)
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
		final AccountDto account = authenticationService.getAccount(authToken);
		final String userId = account.getUserId();
		try {
			final String availId = accountService.updateCheckInfo(userId,
					requestedClubId, amenityId, numOfPerson,
					requestedDateTimeyyyymmddhh24mi);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					availId, "availId");
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

}
