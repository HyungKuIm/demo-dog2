package com.oraclejava.demodog.dog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record DogRequest(
		@NotBlank String kind,
		@PositiveOrZero int price,
		String image,
		String country,
		@PositiveOrZero double height,
		@PositiveOrZero double weight,
		@Size(max = 4000) String content) {
}
