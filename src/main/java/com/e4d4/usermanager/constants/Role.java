package com.e4d4.usermanager.constants;

public enum Role {
	ROLE_USER(Authority.USER_AUTHORITIES),
	ROLE_HR(Authority.HR_AUTHORITIES),
	ROLE_MANAGER(Authority.MANAGER_AUTHORITIES),
	ROLE_ADMIN(Authority.ADMIN_AUTHORITIES),
	ROLE_SUPER_USER(Authority.SUPER_ADMIN_AUTHORITIES);
	
	private String[] authorities;

	Role(String[] authorities) {
		this.authorities=authorities;
	}
	
	public String[] getAuthorities() {
		return authorities;
	}
}
