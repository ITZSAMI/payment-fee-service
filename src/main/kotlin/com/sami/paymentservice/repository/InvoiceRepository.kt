package com.sami.paymentservice.repository

import com.sami.paymentservice.model.Invoice
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InvoiceRepository : JpaRepository<Invoice, UUID> {
    fun findByTransactionId(transactionId: UUID): Invoice?
}
