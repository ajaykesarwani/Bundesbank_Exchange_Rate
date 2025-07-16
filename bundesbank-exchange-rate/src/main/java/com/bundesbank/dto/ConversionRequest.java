package com.bundesbank.dto;

import javax.validation.constraints.*;
import lombok.*;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionRequest {

    @NotBlank(message = "Currency code cannot be blank")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters")
    private String currency;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    @Digits(integer = 10, fraction = 2, message = "Amount must have up to 10 integer and 2 fraction digits")
    private BigDecimal amount;

    @NotNull(message = "Date cannot be null")
    @PastOrPresent(message = "Date must be today or in the past")
    private LocalDate date;
}