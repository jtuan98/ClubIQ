package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.account.AccountDto;

public class AccountDtoCheckInDateMapper implements RowMapper<AccountDto> {
	private final AccountDtoMapper accountDtoMapper = new AccountDtoMapper();
	@Override
	public AccountDto mapRow(final ResultSet rs, final int rowNo) throws SQLException {
		final AccountDto accountDto = accountDtoMapper.mapRow(rs, rowNo);
		final Date checkInDate = rs.getTimestamp("CHECKIN_DATE");
		accountDto.setLastCheckInDate(checkInDate);
		return accountDto;
	}


}
