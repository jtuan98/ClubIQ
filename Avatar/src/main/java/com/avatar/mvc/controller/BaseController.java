package com.avatar.mvc.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.Assert;

import com.avatar.business.AccountBusiness;
import com.avatar.business.AuthenticationTokenizerBusiness;
import com.avatar.business.DbDateBusiness;
import com.avatar.business.NotificationBusiness;
import com.avatar.dto.ImagePic;
import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.promotion.Promotion;
import com.avatar.dto.serializer.DateSerializer;
import com.avatar.dto.serializer.ImagePicSerializer;
import com.avatar.dto.serializer.PairSerializer;
import com.avatar.dto.serializer.PromotionSerializer;
import com.avatar.dto.serializer.SurveyAnswerSerializer;
import com.avatar.dto.serializer.WsResponseSerializer;
import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;
import com.avatar.mvc.view.JsonView;

public abstract class BaseController {
	private static final Privilege[] superUser = new Privilege[] { Privilege.superUser };

	public static void main(final String[] args) {
		final DateTimeFormatter yyyyMMdd_hh24missDtf = DateTimeFormat
				.forPattern("yyyyMMdd HH:mm:ss");
		System.out.println(new Date(yyyyMMdd_hh24missDtf
				.parseMillis("20150530 20:00:00")));
		final DateTimeFormatter yyyyMMddDtf = DateTimeFormat
				.forPattern("yyyyMMdd");

		final Date fromDate = new Date(yyyyMMddDtf.parseMillis("20150517"));
		System.out.println("fromdate="+fromDate);
	}

	@Resource(name = "accountService")
	protected DbDateBusiness dateService;

	@Resource(name = "accountService")
	protected AccountBusiness accountService;

	@Resource(name = "emailSendService")
	protected NotificationBusiness emailNotificationService;

	@Resource(name = "apnsNotificationService")
	protected NotificationBusiness mobileNotificationService;

	@Resource(name = "authenticationTokenizer")
	protected AuthenticationTokenizerBusiness authenticationService;

	@Resource(name = "accountService")
	protected DbDateBusiness nowService;

	protected final DateTimeFormatter yyyyMMddDtf = DateTimeFormat
			.forPattern("yyyyMMdd");

	protected final DateTimeFormatter yyyyMMdd_hh24missDtf = DateTimeFormat
			.forPattern("yyyyMMdd HH:mm:ss");

	protected JsonView jsonView = null;

	protected Set<Privilege> getUserRoles(final String authToken)
			throws NotFoundException, AuthenticationTokenExpiredException,
			PermissionDeniedException, InvalidParameterException {
		final Set<Privilege> roles = authenticationService.getRoles(authToken);
		return roles;
	}

	protected void init() {
		jsonView = init(jsonView);
	}

	protected JsonView init(final JsonView myView) {
		JsonView retVal = myView;
		if (myView == null) {
			retVal = new JsonView();
			retVal.register(WsResponse.class, new WsResponseSerializer());
			retVal.register(ImagePic.class, new ImagePicSerializer());
			retVal.register(ImmutablePair.class, new PairSerializer());
			retVal.register(SurveyAnswer.class, new SurveyAnswerSerializer());
			retVal.register(Promotion.class, new PromotionSerializer());
			final DateSerializer dateSerializer = new DateSerializer(yyyyMMdd_hh24missDtf);
			retVal.register(Date.class, dateSerializer);
		}
		return retVal;
	}
	protected Map<String, Object> toModel(final Object data) {
		final Map<String, Object> retVal = new HashMap<>();
		retVal.put(JsonView.DATA, data);
		return retVal;
	}

	protected Map<String, Object> toModel(final String key1,
			final String value1, final String key2, final String value2) {
		final Map<String, Object> retVal = new HashMap<>();
		final Map<String, Object> value = new HashMap<>();
		value.put(key1, value1);
		value.put(key2, value2);
		retVal.put(JsonView.DATA, value);
		return retVal;
	}

	protected void validateStaffInClub(final AccountDto staff,
			final String clubId) throws PermissionDeniedException {
		Assert.notNull(staff);

		if (!staff.getPriviledges().contains(Privilege.superUser)) {
			Assert.notNull(clubId);
			if ((staff.getHomeClub() == null)
					|| (staff.getHomeClub().getClubId() == null)) {
				throw new PermissionDeniedException("Staff "
						+ staff.getUserId() + " is missing Home Club!");
			}
			final boolean retVal = clubId.equalsIgnoreCase(staff.getHomeClub()
					.getClubId());
			if (!retVal) {
				throw new PermissionDeniedException("Staff "
						+ staff.getUserId() + " is not in " + clubId);
			}
		}
	}

	protected void validateSuperUserRole(final String authToken)
			throws NotFoundException, AuthenticationTokenExpiredException,
			PermissionDeniedException, InvalidParameterException {
		validateUserRoles(authToken, superUser);
	}

	protected void validateUserRoles(final String authToken,
			final Privilege[] requiredRoles) throws NotFoundException,
			AuthenticationTokenExpiredException, PermissionDeniedException, InvalidParameterException {
		final Set<Privilege> roles = authenticationService.getRoles(authToken);
		boolean retVal = false;
		String msg = "";
		for (final Privilege privilege : requiredRoles) {
			msg += privilege.name() + " ";
			if (roles.contains(privilege)) {
				retVal = true;

				break;
			}
		}
		if (retVal == false) {
			throw new PermissionDeniedException("Roles [ " + msg + "] missing");
		}
	}
}