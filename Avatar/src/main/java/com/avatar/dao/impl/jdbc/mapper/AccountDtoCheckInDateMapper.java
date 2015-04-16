package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.account.AccountDto;

public class AccountDtoCheckInDateMapper implements RowMapper<ImmutablePair<AccountDto, Date>> {
	private final AccountDtoMapper accountDtoMapper = new AccountDtoMapper();
	@Override
	public ImmutablePair<AccountDto, Date> mapRow(final ResultSet rs, final int rowNo) throws SQLException {
		final AccountDto accountDto = accountDtoMapper.mapRow(rs, rowNo);
		final Date checkInDate = rs.getTimestamp("CHECKIN_DATE");
		final ImmutablePair<AccountDto, Date> retVal = new ImmutablePair<AccountDto, Date>(accountDto, checkInDate);
		return retVal;
	}


}
