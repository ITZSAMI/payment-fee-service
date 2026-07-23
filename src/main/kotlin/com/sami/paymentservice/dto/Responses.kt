package com.sami.paymentservice.dto

import com.sami.paymentservice.model.Invoice
import com.sami.paymentservice.model.Transaction
import com.sami.paymentservice.model.TransactionStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class TransactionResponse(
    val id: UUID,
    val merchantId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: TransactionStatus,
    val createdAt: Instant
) {
    companion object {
        fun from(transaction: Transaction) = TransactionResponse(
            id = transaction.id,
            merchantId = transaction.merchantId,
            amount = transaction.amount,
            currency = transaction.currency,
            status = transaction.status,
            createdAt = transaction.createdAt
        )
    }
}

data class InvoiceResponse(
    val id: UUID,
    val transactionId: UUID,
    val grossAmount: BigDecimal,
    val feeAmount: BigDecimal,
    val netAmount: BigDecimal,
    val generatedAt: Instant
) {
    companion object {
        fun from(invoice: Invoice) = InvoiceResponse(
            id = invoice.id,
            transactionId = invoice.transactionId,
            grossAmount = invoice.grossAmount,
            feeAmount = invoice.feeAmount,
            netAmount = invoice.netAmount,
            generatedAt = invoice.generatedAt
        )
    }
}
