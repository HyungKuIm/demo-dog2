package com.oraclejava.demodog.member;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Inserts the admin account (role ADMIN) on startup if it does not exist yet.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

	private final MemberRepository memberRepository;

	private final String username;

	private final String password;

	public AdminInitializer(MemberRepository memberRepository,
			@Value("${app.admin.username}") String username,
			@Value("${app.admin.password}") String password) {
		this.memberRepository = memberRepository;
		this.username = username;
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!memberRepository.existsByUsername(username)) {
			memberRepository.save(new Member(username, password, Role.ADMIN));
		}
	}

}
