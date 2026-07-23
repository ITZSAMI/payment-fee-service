package com.sami.paymentservice.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class CreateTransactionRequest(
    @field:NotBlank(message = "merchantId is required")
    val merchantId: String,

    @field:DecimalMin(value = "0.01", message = "amount must be greater than zero")
    val amount: BigDecimal,

    @field:NotBlank
    @field:Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
    val currency: String
)
