package com.sami.paymentservice.repository

import com.sami.paymentservice.model.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TransactionRepository : JpaRepository<Transaction, UUID>
