package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.account.ActivationToken;
import com.avatar.dto.account.MobileActivationPin;

public class ActivationTokenMapper implements RowMapper<ActivationToken> {

	@Override
	public ActivationToken mapRow(final ResultSet rs, final int arg1)
			throws SQLException {
		final boolean mobile = "Y".equals(rs.getString("MOBILE_PIN_FLAG"));
		final ActivationToken retVal = mobile ? new MobileActivationPin()
				: new ActivationToken();
		retVal.setId(rs.getInt("ID"));
		retVal.setToken(rs.getString("TOKEN"));
		retVal.setExpirationDate(rs.getDate("VALID_TILL"));
		return retVal;
	}

}
