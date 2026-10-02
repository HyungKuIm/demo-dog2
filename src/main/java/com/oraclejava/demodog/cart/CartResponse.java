package com.oraclejava.demodog.cart;

import java.util.List;

import com.oraclejava.demodog.dog.Dog;

public record CartResponse(List<Item> items, int totalQuantity, long totalPrice) {

	public static CartResponse of(List<Item> items) {
		int totalQuantity = items.stream().mapToInt(Item::quantity).sum();
		long totalPrice = items.stream().mapToLong(Item::amount).sum();
		return new CartResponse(items, totalQuantity, totalPrice);
	}

	public record Item(Long dogId, String kind, String image, int price, int quantity, long amount) {

		public static Item of(Dog dog, int quantity) {
			return new Item(dog.getId(), dog.getKind(), dog.getImage(), dog.getPrice(), quantity,
					(long) dog.getPrice() * quantity);
		}

	}

}
