package com.oraclejava.demodog.dog;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.oraclejava.demodog.image.ImageStorage;

@Service
@Transactional
public class DogService {

	private final DogRepository dogRepository;

	private final ImageStorage imageStorage;

	public DogService(DogRepository dogRepository, ImageStorage imageStorage) {
		this.dogRepository = dogRepository;
		this.imageStorage = imageStorage;
	}

	@Transactional(readOnly = true)
	public List<Dog> findAll() {
		return dogRepository.findAll();
	}

	public Dog read(Long id) {
		Dog dog = getDog(id);
		dog.increaseReadcount();
		return dog;
	}

	public Dog create(DogRequest request) {
		return dogRepository.save(new Dog(request));
	}

	public Dog update(Long id, DogRequest request) {
		Dog dog = getDog(id);
		String oldImage = dog.getImage();
		dog.update(request);
		if (!Objects.equals(oldImage, dog.getImage())) {
			imageStorage.delete(oldImage);
		}
		return dog;
	}

	public Dog uploadImage(Long id, MultipartFile file) {
		Dog dog = getDog(id);
		String oldImage = dog.getImage();
		dog.changeImage(imageStorage.store(file));
		imageStorage.delete(oldImage);
		return dog;
	}

	public void delete(Long id) {
		Dog dog = getDog(id);
		dogRepository.delete(dog);
		imageStorage.delete(dog.getImage());
	}

	private Dog getDog(Long id) {
		return dogRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found: " + id));
	}

}
