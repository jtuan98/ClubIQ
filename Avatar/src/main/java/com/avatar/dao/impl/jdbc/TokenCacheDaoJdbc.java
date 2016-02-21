package com.avatar.dao.impl.jdbc;

import java.util.Date;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import com.avatar.dao.TokenCacheDao;
import com.avatar.dto.account.AccountDto;
import com.avatar.exception.NotFoundException;

@Repository
public class TokenCacheDaoJdbc extends BaseJdbcDao implements TokenCacheDao {
	@Override
	public int cleanupExpiredTokens() {
		return getJdbcTemplate().update("DELETE FROM TOKEN_CACHE where VALID_TILL < NOW()");
	}

	@Override
	public int fetchAccountIdPk(final String token) throws NotFoundException {
		int userIdPk = 0;
		try {
			userIdPk = getJdbcTemplate()
					.queryForObject(
							"SELECT USER_ID FROM TOKEN_CACHE WHERE token = ? and valid_till > NOW()",
							Integer.class, token);
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException();
		}
		return userIdPk;
	}

	@Override
	public void persist(final String token, final Date validTill,
			final AccountDto account) throws NotFoundException {
		final int idToken = sequencer.nextVal("ID_SEQ");
		getJdbcTemplate()
		.update("INSERT INTO TOKEN_CACHE (ID, TOKEN, VALID_TILL, USER_ID) values (?, ?, ?, ?)",
				idToken, token,
				yyyyMMdd_hh24missDtf.print(validTill.getTime()),
				account.getId());
	}

	@Override
	public void removeAll() {
		truncate("TOKEN_CACHE");
	}

	@Override
	@Resource(name = "avatarDataSource")
	public void setDataSource(final DataSource ds) {
		initTemplate(ds);
	}

}
