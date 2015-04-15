package com.avatar.mvc.controller;

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.business.BeaconBusiness;
import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.BeaconDto;
import com.avatar.dto.enums.Location;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.dto.serializer.AccountDtoCheckInDateSerializer;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;
import com.google.gson.reflect.TypeToken;

// User Must be Authenticated! and must have admin role
@Controller
@RequestMapping(value = "/BeaconMgr")
public class BeaconManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = { Privilege.staff,
			Privilege.superUser };

	@Resource(name = "beaconService")
	BeaconBusiness beaconService;

	private final Type collectionAccountDtoType = new TypeToken<ArrayList<AccountDto>>() {
	}.getType();

	@RequestMapping(value = "/GetAmenityDeptName")
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
			final List<String> amenityNames = beaconService
					.getAmenityDeptName(clubId);
			apiResponse = new WsResponse<List<String>>(ResponseStatus.success,
					"", amenityNames, "amenityDeptList");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<String>>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	private BeaconDto getBeaconInstance(final String clubId,
			final String amenityId, final String beaconActionId,
			final String location, final String description,
			final String installerStaffUserId) throws InvalidParameterException {
		final BeaconDto retVal = new BeaconDto();
		retVal.setBeaconid(beaconActionId);
		retVal.setAmenityId(amenityId);
		retVal.setClubId(clubId);
		retVal.setInstallerStaffId(installerStaffUserId);
		retVal.setDescription(description);
		try {
			retVal.setLocation(Location.valueOf(location));
		} catch (final Exception e) {
			throw new InvalidParameterException("Invalid location " + location);
		}
		return retVal;
	}

	@RequestMapping(value = "/SetAmenityDeptName")
	public ModelAndView setAmenityDeptName(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "apnsToken") final String apnsToken,
			@RequestParam(required = true, value = "amenityDepartment") final String amenityDepartment,
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

		return setAmenityDeptName(authToken, apnsToken, amenityDepartment, clubId);
	}

	ModelAndView setAmenityDeptName(final String authToken,
			final String apnsToken, final String amenityDepartment,
			final String clubId) throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			beaconService.setAmenityDeptName(clubId, apnsToken,
					amenityDepartment);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/SetBeacon")
	public ModelAndView setBeacon(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "amenityId") final String amenityId,
			@RequestParam(required = true, value = "beaconActionId") final String beaconActionId,
			@RequestParam(required = true, value = "location") final String location,
			@RequestParam(required = true, value = "desc") final String description,
			@RequestParam(required = true, value = "installerStaffUserId") final String installerStaffUserId)
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
			final BeaconDto beacon = getBeaconInstance(clubId, amenityId,
					beaconActionId, location, description, installerStaffUserId);
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
			@RequestParam(required = true, value = "amenityDepartment") final String amenityDepartment,
			@RequestParam(required = false, value = "date") final String onDate
			)
			throws Exception {
		init();
		jsonView.register(ImmutablePair.class, new AccountDtoCheckInDateSerializer());

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
		WsResponse<List<ImmutablePair<AccountDto, Date>>> apiResponse = null;
		try {
			final List<ImmutablePair<AccountDto, Date>> users = beaconService.getUsers(
					amenityDepartment, entryDate);
			System.out.println(users.getClass());
			apiResponse = new WsResponse<List<ImmutablePair<AccountDto, Date>>>(
					ResponseStatus.success, "", users,
					collectionAccountDtoType, "users");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<ImmutablePair<AccountDto, Date>>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

}
