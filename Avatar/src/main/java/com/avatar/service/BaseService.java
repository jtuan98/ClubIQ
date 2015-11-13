package com.avatar.service;

import java.util.Date;

import javax.annotation.Resource;

import org.joda.time.DateTime;

import com.avatar.business.DbDateBusiness;
import com.avatar.dao.DbDateDao;
import com.avatar.dto.enums.DbTimeZone;

public class BaseService implements DbDateBusiness {
	@Resource(name = "accountDaoJdbc")
	protected DbDateDao dbDateDao;

	@Override
	public Date getNow() {
		return dbDateDao.getNow();
	}

	@Override
	public Date getNow(final DbTimeZone tz) {
		if (tz == null) {
			return getNow();
		}
		return dbDateDao.getNow(tz);
	}

	@Override
	public DateTime getNowDateTime() {
		final Date now = getNow();
		return new DateTime(now.getTime());
	}

}
