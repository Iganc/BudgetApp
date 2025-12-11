package com.example.demo.repository;

import com.example.demo.model.Transaction;
import com.example.demo.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.example.demo.dto.SpendingByCategoryDTO;
import java.time.LocalDate;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByBudgetId(Long budgetId);
    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.budget.id = :budgetId AND t.type = 'EXPENSE'")
    BigDecimal sumExpenseAmountByBudgetId(@Param("budgetId") Long budgetId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.budget.id = :budgetId AND t.type = 'INCOME'")
    BigDecimal sumIncomeAmountByBudgetId(@Param("budgetId") Long budgetId);

    // W TransactionRepository.java
    @Query("SELECT new com.example.demo.dto.SpendingByCategoryDTO(t.category.name, SUM(t.amount)) " + // <-- Używamy t.category.name
            "FROM Transaction t WHERE t.budget.id = :budgetId AND t.date BETWEEN :startDate AND :endDate GROUP BY t.category.name") // <-- Grupujemy po nazwie
    List<SpendingByCategoryDTO> findSpendingByCategory(
            @Param("budgetId") Long budgetId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // I dla wykresu:
    @Query("SELECT new com.example.demo.dto.SpendingByCategoryDTO(t.category.name, SUM(t.amount)) " + // <-- Używamy t.category.name
            "FROM Transaction t WHERE t.budget.id = :budgetId AND t.type = 'EXPENSE' " +
            "GROUP BY t.category.name") // <-- Grupujemy po nazwie
    List<SpendingByCategoryDTO> findSpendingByCategoryForChart(
            @Param("budgetId") Long budgetId
    );
}