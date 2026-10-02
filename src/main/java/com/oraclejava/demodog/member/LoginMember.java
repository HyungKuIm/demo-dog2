package com.oraclejava.demodog.member;

import java.io.Serializable;

/**
 * Logged-in member kept in the HTTP session. Also used as the API response, so it never carries the password.
 */
public record LoginMember(Long id, String username, Role role) implements Serializable {

	public static final String SESSION_KEY = "loginMember";

	public static LoginMember of(Member member) {
		return new LoginMember(member.getId(), member.getUsername(), member.getRole());
	}

	public boolean isAdmin() {
		return role == Role.ADMIN;
	}

}
