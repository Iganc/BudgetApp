package com.example.demo.service;

import com.example.demo.model.Transaction;
import com.example.demo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getTransactionsByUserIdAndCategory(Long userId, String category) {
        return transactionRepository.findByUserIdAndCategory(userId, category);
    }

    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
        return transactionRepository.findById(id)
                .map(transaction -> {
                    transaction.setAmount(updatedTransaction.getAmount());
                    transaction.setCategory(updatedTransaction.getCategory());
                    transaction.setDescription(updatedTransaction.getDescription());
                    transaction.setDate(updatedTransaction.getDate());
                    return transactionRepository.save(transaction);
                })
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }

    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }

    public double calculateBalance(Long userId) {
        List<Transaction> transactions = getTransactionsByUserId(userId);
        return transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateTotalIncome(Long userId) {
        List<Transaction> transactions = getTransactionsByUserId(userId);
        return transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateTotalExpense(Long userId) {
        List<Transaction> transactions = getTransactionsByUserId(userId);
        return transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}