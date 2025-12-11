package com.example.demo.service;

import com.example.demo.dto.BudgetSummaryDTO;
import com.example.demo.dto.SpendingByCategoryDTO;
import com.example.demo.model.Budget;
import com.example.demo.repository.BudgetRepository;
import com.example.demo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BudgetAnalyticsService budgetAnalyticsService;

    public BudgetSummaryDTO getBudgetSummary(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));

        BigDecimal totalSpent = budgetAnalyticsService.calculateTotalSpent(budgetId);

        BudgetSummaryDTO summary = new BudgetSummaryDTO();
        summary.setBudgetId(budget.getId());
        summary.setBudgetName(budget.getName());
        summary.setTotalSpent(totalSpent);

        return summary;
    }

    public List<SpendingByCategoryDTO> getSpendingByCategory(Long budgetId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findSpendingByCategory(budgetId, startDate, endDate);
    }
}