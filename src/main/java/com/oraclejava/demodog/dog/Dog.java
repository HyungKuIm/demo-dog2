package com.oraclejava.demodog.dog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Dog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String kind;

	private int price;

	private String image;

	private String country;

	private double height;

	private double weight;

	@Column(length = 4000)
	private String content;

	private int readcount;

	protected Dog() {
	}

	public Dog(DogRequest request) {
		update(request);
	}

	public void update(DogRequest request) {
		this.kind = request.kind();
		this.price = request.price();
		this.image = request.image();
		this.country = request.country();
		this.height = request.height();
		this.weight = request.weight();
		this.content = request.content();
	}

	public void changeImage(String image) {
		this.image = image;
	}

	public void increaseReadcount() {
		this.readcount++;
	}

	public Long getId() {
		return id;
	}

	public String getKind() {
		return kind;
	}

	public int getPrice() {
		return price;
	}

	public String getImage() {
		return image;
	}

	public String getCountry() {
		return country;
	}

	public double getHeight() {
		return height;
	}

	public double getWeight() {
		return weight;
	}

	public String getContent() {
		return content;
	}

	public int getReadcount() {
		return readcount;
	}

}
