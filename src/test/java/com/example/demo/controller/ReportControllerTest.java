package com.example.demo.controller;

import com.example.demo.dto.BudgetSummaryDTO;
import com.example.demo.dto.SpendingByCategoryDTO;
import com.example.demo.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private BudgetSummaryDTO testSummary;
    private SpendingByCategoryDTO testCategorySpending;

    @BeforeEach
    void setUp() {
        testSummary = new BudgetSummaryDTO();
        testSummary.setBudgetId(1L);
        testSummary.setBudgetName("Monthly Budget");
        testSummary.setTotalLimit(new BigDecimal("1000.00"));
        testSummary.setTotalSpent(new BigDecimal("500.00"));
        testSummary.setRemainingAmount(new BigDecimal("500.00"));

        testCategorySpending = new SpendingByCategoryDTO();
        testCategorySpending.setCategory("Food");
        testCategorySpending.setTotalAmount(new BigDecimal("300.00"));
    }

    @Test
    void getBudgetSummary_ShouldReturnSummary() {
        when(reportService.getBudgetSummary(1L)).thenReturn(testSummary);

        ResponseEntity<BudgetSummaryDTO> response = reportController.getBudgetSummary(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBudgetId()).isEqualTo(1L);
        verify(reportService, times(1)).getBudgetSummary(1L);
    }

    @Test
    void getSpendingByCategory_ShouldReturnCategoryReport() {
        List<SpendingByCategoryDTO> categoryReport = Arrays.asList(testCategorySpending);
        when(reportService.getSpendingByCategory(1L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)))
                .thenReturn(categoryReport);

        ResponseEntity<List<SpendingByCategoryDTO>> response = reportController.getSpendingByCategory(
                1L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getCategory()).isEqualTo("Food");
        verify(reportService, times(1)).getSpendingByCategory(1L, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
    }
}