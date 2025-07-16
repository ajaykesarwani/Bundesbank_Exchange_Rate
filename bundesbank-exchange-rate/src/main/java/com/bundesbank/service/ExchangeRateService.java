package com.bundesbank.service;

import com.bundesbank.dto.ConversionRequest;
import com.bundesbank.dto.ConversionResult;
import com.bundesbank.dto.ExchangeRateDto;
import com.bundesbank.dto.ExchangeRateResponse;
import com.bundesbank.entity.ExchangeRateEntity;
import com.bundesbank.exception.ExchangeRateException;
import com.bundesbank.repository.ExchangeRateRepository;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final BundesbankApiClient apiClient;
    private final ExchangeRateRepository exchangeRateRepository;

    @Value("${supported.currencies}")
    private List<String> supportedCurrencies;

    // Gets ALL configured currencies
    public List<String> getAvailableCurrencies() {
        return Collections.unmodifiableList(supportedCurrencies);
    }

    // Gets rates for ALL currencies on a date
    public ExchangeRateResponse getExchangeRates(LocalDate date) throws NotFoundException {
        return exchangeRateRepository.findByDate(date)
                .map(this::convertToResponse)
                .orElseThrow(() -> new NotFoundException("No rates found for date: " + date));
    }

    private ExchangeRateResponse convertToResponse(ExchangeRateEntity entity) {
        return ExchangeRateResponse.builder()
                .date(entity.getDate())
                .rates(entity.getRates())
                .build();
    }
    public List<ExchangeRateDto> getAllExchangeRates() {
        return exchangeRateRepository.findAllByOrderByDateAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    private ExchangeRateDto convertToDto(ExchangeRateEntity entity) {
        return ExchangeRateDto.builder()
                .date(entity.getDate())
                .eurToForeignRates(entity.getRates())
                .build();
    }
    public ConversionResult convertToEur(ConversionRequest request) throws NotFoundException {
        validateCurrency(request.getCurrency());

        ExchangeRateEntity entity = exchangeRateRepository.findByDate(request.getDate())
                .orElseThrow(() -> new NotFoundException("No rates available for date " + request.getDate()));

        BigDecimal eurToForeignRate = Optional.ofNullable(entity.getRates().get(request.getCurrency()))
                .orElseThrow(() -> new NotFoundException("No rate available for currency " + request.getCurrency()));

        if (eurToForeignRate.compareTo(BigDecimal.ZERO) == 0) {
            throw new ExchangeRateException("Cannot convert with zero exchange rate", "ZERO_EXCHANGE_RATE");
        }

        try {
            BigDecimal foreignToEuroRate = BigDecimal.ONE.divide(eurToForeignRate, 6, RoundingMode.HALF_UP);
            BigDecimal euroAmount = request.getAmount().multiply(foreignToEuroRate)
                    .setScale(4, RoundingMode.HALF_UP);

            return ConversionResult.builder()
                    .fromCurrency(request.getCurrency())
                    .originalAmount(request.getAmount())
                    .date(request.getDate())
                    .convertedAmount(euroAmount)
                    .build();
        } catch (ArithmeticException ex) {
            throw new ExchangeRateException("Error during currency conversion", "CONVERSION_CALCULATION_ERROR");
        }
    }

    public Optional<BigDecimal> getExchangeRateForCurrency(LocalDate date, String currency) {
        // Validate currency first
        if (!supportedCurrencies.contains(currency.toUpperCase())) {
            throw new ExchangeRateException("Unsupported currency: " + currency);
        }

        return exchangeRateRepository.findByDate(date)
                .map(entity -> entity.getRates().get(currency.toUpperCase()));
    }
    private void validateCurrency(String currency) {
        if (!supportedCurrencies.contains(currency)) {
            throw new ExchangeRateException("Unsupported currency: " + currency);
        }
    }

    @PostConstruct
    @Transactional
    public void initializeData() {
        if (exchangeRateRepository.count() == 0) {
            log.info("Initializing database with recent exchange rates");
            importAllHistoricalData();
        }
        fetchAndStoreCurrentRatesIfMissing();
    }

    @Scheduled(cron = "${exchange-rate.update-cron:0 0 12 * * ?}")
    @Transactional
    public void fetchAndStoreCurrentRatesIfMissing() {
        LocalDate today = LocalDate.now();
        if (!exchangeRateRepository.existsByDate(today)) {
            log.info("Fetching current day exchange rates for {}", today);
            Map<LocalDate, Map<String, BigDecimal>> rates = apiClient.fetchExchangeRates(today, today);
            rates.forEach(this::storeRatesForDate);
        }
    }

    private void storeRatesForDate(LocalDate date, Map<String, BigDecimal> currencyRates) {
        ExchangeRateEntity entity = ExchangeRateEntity.builder()
                .date(date)
                .rates(currencyRates)
                .build();
        exchangeRateRepository.save(entity);
    }

    //@Scheduled(cron = "${exchange-rate.full-import-cron:0 0 3 * * SUN}") // Weekly on Sundays at 3AM

    @Transactional
    public void importAllHistoricalData() {
        log.info("Starting full historical data import");
        try {
            Map<LocalDate, Map<String, BigDecimal>> allRates = apiClient.fetchAllHistoricalRates();

            allRates.forEach((date, rates) -> {
                if (!exchangeRateRepository.existsByDate(date)) {
                    ExchangeRateEntity entity = ExchangeRateEntity.builder()
                            .date(date)
                            .rates(rates)
                            .build();
                    exchangeRateRepository.save(entity);
                    log.debug("Saved rates for {}", date);
                }
            });

            log.info("Completed historical import. Processed {} dates", allRates.size());
        } catch (Exception e) {
            log.error("Failed to import historical data", e);
        }
    }

}