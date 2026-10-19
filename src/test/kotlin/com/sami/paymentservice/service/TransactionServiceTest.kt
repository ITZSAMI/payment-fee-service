package com.sami.paymentservice.service

import com.sami.paymentservice.exception.InvalidTransactionStateException
import com.sami.paymentservice.model.Transaction
import com.sami.paymentservice.model.TransactionStatus
import com.sami.paymentservice.repository.TransactionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Optional
import kotlin.test.assertEquals

class TransactionServiceTest {

    private val transactionRepository = mockk<TransactionRepository>()
    private val transactionService = TransactionService(transactionRepository)

    private fun transactionWithStatus(status: TransactionStatus) = Transaction(
        merchantId = "merchant-1",
        amount = BigDecimal("500.00"),
        currency = "EUR",
        status = status
    )

    @Test
    fun `markCompleted moves a pending transaction to completed`() {
        val transaction = transactionWithStatus(TransactionStatus.PENDING)
        every { transactionRepository.findById(transaction.id) } returns Optional.of(transaction)
        every { transactionRepository.save(any()) } answers { firstArg() }

        val result = transactionService.markCompleted(transaction.id)

        assertEquals(TransactionStatus.COMPLETED, result.status)
        verify(exactly = 1) { transactionRepository.save(any()) }
    }

    @Test
    fun `markCompleted throws for an already completed transaction`() {
        val transaction = transactionWithStatus(TransactionStatus.COMPLETED)
        every { transactionRepository.findById(transaction.id) } returns Optional.of(transaction)

        assertThrows(InvalidTransactionStateException::class.java) {
            transactionService.markCompleted(transaction.id)
        }

        verify(exactly = 0) { transactionRepository.save(any()) }
    }

    @Test
    fun `markCompleted throws for a failed transaction`() {
        val transaction = transactionWithStatus(TransactionStatus.FAILED)
        every { transactionRepository.findById(transaction.id) } returns Optional.of(transaction)

        assertThrows(InvalidTransactionStateException::class.java) {
            transactionService.markCompleted(transaction.id)
        }

        verify(exactly = 0) { transactionRepository.save(any()) }
    }
}