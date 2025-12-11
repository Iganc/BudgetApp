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

    private void validateNoOverlap(Budget budget) {
        budgetOverlapValidator.validateNoOverlap(budget);
    }
}