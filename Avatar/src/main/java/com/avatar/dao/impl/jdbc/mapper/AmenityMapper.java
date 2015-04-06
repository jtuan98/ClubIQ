package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.club.AmenityDto;

public class AmenityMapper implements RowMapper<AmenityDto>{

	@Override
	public AmenityDto mapRow(final ResultSet rs, final int rowNo) throws SQLException {
		final AmenityDto retVal = new AmenityDto ();
		retVal.setId(rs.getInt("ID"));
		retVal.setDescription(rs.getString("DESCRIPTION"));
		retVal.setHoursOfOperation(rs.getString("HOURS_OPERATION_NOTE"));
		retVal.setAmenityId(rs.getString("AMENITYID"));
		retVal.setAmenityType(rs.getString("AT.NAME"));
		//TODO: Get the image
		return retVal;
	}

}
