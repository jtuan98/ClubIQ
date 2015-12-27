package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.SubAmenityDto;

public class SubAmenityDtoMapper implements RowMapper<SubAmenityDto> {

	@Override
	public SubAmenityDto mapRow(final ResultSet rs, final int rownum) throws SQLException {
		final SubAmenityDto retVal = new SubAmenityDto ();
		retVal.setId(rs.getInt("ID"));
		retVal.setDescription(rs.getString("DESCRIPTION"));
		retVal.setSubAmenityId(rs.getString("SUBAMENITYID"));
		retVal.setHoursOfOperation(rs.getString("HOURS_OPERATION_NOTE"));
		retVal.setBody(rs.getString("BODY_TEXT"));
		retVal.setHeader(rs.getString("HEADER_TEXT"));
		retVal.setHeaderSecondary(rs.getString("SECONDARY_HEADER_TEXT"));
		retVal.setAmenity(new AmenityDto());
		retVal.getAmenity().setId(rs.getInt("AMENITY_ID"));
		retVal.getAmenity().setAmenityId(rs.getString("AMENITYID"));
		retVal.getAmenity().setDescription(rs.getString("AMENITY_NAME"));
		return retVal;
	}

}
