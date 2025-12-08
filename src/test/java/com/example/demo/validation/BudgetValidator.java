package com.example.demo.validation;

import com.example.demo.model.Budget;
import com.example.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetValidatorTest {

    @Mock
    private DateRangeValidator dateRangeValidator;

    @Mock
    private BudgetOverlapValidator budgetOverlapValidator;

    @InjectMocks
    private BudgetValidator budgetValidator;

    private Budget testBudget;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);

        testBudget = new Budget();
        testBudget.setUser(testUser);
        testBudget.setName("Test Budget");
        testBudget.setLimit(new BigDecimal("1000.00"));
        testBudget.setCategory("Food");
        testBudget.setStartDate(LocalDate.of(2025, 1, 1));
        testBudget.setEndDate(LocalDate.of(2025, 1, 31));
    }

    @Test
    void validateBudget_ShouldPass_WhenAllFieldsValid() {
        doNothing().when(dateRangeValidator).validateDateRange(any(), any());
        doNothing().when(budgetOverlapValidator).validateNoOverlap(any());

        budgetValidator.validateBudget(testBudget);

        verify(dateRangeValidator, times(1)).validateDateRange(testBudget.getStartDate(), testBudget.getEndDate());
        verify(budgetOverlapValidator, times(1)).validateNoOverlap(testBudget);
    }

    @Test
    void validateBudget_ShouldThrowException_WhenNameIsNull() {
        testBudget.setName(null);

        assertThatThrownBy(() -> budgetValidator.validateBudget(testBudget))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Budget name cannot be empty");
    }

    @Test
    void validateBudget_ShouldThrowException_WhenNameIsBlank() {
        testBudget.setName("   ");

        assertThatThrownBy(() -> budgetValidator.validateBudget(testBudget))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Budget name cannot be empty");
    }

    @Test
    void validateBudget_ShouldThrowException_WhenUserIsNull() {
        testBudget.setUser(null);

        assertThatThrownBy(() -> budgetValidator.validateBudget(testBudget))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User cannot be null");
    }

    @Test
    void validateBudget_ShouldThrowException_WhenLimitIsNull() {
        testBudget.setLimit(null);

        assertThatThrownBy(() -> budgetValidator.validateBudget(testBudget))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Budget limit cannot be null");
    }

    @Test
    void validateBudget_ShouldThrowException_WhenLimitIsZero() {
        testBudget.setLimit(BigDecimal.ZERO);

        assertThatThrownBy(() -> budgetValidator.validateBudget(testBudget))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Budget limit must be greater than zero");
    }

    @Test
    void validateBudget_ShouldThrowException_WhenLimitIsNegative() {
        testBudget.setLimit(new BigDecimal("-100.00"));

        assertThatThrownBy(() -> budgetValidator.validateBudget(testBudget))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Budget limit must be greater than zero");
    }

    @Test
    void validateBudget_ShouldThrowException_WhenCategoryIsNull() {
        testBudget.setCategory(null);

        assertThatThrownBy(() -> budgetValidator.validateBudget(testBudget))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category cannot be empty");
    }

    @Test
    void validateBudget_ShouldThrowException_WhenCategoryIsBlank() {
        testBudget.setCategory("   ");

        assertThatThrownBy(() -> budgetValidator.validateBudget(testBudget))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category cannot be empty");
    }
}