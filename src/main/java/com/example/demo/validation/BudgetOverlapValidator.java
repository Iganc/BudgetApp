package com.example.demo.validation;

import com.example.demo.exception.BudgetOverlapException;
import com.example.demo.model.Budget;
import com.example.demo.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BudgetOverlapValidator {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private DateRangeValidator dateRangeValidator;

    public void validateNoOverlap(Budget newBudget) {
        List<Budget> existingBudgets = budgetRepository.findByUserId(newBudget.getUser().getId());

        for (Budget existing : existingBudgets) {
            if (newBudget.getId() != null && existing.getId().equals(newBudget.getId())) {
                continue;
            }

            if (dateRangeValidator.isOverlapping(
                newBudget.getStartDate(), newBudget.getEndDate(),
                existing.getStartDate(), existing.getEndDate()
            )) {
                throw new BudgetOverlapException(
                    String.format(
                        "Budget overlaps with existing budget '%s' (ID: %d) for category '%s' between %s and %s",
                        existing.getName(),
                        existing.getId(),
                        existing.getStartDate(),
                        existing.getEndDate()
                    )
                );
            }
        }
    }
}