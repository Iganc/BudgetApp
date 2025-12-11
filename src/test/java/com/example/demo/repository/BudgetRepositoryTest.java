package com.example.demo.repository;

import com.example.demo.model.Budget;
import com.example.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BudgetRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BudgetRepository budgetRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void shouldSaveBudget() {
        Budget budget = createBudget("Groceries", "Food", new BigDecimal("1500.00"));

        Budget saved = budgetRepository.save(budget);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Groceries");
        assertThat(saved.getLimit()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    void shouldFindBudgetsByUserId() {
        Budget budget1 = createBudget("Groceries", "Food", new BigDecimal("1500.00"));
        Budget budget2 = createBudget("Entertainment", "Fun", new BigDecimal("500.00"));
        entityManager.persist(budget1);
        entityManager.persist(budget2);
        entityManager.flush();

        List<Budget> budgets = budgetRepository.findByUserId(testUser.getId());

        assertThat(budgets).hasSize(2);
        assertThat(budgets).extracting(Budget::getName)
                .containsExactlyInAnyOrder("Groceries", "Entertainment");
    }


    @Test
    void shouldReturnEmptyListWhenNoBudgetsFound() {
        List<Budget> budgets = budgetRepository.findByUserId(999L);

        assertThat(budgets).isEmpty();
    }

    private Budget createBudget(String name) {
        Budget budget = new Budget();
        budget.setUser(testUser);
        budget.setName(name);
        budget.setStartDate(LocalDate.of(2025, 1, 1));
        budget.setEndDate(LocalDate.of(2025, 1, 31));
        return budget;
    }
}