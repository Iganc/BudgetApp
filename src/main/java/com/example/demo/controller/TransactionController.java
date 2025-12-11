package com.example.demo.controller;

import com.example.demo.model.Budget;
import com.example.demo.model.Category; // DODANO: Import Category
import com.example.demo.model.Transaction;
import com.example.demo.service.TransactionService;
import com.example.demo.service.CategoryService; // DODANO: Import CategoryService
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

    // DODANO: Wstrzyknięcie CategoryService
    @Autowired
    private CategoryService categoryService;

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

        // 1. Ustawienie Budżetu (tworzymy obiekt z samym ID)
        if (transaction.getBudget() != null && transaction.getBudget().getId() != null) {
            transaction.setBudget(new Budget(transaction.getBudget().getId()));
        }

        // 2. NOWA LOGIKA: Ustawienie Kategorii (tworzymy obiekt z samym ID)
        if (transaction.getCategory() != null && transaction.getCategory().getId() != null) {
            // Wymagane, ponieważ serwis oczekuje, że to pole ma ustawione ID do lookup
            transaction.setCategory(new Category(transaction.getCategory().getId()));
        } else {
            // Wymagane, jeśli kategoria jest null lub nie ma ID
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


        try {
            Transaction created = transactionService.createTransaction(transaction, userId);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Zwracamy BAD_REQUEST dla błędów walidacji (np. kategoria nie należy do użytkownika)
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
            // Serwis teraz waliduje dostęp do budżetu
            List<Transaction> transactions = transactionService.getTransactionsByBudgetId(budgetId, userId);
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Błąd dostępu do budżetu (np. nie istnieje lub nie należy do użytkownika)
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

        // NOWA LOGIKA: Walidacja Kategorii przed wysłaniem do serwisu
        if (transaction.getCategory() == null || transaction.getCategory().getId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            // Ustawienie Kategorii z samym ID
            transaction.setCategory(new Category(transaction.getCategory().getId()));
        }

        // Walidacja Budżetu (jeśli jest aktualizowany)
        if (transaction.getBudget() != null && transaction.getBudget().getId() != null) {
            transaction.setBudget(new Budget(transaction.getBudget().getId()));
        } else {
            // Jeśli budżet jest null, musimy upewnić się, że model go obsłuży (lub unieważnić)
            transaction.setBudget(null);
        }

        try {
            // Serwis zweryfikuje właściciela transakcji, budżetu i kategorii
            Transaction updated = transactionService.updateTransaction(id, transaction, userId);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            // W przypadku błędów walidacji (np. Transaction not found)
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
            // W przypadku błędu dostępu lub braku transakcji
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}