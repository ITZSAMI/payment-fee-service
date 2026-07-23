package com.sami.paymentservice.service

import com.sami.paymentservice.model.FeeRule
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Calculates processing fees for a transaction amount.
 *
 * Default rule set (mirrors a typical tiered card-processing fee schedule):
 *   - below EUR 100      -> 2.0%
 *   - EUR 100 to 1000    -> 1.5%
 *   - above EUR 1000     -> 1.0%
 * plus a flat EUR 0.30 per-transaction fee, matching common
 * "percentage + fixed" processor pricing models.
 *
 * Kept as a standalone, dependency-free service so the fee logic is easy
 * to unit test in isolation from persistence/web concerns.
 */
@Service
class FeeCalculatorService {

    private val tieredRule = FeeRule.Tiered(
        brackets = listOf(
            FeeRule.Tiered.Bracket(upperBound = BigDecimal("100"), rate = BigDecimal("0.020")),
            FeeRule.Tiered.Bracket(upperBound = BigDecimal("1000"), rate = BigDecimal("0.015")),
            FeeRule.Tiered.Bracket(upperBound = null, rate = BigDecimal("0.010"))
        )
    )

    private val flatFee = FeeRule.Flat(BigDecimal("0.30"))

    /**
     * Returns the total fee (percentage tier + flat fee) for the given amount,
     * rounded to 2 decimal places using standard half-up rounding.
     */
    fun calculateFee(amount: BigDecimal): BigDecimal {
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }

        val percentageFee = tieredRule.calculate(amount)
        val total = percentageFee.add(flatFee.calculate(amount))

        return total.setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Returns the net amount (gross - fee) for the given amount.
     */
    fun calculateNetAmount(amount: BigDecimal): BigDecimal {
        val fee = calculateFee(amount)
        return amount.subtract(fee).setScale(2, RoundingMode.HALF_UP)
    }
}
