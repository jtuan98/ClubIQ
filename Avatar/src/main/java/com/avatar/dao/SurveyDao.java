package com.avatar.dao;

import java.util.Date;
import java.util.Set;

import com.avatar.dto.survey.Survey;
import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.exception.NotFoundException;

public interface SurveyDao {

	SurveyAnswer fetchAnswer(Integer surveyAnswerIdPk) throws NotFoundException;

	Survey getSurvey(Integer surveyId) throws NotFoundException;

	// Get a list of possible survey pk
	Set<Integer> getSurveyConfiguration(Integer clubIdPk, Integer amenityIdPk)
			throws NotFoundException;

	Set<Integer> getSurveyIdPkHistory(Integer clubIdPk, Integer amenityIdPk,
			Integer memberId, Date since) throws NotFoundException;

	Set<Integer> getSurveyIdPkNotAnsweredHistory(final Integer clubIdPk,
			final Integer amenityIdPk, final Integer memberId, final Date since)
			throws NotFoundException;

	void persistSurveyAnswer(Integer clubIdPk, Integer amenityIdPk,
			final Integer beaconIdPk, Integer memberId, SurveyAnswer answer)
			throws NotFoundException;

	void updateAnswer(SurveyAnswer answer) throws NotFoundException;

}
