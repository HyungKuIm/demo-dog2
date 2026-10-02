package com.oraclejava.demodog.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void signupLoginMeAndLogout() throws Exception {
		mockMvc.perform(post("/members/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"alice\",\"password\":\"alice1234\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("alice"))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.password").doesNotExist());

		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(get("/members/me").session(session))
				.andExpect(status().isUnauthorized());

		login(session, "alice", "alice1234")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("alice"))
				.andExpect(jsonPath("$.password").doesNotExist());
		mockMvc.perform(get("/members/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("alice"))
				.andExpect(jsonPath("$.role").value("USER"));

		mockMvc.perform(post("/members/logout").session(session))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/members/me").session(new MockHttpSession()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminIsPreInserted() throws Exception {
		MockHttpSession session = new MockHttpSession();
		login(session, "admin", "admin1234")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	void signupRejectsDuplicateAndInvalidRequest() throws Exception {
		mockMvc.perform(post("/members/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"whatever\"}"))
				.andExpect(status().isConflict());
		mockMvc.perform(post("/members/signup").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"\",\"password\":\"12\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void loginRejectsWrongPassword() throws Exception {
		login(new MockHttpSession(), "admin", "wrong").andExpect(status().isUnauthorized());
		login(new MockHttpSession(), "nobody", "whatever").andExpect(status().isUnauthorized());
	}

	private ResultActions login(MockHttpSession session, String username, String password) throws Exception {
		return mockMvc.perform(post("/members/login").session(session).contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)));
	}

}
