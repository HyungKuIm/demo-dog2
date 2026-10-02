package com.oraclejava.demodog.cart;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping
	public CartResponse list() {
		return cartService.get();
	}

	@PostMapping("/items")
	public CartResponse add(@Valid @RequestBody CartRequest request) {
		return cartService.add(request);
	}

	@DeleteMapping("/items/{dogId}")
	public CartResponse remove(@PathVariable Long dogId) {
		return cartService.remove(dogId);
	}

	@DeleteMapping
	public ResponseEntity<Void> clear() {
		cartService.clear();
		return ResponseEntity.noContent().build();
	}

}
