package com.example.demo.controller;

import com.example.demo.dto.BudgetSummaryDTO;
import com.example.demo.dto.SpendingByCategoryDTO;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/budget/{budgetId}/summary")
    public ResponseEntity<BudgetSummaryDTO> getBudgetSummary(@PathVariable Long budgetId) {
        return ResponseEntity.ok(reportService.getBudgetSummary(budgetId));
    }

    @GetMapping("/budget/{budgetId}/spending-by-category")
    public ResponseEntity<List<SpendingByCategoryDTO>> getSpendingByCategory(
            @PathVariable Long budgetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getSpendingByCategory(budgetId, startDate, endDate));
    }
}