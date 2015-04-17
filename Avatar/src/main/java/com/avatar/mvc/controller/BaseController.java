package com.avatar.mvc.controller;

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
import com.avatar.business.NotificationBusiness;
import com.avatar.business.NowBusiness;
import com.avatar.dto.ImagePic;
import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.promotion.Promotion;
import com.avatar.dto.serializer.ImagePicSerializer;
import com.avatar.dto.serializer.PairSerializer;
import com.avatar.dto.serializer.PromotionSerializer;
import com.avatar.dto.serializer.SurveyAnswerSerializer;
import com.avatar.dto.serializer.WsResponseSerializer;
import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;
import com.avatar.mvc.view.JsonView;

public abstract class BaseController {
	@Resource(name = "accountService")
	protected AccountBusiness accountService;

	@Resource(name = "emailSendService")
	protected NotificationBusiness emailNotificationService;

	@Resource(name = "apnsNotificationService")
	protected NotificationBusiness mobileNotificationService;

	@Resource(name = "authenticationTokenizer")
	protected AuthenticationTokenizerBusiness authenticationService;

	@Resource(name = "accountService")
	protected NowBusiness nowService;

	protected final DateTimeFormatter yyyyMMddDtf = DateTimeFormat.forPattern("yyyyMMdd");

	protected JsonView jsonView = null;

	protected void init() {
		if (jsonView == null) {
			jsonView = new JsonView();
			jsonView.register(WsResponse.class, new WsResponseSerializer());
			jsonView.register(ImagePic.class, new ImagePicSerializer());
			jsonView.register(ImmutablePair.class, new PairSerializer());
			jsonView.register(SurveyAnswer.class, new SurveyAnswerSerializer());
			jsonView.register(Promotion.class, new PromotionSerializer());
		}
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
			Assert.notNull(staff.getHomeClub());
			Assert.notNull(staff.getHomeClub().getClubId());
			Assert.notNull(clubId);
			final boolean retVal = clubId.equalsIgnoreCase(staff.getHomeClub()
					.getClubId());
			if (!retVal) {
				throw new PermissionDeniedException("Staff "
						+ staff.getUserId() + " is not in " + clubId);
			}
		}
	}

	protected void validateUserRoles(final String authToken,
			final Privilege[] requiredRoles) throws NotFoundException,
			AuthenticationTokenExpiredException, PermissionDeniedException {
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
