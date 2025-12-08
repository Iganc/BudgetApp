package com.example.demo.exception;

public class BudgetOverlapException extends RuntimeException {
    public BudgetOverlapException(String message) {
        super(message);
    }
}