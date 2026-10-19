package com.sami.paymentservice.service

import com.sami.paymentservice.dto.CreateTransactionRequest
import com.sami.paymentservice.exception.InvalidTransactionStateException
import com.sami.paymentservice.exception.TransactionNotFoundException
import com.sami.paymentservice.model.Transaction
import com.sami.paymentservice.model.TransactionStatus
import com.sami.paymentservice.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository
) {

    @Transactional
    fun createTransaction(request: CreateTransactionRequest): Transaction {
        val transaction = Transaction(
            merchantId = request.merchantId,
            amount = request.amount,
            currency = request.currency,
            status = TransactionStatus.PENDING
        )
        return transactionRepository.save(transaction)
    }

    fun getTransaction(id: UUID): Transaction =
        findTransaction(id) ?: throw TransactionNotFoundException(id)

    fun findTransaction(id: UUID): Transaction? =
        transactionRepository.findById(id).orElse(null)

    @Transactional
    fun markCompleted(id: UUID): Transaction {
        val transaction = getTransaction(id)
        if (transaction.status != TransactionStatus.PENDING) {
            throw InvalidTransactionStateException(
                "Transaction $id cannot be completed: status is ${transaction.status}"
            )
        }
        transaction.status = TransactionStatus.COMPLETED
        return transactionRepository.save(transaction)
    }

    fun listAll(): List<Transaction> = transactionRepository.findAll()
}
