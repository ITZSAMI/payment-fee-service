package com.sami.paymentservice.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class TransactionStatus {
    PENDING, COMPLETED, FAILED
}

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val merchantId: String,

    @Column(nullable = false, precision = 19, scale = 4)
    val amount: BigDecimal,

    @Column(nullable = false, length = 3)
    val currency: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TransactionStatus = TransactionStatus.PENDING,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
