package com.bundesbank.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Data
@Builder
public class ConversionResult {
    private String fromCurrency;
    private BigDecimal originalAmount;
    private LocalDate date;
    private BigDecimal convertedAmount;

    public String getFormattedResult() {
        return String.format("%s %s on %s = %s EUR",
                originalAmount.setScale(2, RoundingMode.HALF_EVEN),
                fromCurrency,
                date,
                convertedAmount.setScale(4, RoundingMode.HALF_EVEN));
    }
}