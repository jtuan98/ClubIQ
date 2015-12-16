package com.avatar.mvc.controller;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.business.PromotionBusiness;
import com.avatar.business.SurveyBusiness;
import com.avatar.dto.WsResponse;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.club.SubAmenityDto;
import com.avatar.dto.enums.Privilege;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.dto.promotion.Promotion;
import com.avatar.dto.survey.Survey;
import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

@Controller
@RequestMapping(value = "/SurveyMgr")
public class SurveyManagerController extends BaseController {
	private static Privilege[] REQUIRED_ROLE = { Privilege.staff,
		Privilege.superUser };

	@Resource(name = "surveyService")
	private SurveyBusiness surveyService;

	@Resource(name = "promotionService")
	private PromotionBusiness promotionService;

	@RequestMapping(value = "/DeletePromotion")
	public ModelAndView deletePromotion(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "promotionId") final Integer promotionId)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Use authToken and check if staff is linked to the clubId and
			// amenityId or not.
			final Promotion promo = promotionService.getPromotion(promotionId);
			validateStaffInClub(authenticationService.getAccount(authToken),
					promo.getClub().getClubId());
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		WsResponse<List<Promotion>> apiResponse = null;
		try {
			promotionService.delete(promotionId);
			apiResponse = new WsResponse<List<Promotion>>(
					ResponseStatus.success, "", null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<List<Promotion>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/DeleteSurveyPromoBeaconInfo")
	public ModelAndView deleteSurveyPromoBeaconInfo(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "mobileNumber") final String mobileNumber,
			@RequestParam(required = true, value = "fromDate") final String fromDateStr,
			@RequestParam(required = true, value = "toDate") final String toDateStr)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateSuperUserRole(authToken);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		WsResponse<List<Promotion>> apiResponse = null;
		try {
			final Date fromDate = new Date(yyyyMMddDtf.parseMillis(fromDateStr));
			Date toDate = new Date(yyyyMMddDtf.parseMillis(toDateStr));
			toDate = DateUtils.addDays(toDate, 1);
			promotionService.cleanupPromoBeaconInfo(mobileNumber, fromDate,
					toDate);
			surveyService.deleteSurveyAnswers(mobileNumber, fromDate, toDate);
			apiResponse = new WsResponse<List<Promotion>>(
					ResponseStatus.success, "", null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<List<Promotion>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/GetPromotions")
	public ModelAndView fetchPromotions(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "mobileNumber") final String memberId,
			@RequestParam(required = true, value = "beaconActionId") final String beaconActionId)
					throws Exception {
		init();
		WsResponse<List<Promotion>> apiResponse = null;
		try {
			final List<Promotion> promotions = promotionService
					.getPromotions(beaconActionId);
			apiResponse = new WsResponse<List<Promotion>>(
					ResponseStatus.success, "", promotions, "promotions");
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<List<Promotion>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/GetSurveyQuestion")
	public ModelAndView fetchSurveyQuestion(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "mobileNumber") final String memberId,
			@RequestParam(required = true, value = "beaconActionId") final String beaconActionId)
					throws Exception {
		init();
		WsResponse<Survey> apiResponse = null;
		try {
			final Survey survey = surveyService.getNextSurvey(beaconActionId,
					memberId);
			if (survey != null) {
				apiResponse = new WsResponse<Survey>(ResponseStatus.success,
						"", survey, "survey");
			} else {
				apiResponse = new WsResponse<Survey>(ResponseStatus.success,
						"No More Surveys", null, "");
			}
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<Survey>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	Promotion getPromotionInstance(final String clubId, final String subAmenityId,
			final String promotionTitle, final String promotionDetails,
			final String effectiveDateYYYYMMDDHH24MISS,
			final String endingDateYYYYMMDDHH24MISS)
					throws InvalidParameterException {
		final Promotion promotion = new Promotion();
		promotion.setSubAmenity(new SubAmenityDto());
		promotion.getSubAmenity().setSubAmenityId(subAmenityId);
		promotion.setClub(new ClubDto());
		promotion.getClub().setClubId(clubId);
		promotion.setTitle(promotionTitle);
		promotion.setDescription(promotionDetails);
		if (!StringUtils.isEmpty(effectiveDateYYYYMMDDHH24MISS)) {
			try {
				promotion.setEffectiveDate(new Date(yyyyMMdd_hh24missDtf
						.parseMillis(effectiveDateYYYYMMDDHH24MISS)));
			} catch (final IllegalArgumentException e) {
				throw new InvalidParameterException("Invalid EffectiveDate: "
						+ effectiveDateYYYYMMDDHH24MISS);
			}
		}
		if (!StringUtils.isEmpty(endingDateYYYYMMDDHH24MISS)) {
			try {
				promotion.setEndingDate(new Date(yyyyMMdd_hh24missDtf
						.parseMillis(endingDateYYYYMMDDHH24MISS)));
			} catch (final IllegalArgumentException e) {
				throw new InvalidParameterException("Invalid EndingDate: "
						+ endingDateYYYYMMDDHH24MISS);
			}
		}
		return promotion;
	}

	@RequestMapping(value = "/GetPromotionsList")
	public ModelAndView getPromotionsList(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Use authToken and check if staff is linked to the clubId and
			// amenityId or not.
			validateStaffInClub(authenticationService.getAccount(authToken),
					clubId);
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		WsResponse<List<Promotion>> apiResponse = null;
		try {
			final List<Promotion> promotions = promotionService.getPromotions(
					clubId, subAmenityId);
			apiResponse = new WsResponse<List<Promotion>>(
					ResponseStatus.success, "", promotions, "promotions");
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<List<Promotion>>(
					ResponseStatus.failure, e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/PromotionRead")
	public ModelAndView markPromotionRead(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "mobileNumber") final String memberId,
			@RequestParam(required = true, value = "promotionId") final Integer promotionIdPk,
			@RequestParam(required = true, value = "promotionRead", defaultValue = "N") final String promotionRead)
					throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		try {
			promotionService.recordPromotionRead(promotionIdPk, memberId,
					"Y".equalsIgnoreCase(promotionRead));
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/SetPromotions")
	public ModelAndView newPromotion(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "clubId") final String clubId,
			@RequestParam(required = true, value = "subAmenityId") final String subAmenityId,
			@RequestParam(required = true, value = "promotionTitle") final String promotionTitle,
			@RequestParam(required = true, value = "promotionDetail") final String promotionDetails,
			@RequestParam(required = true, value = "effectiveDate") final String effectiveDateYYYYMMDDHH24MISS,
			@RequestParam(required = true, value = "endingDate") final String endingDateYYYYMMDDHH24MISS)
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
		// TODO: Use authToken and check if staff is linked to the clubId and
		// amenityId or not.
		WsResponse<String> apiResponse = null;
		try {
			final Promotion promotion = getPromotionInstance(clubId, subAmenityId,
					promotionTitle, promotionDetails,
					effectiveDateYYYYMMDDHH24MISS, endingDateYYYYMMDDHH24MISS);

			promotionService.newPromotion(promotion);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/SetSurvey")
	public ModelAndView setSurveyAnswers(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "mobileNumber") final String memberId,
			@RequestParam(required = true, value = "beaconActionId") final String beaconActionId,
			@RequestParam(required = true, value = "surveyQuestionId") final int surveyQuestionId,
			@RequestParam(required = true, value = "answerA") final int answerA,
			@RequestParam(required = true, value = "answerB") final int answerB)
					throws Exception {
		init();
		WsResponse<String> apiResponse = null;
		final SurveyAnswer surveyAnswer = new SurveyAnswer();
		surveyAnswer.setSurvey(new Survey(surveyQuestionId));

		surveyAnswer.setAnswerA(answerA);
		surveyAnswer.setAnswerB(answerB);
		try {
			surveyService.persistSurveyAnswer(beaconActionId, memberId,
					surveyAnswer);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}

	@RequestMapping(value = "/UpdatePromotions")
	public ModelAndView updatePromotions(
			final Principal principal,
			final HttpServletRequest req,
			@RequestParam(required = true, value = "authToken") final String authToken,
			@RequestParam(required = true, value = "promotionId") final Integer promotionId,
			@RequestParam(required = true, value = "promotionTitle") final String promotionTitle,
			@RequestParam(required = true, value = "promotionDetails") final String promotionDetails,
			@RequestParam(required = false, value = "effectiveDate") final String effectiveDateYYYYMMDDHH24MISS,
			@RequestParam(required = false, value = "endingDate") final String endingDateYYYYMMDDHH24MISS)
					throws Exception {
		init();
		WsResponse<String> apiDeniedResponse = null;
		try {
			validateUserRoles(authToken, REQUIRED_ROLE);
			// Use authToken and check if staff is linked to the clubId and
			// amenityId or not.
			final Promotion promo = promotionService.getPromotion(promotionId);
			validateStaffInClub(authenticationService.getAccount(authToken),
					promo.getClub().getClubId());
		} catch (NotFoundException | AuthenticationTokenExpiredException
				| PermissionDeniedException e) {
			apiDeniedResponse = new WsResponse<String>(ResponseStatus.denied,
					e.getMessage(), null);
			return new ModelAndView(jsonView, toModel(apiDeniedResponse));
		}
		WsResponse<String> apiResponse = null;
		try {
			final Promotion promotion = getPromotionInstance(null, null,
					promotionTitle, promotionDetails,
					effectiveDateYYYYMMDDHH24MISS, endingDateYYYYMMDDHH24MISS);
			promotion.setId(promotionId);
			promotionService.update(promotion);
			apiResponse = new WsResponse<String>(ResponseStatus.success, "",
					null);
		} catch (final Exception e) {
			e.printStackTrace();
			apiResponse = new WsResponse<String>(ResponseStatus.failure,
					e.getMessage(), null);
		}
		return new ModelAndView(jsonView, toModel(apiResponse));
	}
}
