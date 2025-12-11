package com.example.demo.controller;

import com.example.demo.model.Category;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getCategories(
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader); // Pobranie userId
        List<Category> categories = categoryService.getAvailableCategories(userId);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(
            @RequestBody Map<String, String> payload,
            @RequestHeader("Authorization") String authHeader) {

        String name = payload.get("name");
        if (name == null || name.trim().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Long userId = getUserIdFromToken(authHeader); // Pobranie userId

        try {
            Category newCategory = categoryService.createCustomCategory(name, userId);
            return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
}