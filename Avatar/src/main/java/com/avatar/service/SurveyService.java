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
	public Survey getNextSurvey(final String beaconId, final String memberId)
			throws NotFoundException {
		Survey retVal = null;
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
		final Set<Integer> surveyIdsSinceLastMon = surveyDao
				.getSurveyIdPkHistory(clubIdPk, amenityIdPk, memberIdPk,
						lastMondayDate);
		// If member HAS NOT done any survey in the past week.
		if (CollectionUtils.isEmpty(surveyIdsSinceLastMon)) {
			final Set<Integer> surveyPks = surveyDao.getSurveyConfiguration(
					clubIdPk, amenityIdPk);
			final Set<Integer> surveyIdsSinceBeginning = surveyDao
					.getSurveyIdPkHistory(clubIdPk, amenityIdPk, memberIdPk,
							null);
			for (final Integer surveyIdPk : surveyIdsSinceBeginning) {
				surveyPks.remove(surveyIdPk);
			}
			if (CollectionUtils.isNotEmpty(surveyPks)) {
				retVal = surveyDao.getSurvey(surveyPks.iterator().next());
			}
		}
		return retVal;
	}

	private void persistSurveyAnswer(final Integer clubIdPk,
			final Integer amenityIdPk, final Integer beaconIdPk,
			final Integer memberIdPk, final SurveyAnswer answer)
			throws NotFoundException {
		surveyDao.persistSurveyAnswer(clubIdPk, amenityIdPk, beaconIdPk,
				memberIdPk, answer);
		surveyDao.updateAnswer(answer);
	}

	@Override
	public void persistSurveyAnswer(final String beaconId,
			final String memberId, final SurveyAnswer answer)
			throws NotFoundException {
		Assert.notNull(answer);
		Assert.notNull(answer.getSurvey());
		Assert.notNull(answer.getSurvey().getId());
		// Verify Question Id
		surveyDao.getSurvey(answer.getSurvey().getId());
		// Get beaconIdPk
		final Integer beaconIdPk = beaconDao.getBeaconIdPk(beaconId);
		// Get amenityIdPk
		final Integer amenityIdPk = beaconDao.getAmenityIdPk(beaconIdPk);
		// Get clubIdPk
		final Integer clubIdPk = beaconDao.getClubIdPkByBeaconIdPk(beaconIdPk);
		// Get memeberIdPk
		final Integer memberIdPk = accountDao.getUserIdPkByUserId(memberId);
		// Create a survey answer
		persistSurveyAnswer(clubIdPk, amenityIdPk, beaconIdPk, memberIdPk,
				answer);

	}

}
