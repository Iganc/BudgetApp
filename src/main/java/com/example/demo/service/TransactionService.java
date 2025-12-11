package com.example.demo.service;

import com.example.demo.dto.SpendingByCategoryDTO;
import com.example.demo.model.Budget;
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

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, BudgetService budgetService) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
    }

    public Transaction createTransaction(Transaction transaction, Long userId) {

        if (transaction.getUser() == null || !transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Transaction must be assigned to the logged-in user.");
        }

        if (transaction.getBudget() != null && transaction.getBudget().getId() != null) {
            Long budgetId = transaction.getBudget().getId();

            Optional<Budget> budget = budgetService.getBudgetById(budgetId);
            if (budget.isEmpty() || !budget.get().getUser().getId().equals(userId)) {
                throw new RuntimeException("Budget not found or access denied.");
            }
            transaction.setBudget(budget.get());
        }

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