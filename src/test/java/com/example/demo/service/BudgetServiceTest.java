package com.example.demo.service;

import com.example.demo.model.Budget;
import com.example.demo.repository.BudgetRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetService budgetService;

    private Budget testBudget;

    @BeforeEach
    void setUp() {
        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setName("Monthly Budget");
        testBudget.setLimit(new BigDecimal("1000.00"));
        testBudget.setCategory("Food");
        testBudget.setStartDate(LocalDate.of(2025, 1, 1));
        testBudget.setEndDate(LocalDate.of(2025, 1, 31));
    }

    @Test
    void createBudget_ShouldSaveBudget() {
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        Budget result = budgetService.createBudget(testBudget);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Monthly Budget");
        verify(budgetRepository, times(1)).save(testBudget);
    }

    @Test
    void getBudgetById_ShouldReturnBudget_WhenExists() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        Optional<Budget> result = budgetService.getBudgetById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(budgetRepository, times(1)).findById(1L);
    }

    @Test
    void getBudgetById_ShouldReturnEmpty_WhenNotExists() {
        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Budget> result = budgetService.getBudgetById(999L);

        assertThat(result).isEmpty();
        verify(budgetRepository, times(1)).findById(999L);
    }

    @Test
    void getAllBudgetsByUserId_ShouldReturnUserBudgets() {
        Budget budget2 = new Budget();
        budget2.setId(2L);
        budget2.setName("Savings");

        List<Budget> budgets = Arrays.asList(testBudget, budget2);
        when(budgetRepository.findByUserId(1L)).thenReturn(budgets);

        List<Budget> result = budgetService.getAllBudgetsByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).contains(testBudget, budget2);
        verify(budgetRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getAllBudgets_ShouldReturnAllBudgets() {
        List<Budget> budgets = Arrays.asList(testBudget);
        when(budgetRepository.findAll()).thenReturn(budgets);

        List<Budget> result = budgetService.getAllBudgets();

        assertThat(result).hasSize(1);
        assertThat(result).contains(testBudget);
        verify(budgetRepository, times(1)).findAll();
    }

    @Test
    void updateBudget_ShouldUpdateExistingBudget() {
        Budget updatedBudget = new Budget();
        updatedBudget.setName("Updated Budget");
        updatedBudget.setLimit(new BigDecimal("2000.00"));
        updatedBudget.setCategory("Entertainment");
        updatedBudget.setStartDate(LocalDate.of(2025, 2, 1));
        updatedBudget.setEndDate(LocalDate.of(2025, 2, 28));

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        Budget result = budgetService.updateBudget(1L, updatedBudget);

        assertThat(result.getName()).isEqualTo("Updated Budget");
        assertThat(result.getLimit()).isEqualTo(new BigDecimal("2000.00"));
        assertThat(result.getCategory()).isEqualTo("Entertainment");
        verify(budgetRepository, times(1)).findById(1L);
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void updateBudget_ShouldThrowException_WhenNotFound() {
        Budget updatedBudget = new Budget();
        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.updateBudget(999L, updatedBudget))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Budget not found with id: 999");

        verify(budgetRepository, times(1)).findById(999L);
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void deleteBudget_ShouldDeleteExistingBudget() {
        when(budgetRepository.existsById(1L)).thenReturn(true);
        doNothing().when(budgetRepository).deleteById(1L);

        budgetService.deleteBudget(1L);

        verify(budgetRepository, times(1)).existsById(1L);
        verify(budgetRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBudget_ShouldThrowException_WhenNotFound() {
        when(budgetRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> budgetService.deleteBudget(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Budget not found with id: 999");

        verify(budgetRepository, times(1)).existsById(999L);
        verify(budgetRepository, never()).deleteById(999L);
    }
}