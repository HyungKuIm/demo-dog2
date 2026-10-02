package com.oraclejava.demodog.member;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MemberService {

	private final MemberRepository memberRepository;

	public MemberService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	/**
	 * Registers a regular member. Admins cannot sign up; they are inserted ahead of time.
	 */
	public LoginMember signup(SignupRequest request) {
		if (memberRepository.existsByUsername(request.username())) {
			throw duplicateUsername(request.username());
		}
		try {
			Member member = memberRepository.saveAndFlush(
					new Member(request.username(), request.password(), Role.USER));
			return LoginMember.of(member);
		}
		catch (DataIntegrityViolationException ex) {
			// Another request registered the same username in the meantime.
			throw duplicateUsername(request.username());
		}
	}

	@Transactional(readOnly = true)
	public LoginMember login(LoginRequest request) {
		return memberRepository.findByUsername(request.username())
				.filter(member -> member.matchesPassword(request.password()))
				.map(LoginMember::of)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"Invalid username or password"));
	}

	private ResponseStatusException duplicateUsername(String username) {
		return new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists: " + username);
	}

}
