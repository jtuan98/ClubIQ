package com.avatar.business;

import java.util.Set;

import com.avatar.dto.AuthenticationTokenPrincipal;
import com.avatar.dto.enums.Privilege;
import com.avatar.exception.AuthenticationTokenExpiredException;
import com.avatar.exception.InvalidPasswordException;
import com.avatar.exception.NotFoundException;

public interface AuthenticationTokenizerBusiness {

	Set<Privilege> getRoles(String token) throws NotFoundException,
			AuthenticationTokenExpiredException;

	AuthenticationTokenPrincipal getToken(String userId, String password)
			throws InvalidPasswordException, NotFoundException;

}