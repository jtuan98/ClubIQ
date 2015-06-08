package com.avatar.business;

import java.util.Date;

import com.avatar.dto.survey.Survey;
import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;

public interface SurveyBusiness {
	void deleteSurveyAnswers(String memberId, Date fromdate, Date toDate)
			throws NotFoundException, InvalidParameterException;

	Survey getNextSurvey(String beaconId, String memberId)
			throws NotFoundException, InvalidParameterException;

	void persistSurveyAnswer(String beaconId, String memberId, SurveyAnswer answer)
			throws NotFoundException;
}
