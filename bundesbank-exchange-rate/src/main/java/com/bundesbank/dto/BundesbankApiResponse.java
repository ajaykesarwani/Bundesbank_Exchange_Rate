package com.bundesbank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class BundesbankApiResponse {
    private Meta meta;
    private Data data;

    @Getter
    @Setter
    public static class Meta {
        private String schema;
        private String id;
        private boolean test;
        private String prepared;

        @JsonProperty("content-languages")
        private List<String> contentLanguages;

        private Sender sender;
    }

    @Getter
    @Setter
    public static class Sender {
        private String id;
        private Map<String, String> names;
        private List<Contact> contact;
    }

    @Getter
    @Setter
    public static class Contact {
        private String name;
        private List<String> emails;
    }

    @Getter
    @Setter
    public static class Data {
        private Structure structure;

        @JsonProperty("dataSets")
        private List<DataSet> dataSets;

        public List<ExchangeRate> getExchangeRates() {
            if (dataSets == null || dataSets.isEmpty()) {
                return Collections.emptyList();
            }
            return dataSets.get(0).getSeries().entrySet().stream()
                    .flatMap(entry -> entry.getValue().getObservations().entrySet().stream())
                    .map(obs -> new ExchangeRate(obs.getKey(), obs.getValue().get(0)))
                    .collect(Collectors.toList());
        }

        public List<String> getTimePeriods() {
            return structure.getDimensions().getObservation().stream()
                    .filter(dim -> "TIME_PERIOD".equals(dim.getId()))
                    .flatMap(dim -> dim.getValues().stream())
                    .map(Value::getId)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    @Setter
    public static class Structure {
        private Dimensions dimensions;
        private Attributes attributes;
    }

    @Getter
    @Setter
    public static class Dimensions {
        @JsonProperty("dataSet")
        private List<Dimension> dataSet;
        private List<Dimension> series;
        private List<Dimension> observation;
    }

    @Getter
    @Setter
    public static class Dimension {
        private String id;
        private String name;

        @JsonProperty("keyPosition")
        private int keyPosition;

        @JsonProperty("default")
        private String defaultValue;

        private List<Value> values;
    }

    @Getter
    @Setter
    public static class Value {
        private String id;
        private String name;
    }

    @Getter
    @Setter
    public static class Attributes {
        private List<Attribute> series;
        private List<Attribute> observation;
    }

    @Getter
    @Setter
    public static class Attribute {
        private String id;
        private String name;
        private Relationship relationship;
        private List<Object> values;
    }

    @Getter
    @Setter
    public static class Relationship {
        private Object none;
    }

    @Getter
    @Setter
    public static class DataSet {
        private String action;

        @JsonProperty("valid-from")
        private String validFrom;

        private Map<String, Series> series;
        private List<Link> links;
    }

    @Getter
    @Setter
    public static class Series {
        private List<Integer> attributes;

        @JsonProperty("observations")
        private Map<String, List<String>> observations;
    }

    @Getter
    @Setter
    public static class Link {
        private String rel;
        private String urn;
    }

    @lombok.Data
    public static class ExchangeRate {
        private final String observationKey;
        private final String rate;
    }

    public Map<LocalDate, String> getDateRateMapping() {
        Map<LocalDate, String> result = new LinkedHashMap<>();
        List<String> timePeriods = data.getTimePeriods();
        List<ExchangeRate> exchangeRates = data.getExchangeRates();

        for (ExchangeRate exchangeRate : exchangeRates) {
            try {
                int index = Integer.parseInt(exchangeRate.getObservationKey());
                if (index < timePeriods.size()) {
                    result.put(LocalDate.parse(timePeriods.get(index)), exchangeRate.getRate());
                }
            } catch (Exception e) {
                // Handle parsing errors
            }
        }
        return result;
    }
}