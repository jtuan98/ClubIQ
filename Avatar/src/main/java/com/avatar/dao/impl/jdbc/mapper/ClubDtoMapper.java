package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.ImagePic;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.enums.DbTimeZone;

public class ClubDtoMapper implements RowMapper<ClubDto> {

	@Override
	public ClubDto mapRow(final ResultSet rs, final int rowNo)
			throws SQLException {
		final ClubDto retVal = new ClubDto();
		retVal.setId(rs.getInt("ID"));
		retVal.setAddress(rs.getString("ADDRESS"));
		retVal.setCity(rs.getString("CITY"));
		retVal.setClubId(rs.getString("CLUBID"));
		retVal.setClubName(rs.getString("NAME"));
		retVal.setPhoneNumber(rs.getString("PHONE_NUMBER"));
		retVal.setState(rs.getString("STATE_ABBR"));
		retVal.setZipCode(rs.getString("ZIPCODE"));
		retVal.setClubType(rs.getString("CLUB_TYPE"));
		retVal.setWebSite(rs.getString("CLUB_WEBSITE"));
		retVal.setHzRestriction(rs.getString("HZRESTRICTION"));
		retVal.setXcoord(rs.getString("X_COORD"));
		retVal.setYcoord(rs.getString("Y_COORD"));
		retVal.setTimeZone(DbTimeZone.convert(rs.getString("TIME_ZONE")));
		if (rs.getString("IMAGE_ID") != null) {
			retVal.setImage(new ImagePic(rs.getInt("IMAGE_ID")));
		}
		return retVal;
	}

}
