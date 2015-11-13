package com.avatar.business;

import java.util.Date;

import org.joda.time.DateTime;

import com.avatar.dto.enums.DbTimeZone;

public interface DbDateBusiness {
	Date getNow();
	Date getNow(DbTimeZone tz);
	DateTime getNowDateTime();
}
