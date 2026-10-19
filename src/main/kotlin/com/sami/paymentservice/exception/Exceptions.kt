package com.sami.paymentservice.exception

import java.util.UUID

class TransactionNotFoundException(id: UUID) :
    RuntimeException("Transaction not found: $id")

class InvoiceNotFoundException(id: UUID) :
    RuntimeException("Invoice not found: $id")


class InvoiceAlreadyExistsException(transactionId: UUID) :
    RuntimeException("Invoice already exists for transaction: $transactionId")


class InvalidTransactionStateException(message: String) :
    RuntimeException(message)
