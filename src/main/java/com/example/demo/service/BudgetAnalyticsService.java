package com.example.demo.service;

import com.example.demo.model.Budget;
import com.example.demo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetAnalyticsService {

    @Autowired
    private TransactionRepository transactionRepository;

    public BigDecimal calculateTotalSpent(Long budgetId) {
        return transactionRepository.findByBudgetId(budgetId)
                .stream()
                .map(transaction -> transaction.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateRemainingAmount(Budget budget) {
        BigDecimal totalSpent = calculateTotalSpent(budget.getId());
        return budget.getLimit().subtract(totalSpent);
    }
}