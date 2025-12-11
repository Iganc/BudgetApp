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
        BigDecimal totalSpent = transactionRepository.sumExpenseAmountByBudgetId(budgetId);

        return totalSpent != null ? totalSpent : BigDecimal.ZERO;
    }
    public BigDecimal calculateTotalIncome(Long budgetId) {
        BigDecimal totalIncome = transactionRepository.sumIncomeAmountByBudgetId(budgetId);
        return totalIncome != null ? totalIncome : BigDecimal.ZERO;
    }
    public BigDecimal calculateBalance(Long budgetId) {
        BigDecimal totalIncome = calculateTotalIncome(budgetId);
        BigDecimal totalSpent = calculateTotalSpent(budgetId);

        return totalIncome.subtract(totalSpent);
    }
}