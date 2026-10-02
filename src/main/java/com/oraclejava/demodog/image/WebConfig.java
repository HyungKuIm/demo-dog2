package com.oraclejava.demodog.image;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final String imageDir;

	public WebConfig(@Value("${app.image-dir}") String imageDir) {
		this.imageDir = imageDir;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(ImageStorage.URL_PREFIX + "**")
				.addResourceLocations(Path.of(imageDir).toAbsolutePath().normalize().toUri().toString());
	}

}
