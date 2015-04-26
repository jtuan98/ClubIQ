package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.promotion.Promotion;

public class PromotionMapper implements RowMapper<Promotion>{

	@Override
	public Promotion mapRow(final ResultSet rs, final int rowNo) throws SQLException {
		final Promotion retVal = new Promotion ();
		retVal.setId(rs.getInt("ID"));
		retVal.setAmenity(new AmenityDto(rs.getInt("CLUB_AMENITY_ID")));
		retVal.setClub(new ClubDto(rs.getInt("CLUB_ID")));
		retVal.setDescription(rs.getString("DETAILS"));
		retVal.setTitle(rs.getString("TITLE"));
		retVal.setEffectiveDate(rs.getTimestamp("EFFECTIVE_DATE"));
		retVal.setEndingDate(rs.getTimestamp("ENDING_DATE"));

		return retVal;
	}

}
