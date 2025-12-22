package com.example.demo.service;

import com.example.demo.model.Budget;
import com.example.demo.model.Category;
import com.example.demo.model.Transaction;
import com.example.demo.model.TransactionType;
import com.example.demo.model.User;
import com.example.demo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetService budgetService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Food");

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setUser(testUser);
        testTransaction.setCategory(testCategory);
        testTransaction.setAmount(new BigDecimal("50.00"));
        testTransaction.setDescription("Groceries");
        testTransaction.setType(TransactionType.EXPENSE);
        testTransaction.setDate(LocalDateTime.now());
    }

    @Test
    void createTransaction_ShouldSaveTransaction() {
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.createTransaction(testTransaction, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Groceries");
        verify(categoryService, times(1)).getCategoryById(1L);
        verify(transactionRepository, times(1)).save(testTransaction);
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenExists() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        Optional<Transaction> result = transactionService.getTransactionById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void getTransactionById_ShouldReturnEmpty_WhenNotExists() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Transaction> result = transactionService.getTransactionById(999L);

        assertThat(result).isEmpty();
        verify(transactionRepository, times(1)).findById(999L);
    }

    @Test
    void getAllTransactionsByUserId_ShouldReturnUserTransactions() {
        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setUser(testUser);
        transaction2.setDescription("Salary");

        List<Transaction> transactions = Arrays.asList(testTransaction, transaction2);
        when(transactionRepository.findByUserId(1L)).thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).contains(testTransaction, transaction2);
        verify(transactionRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getAllTransactionsByBudgetId_ShouldReturnBudgetTransactions() {
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setUser(testUser);

        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(budgetService.getBudgetById(1L)).thenReturn(Optional.of(budget));
        when(transactionRepository.findByBudgetId(1L)).thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsByBudgetId(1L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result).contains(testTransaction);
        verify(budgetService, times(1)).getBudgetById(1L);
        verify(transactionRepository, times(1)).findByBudgetId(1L);
    }

    @Test
    void updateTransaction_ShouldUpdateExistingTransaction() {
        Transaction updatedTransaction = new Transaction();
        Category newCategory = new Category();
        newCategory.setId(2L);
        newCategory.setName("Transport");
        updatedTransaction.setCategory(newCategory);
        updatedTransaction.setAmount(new BigDecimal("100.00"));
        updatedTransaction.setDescription("Updated Groceries");
        updatedTransaction.setType(TransactionType.EXPENSE);
        updatedTransaction.setDate(LocalDateTime.now());

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(categoryService.getCategoryById(2L)).thenReturn(Optional.of(newCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transaction_service_call_update(transactionService, 1L, updatedTransaction, 1L);

        assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.getDescription()).isEqualTo("Updated Groceries");
        assertThat(result.getCategory().getId()).isEqualTo(2L);
        verify(transactionRepository, times(1)).findById(1L);
        verify(categoryService, times(1)).getCategoryById(2L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    // helper to avoid name clash in code block formatting
    private Transaction transaction_service_call_update(TransactionService svc, Long id, Transaction updated, Long userId) {
        return svc.updateTransaction(id, updated, userId);
    }

    @Test
    void updateTransaction_ShouldThrowException_WhenNotFound() {
        Transaction updatedTransaction = new Transaction();
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(999L, updatedTransaction, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Transaction not found with id: 999");

        verify(transactionRepository, times(1)).findById(999L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deleteTransaction_ShouldThrowException_WhenNotFound() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(999L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Transaction not found with id: 999");

        verify(transactionRepository, times(1)).findById(999L);
        verify(transactionRepository, never()).deleteById(999L);
    }
}