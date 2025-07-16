package com.bundesbank.repository;

import com.bundesbank.entity.ExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {
    Optional<ExchangeRateEntity> findByDate(LocalDate date);
    List<ExchangeRateEntity> findAllByOrderByDateAsc();
    boolean existsByDate(LocalDate date);
}