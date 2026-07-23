package com.sami.paymentservice.controller

import com.sami.paymentservice.dto.InvoiceResponse
import com.sami.paymentservice.service.InvoiceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
class InvoiceController(
    private val invoiceService: InvoiceService
) {

    @PostMapping("/transactions/{id}/invoice")
    fun generateInvoice(@PathVariable id: UUID): ResponseEntity<InvoiceResponse> {
        val invoice = invoiceService.generateInvoice(id)
        return ResponseEntity.status(HttpStatus.CREATED).body(InvoiceResponse.from(invoice))
    }

    @GetMapping("/invoices/{id}")
    fun getInvoice(@PathVariable id: UUID): InvoiceResponse =
        InvoiceResponse.from(invoiceService.getInvoice(id))
}
