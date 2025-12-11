package com.example.demo.service;

import com.example.demo.dto.SpendingByCategoryDTO;
import com.example.demo.model.Budget;
import com.example.demo.model.Category;
import com.example.demo.model.Transaction;
import com.example.demo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService;
    private final CategoryService categoryService;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, BudgetService budgetService, CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
        this.categoryService = categoryService;
    }

    public Transaction createTransaction(Transaction transaction, Long userId) {

        if (transaction.getUser() == null || !transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Transaction must be assigned to the logged-in user.");
        }

        // 1. Walidacja i ustawienie Budżetu (istniejąca logika)
        if (transaction.getBudget() != null && transaction.getBudget().getId() != null) {
            Long budgetId = transaction.getBudget().getId();

            Optional<Budget> budget = budgetService.getBudgetById(budgetId);
            if (budget.isEmpty() || !budget.get().getUser().getId().equals(userId)) {
                throw new RuntimeException("Budget not found or access denied.");
            }
            transaction.setBudget(budget.get());
        }

        // 2. NOWA LOGIKA: Walidacja i ustawienie KATEGORII
        if (transaction.getCategory() == null || transaction.getCategory().getId() == null) {
            throw new RuntimeException("Category ID must be provided.");
        }

        Long categoryId = transaction.getCategory().getId();
        Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);

        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found.");
        }

        Category category = categoryOpt.get();

        // Zabezpieczenie (opcjonalnie): Jeśli kategoria nie jest domyślna, musi należeć do użytkownika
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied: Cannot use this custom category.");
        }

        transaction.setCategory(category); // Przypisanie pełnego obiektu Category

        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getTransactionsByBudgetId(Long budgetId, Long userId) {

        Optional<Budget> budget = budgetService.getBudgetById(budgetId);
        if (budget.isEmpty() || !budget.get().getUser().getId().equals(userId)) {
            throw new RuntimeException("Budget not found or access denied.");
        }

        return transactionRepository.findByBudgetId(budgetId);
    }

    public Transaction updateTransaction(Long id, Transaction updatedTransaction, Long userId) {
        return transactionRepository.findById(id)
                .map(transaction -> {
                    if (!transaction.getUser().getId().equals(userId)) {
                        throw new RuntimeException("Access denied: Transaction does not belong to user.");
                    }

                    // 1. NOWA LOGIKA: Walidacja i ustawienie KATEGORII
                    if (updatedTransaction.getCategory() == null || updatedTransaction.getCategory().getId() == null) {
                        throw new RuntimeException("Category ID must be provided.");
                    }

                    Long categoryId = updatedTransaction.getCategory().getId();
                    Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);

                    if (categoryOpt.isEmpty()) {
                        throw new RuntimeException("Category not found.");
                    }

                    Category category = categoryOpt.get();

                    // Zabezpieczenie (opcjonalnie): Jeśli kategoria nie jest domyślna, musi należeć do użytkownika
                    if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
                        throw new RuntimeException("Access denied: Cannot use this custom category.");
                    }

                    transaction.setCategory(category); // Przypisanie pełnego obiektu Category

                    // 2. Aktualizacja pozostałych pól
                    transaction.setAmount(updatedTransaction.getAmount());
                    transaction.setDescription(updatedTransaction.getDescription());
                    transaction.setType(updatedTransaction.getType());
                    transaction.setDate(updatedTransaction.getDate());

                    return transactionRepository.save(transaction);
                })
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    public void deleteTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied: Transaction does not belong to user.");
        }

        transactionRepository.deleteById(id);
    }

    public List<SpendingByCategoryDTO> getSpendingByCategory(Long budgetId, Long userId) {

        // Zabezpieczenie: Sprawdzamy, czy budżet istnieje i należy do użytkownika
        Optional<Budget> budget = budgetService.getBudgetById(budgetId);
        if (budget.isEmpty() || !budget.get().getUser().getId().equals(userId)) {
            throw new RuntimeException("Budget not found or access denied.");
        }

        // Wywołujemy funkcję repozytorium
        return transactionRepository.findSpendingByCategoryForChart(budgetId);
    }
}