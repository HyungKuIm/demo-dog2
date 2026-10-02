package com.oraclejava.demodog.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
		@NotBlank @Size(max = 50) String username,
		@NotBlank @Size(min = 4, max = 100) String password) {
}
