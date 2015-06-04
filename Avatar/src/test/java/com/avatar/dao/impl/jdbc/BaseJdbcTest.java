package com.avatar.dao.impl.jdbc;

import static org.mockito.BDDMockito.given;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.test.util.ReflectionTestUtils;

import com.avatar.dao.Sequencer;
import com.avatar.dao.impl.jdbc.sql.BaseDaoSql;

abstract class BaseJdbcTest {
	@Mock
	protected LobHandler lobHandler;
	@Mock
	protected NamedParameterJdbcTemplate namedParam;
	@Mock
	protected JdbcTemplate jdbcTemplate;
	@Mock
	protected Sequencer sequencer;
	private final String timezone = "WhereEver";
	private int nextVal = 0;

	protected void setUp(final BaseJdbcDao dao) {
		MockitoAnnotations.initMocks(this);
		// Spring utility class for use in unit and integration testing
		// scenarios.
		// Allows mocks to be used where no setters are accessible
		ReflectionTestUtils.setField(dao, "lobHandler", lobHandler);
		ReflectionTestUtils.setField(dao, "namedParam", namedParam);
		ReflectionTestUtils.setField(dao, "jdbcTemplate", jdbcTemplate);
		ReflectionTestUtils.setField(dao, "sequencer", sequencer);
		ReflectionTestUtils.setField(dao, "timezone", timezone);

		given(
				jdbcTemplate.queryForObject(
						BaseDaoSql.GET_SESSION_TIMEZONE, String.class))
						.willReturn(timezone);
		given(sequencer.nextVal("ID_SEQ")).willReturn(nextVal++);
	}
}
