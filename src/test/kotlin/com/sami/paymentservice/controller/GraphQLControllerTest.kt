package com.sami.paymentservice.controller

import com.sami.paymentservice.model.Invoice
import com.sami.paymentservice.model.Transaction
import com.sami.paymentservice.service.InvoiceService
import com.sami.paymentservice.service.TransactionService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GraphQLControllerTest {

    private val invoiceService = mockk<InvoiceService>()
    private val transactionService = mockk<TransactionService>()
    private val controller = GraphQLController(invoiceService, transactionService)

    @Test
    fun `invoice returns the invoice when it exists`() {
        val invoice = Invoice(
            transactionId = UUID.randomUUID(),
            grossAmount = BigDecimal("500.00"),
            feeAmount = BigDecimal("7.80"),
            netAmount = BigDecimal("492.20")
        )
        every { invoiceService.findInvoice(invoice.id) } returns invoice

        val result = controller.invoice(invoice.id)

        assertEquals(invoice.id, result?.id)
        assertEquals(BigDecimal("492.20"), result?.netAmount)
    }

    @Test
    fun `invoice returns null for an unknown id`() {
        val id = UUID.randomUUID()
        every { invoiceService.findInvoice(id) } returns null

        assertNull(controller.invoice(id))
    }

    @Test
    fun `transaction returns the transaction when it exists`() {
        val transaction = Transaction(
            merchantId = "merchant-1",
            amount = BigDecimal("500.00"),
            currency = "EUR"
        )
        every { transactionService.findTransaction(transaction.id) } returns transaction

        val result = controller.transaction(transaction.id)

        assertEquals(transaction.id, result?.id)
    }

    @Test
    fun `transaction returns null for an unknown id`() {
        val id = UUID.randomUUID()
        every { transactionService.findTransaction(id) } returns null

        assertNull(controller.transaction(id))
    }
}
