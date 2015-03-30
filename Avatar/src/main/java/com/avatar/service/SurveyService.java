package com.avatar.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.avatar.business.SurveyBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dao.BeaconDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.SurveyDao;
import com.avatar.dto.survey.Survey;
import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.exception.NotFoundException;

@Service
public class SurveyService implements SurveyBusiness {

	@Resource(name = "surveyDaoJdbc")
	private SurveyDao surveyDao;

	@Resource(name = "beaconDaoJdbc")
	private BeaconDao beaconDao;

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDao;

	private Date getLastMonday() {
		final Calendar c = Calendar.getInstance();
		// ensure the method works within current month
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		// go to the 1st week of february, in which monday was in january
		c.set(Calendar.DAY_OF_MONTH, 1);
		// test that setting day_of_week to monday gives a date in january
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		// same for tuesday
		c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
		// TODO Auto-generated method stub
		return c.getTime();
	}

	@Override
	public SurveyAnswer getNextSurvey(final String beaconId,
			final String memberId) throws NotFoundException {
		// Get beaconIdPk
		final Integer beaconIdPk = beaconDao.getBeaconIdPk(beaconId);
		// Get amenityIdPk
		final Integer amenityIdPk = beaconDao.getAmenityIdPk(beaconIdPk);
		// Get clubIdPk
		final Integer clubIdPk = beaconDao.getClubIdPkByBeaconIdPk(beaconIdPk);
		// Get memeberIdPk
		final Integer memberIdPk = accountDao.getUserIdPkByUserId(memberId);
		// Find Last Mon
		final Date lastMondayDate = getLastMonday();
		final Set<Integer> answerIdsSinceLastMonNotAnsweredYet = surveyDao
				.getSurveyIdPkNotAnsweredHistory(clubIdPk, amenityIdPk, memberIdPk,
						lastMondayDate);
		if (CollectionUtils.isNotEmpty(answerIdsSinceLastMonNotAnsweredYet)) {
			final SurveyAnswer survey = surveyDao.fetchAnswer(answerIdsSinceLastMonNotAnsweredYet.iterator().next());
			return survey;
		}
		final Set<Integer> surveyIdsSinceLastMon = surveyDao
				.getSurveyIdPkHistory(clubIdPk, amenityIdPk, memberIdPk,
						lastMondayDate);
		final Set<Integer> surveyPks = surveyDao.getSurveyConfiguration(
				clubIdPk, amenityIdPk);
		for (final Integer surveyIdPk : surveyIdsSinceLastMon) {
			surveyPks.remove(surveyIdPk);
		}
		Survey survey = null;
		if (CollectionUtils.isNotEmpty(surveyPks)) {
			survey = surveyDao.getSurvey(surveyPks.iterator().next());
		}
		if (survey == null) {
			return null;
		}
		// Create a survey answer
		final SurveyAnswer answer = new SurveyAnswer();
		answer.setSurvey(survey);
		persistSurveyAnswer(clubIdPk, amenityIdPk, beaconIdPk, memberIdPk,
				answer);

		return answer;
	}

	private void persistSurveyAnswer(final Integer clubIdPk,
			final Integer amenityIdPk, final Integer beaconIdPk,
			final Integer memberIdPk, final SurveyAnswer answer)
			throws NotFoundException {
		surveyDao.persistSurveyAnswer(clubIdPk, amenityIdPk, beaconIdPk,
				memberIdPk, answer);
	}

	@Override
	public void persistSurveyAnswer(final SurveyAnswer answer) throws NotFoundException {
		Assert.notNull(answer);
		surveyDao.updateAnswer(answer);
	}

}
