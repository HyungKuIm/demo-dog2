package com.oraclejava.demodog.cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.oraclejava.demodog.dog.Dog;
import com.oraclejava.demodog.dog.DogRepository;

@Service
@Transactional(readOnly = true)
public class CartService {

	private final Cart cart;

	private final DogRepository dogRepository;

	public CartService(Cart cart, DogRepository dogRepository) {
		this.cart = cart;
		this.dogRepository = dogRepository;
	}

	public CartResponse add(CartRequest request) {
		if (!dogRepository.existsById(request.dogId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found: " + request.dogId());
		}
		cart.add(request.dogId(), request.quantityOrDefault());
		return get();
	}

	public CartResponse get() {
		Map<Long, Integer> items = cart.getItems();
		Map<Long, Dog> dogs = dogRepository.findAllById(items.keySet()).stream()
				.collect(Collectors.toMap(Dog::getId, Function.identity()));
		List<CartResponse.Item> lines = new ArrayList<>();
		items.forEach((dogId, quantity) -> {
			Dog dog = dogs.get(dogId);
			if (dog == null) {
				// The dog was deleted after being added to the cart.
				cart.remove(dogId);
			}
			else {
				lines.add(CartResponse.Item.of(dog, quantity));
			}
		});
		return CartResponse.of(lines);
	}

	public CartResponse remove(Long dogId) {
		cart.remove(dogId);
		return get();
	}

	public void clear() {
		cart.clear();
	}

}
