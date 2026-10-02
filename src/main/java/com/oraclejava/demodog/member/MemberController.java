package com.oraclejava.demodog.member;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/members")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@PostMapping("/signup")
	public ResponseEntity<LoginMember> signup(@Valid @RequestBody SignupRequest request) {
		LoginMember member = memberService.signup(request);
		return ResponseEntity.created(URI.create("/members/" + member.id())).body(member);
	}

	@PostMapping("/login")
	public LoginMember login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
		LoginMember member = memberService.login(request);
		HttpSession session = servletRequest.getSession();
		// Issue a new session id on login to prevent session fixation; the cart is kept.
		servletRequest.changeSessionId();
		session.setAttribute(LoginMember.SESSION_KEY, member);
		return member;
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
		HttpSession session = servletRequest.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	public LoginMember me(@SessionAttribute(name = LoginMember.SESSION_KEY, required = false) LoginMember member) {
		if (member == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
		}
		return member;
	}

}
