package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.survey.Survey;

public class SurveyMapper implements RowMapper<Survey> {

	@Override
	public Survey mapRow(final ResultSet rs, final int rowNo)
			throws SQLException {
		final Survey retVal = new Survey();
		retVal.setId(rs.getInt("ID"));
		retVal.setQuestionA(rs.getString("QUESTION_A"));
		retVal.setQuestionB(rs.getString("QUESTION_B"));
		return retVal;
	}

}
