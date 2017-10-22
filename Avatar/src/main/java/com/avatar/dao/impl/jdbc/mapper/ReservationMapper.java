package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.account.MemberAccountDto;
import com.avatar.dto.club.CheckInfo;


public class ReservationMapper implements RowMapper<CheckInfo> {

	@Override
	public CheckInfo mapRow(final ResultSet rs, final int rowNumber) throws SQLException {
		final CheckInfo retVal = new CheckInfo ();
		retVal.setId(rs.getInt("ID"));
		retVal.setAvailId(rs.getString("RESERVATION_NUMBER"));
		retVal.setSubAmenityId(rs.getString("SUBAMENITYID"));
		retVal.setSubAmenityName(rs.getString("SUBAMENITY_NAME"));
		retVal.setPersonNumber(rs.getInt("NO_PERSONS"));
		retVal.setRequestedClubId(rs.getString("CLUBID"));
		if (StringUtils.isNotEmpty(rs.getString("RESERVATION_DATE"))) {
			retVal.setRequestedDateTime(rs.getTimestamp("RESERVATION_DATE"));
		}
		if (StringUtils.isNotEmpty(rs.getString("RESERVATION_TODATE"))) {
			retVal.setRequestedToDate(rs.getTimestamp("RESERVATION_TODATE"));
		}
		final MemberAccountDto member = new MemberAccountDto();
		member.setUserId(rs.getString("USERID"));
		member.setMobileNumber(rs.getString("MOBILE_NUMBER"));
		retVal.setMember(member);
		return retVal;
	}

}
