package com.avatar.dao.impl.jdbc;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.avatar.dao.Sequencer;

@Repository
public class AvatarSequencer extends BaseJdbcDao implements Sequencer {

	private static final String SEQ_SQL = "select sequence.nextval(?) as ID";

	@Override
	public int nextVal(final String sequencerName) {
		return getJdbcTemplate().queryForObject(SEQ_SQL, Integer.class,
				sequencerName);
	}

	@Override
	@Resource(name = "avatarDataSource")
	public void setDataSource(final DataSource ds) {
		initTemplate(ds);
	}
}
