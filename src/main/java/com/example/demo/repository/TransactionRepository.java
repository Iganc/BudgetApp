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

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.budget.id = :budgetId AND t.type = :type")
    BigDecimal sumAmountByBudgetIdAndType(@Param("budgetId") Long budgetId, @Param("type") TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.budget.category = :category AND t.date BETWEEN :start AND :end")
    List<Transaction> findByUserIdAndCategoryAndDateBetween(
        @Param("userId") Long userId,
        @Param("category") String category,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT new com.example.demo.dto.SpendingByCategoryDTO(t.category, SUM(t.amount)) " +
           "FROM Transaction t WHERE t.budget.id = :budgetId " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category")
    List<SpendingByCategoryDTO> findSpendingByCategory(
        @Param("budgetId") Long budgetId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}