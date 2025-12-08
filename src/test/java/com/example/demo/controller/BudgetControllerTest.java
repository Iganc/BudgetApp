package com.example.demo.controller;

import com.example.demo.model.Budget;
import com.example.demo.model.User;
import com.example.demo.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import com.example.demo.config.TestSecurityConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;

    @Test
    void createBudget_ShouldReturnCreatedBudget() throws Exception {
        User user = new User();
        user.setId(1L);

        Budget budget = new Budget();
        budget.setId(1L);
        budget.setUser(user);
        budget.setName("Monthly Budget");
        budget.setLimit(BigDecimal.valueOf(5000.0));
        budget.setCategory("Food");
        budget.setStartDate(LocalDate.of(2024, 1, 1));
        budget.setEndDate(LocalDate.of(2024, 1, 31));

        when(budgetService.createBudget(any(Budget.class))).thenReturn(budget);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Monthly Budget\",\"limit\":5000.0,\"category\":\"Food\",\"startDate\":\"2024-01-01\",\"endDate\":\"2024-01-31\"}"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monthly Budget"))
                .andExpect(jsonPath("$.limit").value(5000.0));
    }

    @Test
    void getBudgetById_ShouldReturnBudget_WhenBudgetExists() throws Exception {
        User user = new User();
        user.setId(1L);

        Budget budget = new Budget();
        budget.setId(1L);
        budget.setUser(user);
        budget.setName("Monthly Budget");
        budget.setLimit(BigDecimal.valueOf(5000.0));
        budget.setCategory("Food");

        when(budgetService.getBudgetById(1L)).thenReturn(Optional.of(budget));

        mockMvc.perform(get("/api/budgets/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monthly Budget"));
    }

    @Test
    void getBudgetById_ShouldReturnNotFound_WhenBudgetDoesNotExist() throws Exception {
        when(budgetService.getBudgetById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/budgets/999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBudgetsByUserId_ShouldReturnListOfBudgets() throws Exception {
        User user = new User();
        user.setId(1L);

        Budget budget1 = new Budget();
        budget1.setId(1L);
        budget1.setUser(user);
        budget1.setName("Monthly Budget");

        Budget budget2 = new Budget();
        budget2.setId(2L);
        budget2.setUser(user);
        budget2.setName("Yearly Budget");

        List<Budget> budgets = Arrays.asList(budget1, budget2);

        when(budgetService.getAllBudgetsByUserId(1L)).thenReturn(budgets);

        mockMvc.perform(get("/api/budgets/user/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Monthly Budget"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Yearly Budget"));
    }

    @Test
    void getAllBudgets_ShouldReturnListOfAllBudgets() throws Exception {
        Budget budget1 = new Budget();
        budget1.setId(1L);
        budget1.setName("Budget 1");

        Budget budget2 = new Budget();
        budget2.setId(2L);
        budget2.setName("Budget 2");

        List<Budget> budgets = Arrays.asList(budget1, budget2);

        when(budgetService.getAllBudgets()).thenReturn(budgets);

        mockMvc.perform(get("/api/budgets"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void updateBudget_ShouldReturnUpdatedBudget_WhenBudgetExists() throws Exception {
        User user = new User();
        user.setId(1L);

        Budget updatedBudget = new Budget();
        updatedBudget.setId(1L);
        updatedBudget.setUser(user);
        updatedBudget.setName("Updated Budget");
        updatedBudget.setLimit(BigDecimal.valueOf(6000.0));

        when(budgetService.updateBudget(eq(1L), any(Budget.class))).thenReturn(updatedBudget);

        mockMvc.perform(put("/api/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Budget\",\"limit\":6000.0}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Budget"))
                .andExpect(jsonPath("$.limit").value(6000.0));
    }

    @Test
    void updateBudget_ShouldReturnNotFound_WhenBudgetDoesNotExist() throws Exception {
        when(budgetService.updateBudget(eq(999L), any(Budget.class)))
                .thenThrow(new RuntimeException("Budget not found"));

        mockMvc.perform(put("/api/budgets/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Budget\",\"limit\":6000.0}"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBudget_ShouldReturnNoContent_WhenBudgetExists() throws Exception {
        doNothing().when(budgetService).deleteBudget(1L);

        mockMvc.perform(delete("/api/budgets/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBudget_ShouldReturnNotFound_WhenBudgetDoesNotExist() throws Exception {
        doThrow(new RuntimeException("Budget not found")).when(budgetService).deleteBudget(999L);

        mockMvc.perform(delete("/api/budgets/999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}