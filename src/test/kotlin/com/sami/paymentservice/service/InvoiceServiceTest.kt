package com.sami.paymentservice.service

import com.sami.paymentservice.exception.InvalidTransactionStateException
import com.sami.paymentservice.exception.InvoiceAlreadyExistsException
import com.sami.paymentservice.model.Invoice
import com.sami.paymentservice.model.Transaction
import com.sami.paymentservice.model.TransactionStatus
import com.sami.paymentservice.repository.InvoiceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

class InvoiceServiceTest {

    private val invoiceRepository = mockk<InvoiceRepository>()
    private val transactionService = mockk<TransactionService>()
    private val feeCalculatorService = FeeCalculatorService() // real instance, it's pure logic

    private lateinit var invoiceService: InvoiceService

    @BeforeEach
    fun setUp() {
        invoiceService = InvoiceService(invoiceRepository, transactionService, feeCalculatorService)
    }

    private fun transactionWithStatus(status: TransactionStatus) = Transaction(
        merchantId = "merchant-1",
        amount = BigDecimal("500.00"),
        currency = "EUR",
        status = status
    )

    @Test
    fun `generateInvoice calculates fee and net amount from transaction`() {
        val transaction = transactionWithStatus(TransactionStatus.COMPLETED)
        every { transactionService.getTransaction(transaction.id) } returns transaction
        every { invoiceRepository.findByTransactionId(transaction.id) } returns null

        val savedInvoiceSlot = slot<Invoice>()
        every { invoiceRepository.save(capture(savedInvoiceSlot)) } answers { savedInvoiceSlot.captured }

        val result = invoiceService.generateInvoice(transaction.id)

        assertEquals(transaction.amount, result.grossAmount)
        assertEquals(BigDecimal("7.80"), result.feeAmount)
        assertEquals(BigDecimal("492.20"), result.netAmount)

        verify(exactly = 1) { invoiceRepository.save(any()) }
    }

    @Test
    fun `generateInvoice throws if an invoice already exists for the transaction`() {
        val transaction = transactionWithStatus(TransactionStatus.COMPLETED)
        val existingInvoice = Invoice(
            transactionId = transaction.id,
            grossAmount = BigDecimal("500.00"),
            feeAmount = BigDecimal("7.80"),
            netAmount = BigDecimal("492.20")
        )
        every { transactionService.getTransaction(transaction.id) } returns transaction
        every { invoiceRepository.findByTransactionId(transaction.id) } returns existingInvoice

        assertThrows(InvoiceAlreadyExistsException::class.java) {
            invoiceService.generateInvoice(transaction.id)
        }

        verify(exactly = 0) { invoiceRepository.save(any()) }
    }

    @Test
    fun `generateInvoice throws if the transaction is not completed`() {
        val transaction = transactionWithStatus(TransactionStatus.PENDING)
        every { transactionService.getTransaction(transaction.id) } returns transaction

        assertThrows(InvalidTransactionStateException::class.java) {
            invoiceService.generateInvoice(transaction.id)
        }

        verify(exactly = 0) { invoiceRepository.save(any()) }
    }
}