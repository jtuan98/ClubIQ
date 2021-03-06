package com.avatar.service;

import java.util.Date;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.stereotype.Service;

import com.avatar.business.SurveyBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dao.BeaconDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.SurveyDao;
import com.avatar.dto.survey.Survey;
import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;

@Service
public class SurveyService extends BaseService implements SurveyBusiness {

	public static void main(final String[] args) {
		final SurveyService s = new SurveyService();
		System.out.println("Date of last mon: " + s.getLastMonday(1));
		System.out.println("Date of last mon: " + s.getLastMonday(2));
		System.out.println("Date of last mon: " + s.getLastMonday(3));
	}

	@Resource(name = "surveyDaoJdbc")
	private SurveyDao surveyDao;

	@Resource(name = "beaconDaoJdbc")
	private BeaconDao beaconDao;

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;
	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDao;

	@Override
	public void deleteSurveyAnswers(final String memberId, final Date fromDate,
			final Date toDate) throws NotFoundException, InvalidParameterException {
		if (StringUtils.isEmpty(memberId)) {
			throw new InvalidParameterException("memberId cannot be null");
		}
		if (fromDate == null) {
			throw new InvalidParameterException("fromDate cannot be null");
		}
		if (toDate == null) {
			throw new InvalidParameterException("toDate cannot be null");
		}
		final Integer memberIdPk = accountDao.getUserIdPkByUserId(memberId);
		surveyDao.delete(memberIdPk, fromDate, toDate);
	}

	private Date getLastMonday(final int pastWeeks) {
		final DateTime today = new DateTime().toDateMidnight().toDateTime();
		final DateTime sameDayPastWeek = today.minusWeeks(pastWeeks);
		final DateTime mondayPastWeek = sameDayPastWeek
				.withDayOfWeek(DateTimeConstants.MONDAY);
		return new Date(mondayPastWeek.getMillis());
	}

	@Override
	public Survey getNextSurvey(final String beaconId, final String memberId)
			throws NotFoundException, InvalidParameterException {
		if (StringUtils.isEmpty(memberId) || StringUtils.isEmpty(beaconId)) {
			throw new InvalidParameterException("Params cannot be null");
		}
		Survey retVal = null;
		// Get beaconIdPk
		final Integer beaconIdPk = beaconDao.getBeaconIdPk(beaconId);
		// Get amenityIdPk
		final Integer amenityIdPk = beaconDao.getAmenityIdPk(beaconIdPk);

		// Get clubIdPk
		final Integer clubIdPk = beaconDao.getClubIdPkByBeaconIdPk(beaconIdPk);
		// Find Last Mon
		// Get memeberIdPk
		final Integer memberIdPk = accountDao.getUserIdPkByUserId(memberId);
		final Set<Integer> surveyPks = surveyDao.getSurveyConfiguration(amenityIdPk);
		final Date since = getLastMonday(surveyPks.size());
		final Date lastMondayDate = getLastMonday(1);
		final Set<Integer> surveyIdsSincePastMon = surveyDao
				.getSurveyIdPkHistory(clubIdPk, amenityIdPk, memberIdPk,
						lastMondayDate);
		// If member HAS NOT done any survey in the past week.
		if (CollectionUtils.isEmpty(surveyIdsSincePastMon)) {
			System.out
			.println("member HAS NOT done any survey in the past week.["
					+ lastMondayDate + "]");
			final Set<Integer> surveyIdsSinceBeginning = surveyDao
					.getSurveyIdPkHistory(clubIdPk, amenityIdPk, memberIdPk,
							since);
			for (final Integer surveyIdPk : surveyIdsSinceBeginning) {
				surveyPks.remove(surveyIdPk);
			}
			if (CollectionUtils.isNotEmpty(surveyPks)) {
				System.out.println("surveyPks is not empty");
				retVal = surveyDao.getSurvey(surveyPks.iterator().next());
			} else if (CollectionUtils.isNotEmpty(surveyIdsSinceBeginning)) {
				// recycle
				System.out.println("Recycling: surveyIdsSinceBeginning");
				retVal = surveyDao.getSurvey(surveyIdsSinceBeginning.iterator()
						.next());
			} else if (CollectionUtils.isNotEmpty(surveyPks)) {
				// recycle
				System.out.println("Recycling: surveyPks");
				retVal = surveyDao.getSurvey(surveyPks.iterator().next());
			}
		} else {
			System.out.println("member HAS done a survey in the past week.["
					+ lastMondayDate + "]");
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
					throws NotFoundException, InvalidParameterException {
		if (answer == null || answer.getSurvey() == null || answer.getSurvey().getId()==null) {
			throw new InvalidParameterException("answer cannot be null");
		}
		if (StringUtils.isEmpty(beaconId)) {
			throw new InvalidParameterException("beaconId cannot be null");
		}
		if (StringUtils.isEmpty(memberId)) {
			throw new InvalidParameterException("memberId cannot be null");
		}
		// Verify Question Id
		surveyDao.getSurvey(answer.getSurvey().getId());
		// Get beaconIdPk
		final Integer beaconIdPk = beaconDao.getBeaconIdPk(beaconId);
		// Get amenityIdPk
		final Integer amenityIdPk = beaconDao.getSubAmenityIdPk(beaconIdPk);
		// Get clubIdPk
		final Integer clubIdPk = beaconDao.getClubIdPkByBeaconIdPk(beaconIdPk);
		// Get memeberIdPk
		final Integer memberIdPk = accountDao.getUserIdPkByUserId(memberId);
		// Create a survey answer
		persistSurveyAnswer(clubIdPk, amenityIdPk, beaconIdPk, memberIdPk,
				answer);

	}

}
