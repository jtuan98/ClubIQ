package com.avatar.dao;

import java.util.Date;

import com.avatar.dto.enums.DbTimeZone;

public interface DbDateDao {
	Date getNow();
	Date getNow(DbTimeZone tz);
}
