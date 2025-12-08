package com.example.demo.service;

import com.example.demo.model.Budget;
import com.example.demo.repository.BudgetRepository;
import com.example.demo.validation.BudgetValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetValidator budgetValidator;

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public Optional<Budget> getBudgetById(Long id) {
        return budgetRepository.findById(id);
    }

    public List<Budget> getBudgetsByUserId(Long userId) {
        return budgetRepository.findByUserId(userId);
    }

    public Budget createBudget(Budget budget) {
        budgetValidator.validateBudget(budget);
        return budgetRepository.save(budget);
    }

    public Budget updateBudget(Long id, Budget budgetDetails) {
        return budgetRepository.findById(id).map(budget -> {
            budget.setName(budgetDetails.getName());
            budget.setCategory(budgetDetails.getCategory());
            budget.setLimit(budgetDetails.getLimit());
            budget.setStartDate(budgetDetails.getStartDate());
            budget.setEndDate(budgetDetails.getEndDate());

            budgetValidator.validateBudget(budget);

            return budgetRepository.save(budget);
        }).orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));
    }

    public void deleteBudget(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new RuntimeException("Budget not found with id: " + id);
        }
        budgetRepository.deleteById(id);
    }
}