package com.example.demo.service;

import com.example.demo.model.Transaction;
import com.example.demo.model.TransactionType;
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

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setAmount(new BigDecimal("50.00"));
        testTransaction.setDescription("Groceries");
        testTransaction.setType(TransactionType.EXPENSE);
        testTransaction.setDate(LocalDateTime.now());
    }

    @Test
    void createTransaction_ShouldSaveTransaction() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.createTransaction(testTransaction);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Groceries");
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
        transaction2.setDescription("Salary");

        List<Transaction> transactions = Arrays.asList(testTransaction, transaction2);
        when(transactionRepository.findByUserId(1L)).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactionsByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).contains(testTransaction, transaction2);
        verify(transactionRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getAllTransactionsByBudgetId_ShouldReturnBudgetTransactions() {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findByBudgetId(1L)).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactionsByBudgetId(1L);

        assertThat(result).hasSize(1);
        assertThat(result).contains(testTransaction);
        verify(transactionRepository, times(1)).findByBudgetId(1L);
    }

    @Test
    void getAllTransactions_ShouldReturnAllTransactions() {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertThat(result).hasSize(1);
        assertThat(result).contains(testTransaction);
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void updateTransaction_ShouldUpdateExistingTransaction() {
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setAmount(new BigDecimal("100.00"));
        updatedTransaction.setDescription("Updated Groceries");
        updatedTransaction.setType(TransactionType.EXPENSE);
        updatedTransaction.setDate(LocalDateTime.now());

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction result = transactionService.updateTransaction(1L, updatedTransaction);

        assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.getDescription()).isEqualTo("Updated Groceries");
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void updateTransaction_ShouldThrowException_WhenNotFound() {
        Transaction updatedTransaction = new Transaction();
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.updateTransaction(999L, updatedTransaction))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Transaction not found with id: 999");

        verify(transactionRepository, times(1)).findById(999L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void deleteTransaction_ShouldDeleteExistingTransaction() {
        when(transactionRepository.existsById(1L)).thenReturn(true);
        doNothing().when(transactionRepository).deleteById(1L);

        transactionService.deleteTransaction(1L);

        verify(transactionRepository, times(1)).existsById(1L);
        verify(transactionRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTransaction_ShouldThrowException_WhenNotFound() {
        when(transactionRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.deleteTransaction(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Transaction not found with id: 999");

        verify(transactionRepository, times(1)).existsById(999L);
        verify(transactionRepository, never()).deleteById(999L);
    }
}