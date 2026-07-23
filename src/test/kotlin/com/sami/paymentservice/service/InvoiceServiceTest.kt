package com.sami.paymentservice.service

import com.sami.paymentservice.model.Transaction
import com.sami.paymentservice.repository.InvoiceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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

    @Test
    fun `generateInvoice calculates fee and net amount from transaction`() {
        val transaction = Transaction(
            merchantId = "merchant-1",
            amount = BigDecimal("500.00"),
            currency = "EUR"
        )
        every { transactionService.getTransaction(transaction.id) } returns transaction

        val savedInvoiceSlot = slot<com.sami.paymentservice.model.Invoice>()
        every { invoiceRepository.save(capture(savedInvoiceSlot)) } answers { savedInvoiceSlot.captured }

        val result = invoiceService.generateInvoice(transaction.id)

        assertEquals(transaction.amount, result.grossAmount)
        assertEquals(BigDecimal("7.80"), result.feeAmount)
        assertEquals(BigDecimal("492.20"), result.netAmount)

        verify(exactly = 1) { invoiceRepository.save(any()) }
    }
}
