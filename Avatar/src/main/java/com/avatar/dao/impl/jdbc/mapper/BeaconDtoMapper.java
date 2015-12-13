package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.club.BeaconDto;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.club.SubAmenityDto;

public class BeaconDtoMapper implements RowMapper<BeaconDto> {

	@Override
	public BeaconDto mapRow(final ResultSet rs, final int rowNo)
			throws SQLException {
		final BeaconDto retVal = new BeaconDto();
		retVal.setId(rs.getInt("ID"));
		retVal.setBeaconActionId(rs.getString("BEACONID"));
		retVal.setClub(new ClubDto(rs.getInt("CLUB_ID")));
		retVal.setSubAmenity(new SubAmenityDto(rs.getInt("SUBAMENITY_ID")));
		retVal.setDescription(rs.getString("DESCRIPTION"));
		retVal.setLocation(rs.getString("LOCATION"));
		retVal.setInstallerStaff(new EmployeeAccountDto(rs
				.getInt("INSTALLATION_STAFF_ID")));
		retVal.setInstallationDate(rs.getTimestamp("INSTALLATION_DATE"));
		return retVal;
	}

}
