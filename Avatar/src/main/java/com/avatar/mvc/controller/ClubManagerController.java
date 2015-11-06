package com.avatar.mvc.controller;

import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.business.BeaconBusiness;
import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.BlackoutDate;
import com.avatar.dto.club.BlackoutTime;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.ClubListingSortBy;
import com.avatar.dto.enums.DbTimeZone;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.dto.serializer.AmenityListingSerializer;
import com.avatar.dto.serializer.ClubListingSerializer;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;
import com.avatar.mvc.view.JsonView;

@Controller
@RequestMapping(value = "/ClubMgr")
public class ClubManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = { Privilege.clubAdmin,
		Privilege.staff, Privilege.superUser };

	@Resource(name = "beaconManagerController")
	BeaconManagerController beaconManager;

	@Resource(name = "beaconService")
	private BeaconBusiness beaconService;

	protected JsonView jsonAmenitiesListingView = null;
	private JsonView jsonClubListingView;

	@RequestMapping(value = "/ClubPinVerification")
	public ModelAndView clubPinVerify(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "clubPin") final String clubPin)
					throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			final boolean verified = beaconService.verifyClubPin(clubPin);
			apiResponse = new WsResponse<String>(
					verified ? ResponseStatus.success : ResponseStatus.failure,
							verified ? "" : "club pin not found.", null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

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

	// Phase2
	@RequestMapping(value = { "/GetAmenityDept", "/getAmenityDept" })
	public ModelAndView getAmenityDept(
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

		WsResponse<List<AmenityDto>> apiResponse = null;
		try {
			final List<AmenityDto> amenities = beaconService
					.getAmenities(clubId);
			apiResponse = new WsResponse<List<AmenityDto>>(
					ResponseStatus.success, "", amenities, "amenities");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<AmenityDto>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonAmenitiesListingView, toModel(apiResponse));
	}


	// Phase 2
	@RequestMapping(value = {"/getBlackOutDates", "/GetBlackOutDates"})
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
			final List<BlackoutDate> dates = beaconService.getBlackoutDates(clubId, amenityId, month);
			apiResponse = new WsResponse<List<BlackoutDate>>(ResponseStatus.success,
					"", dates, "blackoutDates");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<BlackoutDate>>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}


	// Phase 2
	@RequestMapping(value = {"/getBlackOutTimes", "/GetBlackOutTimes"})
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
			final List<BlackoutTime> times = beaconService.getBlackoutTimes(clubId, amenityId, requestedDateMMDD);
			apiResponse = new WsResponse<List<BlackoutTime>>(ResponseStatus.success,
					"", times, "blackoutTimes");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<BlackoutTime>>(ResponseStatus.failure,
					e.getMessage(), null);
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
		final AccountDto principal = authenticationService
				.getAccount(authToken);
		WsResponse<List<ClubDto>> apiResponse = null;
		try {
			final List<ClubDto> clubs = beaconService.getClubs(principal
					.getId());
			apiResponse = new WsResponse<List<ClubDto>>(ResponseStatus.success,
					"", clubs, "clubList");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<ClubDto>>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}


	//Phase 2
	@RequestMapping(value = {"/GetClubListByAlpha", "/getClubListByAlpha"})
	public ModelAndView getClubListByAlpha(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = false, value = "state") final String state)
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
		WsResponse<List<ClubDto>> apiResponse = null;
		try {
			final List<ClubDto> clubs = beaconService.getClubs(state, ClubListingSortBy.clubName);
			apiResponse = new WsResponse<List<ClubDto>>(ResponseStatus.success,
					"", clubs, "clubList");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<ClubDto>>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	//Phase 2
	@RequestMapping(value = {"/GetClubListByState", "/getClubListByState"})
	public ModelAndView getClubListByState(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = false, value = "state") final String state)
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
		WsResponse<List<ClubDto>> apiResponse = null;
		try {
			final List<ClubDto> clubs = beaconService.getClubs(state, ClubListingSortBy.state);
			apiResponse = new WsResponse<List<ClubDto>>(ResponseStatus.success,
					"", clubs, "clubList");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<ClubDto>>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = {"/GetClubName/{clubKeycode}", "/getClubName/{clubKeycode}"})
	public ModelAndView getClubName(
			final HttpServletRequest req,
			@PathVariable(value = "clubKeycode") final String clubKeycode)
					throws Exception {
		init();
		WsResponse<ClubDto> apiResponse = null;
		try {
			final ClubDto club = beaconService.getClubByKeycode(clubKeycode);
			apiResponse = new WsResponse<ClubDto>(ResponseStatus.success,
					"", club);
		} catch (final Exception e) {
			apiResponse = new WsResponse<ClubDto>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
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

	@Override
	protected void init() {
		super.init();
		jsonAmenitiesListingView = init (jsonAmenitiesListingView);
		jsonClubListingView = init(jsonClubListingView);
		jsonClubListingView.register(ClubDto.class,
				new ClubListingSerializer());
		jsonAmenitiesListingView.register(AmenityDto.class,
				new AmenityListingSerializer());
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
			validateStaffInClub(authenticationService.getAccount(authToken),
					clubId);
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
			@RequestParam(required = false, value = "timezone", defaultValue = "US_PST") final DbTimeZone timezone)
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
