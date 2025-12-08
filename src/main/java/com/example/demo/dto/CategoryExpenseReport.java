package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

public class CategoryExpenseReport {
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<CategoryExpense> categories;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<CategoryExpense> getCategories() { return categories; }
    public void setCategories(List<CategoryExpense> categories) { this.categories = categories; }
}