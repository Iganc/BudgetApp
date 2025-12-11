package com.example.demo.controller;

import com.example.demo.model.Budget;
import com.example.demo.model.User;
import com.example.demo.service.BudgetAnalyticsService;
import com.example.demo.service.BudgetService;
import com.example.demo.service.UserService;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BudgetAnalyticsService budgetAnalyticsService;

    @PostMapping
    public ResponseEntity<Budget> createBudget(@RequestBody Budget budget, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        budget.setUser(user);
        Budget created = budgetService.createBudget(budget);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return budgetService.getBudgetById(id)
                .filter(budget -> budget.getUser().getId().equals(userId))
                .map(budget -> new ResponseEntity<>(budget, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets(@RequestHeader("Authorization") String authHeader) {
        System.out.println("Getting budgets, auth header: " + authHeader);
        Long userId = getUserIdFromToken(authHeader);
        System.out.println("Extracted userId: " + userId);
        List<Budget> budgets = budgetService.getBudgetsByUserId(userId);
        System.out.println("Found budgets: " + budgets.size());
        return new ResponseEntity<>(budgets, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(@PathVariable Long id, @RequestBody Budget budget, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        try {
            Budget existing = budgetService.getBudgetById(id)
                    .filter(b -> b.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Budget not found"));
            Budget updated = budgetService.updateBudget(id, budget);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        try {
            Budget budget = budgetService.getBudgetById(id)
                    .filter(b -> b.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Budget not found"));
            budgetService.deleteBudget(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{budgetId}/spent")
    public ResponseEntity<BigDecimal> getTotalSpentForBudget(
            @PathVariable Long budgetId,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);

        budgetService.getBudgetById(budgetId)
                .filter(b -> b.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Budget not found or access denied"));

        BigDecimal totalSpent = budgetAnalyticsService.calculateTotalSpent(budgetId);

        return new ResponseEntity<>(totalSpent, HttpStatus.OK);
    }

    @GetMapping("/{budgetId}/earned")
    public ResponseEntity<BigDecimal> getTotalIncomeForBudget(
            @PathVariable Long budgetId,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);

        budgetService.getBudgetById(budgetId)
                .filter(b -> b.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Budget not found or access denied"));

        BigDecimal totalIncome = budgetAnalyticsService.calculateTotalIncome(budgetId);

        return new ResponseEntity<>(totalIncome, HttpStatus.OK);
    }

    @GetMapping("/{budgetId}/balance")
    public ResponseEntity<BigDecimal> getBalanceForBudget(
            @PathVariable Long budgetId,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);

        budgetService.getBudgetById(budgetId)
                .filter(b -> b.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Budget not found or access denied"));

        BigDecimal balance = budgetAnalyticsService.calculateBalance(budgetId);

        return new ResponseEntity<>(balance, HttpStatus.OK);
    }

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}