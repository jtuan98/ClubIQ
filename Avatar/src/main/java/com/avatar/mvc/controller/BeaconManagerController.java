package com.avatar.mvc.controller;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.business.BeaconBusiness;
import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.MemberAccountDto;
import com.avatar.dto.club.BeaconDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.club.SubAmenityDto;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.dto.serializer.AccountDtoMinimalSerializer;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;
import com.avatar.mvc.view.JsonView;

// User Must be Authenticated! and must have admin role
@Controller
@RequestMapping(value = "/BeaconMgr")
public class BeaconManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = { Privilege.staff,
		Privilege.superUser };

	@Resource(name = "beaconService")
	BeaconBusiness beaconService;

	protected JsonView jsonShowMemberByDeptView = null;

	@RequestMapping(value = { "/DeleteBeacon" })
	public ModelAndView deleteBeacon(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "beaconActionId") final String beaconActionId)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		BeaconDto beacon = null;
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
			beacon = beaconService.getBeacon(beaconActionId);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		System.out.println("Beacon=>" + beacon);
		if (beacon != null) {
			try {
				// Verify using authToken to see if user have the perm to edit
				// club
				// info.
				validateStaffInClub(
						authenticationService.getAccount(authToken), beacon
						.getClub().getClubId());
			} catch (NotFoundException | AuthenticationTokenExpiredException
					| PermissionDeniedException e) {
				apiDeniedResponse = new WsResponse<String>(
						ResponseStatus.denied, e.getMessage(), null);
				return new ModelAndView(jsonView, toModel(apiDeniedResponse));
			}

			try {
				System.out.println("Deleting Beacon=>" + beacon);
				beaconService.deleteBeacon(beacon);
				apiResponse = new WsResponse<String>(ResponseStatus.success,
						"", null);
			} catch (final Exception e) {
				apiResponse = new WsResponse<String>(ResponseStatus.failure,
						e.getMessage(), null);
			}
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	//GetAmenityDeptName is equivalent to getSubAmenities
	@RequestMapping(value = { "/GetAmenityDeptName" })
	public ModelAndView getAmenityDeptName(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
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

		WsResponse<List<String>> apiResponse = null;
		try {
			final List<String> subAmenityNames = beaconService
					.getSubAmenityDeptName(clubId);
			apiResponse = new WsResponse<List<String>>(ResponseStatus.success,
					"", subAmenityNames, "amenityDeptList");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<String>>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	private BeaconDto getBeaconInstance(final String clubId,
			final String subAmenityId, final String beaconActionId,
			final String location, final String description,
			final String installerStaffUserId, final String installationDate)
					throws InvalidParameterException {
		final BeaconDto retVal = new BeaconDto();
		retVal.setBeaconActionId(beaconActionId);
		retVal.setSubAmenityId(subAmenityId);
		retVal.setClubId(clubId);
		retVal.setInstallerStaffId(installerStaffUserId);
		retVal.setDescription(description);
		ClubDto club;
		try {
			club = beaconService.getClub(clubId);
		} catch (final NotFoundException e1) {
			throw new InvalidParameterException("ClubId " + clubId
					+ " does not exist");
		}

		if (StringUtils.isEmpty(installationDate)) {
			retVal.setInstallationDate(nowService.getNow(club.getTimeZone()));
		} else {
			retVal.setInstallationDate(new Date(yyyyMMddDtf
					.parseMillis(installationDate)));
		}

		retVal.setLocation(location);
		return retVal;
	}

	@RequestMapping(value = "/GetBeaconList")
	public ModelAndView getBeaconList(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId)
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

		WsResponse<List<BeaconDto>> apiResponse = null;
		try {
			final List<BeaconDto> beacons = beaconService.getBeacons(clubId,
					subAmenityId);
			apiResponse = new WsResponse<List<BeaconDto>>(
					ResponseStatus.success, "", beacons, "beaconList");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<BeaconDto>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/BeaconDetectionWithSubAmenity")
	public ModelAndView getSubAmenityInfo(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "beaconActionId") final String beaconActionId)
					throws Exception {
		init();
		BeaconDto beacon = null;
		WsResponse<Map<String, String>> apiResponse = null;
		try {
			beacon = beaconService.getBeacon(beaconActionId);
			System.out.println("Beacon=>" + beacon);
			final SubAmenityDto subAmenity = beacon.getSubAmenity();
			final Map<String, String> result = new HashMap<String, String>();
			result.put("subAmenityId", subAmenity.getSubAmenityId());
			result.put("amenityId", subAmenity.getAmenityId());
			apiResponse = new WsResponse<Map<String, String>>(
					ResponseStatus.success, "", result);
		} catch (final Exception e) {
			apiResponse = new WsResponse<Map<String, String>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@Override
	protected void init() {
		super.init();
		jsonShowMemberByDeptView = init(jsonShowMemberByDeptView);
		jsonShowMemberByDeptView.register(MemberAccountDto.class, new AccountDtoMinimalSerializer());
	}

	// SetAmenityDeptName is equivalent to setSubAmenity
	@RequestMapping(value = "/SetAmenityDeptName")
	public ModelAndView setAmenityDeptName(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "apnsToken") final String apnsToken,
			@RequestParam(required = true, value = "amenityDepartment") final String subAmenityId,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "amenityId") final String amenityId)
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

		return setAmenityDeptName(authToken, apnsToken, subAmenityId,
				clubId, amenityId);
	}

	ModelAndView setAmenityDeptName(final String authToken,
			final String apnsToken, final String subAmenityId,
			final String clubId, final String amenityId) throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			beaconService.setSubAmenityId(clubId, apnsToken,
					amenityId, subAmenityId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/SetBeacon", "/UpdateBeacon" })
	public ModelAndView setBeacon(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId,
			@RequestParam(required = true, value = "beaconActionId") final String beaconActionId,
			@RequestParam(required = true, value = "location") final String location,
			@RequestParam(required = true, value = "desc") final String description,
			@RequestParam(required = true, value = "installerStaffUserId") final String installerStaffUserId,
			@RequestParam(required = true, value = "installDate") final String installationDate)
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
		WsResponse<String> apiResponse = null;
		try {
			final BeaconDto beacon = getBeaconInstance(clubId, subAmenityId,
					beaconActionId, location, description,
					installerStaffUserId, installationDate);
			beaconService.updateBeacon(beacon);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/SetMemberEntry")
	public ModelAndView setMemberAcct(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "mobileNumber") final String userId,
			@RequestParam(required = true, value = "beaconActionId") final String beaconId)
					throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			beaconService.addUserIdToBeacon(beaconId, userId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, userId
					+ " added to " + beaconId, null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/ShowMemberByDept")
	public ModelAndView showMemberByDept(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId,
			@RequestParam(required = false, value = "date") final String onDate)
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
		Date entryDate = null;
		if (StringUtils.isNotEmpty(onDate)) {
			entryDate = new Date(yyyyMMddDtf.parseMillis(onDate));
		}
		WsResponse<List<AccountDto>> apiResponse = null;
		try {
			final List<AccountDto> users = beaconService
					.getUsers(subAmenityId, entryDate);
			System.out.println(users.getClass());
			apiResponse = new WsResponse<List<AccountDto>>(
					ResponseStatus.success, "", users, "users");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<AccountDto>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonShowMemberByDeptView, toModel(apiResponse));
	}

}
