package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.club.BlackoutTime;

public class BlackoutTimesMapper implements RowMapper<List<BlackoutTime>> {

	@Override
	public List<BlackoutTime> mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
		final List<BlackoutTime> retVal = new LinkedList<>();
		final String times = rs.getString("BLACKOUT_HOURS");
		for(int i=0;i<times.length();i++) {
			final boolean flag = 'Y'==times.charAt(i);
			if (flag) {
				final BlackoutTime t = new BlackoutTime();
				final int hour = (i*30)/60;
				final int min = (i%2)*30;
				t.setTime(String.format("%02d%02d", hour, min));
				retVal.add(t);
			}
		}
		return retVal;
	}

}
