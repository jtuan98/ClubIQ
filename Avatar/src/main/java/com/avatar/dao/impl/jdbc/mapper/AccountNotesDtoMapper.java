package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.avatar.dto.account.AccountNotes;

public class AccountNotesDtoMapper implements RowMapper<AccountNotes> {

	@Override
	public AccountNotes mapRow(final ResultSet rs, final int rowNo)
			throws SQLException {
		final AccountNotes retVal = new AccountNotes();
		retVal.setId(rs.getInt("ID"));
		retVal.setNoteDate(rs.getDate("NOTE_DATE"));
		retVal.setNoteText(rs.getString("NOTE_TEXT"));
		return retVal;
	}

}
