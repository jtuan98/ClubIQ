package com.avatar.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.avatar.dto.club.SubAmenityDto;
import com.avatar.dto.survey.Survey;
import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;

public class SurveyServiceTest extends BaseServiceTest {

	private SurveyService service;

	final Set<Integer> surveyIdsSinceBeginning = new HashSet<Integer>();

	private Date getLastMonday(final int pastWeeks) {
		final DateTime today = new DateTime().toDateMidnight().toDateTime();
		final DateTime sameDayPastWeek = today.minusWeeks(pastWeeks);
		final DateTime mondayPastWeek = sameDayPastWeek
				.withDayOfWeek(DateTimeConstants.MONDAY);
		return new Date(mondayPastWeek.getMillis());
	}

	@Before
	public void setUp() {
		service = new SurveyService();
		ReflectionTestUtils.setField(service, "clubDao", clubDao);
		ReflectionTestUtils.setField(service, "accountDao", accountDao);
		ReflectionTestUtils.setField(service, "dbDateDao", dbDateDao);
		ReflectionTestUtils.setField(service, "surveyDao", surveyDao);
		ReflectionTestUtils.setField(service, "beaconDao", beaconDao);
		super.setup();
	}

	// *** deleteSurveyAnswers
	@Test(expected = InvalidParameterException.class)
	public void test001DeleteSurveyAnswers_01_nullMemberId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = null;
		final Date toDate = new Date();
		final Date fromDate = new Date();
		service.deleteSurveyAnswers(memberId, fromDate, toDate);
	}

	@Test(expected = InvalidParameterException.class)
	public void test001DeleteSurveyAnswers_02_nullFromDate()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final Date toDate = new Date();
		final Date fromDate = null;
		service.deleteSurveyAnswers(memberId, fromDate, toDate);
	}

	@Test(expected = InvalidParameterException.class)
	public void test001DeleteSurveyAnswers_03_nullToDate()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final Date toDate = null;
		final Date fromDate = new Date();
		service.deleteSurveyAnswers(memberId, fromDate, toDate);
	}

	@Test(expected = NotFoundException.class)
	public void test002DeleteSurveyAnswers_01_NotFound()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final Date toDate = new Date();
		final Date fromDate = new Date();
		given(accountDao.getUserIdPkByUserId(memberId)).willThrow(
				NotFoundException.class);
		service.deleteSurveyAnswers(memberId, fromDate, toDate);
	}

	@Test
	public void test003DeleteSurveyAnswers_01_valid() throws NotFoundException,
	InvalidParameterException {
		final String memberId = "123";
		final Date toDate = new Date();
		final Date fromDate = new Date();
		service.deleteSurveyAnswers(memberId, fromDate, toDate);
		Mockito.verify(surveyDao, times(1)).delete(anyInt(), eq(fromDate),
				eq(toDate));
	}

	// **** getNextSurvey
	// Case 1: null params
	@Test(expected = InvalidParameterException.class)
	public void test004GetNextSurvey_01a_nullParamsBoth()
			throws NotFoundException, InvalidParameterException {
		final String memberId = null;
		final String beaconId = null;
		service.getNextSurvey(beaconId, memberId);
	}

	@Test(expected = InvalidParameterException.class)
	public void test004GetNextSurvey_01b_nullParamsMemberId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = null;
		final String beaconId = "123";
		service.getNextSurvey(beaconId, memberId);
	}

	@Test(expected = InvalidParameterException.class)
	public void test004GetNextSurvey_01c_nullParamsBeaconId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = null;
		service.getNextSurvey(beaconId, memberId);
	}

	// Case 2: NotFound beacon, amenityIdPk, clubIdPk, memberid
	@Test(expected = NotFoundException.class)
	public void test004GetNextSurvey_02a_NotFoundBeaconId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		given(beaconDao.getBeaconIdPk(beaconId)).willThrow(
				NotFoundException.class);
		service.getNextSurvey(beaconId, memberId);
	}

	@Test(expected = NotFoundException.class)
	public void test004GetNextSurvey_02b_NotFoundAmenityIdPk()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final Integer beaconIdPk = 1;
		given(beaconDao.getBeaconIdPk(beaconId)).willReturn(beaconIdPk);
		given(beaconDao.getSubAmenityIdPk(beaconIdPk)).willThrow(
				NotFoundException.class);
		service.getNextSurvey(beaconId, memberId);
	}

	@Test(expected = NotFoundException.class)
	public void test004GetNextSurvey_02c_NotFoundClubId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final Integer beaconIdPk = 1;
		final Integer amenityIdPk = 1;
		given(beaconDao.getBeaconIdPk(beaconId)).willReturn(beaconIdPk);
		given(beaconDao.getSubAmenityIdPk(beaconIdPk)).willReturn(amenityIdPk);
		given(beaconDao.getClubIdPkByBeaconIdPk(beaconIdPk)).willThrow(
				NotFoundException.class);
		service.getNextSurvey(beaconId, memberId);
	}

	@Test(expected = NotFoundException.class)
	public void test004GetNextSurvey_02d_NotFoundMemberId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		given(accountDao.getUserIdPkByUserId(memberId)).willThrow(
				NotFoundException.class);
		service.getNextSurvey(beaconId, memberId);
	}

	private void test004GetNextSurvey_03_setUp(final String beaconId,
			final String memberId, final boolean surveyEmpty,
			final boolean surveyIdsSincePastMonFlag,
			final boolean surveyIdsSinceBeginningFlag)
					throws NotFoundException, InvalidParameterException {
		final Integer beaconIdPk = 1;
		final Integer subAmenityIdPk = 1;
		final Integer clubIdPk = 1;
		final Integer userIdPk = 1;
		given(beaconDao.getBeaconIdPk(beaconId)).willReturn(beaconIdPk);
		given(beaconDao.getSubAmenityIdPk(beaconIdPk)).willReturn(subAmenityIdPk);
		given(beaconDao.getClubIdPkByBeaconIdPk(beaconIdPk)).willReturn(
				clubIdPk);
		given(accountDao.getUserIdPkByUserId(memberId)).willReturn(userIdPk);

		final SubAmenityDto subAmenity = getSubAmenityInstance(subAmenityIdPk, "amenity1");
		given(clubDao.getSubAmenity(subAmenityIdPk)).willReturn(subAmenity);

		final Set<Integer> surveyPks = new HashSet<Integer>();
		if (!surveyEmpty) {
			for (int surveyIdPk = 0; surveyIdPk < 3; surveyIdPk++) {
				surveyPks.add(surveyIdPk);
				final Survey survey = new Survey();
				survey.setId(surveyIdPk);
				survey.setQuestionA("question A-" + surveyIdPk);
				survey.setQuestionB("question B-" + surveyIdPk);
				given(surveyDao.getSurvey(eq(surveyIdPk))).willReturn(survey);
			}
		}
		given(surveyDao.getSurveyConfiguration(subAmenity.getAmenity().getId()))
		.willReturn(surveyPks);

		final Set<Integer> surveyIdsSincePastMon = new HashSet<Integer>();
		if (surveyIdsSincePastMonFlag) {
			surveyIdsSincePastMon.add(1);
		}
		final Date lastMon = getLastMonday(1);
		given(
				surveyDao.getSurveyIdPkHistory(eq(clubIdPk), eq(subAmenityIdPk),
						eq(userIdPk), eq(lastMon))).willReturn(
								surveyIdsSincePastMon);

		final Date since = getLastMonday(surveyPks.size());
		if (surveyIdsSinceBeginningFlag) {
			surveyIdsSinceBeginning.add(0);
		}
		given(
				surveyDao.getSurveyIdPkHistory(eq(clubIdPk), eq(subAmenityIdPk),
						eq(userIdPk), eq(since))).willReturn(
								surveyIdsSinceBeginning);
	}

	// Case 3a: member HAS done a survey in the past week
	@Test
	public void test004GetNextSurvey_03a_memberHadSurveyInPastWeek()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";

		final boolean emptySurveyConfig = false;
		final boolean surveySinceLastMon = true;
		final boolean surveySinceBeginning = false;
		test004GetNextSurvey_03_setUp(beaconId, memberId, emptySurveyConfig,
				surveySinceLastMon, surveySinceBeginning);
		final Survey survey = service.getNextSurvey(beaconId, memberId);
		Assert.assertNull("Survey should be null", survey);
	}

	// Case 3b: recycling message tests, memberDidNotHadSurveysEver
	@Test
	public void test004GetNextSurvey_03b_memberDidNotHadSurveysEver()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final boolean emptySurveyConfig = false;
		final boolean surveySinceLastMon = false;
		final boolean surveySinceBeginning = false;
		test004GetNextSurvey_03_setUp(beaconId, memberId, emptySurveyConfig,
				surveySinceLastMon, surveySinceBeginning);
		final Survey survey = service.getNextSurvey(beaconId, memberId);
		Assert.assertNotNull("Survey should not be null", survey);
		Assert.assertEquals("Survey id be the first", new Integer(0),
				survey.getId());
	}

	@Test
	public void test004GetNextSurvey_03c_memberHadNotSurveyInPastWeekButHad1Prior()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";

		final boolean emptySurveyConfig = false;
		final boolean surveySinceLastMon = false;
		final boolean surveySinceBeginning = true;
		test004GetNextSurvey_03_setUp(beaconId, memberId, emptySurveyConfig,
				surveySinceLastMon, surveySinceBeginning);
		final Survey survey = service.getNextSurvey(beaconId, memberId);
		Assert.assertNotNull("Survey should be null", survey);
		Assert.assertEquals("Survey id be the first", new Integer(1),
				survey.getId());
	}

	@Test
	public void test004GetNextSurvey_03d_memberHadNotSurveyInPastWeekButHad2PriorRecycle()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";

		final boolean emptySurveyConfig = false;
		final boolean surveySinceLastMon = false;
		final boolean surveySinceBeginning = true;
		test004GetNextSurvey_03_setUp(beaconId, memberId, emptySurveyConfig,
				surveySinceLastMon, surveySinceBeginning);
		surveyIdsSinceBeginning.add(2);

		final Survey survey = service.getNextSurvey(beaconId, memberId);
		Assert.assertNotNull("Survey should be null", survey);
		Assert.assertEquals("Survey id be the first", new Integer(1),
				survey.getId());
	}

	@Test
	public void test004GetNextSurvey_03e_memberHadNotSurveyInPastWeekButHadAllPriorRecycle()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";

		final boolean emptySurveyConfig = false;
		final boolean surveySinceLastMon = false;
		final boolean surveySinceBeginning = true;
		test004GetNextSurvey_03_setUp(beaconId, memberId, emptySurveyConfig,
				surveySinceLastMon, surveySinceBeginning);
		surveyIdsSinceBeginning.add(1);
		surveyIdsSinceBeginning.add(2);

		final Survey survey = service.getNextSurvey(beaconId, memberId);
		Assert.assertNotNull("Survey should be null", survey);
		Assert.assertEquals("Survey id be the first", new Integer(0),
				survey.getId());
	}

	// persistSurveyAnswer
	@Test(expected = InvalidParameterException.class)
	public void test005PersistSurveyAnswer_01a_nullParams_all()
			throws NotFoundException, InvalidParameterException {
		final String memberId = null;
		final String beaconId = null;
		final SurveyAnswer answer = null;
		service.persistSurveyAnswer(beaconId, memberId, answer);
	}

	@Test(expected = InvalidParameterException.class)
	public void test005PersistSurveyAnswer_01b_nullParams_beaconId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = null;
		final SurveyAnswer answer = new SurveyAnswer();
		service.persistSurveyAnswer(beaconId, memberId, answer);
	}

	@Test(expected = InvalidParameterException.class)
	public void test005PersistSurveyAnswer_01c_nullParams_memberId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = null;
		final String beaconId = "123";
		final SurveyAnswer answer = new SurveyAnswer();
		service.persistSurveyAnswer(beaconId, memberId, answer);
	}

	@Test(expected = InvalidParameterException.class)
	public void test005PersistSurveyAnswer_01d_nullParams_answer()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final SurveyAnswer answer = null;
		service.persistSurveyAnswer(beaconId, memberId, answer);
	}

	@Test(expected = InvalidParameterException.class)
	public void test005PersistSurveyAnswer_01d_nullParams_answer_survey_id()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final SurveyAnswer answer = new SurveyAnswer();
		answer.setSurvey(new Survey());
		service.persistSurveyAnswer(beaconId, memberId, answer);
	}

	@Test(expected = InvalidParameterException.class)
	public void test005PersistSurveyAnswer_01e_nullParams_answer_survey()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final SurveyAnswer answer = new SurveyAnswer();
		service.persistSurveyAnswer(beaconId, memberId, answer);
	}

	@Test(expected = NotFoundException.class)
	public void test005PersistSurveyAnswer_02a_NotFound_survey()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final SurveyAnswer answer = new SurveyAnswer();
		answer.setSurvey(new Survey());
		answer.getSurvey().setId(1);
		given(surveyDao.getSurvey(eq(1))).willThrow(NotFoundException.class);
		service.persistSurveyAnswer(beaconId, memberId, answer);
	}

	@Test(expected = NotFoundException.class)
	public void test005PersistSurveyAnswer_02b_NotFound_beaconId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final SurveyAnswer answer = new SurveyAnswer();
		answer.setSurvey(new Survey());
		answer.getSurvey().setId(1);
		given(beaconDao.getBeaconIdPk(beaconId)).willThrow(
				NotFoundException.class);
		service.persistSurveyAnswer(beaconId, memberId, answer);
	}

	@Test(expected = NotFoundException.class)
	public void test005PersistSurveyAnswer_02c_NotFound_amenityId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final SurveyAnswer answer = new SurveyAnswer();
		answer.setSurvey(new Survey());
		answer.getSurvey().setId(1);
		given(beaconDao.getBeaconIdPk(beaconId)).willReturn(1);
		given(beaconDao.getSubAmenityIdPk(1)).willThrow(NotFoundException.class);
		service.persistSurveyAnswer(beaconId, memberId, answer);
	}

	@Test(expected = NotFoundException.class)
	public void test005PersistSurveyAnswer_02d_NotFound_clubId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final SurveyAnswer answer = new SurveyAnswer();
		answer.setSurvey(new Survey());
		answer.getSurvey().setId(1);
		given(beaconDao.getBeaconIdPk(beaconId)).willReturn(1);
		given(beaconDao.getSubAmenityIdPk(1)).willReturn(1);
		given(beaconDao.getClubIdPkByBeaconIdPk(1)).willThrow(
				NotFoundException.class);
		service.persistSurveyAnswer(beaconId, memberId, answer);
	}

	@Test(expected = NotFoundException.class)
	public void test005PersistSurveyAnswer_02e_NotFound_memberId()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final SurveyAnswer answer = new SurveyAnswer();
		answer.setSurvey(new Survey());
		answer.getSurvey().setId(1);
		given(beaconDao.getBeaconIdPk(beaconId)).willReturn(1);
		given(beaconDao.getSubAmenityIdPk(1)).willReturn(1);
		given(beaconDao.getClubIdPkByBeaconIdPk(1)).willReturn(1);
		given(accountDao.getUserIdPkByUserId(memberId)).willThrow(
				NotFoundException.class);

		service.persistSurveyAnswer(beaconId, memberId, answer);
	}

	@Test(expected = NotFoundException.class)
	public void test005PersistSurveyAnswer_02f_NotFound_updateAnswer()
			throws NotFoundException, InvalidParameterException {
		final String memberId = "123";
		final String beaconId = "123";
		final SurveyAnswer answer = new SurveyAnswer();
		answer.setSurvey(new Survey());
		answer.getSurvey().setId(1);
		given(beaconDao.getBeaconIdPk(beaconId)).willReturn(1);
		given(beaconDao.getSubAmenityIdPk(1)).willReturn(1);
		given(beaconDao.getClubIdPkByBeaconIdPk(1)).willReturn(1);
		given(accountDao.getUserIdPkByUserId(memberId)).willReturn(1);
		Mockito.doThrow(NotFoundException.class).when(surveyDao).updateAnswer(answer);

		service.persistSurveyAnswer(beaconId, memberId, answer);
	}
}
