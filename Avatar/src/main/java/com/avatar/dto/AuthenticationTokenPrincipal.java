package com.avatar.dto;

import java.util.Set;
import java.util.UUID;

import com.avatar.dto.enums.Privilege;

public class AuthenticationTokenPrincipal {
	private final UUID token;
	private final Set<Privilege> roles;

	public AuthenticationTokenPrincipal(final Set<Privilege> roles) {
		token = UUID.randomUUID();
		this.roles = roles;
	}

	public Set<Privilege> getRoles() {
		return roles;
	}

	public UUID getToken() {
		return token;
	}

	boolean hasRole(final Privilege role) {
		return (roles != null) && roles.contains(role);
	}
}
