package com.example.demo.service;

import com.example.demo.dto.BudgetSummaryDTO;
import com.example.demo.dto.SpendingByCategoryDTO;
import com.example.demo.model.Budget;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.repository.BudgetRepository;
import com.example.demo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetAnalyticsService budgetAnalyticsService;

    @InjectMocks
    private ReportService reportService;

    private Budget testBudget;
    private Transaction testTransaction1;
    private Transaction testTransaction2;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);

        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setName("Monthly Budget");
        testBudget.setLimit(new BigDecimal("1000.00"));
        testBudget.setUser(testUser);

        testTransaction1 = new Transaction();
        testTransaction1.setId(1L);
        testTransaction1.setAmount(new BigDecimal("300.00"));
        testTransaction1.setCategory("Food");
        testTransaction1.setBudget(testBudget);

        testTransaction2 = new Transaction();
        testTransaction2.setId(2L);
        testTransaction2.setAmount(new BigDecimal("200.00"));
        testTransaction2.setCategory("Transport");
        testTransaction2.setBudget(testBudget);
    }

    @Test
    void getBudgetSummary_ShouldReturnSummary_WhenBudgetExists() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));
        when(budgetAnalyticsService.calculateTotalSpent(1L)).thenReturn(new BigDecimal("500.00")); // Dodaj tę linię

        BudgetSummaryDTO result = reportService.getBudgetSummary(1L);

        assertThat(result.getBudgetId()).isEqualTo(1L);
        assertThat(result.getBudgetName()).isEqualTo("Monthly Budget");
        assertThat(result.getTotalLimit()).isEqualByComparingTo("1000.00");
        assertThat(result.getTotalSpent()).isEqualByComparingTo("500.00");
        assertThat(result.getRemainingAmount()).isEqualByComparingTo("500.00");
        verify(budgetRepository, times(1)).findById(1L);
        verify(budgetAnalyticsService, times(1)).calculateTotalSpent(1L); // Dodaj weryfikację
    }

    @Test
    void getBudgetSummary_ShouldThrowException_WhenBudgetNotFound() {
        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getBudgetSummary(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Budget not found");

        verify(budgetRepository, times(1)).findById(999L);
        verify(transactionRepository, never()).findByBudgetId(anyLong());
    }

    @Test
    void getSpendingByCategory_ShouldReturnCategoryReport() {
        SpendingByCategoryDTO dto1 = new SpendingByCategoryDTO();
        dto1.setCategory("Food");
        dto1.setTotalAmount(new BigDecimal("300.00"));

        SpendingByCategoryDTO dto2 = new SpendingByCategoryDTO();
        dto2.setCategory("Transport");
        dto2.setTotalAmount(new BigDecimal("200.00"));

        when(transactionRepository.findSpendingByCategory(1L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)))
                .thenReturn(Arrays.asList(dto1, dto2));

        List<SpendingByCategoryDTO> result = reportService.getSpendingByCategory(
                1L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategory()).isEqualTo("Food");
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("300.00");
        verify(transactionRepository, times(1)).findSpendingByCategory(1L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
    }
}