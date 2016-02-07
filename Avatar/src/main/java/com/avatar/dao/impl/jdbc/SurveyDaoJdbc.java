package com.avatar.dao.impl.jdbc;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.avatar.dao.SurveyDao;
import com.avatar.dao.impl.jdbc.mapper.SurveyAnswerMapper;
import com.avatar.dao.impl.jdbc.mapper.SurveyMapper;
import com.avatar.dto.survey.Survey;
import com.avatar.dto.survey.SurveyAnswer;
import com.avatar.exception.NotFoundException;

@Repository
public class SurveyDaoJdbc extends BaseJdbcDao implements SurveyDao {
	private static String SEL_QUESTION_IDS_SURVEY_BY_PK = "SELECT * FROM SURVEYS S WHERE ID = ? ";

	private static String SEL_SURVEY_IDS_BY_AMENITY_TYPE_ID = "SELECT ID FROM SURVEYS S WHERE AMENITY_TYPE_ID = ?";
	private static String SEL_SURVEY_IDS_BY_CLUBID_AMNT_ID_MEMID = "SELECT SURVEY_ID FROM SURVEY_ANSWERS WHERE CLUB_ID = ? AND CLUB_AMENITY_ID=? AND MEMBER_ID = ? AND CREATE_DATE > ? AND SURVEY_ANS=? ORDER BY CREATE_DATE";
	private static String SEL_SURVEY_IDS_BY_CLUBID_AMNT_ID_MEMID_ALLDATES = "SELECT SURVEY_ID FROM SURVEY_ANSWERS WHERE CLUB_ID = ? AND CLUB_AMENITY_ID=? AND MEMBER_ID = ? AND SURVEY_ANS=? ORDER BY CREATE_DATE";
	private static String SEL_SURVEY_ANSIDS_BY_CLUBID_AMNT_ID_MEMID = "SELECT ID FROM SURVEY_ANSWERS WHERE CLUB_ID = ? AND CLUB_AMENITY_ID=? AND MEMBER_ID = ? AND CREATE_DATE > ? AND SURVEY_ANS=? ORDER BY CREATE_DATE";
	private static String INS_ANSWERS = "INSERT INTO SURVEY_ANSWERS (ID, CLUB_ID, CLUB_AMENITY_ID, "
			+ " MEMBER_ID, BEACON_ID, SURVEY_ID, SURVEY_ANS, CREATE_DATE) values (?,?,?,?,?,?,'N',NOW())";

	private static String UPD_ANSWERS = "UPDATE SURVEY_ANSWERS SET ANS_PART_A = ?, ANS_PART_B = ?, SURVEY_ANS = 'Y', ANSWERED_ON=NOW() WHERE ID = ?";

	private static String SEL_SURVEY_ANSWER_BY_PK = "SELECT * FROM SURVEY_ANSWERS WHERE ID=?";

	private static String SEL_AMENITY_ID_PK = "select ID from CLUB_AMENITIES where AMENITYID=?";

	static private final String DEL_ANSWERS = "DELETE FROM SURVEY_ANSWERS where MEMBER_ID = ? and CREATE_DATE >= ? and CREATE_DATE <= ?";

	private final SurveyMapper surveyMapper = new SurveyMapper();

	private final SurveyAnswerMapper surveyAnswerMapper = new SurveyAnswerMapper();


	@Override
	public void delete(final Integer memberIdPk, final Date fromDate,
			final Date toDate) {
		getJdbcTemplate().update(DEL_ANSWERS, memberIdPk, fromDate, toDate);
	}

	@Override
	public SurveyAnswer fetchAnswer(final Integer surveyAnswerIdPk)
			throws NotFoundException {
		SurveyAnswer retVal = null;
		try {
			retVal = getJdbcTemplate().queryForObject(SEL_SURVEY_ANSWER_BY_PK,
					surveyAnswerMapper, surveyAnswerIdPk);
			final Survey survey = getSurvey(retVal.getSurvey().getId());
			retVal.setSurvey(survey);
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Survey Answer ID " + surveyAnswerIdPk
					+ " not found!");
		}
		return retVal;
	}

	@Override
	public Survey getSurvey(final Integer surveyId) throws NotFoundException {
		Survey retVal = null;
		try {
			retVal = getJdbcTemplate().queryForObject(
					SEL_QUESTION_IDS_SURVEY_BY_PK, surveyMapper, surveyId);
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Survey " + surveyId + " not found!");
		}
		return retVal;
	}

	// Returns Survey ID pk
	@Override
	public Set<Integer> getSurveyConfiguration(final int amenityIdPk)
			throws NotFoundException {
		final List<Integer> questionIdsPk = getJdbcTemplate().queryForList(
				SEL_SURVEY_IDS_BY_AMENITY_TYPE_ID, Integer.class,
				amenityIdPk);
		return new HashSet<Integer>(questionIdsPk);
	}

	// Returns key is survey id pk
	@Override
	public Set<Integer> getSurveyIdPkHistory(final Integer clubIdPk,
			final Integer amenityIdPk, final Integer memberId, final Date since) {
		List<Integer> questionIdsPk = null;
		if (since == null) {
			questionIdsPk = getJdbcTemplate().queryForList(
					SEL_SURVEY_IDS_BY_CLUBID_AMNT_ID_MEMID_ALLDATES,
					Integer.class, clubIdPk, amenityIdPk, memberId, "Y");
		} else {
			questionIdsPk = getJdbcTemplate().queryForList(
					SEL_SURVEY_IDS_BY_CLUBID_AMNT_ID_MEMID, Integer.class,
					clubIdPk, amenityIdPk, memberId, yyyyMMdd_hh24missDtf.print(since.getTime()), "Y");
		}
		final Set<Integer> retVal = new LinkedHashSet<>();
		if (CollectionUtils.isNotEmpty(questionIdsPk)) {
			retVal.addAll(questionIdsPk);
		}
		return retVal;
	}

	// Returns key is survey answer id pk
	@Override
	public Set<Integer> getSurveyIdPkNotAnsweredHistory(final Integer clubIdPk,
			final Integer amenityIdPk, final Integer memberId, final Date since)
					throws NotFoundException {
		final List<Integer> answerIdsPk = getJdbcTemplate().queryForList(
				SEL_SURVEY_ANSIDS_BY_CLUBID_AMNT_ID_MEMID, Integer.class,
				clubIdPk, amenityIdPk, memberId, since, "N");
		final Set<Integer> retVal = new HashSet<>();
		if (CollectionUtils.isNotEmpty(answerIdsPk)) {
			retVal.addAll(answerIdsPk);
		}
		return retVal;
	}

	@Override
	public void persistSurveyAnswer(final Integer clubIdPk,
			final Integer amenityIdPk, final Integer beaconIdPk,
			final Integer memberIdPk, final SurveyAnswer surveyAnswer)
					throws NotFoundException {
		Assert.notNull(surveyAnswer);
		if (surveyAnswer.getId() == null) {
			Assert.notNull(clubIdPk);
			Assert.notNull(amenityIdPk);
			Assert.notNull(memberIdPk);
			Assert.notNull(surveyAnswer.getSurvey());
			Assert.notNull(surveyAnswer.getSurvey().getId());
			// Insert
			final int idPk = sequencer.nextVal("ID_SEQ");
			surveyAnswer.setId(idPk);
			getJdbcTemplate().update(INS_ANSWERS,
					// ID
					idPk,
					// CLUB_ID
					clubIdPk,
					// AMENITY_ID
					amenityIdPk,
					// MEMBER_ID
					memberIdPk,
					// BEACON_ID
					beaconIdPk,
					// SURVEY_ID
					surveyAnswer.getSurvey().getId());
		}
	}

	@Override
	@Resource(name = "avatarDataSource")
	public void setDataSource(final DataSource ds) {
		initTemplate(ds);
	}

	@Override
	public void updateAnswer(final SurveyAnswer surveyAnswer)
			throws NotFoundException {
		final int rowUpdated = getJdbcTemplate().update(UPD_ANSWERS,
				// ANS_PART_A
				surveyAnswer.getAnswerA(),
				// ANS_PART_B
				surveyAnswer.getAnswerB(),
				// ID
				surveyAnswer.getId());
		if (rowUpdated == 0) {
			throw new NotFoundException("Answer ID " + surveyAnswer.getId()
					+ " not found!");
		}
	}

}
