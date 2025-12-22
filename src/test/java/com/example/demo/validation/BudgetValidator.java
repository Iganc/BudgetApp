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
        testBudget.setStartDate(LocalDate.of(2025, 1, 1));
        testBudget.setEndDate(LocalDate.of(2025, 1, 31));
    }

    @Test
    void validateBudget_ShouldPass_WhenAllFieldsValid() {
        doNothing().when(dateRangeValidator).validateDateRange(any(), any());

        budgetValidator.validateBudget(testBudget);

        verify(dateRangeValidator, times(1)).validateDateRange(testBudget.getStartDate(), testBudget.getEndDate());
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
}