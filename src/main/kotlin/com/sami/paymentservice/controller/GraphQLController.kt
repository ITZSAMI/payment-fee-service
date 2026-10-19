package com.sami.paymentservice.controller

import com.sami.paymentservice.dto.InvoiceResponse
import com.sami.paymentservice.dto.TransactionResponse
import com.sami.paymentservice.service.InvoiceService
import com.sami.paymentservice.service.TransactionService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.util.UUID

/**
 * GraphQL queries. In GraphQL, "not found" is expressed as a null result
 * (the schema fields are nullable), not as an error, so these use the
 * find* service methods instead of the throwing get* ones.
 */
@Controller
class GraphQLController(
    private val invoiceService: InvoiceService,
    private val transactionService: TransactionService
) {

    @QueryMapping
    fun invoice(@Argument id: UUID): InvoiceResponse? =
        invoiceService.findInvoice(id)?.let { InvoiceResponse.from(it) }

    @QueryMapping
    fun transaction(@Argument id: UUID): TransactionResponse? =
        transactionService.findTransaction(id)?.let { TransactionResponse.from(it) }
}
