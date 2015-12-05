package com.avatar.dao;

import com.avatar.exception.NotFoundException;

public interface ReferenceDataDao {
	int getStatePk(String state) throws NotFoundException;
}
