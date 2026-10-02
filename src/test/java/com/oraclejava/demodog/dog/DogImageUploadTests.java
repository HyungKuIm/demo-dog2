package com.oraclejava.demodog.dog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.oraclejava.demodog.image.ImageStorage;

@SpringBootTest
@AutoConfigureMockMvc
class DogImageUploadTests {

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 1, 2, 3 };

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ImageStorage imageStorage;

	@Test
	void uploadServeReplaceAndDelete() throws Exception {
		Number id = createDog();

		String first = uploadImage(id, "a.png");
		assertThat(first).startsWith("/images/").endsWith(".png");
		Path firstFile = imageStorage.getDirectory().resolve(first.substring("/images/".length()));
		assertThat(firstFile).exists();

		mockMvc.perform(get(first))
				.andExpect(status().isOk())
				.andExpect(content().bytes(PNG));

		String second = uploadImage(id, "b.png");
		assertThat(second).isNotEqualTo(first);
		assertThat(firstFile).doesNotExist();

		mockMvc.perform(delete("/dogs/{id}", id)).andExpect(status().isNoContent());
		assertThat(Files.exists(imageStorage.getDirectory().resolve(second.substring("/images/".length()))))
				.isFalse();
	}

	@Test
	void rejectsNonImageFile() throws Exception {
		Number id = createDog();
		mockMvc.perform(multipart("/dogs/{id}/image", id)
				.file(new MockMultipartFile("file", "evil.txt", "text/plain", "hello".getBytes())))
				.andExpect(status().isUnsupportedMediaType());
	}

	@Test
	void uploadToMissingDogReturns404() throws Exception {
		mockMvc.perform(multipart("/dogs/{id}/image", 999999)
				.file(new MockMultipartFile("file", "a.png", MediaType.IMAGE_PNG_VALUE, PNG)))
				.andExpect(status().isNotFound());
	}

	private Number createDog() throws Exception {
		String body = mockMvc.perform(post("/dogs").contentType(MediaType.APPLICATION_JSON)
				.content("{\"kind\":\"Beagle\",\"price\":900000,\"height\":38,\"weight\":10}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.id");
	}

	private String uploadImage(Number id, String filename) throws Exception {
		String body = mockMvc.perform(multipart("/dogs/{id}/image", id)
				.file(new MockMultipartFile("file", filename, MediaType.IMAGE_PNG_VALUE, PNG)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.image");
	}

}
