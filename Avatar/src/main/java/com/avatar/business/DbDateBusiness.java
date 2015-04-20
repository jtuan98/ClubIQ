package com.avatar.business;

import java.util.Date;

import com.avatar.dto.enums.DbTimeZone;

public interface DbDateBusiness {
	Date getNow();
	Date getNow(DbTimeZone tz);
}
