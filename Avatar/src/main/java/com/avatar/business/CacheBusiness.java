package com.avatar.business;

import com.avatar.exception.InvalidParameterException;
import com.avatar.exception.NotFoundException;

public interface CacheBusiness<M, N> {
	void clear();

	N get(M m) throws NotFoundException, InvalidParameterException;

	void put(M m, N n) throws NotFoundException;
}
