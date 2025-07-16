package com.bundesbank.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Data
@Builder
public class ExchangeRateDto {
    private LocalDate date;
    // This map contains 1 EUR = X USD/GBP/etc.
    private Map<String, BigDecimal> eurToForeignRates;

    // Optional: Helper method for specific currency access
    public Optional<BigDecimal> getRate(String currency) {
        return Optional.ofNullable(eurToForeignRates.get(currency));
    }
}



