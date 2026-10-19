package com.sami.paymentservice.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "invoices")
data class Invoice(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    val transactionId: UUID,

    @Column(nullable = false, precision = 19, scale = 4)
    val grossAmount: BigDecimal,

    @Column(nullable = false, precision = 19, scale = 4)
    val feeAmount: BigDecimal,

    @Column(nullable = false, precision = 19, scale = 4)
    val netAmount: BigDecimal,

    @Column(nullable = false, updatable = false)
    val generatedAt: Instant = Instant.now()
)
