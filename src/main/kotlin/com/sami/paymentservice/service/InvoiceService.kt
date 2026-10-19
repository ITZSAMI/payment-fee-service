package com.sami.paymentservice.service

import com.sami.paymentservice.exception.InvalidTransactionStateException
import com.sami.paymentservice.exception.InvoiceAlreadyExistsException
import com.sami.paymentservice.exception.InvoiceNotFoundException
import com.sami.paymentservice.model.Invoice
import com.sami.paymentservice.model.TransactionStatus
import com.sami.paymentservice.repository.InvoiceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val transactionService: TransactionService,
    private val feeCalculatorService: FeeCalculatorService
) {

    @Transactional
    fun generateInvoice(transactionId: UUID): Invoice {
        val transaction = transactionService.getTransaction(transactionId)

        if (transaction.status != TransactionStatus.COMPLETED) {
            throw InvalidTransactionStateException(
                "Transaction $transactionId cannot be invoiced: status is ${transaction.status}"
            )
        }

        if (invoiceRepository.findByTransactionId(transactionId) != null) {
            throw InvoiceAlreadyExistsException(transactionId)
        }

        val invoice = Invoice(
            transactionId = transaction.id,
            grossAmount = transaction.amount,
            feeAmount = feeCalculatorService.calculateFee(transaction.amount),
            netAmount = feeCalculatorService.calculateNetAmount(transaction.amount)
        )

        return invoiceRepository.save(invoice)
    }

    fun getInvoice(id: UUID): Invoice =
        findInvoice(id) ?: throw InvoiceNotFoundException(id)

    fun findInvoice(id: UUID): Invoice? =
        invoiceRepository.findById(id).orElse(null)

    fun getInvoiceByTransaction(transactionId: UUID): Invoice? =
        invoiceRepository.findByTransactionId(transactionId)
}
