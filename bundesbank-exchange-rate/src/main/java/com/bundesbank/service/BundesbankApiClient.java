package com.bundesbank.service;

import com.bundesbank.dto.BundesbankApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BundesbankApiClient {

    @Value("${bundesbank.api.base-url}")
    private String baseUrl;

    @Value("${supported.currencies}")
    private List<String> supportedCurrencies;

    private static final String DATA_FLOW = "BBEX3";
    private static final String FREQUENCY = "D";
    private static final String SERIES_TYPE = "BB";
    private static final String RATE_TYPE = "AC";
    private static final String SUFFIX = "000";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private final RestTemplate restTemplate;

    public Map<LocalDate, Map<String, BigDecimal>> fetchExchangeRates(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Map<String, BigDecimal>> rates = new ConcurrentHashMap<>();

        supportedCurrencies.parallelStream().forEach(currency -> {
            try {
                String url = buildUrlForCurrency(currency, startDate, endDate);
                log.debug("Fetching rates for {} from URL: {}", currency, url);

                ResponseEntity<BundesbankApiResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(createHeaders()),
                        BundesbankApiResponse.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    processResponse(currency, response.getBody(), rates);
                } else {
                    log.warn("No data available for {}: HTTP {}", currency, response.getStatusCode());
                }
            } catch (Exception e) {
                log.warn("Error processing currency {}: {}", currency, e.getMessage());
            }
        });

        return rates;
    }

    private void processResponse(String currency, BundesbankApiResponse response,
                                 Map<LocalDate, Map<String, BigDecimal>> rates) {
        if (response == null || response.getData() == null || response.getData().getDataSets().isEmpty()) {
            log.debug("No data sets received for currency: {}", currency);
            return;
        }

        BundesbankApiResponse.DataSet dataSet = response.getData().getDataSets().get(0);
        if (dataSet.getSeries() == null || dataSet.getSeries().isEmpty()) {
            log.debug("No series data available for currency: {}", currency);
            return;
        }

        // Get the first series entry
        BundesbankApiResponse.Series series = dataSet.getSeries().values().iterator().next();
        if (series.getObservations() == null || series.getObservations().isEmpty()) {
            log.debug("No observations available for currency: {} on requested date", currency);
            return;
        }

        List<String> timePeriods = response.getData().getTimePeriods();
        series.getObservations().forEach((obsIndex, values) -> {
            try {
                int index = Integer.parseInt(obsIndex);
                if (index < timePeriods.size() && values != null && !values.isEmpty() && values.get(0) != null) {
                    LocalDate date = LocalDate.parse(timePeriods.get(index));
                    BigDecimal rate = new BigDecimal(values.get(0));
                    rates.computeIfAbsent(date, k -> new ConcurrentHashMap<>())
                            .put(currency, rate);
                }
            } catch (Exception e) {
                log.warn("Failed to process observation {} for {}: {}", obsIndex, currency, e.getMessage());
            }
        });
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private String buildUrlForCurrency(String currency, LocalDate startDate, LocalDate endDate) {
        return String.format("%s/data/%s/%s.%s.EUR.%s.%s.%s?startPeriod=%s&endPeriod=%s&format=json",
                baseUrl,
                DATA_FLOW,
                FREQUENCY,
                currency,
                SERIES_TYPE,
                RATE_TYPE,
                SUFFIX,
                startDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER));
    }

    public Map<LocalDate, Map<String, BigDecimal>> fetchAllHistoricalRates() {
        Map<LocalDate, Map<String, BigDecimal>> allRates = new ConcurrentHashMap<>();

        supportedCurrencies.parallelStream().forEach(currency -> {
            try {
                String url = buildAllHistoricalDataUrl(currency);
                log.info("Fetching all historical rates for {}", currency);

                BundesbankApiResponse response = restTemplate.getForObject(url, BundesbankApiResponse.class);
                if (response != null && response.getData() != null) {
                    processResponse(currency, response, allRates);
                }
            } catch (Exception e) {
                log.error("Error fetching historical rates for {}: {}", currency, e.getMessage());
            }
        });

        return allRates;
    }

    private String buildAllHistoricalDataUrl(String currency) {
        return String.format("%s/data/%s/%s.%s.EUR.%s.%s.%s?format=json",
                baseUrl,
                "BBEX3",  // Data flow
                "D",      // Daily frequency
                currency, // Currency code
                "BB",     // Series type
                "AC",     // Rate type (average)
                "000");   // Time suffix
    }
}