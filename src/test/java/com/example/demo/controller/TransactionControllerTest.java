package com.example.demo.controller;

import com.example.demo.model.Budget;
import com.example.demo.model.Transaction;
import com.example.demo.model.TransactionType;
import com.example.demo.model.User;
import com.example.demo.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void createTransaction_ShouldReturnCreatedTransaction() throws Exception {
        User user = new User();
        user.setId(1L);

        Budget budget = new Budget();
        budget.setId(1L);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUser(user);
        transaction.setBudget(budget);
        transaction.setAmount(BigDecimal.valueOf(100.0));
        transaction.setDescription("Grocery shopping");
        transaction.setType(TransactionType.EXPENSE);
        transaction.setDate(LocalDateTime.now());

        when(transactionService.createTransaction(any(Transaction.class))).thenReturn(transaction);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100.0,\"description\":\"Grocery shopping\",\"type\":\"EXPENSE\"}"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.description").value("Grocery shopping"));
    }

    @Test
    void getTransactionById_ShouldReturnTransaction_WhenTransactionExists() throws Exception {
        User user = new User();
        user.setId(1L);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setUser(user);
        transaction.setAmount(BigDecimal.valueOf(100.0));
        transaction.setDescription("Grocery shopping");
        transaction.setType(TransactionType.EXPENSE);

        when(transactionService.getTransactionById(1L)).thenReturn(Optional.of(transaction));

        mockMvc.perform(get("/api/transactions/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Grocery shopping"));
    }

    @Test
    void getTransactionById_ShouldReturnNotFound_WhenTransactionDoesNotExist() throws Exception {
        when(transactionService.getTransactionById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllTransactionsByUserId_ShouldReturnListOfTransactions() throws Exception {
        User user = new User();
        user.setId(1L);

        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setUser(user);
        transaction1.setDescription("Transaction 1");

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setUser(user);
        transaction2.setDescription("Transaction 2");

        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        when(transactionService.getAllTransactionsByUserId(1L)).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions/user/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Transaction 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Transaction 2"));
    }

    @Test
    void getAllTransactionsByBudgetId_ShouldReturnListOfTransactions() throws Exception {
        Budget budget = new Budget();
        budget.setId(1L);

        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setBudget(budget);
        transaction1.setDescription("Budget Transaction 1");

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setBudget(budget);
        transaction2.setDescription("Budget Transaction 2");

        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        when(transactionService.getAllTransactionsByBudgetId(1L)).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions/budget/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Budget Transaction 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Budget Transaction 2"));
    }

    @Test
    void getAllTransactions_ShouldReturnListOfAllTransactions() throws Exception {
        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setDescription("Transaction 1");

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setDescription("Transaction 2");

        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        when(transactionService.getAllTransactions()).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void updateTransaction_ShouldReturnUpdatedTransaction_WhenTransactionExists() throws Exception {
        User user = new User();
        user.setId(1L);

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(1L);
        updatedTransaction.setUser(user);
        updatedTransaction.setAmount(BigDecimal.valueOf(150.0));
        updatedTransaction.setDescription("Updated description");
        updatedTransaction.setType(TransactionType.EXPENSE);

        when(transactionService.updateTransaction(eq(1L), any(Transaction.class))).thenReturn(updatedTransaction);

        mockMvc.perform(put("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":150.0,\"description\":\"Updated description\",\"type\":\"EXPENSE\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(150.0))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateTransaction_ShouldReturnNotFound_WhenTransactionDoesNotExist() throws Exception {
        when(transactionService.updateTransaction(eq(999L), any(Transaction.class)))
                .thenThrow(new RuntimeException("Transaction not found"));

        mockMvc.perform(put("/api/transactions/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":150.0,\"description\":\"Updated description\"}"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTransaction_ShouldReturnNoContent_WhenTransactionExists() throws Exception {
        doNothing().when(transactionService).deleteTransaction(1L);

        mockMvc.perform(delete("/api/transactions/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTransaction_ShouldReturnNotFound_WhenTransactionDoesNotExist() throws Exception {
        doThrow(new RuntimeException("Transaction not found")).when(transactionService).deleteTransaction(999L);

        mockMvc.perform(delete("/api/transactions/999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}