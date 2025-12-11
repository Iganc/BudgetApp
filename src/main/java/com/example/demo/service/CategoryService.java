package com.example.demo.service;

import com.example.demo.model.Category;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    private static final String[] DEFAULT_CATEGORY_NAMES = {
            "Groceries", "Rent", "Salary", "Transport", "Entertainment", "Utilities"
    };

    public void initializeDefaultCategories() {
        for (String name : DEFAULT_CATEGORY_NAMES) {
            if (categoryRepository.findByNameAndIsDefault(name, true).isEmpty()) {
                Category defaultCat = new Category(name, null, true); // user=null dla domyślnych
                categoryRepository.save(defaultCat);
            }
        }
    }

    public List<Category> getAvailableCategories(Long userId) {
        return categoryRepository.findDefaultAndUserCategories(userId);
    }

    public Category createCustomCategory(String name, Long userId) {
        if (categoryRepository.findByNameAndUserId(name, userId).isPresent()) {
            throw new RuntimeException("Category with this name already exists.");
        }

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category newCategory = new Category(name, user, false);
        return categoryRepository.save(newCategory);
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
}