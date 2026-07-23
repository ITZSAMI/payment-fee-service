package com.sami.paymentservice.service

import com.sami.paymentservice.dto.CreateTransactionRequest
import com.sami.paymentservice.exception.TransactionNotFoundException
import com.sami.paymentservice.model.Transaction
import com.sami.paymentservice.model.TransactionStatus
import com.sami.paymentservice.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository
) {

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
        transactionRepository.findById(id)
            .orElseThrow { TransactionNotFoundException(id) }

    fun markCompleted(id: UUID): Transaction {
        val transaction = getTransaction(id)
        transaction.status = TransactionStatus.COMPLETED
        return transactionRepository.save(transaction)
    }

    fun listAll(): List<Transaction> = transactionRepository.findAll()
}
