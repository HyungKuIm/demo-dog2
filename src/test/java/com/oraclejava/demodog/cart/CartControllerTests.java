package com.oraclejava.demodog.cart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void addFromDetailAndList() throws Exception {
		Number poodle = createDog("Poodle", 1500000);
		Number beagle = createDog("Beagle", 800000);
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(get("/dogs/{id}", poodle).session(session))
				.andExpect(status().isOk());
		addToCart(session, "{\"dogId\":%s}".formatted(poodle))
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].quantity").value(1));
		addToCart(session, "{\"dogId\":%s,\"quantity\":2}".formatted(poodle))
				.andExpect(jsonPath("$.items[0].quantity").value(3));
		addToCart(session, "{\"dogId\":%s}".formatted(beagle));

		mockMvc.perform(get("/cart").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].kind").value("Poodle"))
				.andExpect(jsonPath("$.items[0].amount").value(4500000))
				.andExpect(jsonPath("$.items[1].kind").value("Beagle"))
				.andExpect(jsonPath("$.totalQuantity").value(4))
				.andExpect(jsonPath("$.totalPrice").value(5300000));

		// Another session has its own empty cart.
		mockMvc.perform(get("/cart").session(new MockHttpSession()))
				.andExpect(jsonPath("$.items.length()").value(0));

		mockMvc.perform(delete("/cart/items/{dogId}", poodle).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.totalPrice").value(800000));

		// Deleted dogs drop out of the cart.
		mockMvc.perform(delete("/dogs/{id}", beagle)).andExpect(status().isNoContent());
		mockMvc.perform(get("/cart").session(session))
				.andExpect(jsonPath("$.items.length()").value(0))
				.andExpect(jsonPath("$.totalPrice").value(0));

		addToCart(session, "{\"dogId\":%s}".formatted(poodle));
		mockMvc.perform(delete("/cart").session(session)).andExpect(status().isNoContent());
		mockMvc.perform(get("/cart").session(session))
				.andExpect(jsonPath("$.items.length()").value(0));
	}

	@Test
	void addRejectsUnknownDogAndInvalidQuantity() throws Exception {
		mockMvc.perform(post("/cart/items").contentType(MediaType.APPLICATION_JSON).content("{\"dogId\":999999}"))
				.andExpect(status().isNotFound());
		Number id = createDog("Shiba", 1000000);
		mockMvc.perform(post("/cart/items").contentType(MediaType.APPLICATION_JSON)
				.content("{\"dogId\":%s,\"quantity\":0}".formatted(id)))
				.andExpect(status().isBadRequest());
	}

	private ResultActions addToCart(MockHttpSession session, String body)
			throws Exception {
		return mockMvc.perform(post("/cart/items").session(session)
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isOk());
	}

	private Number createDog(String kind, int price) throws Exception {
		String body = mockMvc.perform(post("/dogs").contentType(MediaType.APPLICATION_JSON)
				.content("{\"kind\":\"%s\",\"price\":%d,\"height\":30,\"weight\":5}".formatted(kind, price)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.id");
	}

}
