package com.oraclejava.demodog.dog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class DogControllerTests {

	private static final String POODLE = """
			{"kind":"Poodle","price":1500000,"image":"poodle.jpg","country":"France",
			 "height":30.5,"weight":4.2,"content":"Smart and friendly"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void crud() throws Exception {
		String body = mockMvc.perform(post("/dogs").contentType(MediaType.APPLICATION_JSON).content(POODLE))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.kind").value("Poodle"))
				.andExpect(jsonPath("$.readcount").value(0))
				.andReturn().getResponse().getContentAsString();
		Number id = JsonPath.read(body, "$.id");

		mockMvc.perform(get("/dogs/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.readcount").value(1));
		mockMvc.perform(get("/dogs/{id}", id))
				.andExpect(jsonPath("$.readcount").value(2));

		mockMvc.perform(get("/dogs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == %s)].kind".formatted(id)).value("Poodle"));

		mockMvc.perform(put("/dogs/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(POODLE.replace("1500000", "1200000")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.price").value(1200000))
				.andExpect(jsonPath("$.readcount").value(2));

		mockMvc.perform(delete("/dogs/{id}", id))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/dogs/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void createRejectsInvalidRequest() throws Exception {
		mockMvc.perform(post("/dogs").contentType(MediaType.APPLICATION_JSON)
				.content("{\"kind\":\"\",\"price\":-1}"))
				.andExpect(status().isBadRequest());
	}

}
