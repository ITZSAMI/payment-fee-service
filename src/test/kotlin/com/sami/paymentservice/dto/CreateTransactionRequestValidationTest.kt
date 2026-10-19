package com.sami.paymentservice.dto

import jakarta.validation.Validation
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateTransactionRequestValidationTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun request(amount: String = "10.00", currency: String = "EUR") =
        CreateTransactionRequest(
            merchantId = "merchant-1",
            amount = BigDecimal(amount),
            currency = currency
        )

    /** Returns the names of the fields that failed validation. */
    private fun invalidFields(request: CreateTransactionRequest): Set<String> =
        validator.validate(request).map { it.propertyPath.toString() }.toSet()

    @Test
    fun `a valid request has no violations`() {
        assertTrue(invalidFields(request()).isEmpty())
    }

    @Test
    fun `amount below the minimum is rejected`() {
        assertEquals(setOf("amount"), invalidFields(request(amount = "0.49")))
    }

    @Test
    fun `amount with more than 2 decimal places is rejected`() {
        assertEquals(setOf("amount"), invalidFields(request(amount = "10.005")))
    }

    @Test
    fun `lowercase currency is rejected`() {
        assertEquals(setOf("currency"), invalidFields(request(currency = "eur")))
    }

    @Test
    fun `currency that is not 3 letters is rejected`() {
        assertEquals(setOf("currency"), invalidFields(request(currency = "EU1")))
    }
}
