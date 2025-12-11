package com.example.demo.validation;

import com.example.demo.exception.InvalidDateRangeException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DateRangeValidator {

    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new InvalidDateRangeException("Start date cannot be null");
        }

        if (endDate != null) {
            if (endDate.isBefore(startDate)) {
                throw new InvalidDateRangeException(
                        String.format("End date (%s) must be after start date (%s)", endDate, startDate)
                );
            }

            if (startDate.equals(endDate)) {
                throw new InvalidDateRangeException("Start date and end date cannot be the same");
            }
        }
    }

    public boolean isOverlapping(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        // Jeśli którykolwiek budżet jest nieograniczony (end == null), zawsze może być nakładanie
        if (end1 == null || end2 == null) {
            return true; // Zakładamy nakładanie dla uproszczenia
        }
        return !end1.isBefore(start2) && !end2.isBefore(start1);
    }
}