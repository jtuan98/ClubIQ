package com.avatar.mvc.controller;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.business.BeaconBusiness;
import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.DbTimeZone;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

@Controller
@RequestMapping(value = "/ClubMgr")
public class ClubManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = { Privilege.clubAdmin,
			Privilege.staff, Privilege.superUser };

	@Resource(name = "beaconManagerController")
	BeaconManagerController beaconManager;

	@Resource(name = "beaconService")
	private BeaconBusiness beaconService;

	@RequestMapping(value = "/GetAmenityList")
	public ModelAndView getAmenities(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
			throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Check authToken with clubId
			validateStaffInClub(authenticationService.getAccount(authToken),
					clubId);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		WsResponse<List<AmenityDto>> apiResponse = null;
		try {
			final List<AmenityDto> amenities = beaconService
					.getAmenities(clubId);
			apiResponse = new WsResponse<List<AmenityDto>>(
					ResponseStatus.success, "", amenities, "amenityList");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<AmenityDto>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/GetClubList")
	public ModelAndView getClubList(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken)
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
		final AccountDto principal = authenticationService.getAccount(authToken);
		WsResponse<List<ClubDto>> apiResponse = null;
		try {
			final List<ClubDto> clubs = beaconService
					.getClubs(principal.getId());
			apiResponse = new WsResponse<List<ClubDto>>(
					ResponseStatus.success, "", clubs, "clubList");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<ClubDto>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	private ClubDto getInstance(final String clubId, final String clubName,
			final String clubAddress, final String clubZip,
			final String clubState, final String clubPhoneNumber,
			final String hzRestriction, final DbTimeZone timezone) {
		final ClubDto retVal = new ClubDto();
		retVal.setClubId(clubId);
		retVal.setClubName(clubName);
		retVal.setAddress(clubAddress);
		retVal.setZipCode(clubZip);
		retVal.setState(clubState);
		retVal.setPhoneNumber(clubPhoneNumber);
		retVal.setHzRestriction(hzRestriction);
		retVal.setTimeZone(timezone);
		return retVal;
	}

	@RequestMapping(value = "/RegisterAPNsToken")
	public ModelAndView setAmenityDeptName(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = false, value = "userId") final String userId,
			@RequestParam(required = true, value = "apnsToken") final String apnsToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
			throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			validateStaffInClub(authenticationService.getAccount(authToken), clubId);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		return beaconManager.setAmenityDeptName(authToken, apnsToken, null,
				clubId);
	}

	@RequestMapping(value = "/SetClubAddress")
	public ModelAndView setClubAddress(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = false, value = "clubName") final String clubName,
			@RequestParam(required = false, value = "clubAddress") final String clubAddress,
			@RequestParam(required = false, value = "clubZipCode") final String clubZip,
			@RequestParam(required = false, value = "clubState") final String clubState,
			@RequestParam(required = false, value = "clubPhoneNumber") final String clubPhoneNumber,
			@RequestParam(required = false, value = "hzRestriction") final String hzRestriction,
			@RequestParam(required = false, value = "timezone", defaultValue="US_PST") final DbTimeZone timezone
			)
			throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Verify using authToken to see if user have the perm to edit club
			// info.
			validateStaffInClub(authenticationService.getAccount(authToken),
					clubId);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		final ClubDto clubUpdated = getInstance(clubId, clubName, clubAddress,
				clubZip, clubState, clubPhoneNumber, hzRestriction, timezone);

		WsResponse<String> apiResponse = null;
		try {
			beaconService.update(clubUpdated);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}
}
