package com.oraclejava.demodog.image;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ImageStorage {

	public static final String URL_PREFIX = "/images/";

	private static final Map<String, String> EXTENSIONS = Map.of(
			"image/jpeg", ".jpg",
			"image/png", ".png",
			"image/gif", ".gif",
			"image/webp", ".webp");

	private final Path directory;

	public ImageStorage(@Value("${app.image-dir}") String directory) {
		this.directory = Path.of(directory).toAbsolutePath().normalize();
	}

	public Path getDirectory() {
		return directory;
	}

	/**
	 * Saves the file under a generated name and returns its public URL.
	 */
	public String store(MultipartFile file) {
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is empty");
		}
		String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
		String extension = EXTENSIONS.get(contentType);
		if (extension == null) {
			throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
					"Only JPEG, PNG, GIF and WebP images are allowed");
		}
		String filename = UUID.randomUUID() + extension;
		try (InputStream in = file.getInputStream()) {
			Files.createDirectories(directory);
			Files.copy(in, directory.resolve(filename));
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to store image " + filename, ex);
		}
		return URL_PREFIX + filename;
	}

	/**
	 * Deletes an image previously returned by {@link #store}; other values are ignored.
	 */
	public void delete(String url) {
		if (url == null || !url.startsWith(URL_PREFIX)) {
			return;
		}
		Path file = directory.resolve(url.substring(URL_PREFIX.length())).normalize();
		if (!file.getParent().equals(directory)) {
			return;
		}
		try {
			Files.deleteIfExists(file);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to delete image " + file, ex);
		}
	}

}
