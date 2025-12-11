package com.example.demo;

import com.example.demo.service.CategoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(CategoryService categoryService) {
		return args -> {
			System.out.println("--- Initializing default categories ---");
			categoryService.initializeDefaultCategories();
			System.out.println("--- Default categories initialization complete ---");
		};
	}
}