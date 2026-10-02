package com.oraclejava.demodog.cart;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Per-session shopping cart holding dog ids and their quantities in the order they were added.
 */
@Component
@SessionScope
public class Cart implements Serializable {

	private final Map<Long, Integer> items = new LinkedHashMap<>();

	public synchronized void add(Long dogId, int quantity) {
		items.merge(dogId, quantity, Integer::sum);
	}

	public synchronized void remove(Long dogId) {
		items.remove(dogId);
	}

	public synchronized void clear() {
		items.clear();
	}

	public synchronized Map<Long, Integer> getItems() {
		return new LinkedHashMap<>(items);
	}

}
