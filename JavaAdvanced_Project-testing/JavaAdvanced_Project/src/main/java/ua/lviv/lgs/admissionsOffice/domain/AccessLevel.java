package ua.lviv.lgs.admissionsOffice.domain;

import org.springframework.security.core.GrantedAuthority;

public enum AccessLevel implements GrantedAuthority {
	USER, ADMIN;

	@Override
	public String getAuthority() {
		return name();
	}
}
