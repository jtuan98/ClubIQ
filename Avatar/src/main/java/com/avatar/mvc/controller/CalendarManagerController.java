package com.avatar.mvc.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import com.avatar.dto.serializer.DateSerializer;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

@Controller
@RequestMapping(value = "/CalendarMgr")
public class CalendarManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = Privilege.values();

	public static void main(final String[] args) {
		final Calendar localCalendar = Calendar.getInstance(TimeZone
				.getDefault());
		System.out.println(localCalendar.get(Calendar.YEAR));
	}

	@Resource(name = "beaconService")
	private BeaconBusiness beaconService;

	final Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());

	protected final DateTimeFormatter yyyyMMddDtf = DateTimeFormat
			.forPattern("yyyyMMdd");

	protected final DateTimeFormatter ddMMyyyyDtf = DateTimeFormat
			.forPattern("ddMMyyyy");

	// Phase 2
	@RequestMapping(value = { "/getBlackOutDates", "/GetBlackOutDates" })
	public ModelAndView getBlackOutDates(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId,
			@RequestParam(required = false, value = "blackOutYear") final String blackOutYear,
			/*
			 * month is 1 - 12
			 */
			@RequestParam(required = false, value = "blackOutMonth") final String blackOutMonth)
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
		String month = blackOutMonth;
		if (StringUtils.isEmpty(month)) {
			month = getCurrentMonth();
		}
		WsResponse<List<BlackoutDate>> apiResponse = null;
		try {
			final List<BlackoutDate> dates = beaconService.getBlackoutDates(
					clubId, subAmenityId,
					StringUtils.isEmpty(blackOutYear) ? getCurrentYear()
							: blackOutYear, month);
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
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId,
			@RequestParam(required = false, value = "blackOutYear") final String blackOutYear,
			@RequestParam(required = false, value = "blackOutMonth") final String blackOutMonth,
			@RequestParam(required = false, value = "blackOutDate") final String blackOutDate)
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
					clubId, subAmenityId,
					StringUtils.isEmpty(blackOutYear) ? getCurrentYear()
							: blackOutYear,
							StringUtils.isEmpty(blackOutMonth) ? getCurrentMonth()
									: blackOutMonth,
									StringUtils.isEmpty(blackOutDate) ? getCurrentDate()
											: blackOutDate
					);
			apiResponse = new WsResponse<List<BlackoutTime>>(
					ResponseStatus.success, "", times, "blackoutTimes");
		} catch (final Exception e) {
			apiResponse = new WsResponse<List<BlackoutTime>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/getBlackOutTimesDateRange", "/GetBlackOutTimesDateRange" })
	public ModelAndView getBlackOutTimesDateRange(
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId,
			@RequestParam(required = true, value = "blackOutFromDate") final String blackOutFromDateDDMMYYYY,
			@RequestParam(required = true, value = "blackOutToDate") final String blackOutToDateDDMMYYYY)
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
		WsResponse<Map<String, List<BlackoutTime>>> apiResponse = null;
		try {
			final Date blackoutDateTimeFrom = ddMMyyyyDtf.parseDateTime(
					blackOutFromDateDDMMYYYY).toDate();
			final Date blackoutDateTimeTo = DateUtils.addDays(ddMMyyyyDtf.parseDateTime(
					blackOutToDateDDMMYYYY).toDate(), 1);
			final Map<String, List<BlackoutTime>> times = beaconService.getBlackoutTimes(
					clubId, subAmenityId,
					blackoutDateTimeFrom, blackoutDateTimeTo);
			apiResponse = new WsResponse<Map<String, List<BlackoutTime>>>(
					ResponseStatus.success, "", times, "blackoutTimes");
		} catch (final Exception e) {
			apiResponse = new WsResponse<Map<String, List<BlackoutTime>>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	private String getCurrentDate() {
		final int currentDate = localCalendar.get(Calendar.DAY_OF_MONTH);
		return String.valueOf(currentDate);
	}

	private String getCurrentMonth() {
		final int currentMonth = localCalendar.get(Calendar.MONTH) + 1;
		return String.valueOf(currentMonth);
	}

	private String getCurrentYear() {
		final int currentYear = localCalendar.get(Calendar.YEAR);
		return String.valueOf(currentYear);
	}

	@Override
	protected void init() {
		super.init();
		final DateSerializer dateSerializer = new DateSerializer(yyyyMMddDtf);
		jsonView.register(Date.class, dateSerializer);

	}

	@RequestMapping(value = { "/setBlackOut", "/SetBlackOut" })
	public ModelAndView setCheckInfo(
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId,
			@RequestParam(required = false, value = "blackOutYear", defaultValue = "-1") final int blackOutYear,
			@RequestParam(required = true, value = "blackOutMonth", defaultValue = "1") final int blackOutMonth /*
			 * month
			 * is
			 * 1
			 * -
			 * 12
			 */,
			 @RequestParam(required = true, value = "blackOutDate", defaultValue = "1") final int blackOutDate,
			 @RequestParam(required = true, value = "blackOutTime") final String blackOutTime)
					 throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, superUserOrStaff);
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
			//Validate blackOutTime
			validateBlackoutTimes(blackOutTime);
			final String year = blackOutYear == -1 ? getCurrentYear() : Integer
					.toString(blackOutYear);
			final String requestedDateTimemmdd = String.format("%s%02d%02d",
					year, blackOutMonth, blackOutDate);
			final Date blackoutDateTime = yyyyMMddDtf.parseDateTime(
					requestedDateTimemmdd).toDate();
			beaconService.setBlackoutTimes(clubId, subAmenityId,
					blackoutDateTime, blackOutTime);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "");
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = { "/setBlackOutDateRange", "/SetBlackOutDateRange" })
	public ModelAndView setCheckInfoDateRange(
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId,
			@RequestParam(required = true, value = "blackOutFromDate") final String blackOutFromDateDDMMYYYY,
			@RequestParam(required = true, value = "blackOutToDate") final String blackOutToDateDDMMYYYY,
			@RequestParam(required = true, value = "blackOutTime") final String blackOutTime)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, superUserOrStaff);
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
			//Validate blackOutTime
			validateBlackoutTimes(blackOutTime);
			final Date blackoutDateTimeFrom = ddMMyyyyDtf.parseDateTime(
					blackOutFromDateDDMMYYYY).toDate();
			final Date blackoutDateTimeTo = DateUtils.addDays(ddMMyyyyDtf.parseDateTime(
					blackOutToDateDDMMYYYY).toDate(), 1);

			Date blackoutDateTime = blackoutDateTimeFrom;
			do {
				beaconService.setBlackoutTimes(clubId, subAmenityId,
						blackoutDateTime, blackOutTime);
				blackoutDateTime = DateUtils.addDays(blackoutDateTime, 1);
			} while (blackoutDateTime.before(blackoutDateTimeTo));
			apiResponse = new WsResponse<String>(ResponseStatus.success, "");
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	private void validateBlackoutTimes(final String blackOutTime) throws InvalidParameterException {
		if (!blackOutTime.contains("Y") && !blackOutTime.contains("N")) {
			throw new InvalidParameterException("blackoutTime must only contain Y or N");
		}
		if (blackOutTime.length() < 24*2) {
			throw new InvalidParameterException("blackoutTime length must be 48.  30 min blocks");
		}
	}

}