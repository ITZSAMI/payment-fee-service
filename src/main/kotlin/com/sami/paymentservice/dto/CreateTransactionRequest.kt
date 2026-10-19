package com.sami.paymentservice.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.math.BigDecimal

data class CreateTransactionRequest(
    @field:NotBlank(message = "merchantId is required")
    val merchantId: String,

    @field:DecimalMin(value = "0.50", message = "amount must be at least 0.50")
    @field:Digits(integer = 15, fraction = 2, message = "amount must have at most 2 decimal places")
    val amount: BigDecimal,

    @field:Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter uppercase ISO code")
    val currency: String
)
