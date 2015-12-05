package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.club.CheckInfo;


public class ReservationMapper implements RowMapper<CheckInfo> {

	@Override
	public CheckInfo mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
		final CheckInfo retVal = new CheckInfo ();
		retVal.setId(rs.getInt("ID"));
		retVal.setAvailId(rs.getString("RESERVATION_ID"));
		retVal.setAmenityId(rs.getString("AMENITY_ID"));
		retVal.setAmenityName(rs.getString("AMENITY_NAME"));
		retVal.setAmenityName(rs.getString("AMENITY_NAME"));
		retVal.setPersonNumber(rs.getInt("NO_PERSONS"));
		retVal.setRequestedClubId(rs.getString("CLUB_ID"));
		if (StringUtils.isNotEmpty(rs.getString("RESERVATION_DATE"))) {
			retVal.setRequestedDateTime(rs.getDate("RESERVATION_DATE"));
		}
		return retVal;
	}

}
