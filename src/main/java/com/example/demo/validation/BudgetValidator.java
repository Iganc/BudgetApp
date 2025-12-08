package com.example.demo.validation;

import com.example.demo.model.Budget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BudgetValidator {

    @Autowired
    private DateRangeValidator dateRangeValidator;

    @Autowired
    private BudgetOverlapValidator budgetOverlapValidator;

    public void validateBudget(Budget budget) {
        validateBasicFields(budget);
        validateDateRange(budget);
        validateLimit(budget);
        validateCategory(budget);
        validateNoOverlap(budget);
    }

    private void validateBasicFields(Budget budget) {
        if (budget.getName() == null || budget.getName().isBlank()) {
            throw new IllegalArgumentException("Budget name cannot be empty");
        }
        if (budget.getUser() == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
    }

    private void validateDateRange(Budget budget) {
        dateRangeValidator.validateDateRange(budget.getStartDate(), budget.getEndDate());
    }

    private void validateLimit(Budget budget) {
        if (budget.getLimit() == null) {
            throw new IllegalArgumentException("Budget limit cannot be null");
        }
        if (budget.getLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                String.format("Budget limit must be greater than zero, got: %s", budget.getLimit())
            );
        }
    }

    private void validateCategory(Budget budget) {
        if (budget.getCategory() == null || budget.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
    }

    private void validateNoOverlap(Budget budget) {
        budgetOverlapValidator.validateNoOverlap(budget);
    }
}