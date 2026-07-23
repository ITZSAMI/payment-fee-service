package com.sami.paymentservice.controller

import com.sami.paymentservice.dto.CreateTransactionRequest
import com.sami.paymentservice.dto.TransactionResponse
import com.sami.paymentservice.service.TransactionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {

    @PostMapping
    fun createTransaction(
        @Valid @RequestBody request: CreateTransactionRequest
    ): ResponseEntity<TransactionResponse> {
        val transaction = transactionService.createTransaction(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(transaction))
    }

    @GetMapping("/{id}")
    fun getTransaction(@PathVariable id: UUID): TransactionResponse =
        TransactionResponse.from(transactionService.getTransaction(id))

    @GetMapping
    fun listTransactions(): List<TransactionResponse> =
        transactionService.listAll().map { TransactionResponse.from(it) }

    @PostMapping("/{id}/complete")
    fun completeTransaction(@PathVariable id: UUID): TransactionResponse =
        TransactionResponse.from(transactionService.markCompleted(id))
}
