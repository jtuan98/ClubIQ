package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.club.BlackoutDate;

public class BlackoutDateMapper implements RowMapper<BlackoutDate> {

	@Override
	public BlackoutDate mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
		final BlackoutDate retVal = new BlackoutDate();
		retVal.setDate(rs.getString("BLACKOUT_DAY"));
		return retVal;
	}

}
