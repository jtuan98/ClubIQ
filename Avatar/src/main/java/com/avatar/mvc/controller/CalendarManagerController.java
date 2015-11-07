package com.avatar.mvc.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.business.BeaconBusiness;
import com.avatar.dto.WsResponse;
import com.avatar.dto.club.BlackoutDate;
import com.avatar.dto.club.BlackoutTime;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

@Controller
@RequestMapping(value = "/CalendarMgr")
public class CalendarManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = { Privilege.user };
	@Resource(name = "beaconService")
	private BeaconBusiness beaconService;

	// Phase 2
	@RequestMapping(value = { "/getBlackOutDates", "/GetBlackOutDates" })
	public ModelAndView getBlackOutDates(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "amenityId") final String amenityId,
			@RequestParam(required = false, value = "month") final String month)
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
		WsResponse<List<BlackoutDate>> apiResponse = null;
		try {
			final List<BlackoutDate> dates = beaconService.getBlackoutDates(
					clubId, amenityId, month);
			apiResponse = new WsResponse<List<BlackoutDate>>(
					ResponseStatus.success, "", dates, "blackoutDates");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<BlackoutDate>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/getBlackOutTimes", "/GetBlackOutTimes" })
	public ModelAndView getBlackOutTimes(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "amenityId") final String amenityId,
			@RequestParam(required = false, value = "requestedDate") final String requestedDateMMDD)
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
		WsResponse<List<BlackoutTime>> apiResponse = null;
		try {
			final List<BlackoutTime> times = beaconService.getBlackoutTimes(
					clubId, amenityId, requestedDateMMDD);
			apiResponse = new WsResponse<List<BlackoutTime>>(
					ResponseStatus.success, "", times, "blackoutTimes");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<BlackoutTime>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

}
