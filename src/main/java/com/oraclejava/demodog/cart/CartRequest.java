package com.oraclejava.demodog.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartRequest(
		@NotNull Long dogId,
		@Min(1) @Max(99) Integer quantity) {

	public int quantityOrDefault() {
		return quantity == null ? 1 : quantity;
	}

}
