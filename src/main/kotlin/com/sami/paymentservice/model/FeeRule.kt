package com.sami.paymentservice.model

import java.math.BigDecimal

/**
 * Defines how a fee is calculated for a given transaction amount.
 * Kept in-memory/config-driven for this mock service rather than persisted,
 * to keep the fee logic simple and easily unit-testable.
 */
sealed class FeeRule {

    abstract fun calculate(amount: BigDecimal): BigDecimal

    /** A fixed fee regardless of transaction amount, e.g. EUR 0.30 per transaction. */
    data class Flat(val value: BigDecimal) : FeeRule() {
        override fun calculate(amount: BigDecimal): BigDecimal = value
    }

    /** A percentage of the transaction amount, e.g. 1.5%. */
    data class Percentage(val rate: BigDecimal) : FeeRule() {
        override fun calculate(amount: BigDecimal): BigDecimal =
            amount.multiply(rate)
    }

    /**
     * Different percentage rates depending on which amount bracket the
     * transaction falls into. Brackets are defined as (upperBoundExclusive, rate),
     * evaluated in ascending order; the last bracket should use null as an
     * open-ended upper bound.
     */
    data class Tiered(val brackets: List<Bracket>) : FeeRule() {
        data class Bracket(val upperBound: BigDecimal?, val rate: BigDecimal)

        override fun calculate(amount: BigDecimal): BigDecimal {
            val bracket = brackets.firstOrNull { it.upperBound == null || amount < it.upperBound }
                ?: brackets.last()
            return amount.multiply(bracket.rate)
        }
    }
}
