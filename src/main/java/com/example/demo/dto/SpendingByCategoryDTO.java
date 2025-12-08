package com.example.demo.dto;

import java.math.BigDecimal;

public class SpendingByCategoryDTO {
    private String category;
    private BigDecimal totalAmount;

    // Konstruktor dla JPA Query
    public SpendingByCategoryDTO(String category, BigDecimal totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
    }

    // Konstruktor bezparametrowy
    public SpendingByCategoryDTO() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}