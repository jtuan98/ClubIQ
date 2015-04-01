package com.avatar.business;

import com.avatar.dto.survey.Survey;
import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.exception.NotFoundException;

public interface SurveyBusiness {
	Survey getNextSurvey(String beaconId, String memberId)
			throws NotFoundException;

	void persistSurveyAnswer(String beaconId, String memberId, SurveyAnswer answer)
			throws NotFoundException;

}
