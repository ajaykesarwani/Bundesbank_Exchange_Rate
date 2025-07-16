    package com.bundesbank.controller;

    import com.bundesbank.dto.ConversionRequest;
    import com.bundesbank.dto.ConversionResult;
    import com.bundesbank.dto.ExchangeRateDto;
    import com.bundesbank.dto.ExchangeRateResponse;
    import com.bundesbank.exception.ExchangeRateException;
    import com.bundesbank.service.ExchangeRateService;
    import javassist.NotFoundException;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.http.ResponseEntity;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.*;

    import javax.validation.constraints.DecimalMin;
    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.util.List;

    @RestController
    @RequestMapping("/api")
    @RequiredArgsConstructor
    @Validated
    @Slf4j
    public class ExchangeRateController {
        private final ExchangeRateService exchangeRateService;

        // Get all available currencies
        @GetMapping("/currencies")
        public ResponseEntity<List<String>> getAvailableCurrencies() {
            log.info("Fetching available currencies");
            return ResponseEntity.ok(exchangeRateService.getAvailableCurrencies());
        }

        // Get all exchange rates for all dates
        @GetMapping("/rates")
        public ResponseEntity<List<ExchangeRateDto>> getAllExchangeRates() {
            log.info("Fetching all exchange rates");
            return ResponseEntity.ok(exchangeRateService.getAllExchangeRates());
        }

        // Get exchange rates for a specific date
        @GetMapping("/rates/{date}")
        public ResponseEntity<ExchangeRateResponse> getRates(
                @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
            log.info("Fetching exchange rates for date: {}", date);
            try {
                return ResponseEntity.ok(exchangeRateService.getExchangeRates(date));
            } catch (NotFoundException ex) {
                throw new ExchangeRateException("No exchange rates found for date: " + date, "RATE_NOT_FOUND");
            }
        }

        // Get exchange rate for a specific currency on a specific date
        @GetMapping("/rates/{date}/{currency}")
        public ResponseEntity<BigDecimal> getExchangeRateForCurrency(
                @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                @PathVariable String currency) {
            log.info("Fetching {} rate for date: {}", currency, date);

            try {
                return exchangeRateService.getExchangeRateForCurrency(date, currency)
                        .map(ResponseEntity::ok)
                        .orElseThrow(() -> new NotFoundException("No rate found for " + currency + " on " + date));
            } catch (ExchangeRateException ex) {
                throw ex; // Re-throw as is
            } catch (Exception ex) {
                throw new ExchangeRateException("Failed to retrieve exchange rate", "RATE_RETRIEVAL_ERROR");
            }
        }

        // Convert amount to EUR for a given currency and date
        @GetMapping("/convert")
        public ResponseEntity<ConversionResult> convertToEuro(
                @RequestParam String currency,
                @RequestParam @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
            log.info("Converting {} {} to EUR on date {}", amount, currency, date);

            try {
                ConversionRequest request = new ConversionRequest(currency, amount, date);
                return ResponseEntity.ok(exchangeRateService.convertToEur(request));
            } catch (NotFoundException ex) {
                throw new ExchangeRateException(ex.getMessage(), "CONVERSION_DATA_MISSING");
            } catch (IllegalArgumentException ex) {
                throw new ExchangeRateException(ex.getMessage(), "INVALID_CONVERSION_REQUEST");
            }
        }

    }