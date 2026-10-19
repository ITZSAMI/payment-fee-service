package com.sami.paymentservice.service

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import kotlin.test.assertEquals

class FeeCalculatorServiceTest {

    private val feeCalculatorService = FeeCalculatorService()

    @ParameterizedTest(name = "amount={0} -> expectedFee={1}")
    @CsvSource(
        // Low tier (< 100): 2.0% + 0.30 flat
        "50.00, 1.30",
        "99.99, 2.30",
        // Mid tier (100 - 1000): 1.5% + 0.30 flat
        "100.00, 1.80",
        "500.00, 7.80",
        "999.99, 15.30",
        // High tier (> 1000): 1.0% + 0.30 flat
        "1000.00, 10.30",
        "5000.00, 50.30"
    )
    fun `calculateFee applies correct tiered rate plus flat fee`(
        amount: String,
        expectedFee: String
    ) {
        val result = feeCalculatorService.calculateFee(BigDecimal(amount))
        assertEquals(BigDecimal(expectedFee), result)
    }

    @Test
    fun `calculateFee throws for zero amount`() {
        assertThrows(IllegalArgumentException::class.java) {
            feeCalculatorService.calculateFee(BigDecimal.ZERO)
        }
    }

    @Test
    fun `calculateFee throws for negative amount`() {
        assertThrows(IllegalArgumentException::class.java) {
            feeCalculatorService.calculateFee(BigDecimal("-10.00"))
        }
    }

    @Test
    fun `calculateNetAmount subtracts fee from gross amount`() {
        val amount = BigDecimal("500.00")
        val fee = feeCalculatorService.calculateFee(amount)
        val net = feeCalculatorService.calculateNetAmount(amount)

        assertEquals(amount.subtract(fee), net)
    }

    @Test
    fun `calculateFee accepts the minimum amount and leaves a positive net`() {
        val amount = BigDecimal("0.50")
        assertEquals(BigDecimal("0.31"), feeCalculatorService.calculateFee(amount))
        assertEquals(BigDecimal("0.19"), feeCalculatorService.calculateNetAmount(amount))
    }

    @Test
    fun `calculateFee throws below the minimum amount`() {
        assertThrows(IllegalArgumentException::class.java) {
            feeCalculatorService.calculateFee(BigDecimal("0.49"))
        }
    }
}
