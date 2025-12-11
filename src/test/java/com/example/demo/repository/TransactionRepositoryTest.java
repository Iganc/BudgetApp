package com.example.demo.repository;

import com.example.demo.model.Budget;
import com.example.demo.model.Transaction;
import com.example.demo.model.TransactionType;
import com.example.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testUser;
    private Budget testBudget;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        entityManager.persist(testUser);

        testBudget = new Budget();
        testBudget.setUser(testUser);
        testBudget.setName("Groceries");
        testBudget.setLimit(new BigDecimal("1500.00"));
        testBudget.setStartDate(LocalDate.of(2025, 1, 1));
        testBudget.setEndDate(LocalDate.of(2025, 1, 31));
        entityManager.persist(testBudget);

        entityManager.flush();
    }

    @Test
    void shouldSaveTransaction() {
        Transaction transaction = createTransaction(new BigDecimal("250.50"), "Biedronka", TransactionType.EXPENSE);

        Transaction saved = transactionRepository.save(transaction);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("250.50"));
        assertThat(saved.getType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    void shouldFindTransactionsByUserId() {
        Transaction t1 = createTransaction(new BigDecimal("250.50"), "Biedronka", TransactionType.EXPENSE);
        Transaction t2 = createTransaction(new BigDecimal("180.00"), "Lidl", TransactionType.EXPENSE);
        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.flush();

        List<Transaction> transactions = transactionRepository.findByUserId(testUser.getId());

        assertThat(transactions).hasSize(2);
        assertThat(transactions).extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Biedronka", "Lidl");
    }

    @Test
    void shouldFindTransactionsByBudgetId() {
        Transaction t1 = createTransaction(new BigDecimal("250.50"), "Biedronka", TransactionType.EXPENSE);
        Transaction t2 = createTransaction(new BigDecimal("180.00"), "Lidl", TransactionType.EXPENSE);
        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.flush();

        List<Transaction> transactions = transactionRepository.findByBudgetId(testBudget.getId());

        assertThat(transactions).hasSize(2);
    }

    @Test
    void shouldFindTransactionsByUserIdAndDateBetween() {
        LocalDateTime now = LocalDateTime.now();
        Transaction t1 = createTransaction(new BigDecimal("100.00"), "Old", TransactionType.EXPENSE);
        t1.setDate(now.minusDays(10));
        Transaction t2 = createTransaction(new BigDecimal("200.00"), "Recent", TransactionType.EXPENSE);
        t2.setDate(now.minusDays(2));
        Transaction t3 = createTransaction(new BigDecimal("300.00"), "Today", TransactionType.EXPENSE);
        t3.setDate(now);
        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.persist(t3);
        entityManager.flush();

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(
                testUser.getId(),
                now.minusDays(5),
                now.plusDays(1)
        );

        assertThat(transactions).hasSize(2);
        assertThat(transactions).extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Recent", "Today");
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsFound() {
        List<Transaction> transactions = transactionRepository.findByUserId(999L);

        assertThat(transactions).isEmpty();
    }

    private Transaction createTransaction(BigDecimal amount, String description, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setBudget(testBudget);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setType(type);
        transaction.setDate(LocalDateTime.now());
        return transaction;
    }
}