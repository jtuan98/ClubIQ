package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.survey.Survey;
import com.avatar.dto.survey.SurveyAnswer;

public class SurveyAnswerMapper implements RowMapper<SurveyAnswer>{

	@Override
	public SurveyAnswer mapRow(final ResultSet rs, final int rowNo) throws SQLException {
		final SurveyAnswer retVal = new SurveyAnswer ();
		retVal.setSurvey(new Survey());
		retVal.setId(rs.getInt("ID"));
		retVal.getSurvey().setId(rs.getInt("SURVEY_ID"));
		retVal.setAnswerA(rs.getInt("ANS_PART_A"));
		retVal.setAnswerB(rs.getInt("ANS_PART_B"));
		return retVal;
	}

}
