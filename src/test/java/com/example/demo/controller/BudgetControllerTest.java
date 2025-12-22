// java
package com.example.demo.controller;

import com.example.demo.config.TestSecurityConfig;
import com.example.demo.model.Budget;
import com.example.demo.model.User;
import com.example.demo.service.BudgetService;
import com.example.demo.service.UserService;
import com.example.demo.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private BudgetService budgetService;

    @SuppressWarnings("removal")
    @MockBean
    private UserService userService;

    @SuppressWarnings("removal")
    @MockBean
    private JwtUtil jwtUtil;

    private static final String AUTH_HEADER = "Bearer token";

    @Test
    void createBudget_ShouldReturnCreatedBudget() throws Exception {
        User user = new User();
        user.setId(1L);

        Budget budget = new Budget();
        budget.setId(1L);
        budget.setUser(user);
        budget.setName("Monthly Budget");
        budget.setStartDate(LocalDate.of(2024, 1, 1));
        budget.setEndDate(LocalDate.of(2024, 1, 31));

        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(budgetService.createBudget(any(Budget.class))).thenReturn(budget);

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Monthly Budget\",\"startDate\":\"2024-01-01\",\"endDate\":\"2024-01-31\"}"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monthly Budget"));
    }

    @Test
    void getBudgetById_ShouldReturnBudget_WhenBudgetExists() throws Exception {
        User user = new User();
        user.setId(1L);

        Budget budget = new Budget();
        budget.setId(1L);
        budget.setUser(user);
        budget.setName("Monthly Budget");

        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(budgetService.getBudgetById(1L)).thenReturn(Optional.of(budget));

        mockMvc.perform(get("/api/budgets/1")
                        .header("Authorization", AUTH_HEADER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monthly Budget"));
    }

    @Test
    void getBudgetById_ShouldReturnNotFound_WhenBudgetDoesNotExist() throws Exception {
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(budgetService.getBudgetById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/budgets/999")
                        .header("Authorization", AUTH_HEADER))
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

        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(budgetService.getBudgetsByUserId(1L)).thenReturn(budgets);

        mockMvc.perform(get("/api/budgets")
                        .header("Authorization", AUTH_HEADER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Monthly Budget"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Yearly Budget"));
    }

    @Test
    void getAllBudgets_ShouldReturnListOfAllBudgets() throws Exception {
        // Align with controller: it returns user-specific budgets at GET /api/budgets
        Budget budget1 = new Budget();
        budget1.setId(1L);
        budget1.setName("Budget 1");

        Budget budget2 = new Budget();
        budget2.setId(2L);
        budget2.setName("Budget 2");

        List<Budget> budgets = Arrays.asList(budget1, budget2);

        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(budgetService.getBudgetsByUserId(1L)).thenReturn(budgets);

        mockMvc.perform(get("/api/budgets")
                        .header("Authorization", AUTH_HEADER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void updateBudget_ShouldReturnUpdatedBudget_WhenBudgetExists() throws Exception {
        User user = new User();
        user.setId(1L);

        Budget existing = new Budget();
        existing.setId(1L);
        existing.setUser(user);
        existing.setName("Old Name");

        Budget updatedBudget = new Budget();
        updatedBudget.setId(1L);
        updatedBudget.setUser(user);
        updatedBudget.setName("Updated Budget");

        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(budgetService.getBudgetById(1L)).thenReturn(Optional.of(existing));
        when(budgetService.updateBudget(eq(1L), any(Budget.class))).thenReturn(updatedBudget);

        mockMvc.perform(put("/api/budgets/1")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Budget\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Budget"));
    }

    @Test
    void updateBudget_ShouldReturnNotFound_WhenBudgetDoesNotExist() throws Exception {
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(budgetService.getBudgetById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/budgets/999")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Budget\"}"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBudget_ShouldReturnNoContent_WhenBudgetExists() throws Exception {
        User user = new User();
        user.setId(1L);

        Budget budget = new Budget();
        budget.setId(1L);
        budget.setUser(user);

        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(budgetService.getBudgetById(1L)).thenReturn(Optional.of(budget));
        doNothing().when(budgetService).deleteBudget(1L);

        mockMvc.perform(delete("/api/budgets/1")
                        .header("Authorization", AUTH_HEADER))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBudget_ShouldReturnNotFound_WhenBudgetDoesNotExist() throws Exception {
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(budgetService.getBudgetById(999L)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Budget not found")).when(budgetService).deleteBudget(999L);

        mockMvc.perform(delete("/api/budgets/999")
                        .header("Authorization", AUTH_HEADER))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}