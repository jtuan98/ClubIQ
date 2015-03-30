package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class StringMapper implements RowMapper<String> {

	@Override
	public String mapRow(final ResultSet rs, final int rowNo) throws SQLException {
		return rs.getString(1);
	}

}
