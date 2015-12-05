package com.avatar.dao.impl.jdbc;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.avatar.dao.ReferenceDataDao;
import com.avatar.exception.NotFoundException;

@Repository
public class ReferenceDataDaoJdbc extends BaseJdbcDao implements ReferenceDataDao {

	@Override
	public int getStatePk(final String state) throws NotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDataSource(final DataSource ds) {
		// TODO Auto-generated method stub

	}

}
