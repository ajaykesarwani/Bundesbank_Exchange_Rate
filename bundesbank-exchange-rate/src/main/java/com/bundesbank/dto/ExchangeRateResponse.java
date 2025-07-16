package com.bundesbank.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class ExchangeRateResponse {
    private LocalDate date;
    private Map<String, BigDecimal> rates; // currency -> rate
}
