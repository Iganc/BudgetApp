package com.example.demo.service;

import com.example.demo.model.Budget;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetAnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetAnalyticsService budgetAnalyticsService;

    private Budget testBudget;
    private Transaction testTransaction1;
    private Transaction testTransaction2;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);

        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setLimit(new BigDecimal("1000.00"));
        testBudget.setUser(testUser);

        testTransaction1 = new Transaction();
        testTransaction1.setAmount(new BigDecimal("300.00"));

        testTransaction2 = new Transaction();
        testTransaction2.setAmount(new BigDecimal("200.00"));
    }

    @Test
    void calculateTotalSpent_ShouldReturnSum() {
        List<Transaction> transactions = Arrays.asList(testTransaction1, testTransaction2);
        when(transactionRepository.findByBudgetId(1L)).thenReturn(transactions);

        BigDecimal result = budgetAnalyticsService.calculateTotalSpent(1L);

        assertThat(result).isEqualByComparingTo("500.00");
        verify(transactionRepository, times(1)).findByBudgetId(1L);
    }

    @Test
    void calculateTotalSpent_ShouldReturnZero_WhenNoTransactions() {
        when(transactionRepository.findByBudgetId(1L)).thenReturn(Arrays.asList());

        BigDecimal result = budgetAnalyticsService.calculateTotalSpent(1L);

        assertThat(result).isEqualByComparingTo("0.00");
        verify(transactionRepository, times(1)).findByBudgetId(1L);
    }

    @Test
    void calculateRemainingAmount_ShouldReturnDifference() {
        List<Transaction> transactions = Arrays.asList(testTransaction1, testTransaction2);
        when(transactionRepository.findByBudgetId(1L)).thenReturn(transactions);

        BigDecimal result = budgetAnalyticsService.calculateRemainingAmount(testBudget);

        assertThat(result).isEqualByComparingTo("500.00");
        verify(transactionRepository, times(1)).findByBudgetId(1L);
    }
}