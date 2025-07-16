package com.bundesbank.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;


@Entity
@Table(name = "exchange_rates")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ExchangeRateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ElementCollection
    @CollectionTable(name = "currency_rates",
            joinColumns = @JoinColumn(name = "exchange_rate_id"))
    @MapKeyColumn(name = "currency_code")
    @Column(name = "rate", precision = 19, scale = 6)
    private Map<String, BigDecimal> rates;
}