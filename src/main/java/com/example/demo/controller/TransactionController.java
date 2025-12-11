package com.example.demo.controller;

import com.example.demo.model.Budget;
import com.example.demo.model.Transaction;
import com.example.demo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.security.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @RequestBody Transaction transaction,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        transaction.setUser(user);

        if (transaction.getBudget() != null && transaction.getBudget().getId() != null) {
            transaction.setBudget(new Budget(transaction.getBudget().getId()));
        }

        try {
            Transaction created = transactionService.createTransaction(transaction, userId);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);

        return transactionService.getTransactionById(id)
                .filter(t -> t.getUser().getId().equals(userId))
                .map(transaction -> new ResponseEntity<>(transaction, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/budget/{budgetId}")
    public ResponseEntity<List<Transaction>> getAllTransactionsByBudgetId(
            @PathVariable Long budgetId,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        try {
            List<Transaction> transactions = transactionService.getTransactionsByBudgetId(budgetId, userId);
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions(
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable Long id,
            @RequestBody Transaction transaction,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);

        try {
            Transaction updated = transactionService.updateTransaction(id, transaction, userId);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = getUserIdFromToken(authHeader);

        try {
            transactionService.deleteTransaction(id, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}