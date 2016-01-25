package com.avatar.mvc.controller;

import java.io.File;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.business.BeaconBusiness;
import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.club.SubAmenityDto;
import com.avatar.dto.enums.ClubListingSortBy;
import com.avatar.dto.enums.DbTimeZone;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.dto.serializer.AmenityListingSerializer;
import com.avatar.dto.serializer.ClubAddressSerializer;
import com.avatar.dto.serializer.ClubConciergeNotifEmailSerializer;
import com.avatar.dto.serializer.ClubListingSerializer;
import com.avatar.dto.serializer.SubAmenityListingSerializer;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;
import com.avatar.mvc.view.JsonView;
import com.avatar.mvc.view.RenderingImageView;
import com.avatar.util.Md5Sum;

@Controller
@RequestMapping(value = "/ClubMgr")
public class ClubManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = { Privilege.clubAdmin,
		Privilege.staff, Privilege.superUser };

	private static Privilege[] ANY_ROLE = Privilege.values();

	@Resource(name = "beaconManagerController")
	BeaconManagerController beaconManager;

	@Resource(name = "beaconService")
	private BeaconBusiness beaconService;

	protected JsonView jsonAmenitiesListingView = null;
	protected JsonView jsonClubAddressView = null;
	private JsonView jsonClubListingView;
	private JsonView jsonClubNotificationEmailView;
	private final RenderingImageView imageRenderer = new RenderingImageView();

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
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();
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
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();

		WsResponse<List<SubAmenityDto>> apiResponse = null;
		try {
			final List<SubAmenityDto> subAmenities = beaconService
					.getSubAmenities(clubId);
			apiResponse = new WsResponse<List<SubAmenityDto>>(
					ResponseStatus.success, "", subAmenities, "subAmenities");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<SubAmenityDto>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonAmenitiesListingView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/GetAmenityHeadline", "/getAmenityHeadline" })
	public ModelAndView getAmenityHeadline(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "amenityId") final String amenityId)
					throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			final String headerText = beaconService.getAmenityHeaderText(clubId, amenityId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					headerText, "headerText");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/GetAmenityPhotoHeadline", "/getAmenityPhotoHeadline" })
	public ModelAndView getAmenityPhotoHeadline(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "amenityId") final String amenityId)
					throws Exception {
		init();
		try {
			final AmenityDto amenity = beaconService.getAmenity(clubId, amenityId);
			final String image = amenity.getImage() != null? amenity.getImage().getPictureAsBase64String(): "";
			final String headerText = amenity.getHeader();
			final Map<String, String> data = new HashMap<>();
			data.put("pictureBase64", image);
			data.put("amenityHeaderText", headerText);
			final WsResponse<Map<String, String>> apiResponse = new WsResponse<Map<String, String>>(ResponseStatus.success, "",
					data, null);
			return new ModelAndView(jsonClubListingView, toModel(apiResponse));
		} catch (final Exception e) {
			final WsResponse<String> apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
			return new ModelAndView(jsonClubListingView, toModel(apiResponse));
		}
	}


	// Phase 2
	@RequestMapping(value = { "/GetClubAddress", "/getClubAddress" })
	public ModelAndView getClubAddress(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();

		WsResponse<ClubDto> apiResponse = null;
		try {
			final ClubDto club = beaconService.getClub(clubId);
			apiResponse = new WsResponse<ClubDto>(ResponseStatus.success, "",
					club);
		} catch (final Exception e) {
			apiResponse = new WsResponse<ClubDto>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubAddressView, toModel(apiResponse));
	}


	// Phase 2
	@RequestMapping(value = { "/GetClubBody", "/getClubBody" })
	public ModelAndView getClubBody(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();

		WsResponse<String> apiResponse = null;
		try {
			final String bodyText = beaconService.getClubBodyText(clubId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					bodyText, "bodyText");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/GetClubDetail", "/getClubDetail" })
	public ModelAndView getClubDetail(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();

		WsResponse<ClubDto> apiResponse = null;
		try {
			final ClubDto club = beaconService.getClubDetails(clubId);
			apiResponse = new WsResponse<ClubDto>(ResponseStatus.success, "",
					club);
		} catch (final Exception e) {
			apiResponse = new WsResponse<ClubDto>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubAddressView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/GetClubHeadline", "/getClubHeadline" })
	public ModelAndView getClubHeadline(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();

		WsResponse<String> apiResponse = null;
		try {
			final String headlineText = beaconService.getClubHeadlineText(clubId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					headlineText, "headlineText");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	@RequestMapping(value = "/GetClubList")
	public ModelAndView getClubList(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken)
					throws Exception {
		init();
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

	// Phase 2
	@RequestMapping(value = { "/GetClubListByAlpha", "/getClubListByAlpha" })
	public ModelAndView getClubListByAlpha(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = false, value = "state") final String state)
					throws Exception {
		init();
		WsResponse<List<ClubDto>> apiResponse = null;
		try {
			final List<ClubDto> clubs = beaconService.getClubs(state,
					ClubListingSortBy.clubName);
			apiResponse = new WsResponse<List<ClubDto>>(ResponseStatus.success,
					"", clubs, "clubList");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<ClubDto>>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/GetClubListByState", "/getClubListByState" })
	public ModelAndView getClubListByState(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = false, value = "state") final String state)
					throws Exception {
		init();
		WsResponse<List<ClubDto>> apiResponse = null;
		try {
			final List<ClubDto> clubs = beaconService.getClubs(state,
					ClubListingSortBy.state);
			apiResponse = new WsResponse<List<ClubDto>>(ResponseStatus.success,
					"", clubs, "clubList");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<ClubDto>>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/GetClubName", "/getClubName" })
	public ModelAndView getClubName(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "clubKeyCode") final String clubKeycode)
					throws Exception {
		init();
		WsResponse<ClubDto> apiResponse = null;
		try {
			final ClubDto club = beaconService.getClubByKeycode(clubKeycode);
			apiResponse = new WsResponse<ClubDto>(ResponseStatus.success, "",
					club);
		} catch (final Exception e) {
			apiResponse = new WsResponse<ClubDto>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/GetClubNotifEmail", "/getClubNotifEmail" })
	public ModelAndView getClubNotifEmail(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();

		WsResponse<ClubDto> apiResponse = null;
		try {
			final ClubDto club = beaconService.getClubDetails(clubId);
			apiResponse = new WsResponse<ClubDto>(ResponseStatus.success, "",
					club);
		} catch (final Exception e) {
			apiResponse = new WsResponse<ClubDto>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubNotificationEmailView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/GetClubPhoto", "/getClubPhoto" })
	public ModelAndView getClubPhoto(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();

		WsResponse<String> apiResponse = null;
		try {
			final ClubDto club = beaconService.getClub(clubId);
			final String image = club.getImage() != null? club.getImage().getPictureAsBase64String(): "";
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					image, "pictureBase64");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubAddressView, toModel(apiResponse));
	}

	private ClubDto getInstance(final String clubId, final String clubName,
			final String clubAddress, final String clubZip,
			final String clubState, final String clubPhoneNumber,
			final String hzRestriction, final String xCoord,
			final String yCoord, final DbTimeZone timezone) {
		final ClubDto retVal = new ClubDto();
		retVal.setClubId(clubId);
		retVal.setClubName(clubName);
		retVal.setAddress(clubAddress);
		retVal.setZipCode(clubZip);
		retVal.setState(clubState);
		retVal.setPhoneNumber(clubPhoneNumber);
		retVal.setHzRestriction(hzRestriction);
		retVal.setTimeZone(timezone);
		retVal.setXcoord(xCoord);
		retVal.setYcoord(yCoord);
		return retVal;
	}

	// Phase 2
	@RequestMapping(value = { "/GetSubAmenityBody", "/getSubAmenityBody" })
	public ModelAndView getSubAmenityBody(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId)
					throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			final String bodyText = beaconService.getSubAmenityBodyText(clubId, subAmenityId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					bodyText, "bodyText");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/GetSubAmenityHeadline", "/getSubAmenityHeadline" })
	public ModelAndView getSubAmenityHeadline(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId)
					throws Exception {
		init();

		WsResponse<String> apiResponse = null;
		try {
			final String headerText = beaconService.getSubAmenityHeaderText(clubId, subAmenityId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					headerText, "headerText");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/GetSubAmenityList", "/getSubAmenityList" })
	public ModelAndView getSubAmenityList(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "amenityId") final String amenityId)
					throws Exception {
		init();

		WsResponse<List<SubAmenityDto>> apiResponse = null;
		try {
			final List<SubAmenityDto> amenities = beaconService.getSubAmenityList(clubId, amenityId);
			apiResponse = new WsResponse<List<SubAmenityDto>>(ResponseStatus.success, "",
					amenities, "subAmenities");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<SubAmenityDto>>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/GetSubAmenitySecondaryHeadline", "/getSubAmenitySecondaryHeadline" })
	public ModelAndView getSubAmenitySecondaryHeadline(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId)
					throws Exception {
		init();

		WsResponse<String> apiResponse = null;
		try {
			final String headerText = beaconService.getSubAmenitySecondaryHeaderText(clubId, subAmenityId);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					headerText, "headerText");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	@Override
	protected void init() {
		super.init();
		jsonAmenitiesListingView = init(jsonAmenitiesListingView);
		jsonClubListingView = init(jsonClubListingView);
		jsonClubAddressView = init(jsonClubAddressView);
		jsonClubNotificationEmailView = init(jsonClubNotificationEmailView);
		jsonClubNotificationEmailView.register(ClubDto.class, new ClubConciergeNotifEmailSerializer());
		jsonClubAddressView.register(ClubDto.class, new ClubAddressSerializer());
		jsonClubAddressView.register(AmenityDto.class, new AmenityListingSerializer());
		jsonClubListingView.register(ClubDto.class, new ClubListingSerializer());
		jsonAmenitiesListingView.register(AmenityDto.class,
				new AmenityListingSerializer());
		jsonAmenitiesListingView.register(SubAmenityDto.class,
				new SubAmenityListingSerializer());

	}

	@RequestMapping(value = "/RegisterAPNsToken")
	public ModelAndView registerAPNsToken(
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
				clubId, null);
	}

	@RequestMapping(value = { "/render/ClubAmenityPhoto", "/render/clubAmenityPhoto" })
	public ModelAndView renderClubAmenityPhoto(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "amenityId") final String amenityId)
					throws Exception {
		init();
		byte[] image = null;
		try {
			final AmenityDto amenity = beaconService.getAmenity(clubId, amenityId);
			image = amenity.getImage() != null? amenity.getImage().getPicture(): null;

		} catch (final Exception e) {
		}
		return new ModelAndView(imageRenderer, toModel(image));
	}

	@RequestMapping(value = { "/render/TestPhoto", "/render/testPhoto" })
	public ModelAndView renderClubPhoto(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "filename") final String filename
			)
					throws Exception {
		init();
		final byte[] image = FileUtils.readFileToByteArray(new File(filename));
		return new ModelAndView(imageRenderer, toModel(image));
	}


	@RequestMapping(value = { "/render/ClubPhoto", "/render/clubPhoto" })
	public ModelAndView renderClubPhoto(
			final HttpServletRequest req,
			@RequestParam(required = false, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId)
					throws Exception {
		init();
		byte[] image = null;
		try {
			final ClubDto club = beaconService.getClub(clubId);
			if (club.getImage() != null) {
				image = club.getImage().getPicture();
			}

		} catch (final Exception e) {
		}
		return new ModelAndView(imageRenderer, toModel(image));
	}

	// Phase 2
	@RequestMapping(value = { "/SetAmenityHeadline", "/setAmenityHeadline" })
	public ModelAndView setAmenityHeadline(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "amenityId") final String amenityId,
			@RequestParam(required = true, value = "headerText") final String headerText)
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
			beaconService.setAmenityHeaderText(clubId, amenityId, headerText);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	// Phase2
	@RequestMapping(value = { "/SetAmenityPhoto", "/setAmenityPhoto" })
	public ModelAndView setAmenityPhoto(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "amenityId") final String amenityId,
			@RequestParam(required = true, value = "pictureBase64") final String pictureBase64
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

		WsResponse<String> apiResponse = null;
		try {
			beaconService.setAmenityPhoto(clubId, amenityId, pictureBase64);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "", "");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonAmenitiesListingView, toModel(apiResponse));
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
			@RequestParam(required = false, value = "xCoord") final String xCoord,
			@RequestParam(required = false, value = "yCoord") final String yCoord,
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
				clubZip, clubState, clubPhoneNumber, hzRestriction, xCoord, yCoord, timezone);

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

	// Phase 2
	@RequestMapping(value = { "/SetClubBody", "/setClubBody" })
	public ModelAndView setClubBody(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "bodyText") final String bodyText)
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
			beaconService.setClubBodyText(clubId, bodyText);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/SetClubHeadline", "/setClubHeadline" })
	public ModelAndView setClubHeadline(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "headerText") final String headerText)
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
			beaconService.setClubHeaderText(clubId, headerText);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}


	// Phase2
	@RequestMapping(value = { "/SetClubPhoto", "/setClubPhoto" })
	public ModelAndView setClubPhoto(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "pictureBase64") final String pictureBase64,
			@RequestParam(required = false, value = "md5HashValidate") final String pictureMd5Hash
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

		WsResponse<String> apiResponse = null;
		try {
			final String hashBase64Decoded = Md5Sum.hashStringBase64Data(pictureBase64);
			if (StringUtils.isNotEmpty(pictureMd5Hash) && hashBase64Decoded.equalsIgnoreCase(pictureMd5Hash)) {
				throw new InvalidParameterException("Md5 not matching...");
			}
			beaconService.setClubPhoto(clubId, pictureBase64);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "", "");
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonAmenitiesListingView, toModel(apiResponse));
	}


	// Phase 2
	@RequestMapping(value = { "/SetSubAmenityBody", "/setSubAmenityBody" })
	public ModelAndView setSubAmenityBody(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId,
			@RequestParam(required = true, value = "bodyText") final String bodyText)
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
			beaconService.setSubAmenityBodyText(clubId, subAmenityId, bodyText);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/SetSubAmenityHeadline", "/setSubAmenityHeadline" })
	public ModelAndView setSubAmenityHeadline(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId,
			@RequestParam(required = true, value = "headerText") final String headerText)
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
			beaconService.setSubAmenityHeaderText(clubId, subAmenityId, headerText);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}

	// Phase 2
	@RequestMapping(value = { "/SetSubAmenitySecondaryHeadline", "/setSubAmenitySecondaryHeadline" })
	public ModelAndView setSubAmenitySecondaryHeadline(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId,
			@RequestParam(required = true, value = "headerText") final String headerText)
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
			beaconService.setSubAmenitySecondaryHeaderText(clubId, subAmenityId, headerText);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonClubListingView, toModel(apiResponse));
	}
}
