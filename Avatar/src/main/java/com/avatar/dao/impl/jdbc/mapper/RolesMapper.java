package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.enums.Privilege;

public class RolesMapper implements RowMapper<Privilege> {

	@Override
	public Privilege mapRow(final ResultSet rs, final int rowNo) throws SQLException {
		final Privilege retVal = Privilege.valueOf(rs.getString("ROLE"));
		return retVal;
	}

}
