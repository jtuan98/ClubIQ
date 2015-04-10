package com.avatar.mvc.controller;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.avatar.business.PromotionBusiness;
import com.avatar.business.SurveyBusiness;
import com.avatar.dto.WsResponse;
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
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

	@Resource(name = "surveyService")
	private SurveyBusiness surveyService;

	@Resource(name = "promotionService")
	private PromotionBusiness promotionService;

	private final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd");

	private static Privilege[] REQUIRED_ROLE = { Privilege.staff,
			Privilege.superUser };

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

	Promotion getPromotionInstance(final String clubId, final String amenityId,
			final String promotionTitle, final String promotionDetails,
			final String effectiveDateYYYYMMDD, final String endingDateYYYYMMDD)
			throws InvalidParameterException {
		final Promotion promotion = new Promotion();
		promotion.setAmenity(new AmenityDto());
		promotion.getAmenity().setAmenityId(amenityId);
		promotion.setClub(new ClubDto());
		promotion.getClub().setClubId(clubId);
		promotion.setTitle(promotionTitle);
		promotion.setDescription(promotionDetails);
		try {
			promotion.setEffectiveDate(new Date(dtf
					.parseMillis(effectiveDateYYYYMMDD)));
		} catch (final IllegalArgumentException e) {
			throw new InvalidParameterException("Invalid EffectiveDate: "
					+ effectiveDateYYYYMMDD);
		}
		try {
			promotion.setEndingDate(new Date(dtf
					.parseMillis(endingDateYYYYMMDD)));
		} catch (final IllegalArgumentException e) {
			throw new InvalidParameterException("Invalid EndingDate: "
					+ endingDateYYYYMMDD);
		}

		return promotion;
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
			@RequestParam(required = true, value = "amenityId") final String amenityId,
			@RequestParam(required = true, value = "promotionTitle") final String promotionTitle,
			@RequestParam(required = true, value = "promotionDetail") final String promotionDetails,
			@RequestParam(required = true, value = "effectiveDate") final String effectiveDateYYYYMMDD,
			@RequestParam(required = true, value = "endingDate") final String endingDateYYYYMMDD)
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
		//TODO: Use authToken and check if staff is linked to the clubId and amenityId or not.
		WsResponse<String> apiResponse = null;
		try {
			final Promotion promotion = getPromotionInstance(clubId, amenityId,
					promotionTitle, promotionDetails, effectiveDateYYYYMMDD,
					endingDateYYYYMMDD);

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
			@RequestParam(required = false, value = "effectiveDate") final String effectiveDateYYYYMMDD,
			@RequestParam(required = false, value = "endingDate") final String endingDateYYYYMMDD)
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
		//TODO: Use authToken and check if staff is linked to the clubId and amenityId or not.
		WsResponse<String> apiResponse = null;
		try {
			final Promotion promotion = getPromotionInstance(null, null,
					promotionTitle, promotionDetails, effectiveDateYYYYMMDD,
					endingDateYYYYMMDD);
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
