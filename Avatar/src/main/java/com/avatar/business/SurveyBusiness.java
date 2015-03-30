package com.avatar.business;

import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.exception.NotFoundException;

public interface SurveyBusiness {
	SurveyAnswer getNextSurvey(final String beaconId, String memberId)
			throws NotFoundException;

	void persistSurveyAnswer(SurveyAnswer answer)
			throws NotFoundException;

}
