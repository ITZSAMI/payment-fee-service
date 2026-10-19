package com.sami.paymentservice.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class CreateTransactionRequest(
    @field:NotBlank(message = "merchantId is required")
    val merchantId: String,

    @field:DecimalMin(value = "0.50", message = "amount must be at least 0.50")
    val amount: BigDecimal,

    @field:NotBlank
    @field:Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
    val currency: String
)
