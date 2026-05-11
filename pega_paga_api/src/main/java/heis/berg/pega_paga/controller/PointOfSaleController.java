package heis.berg.pega_paga.controller;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.domain.entity.Invoice;
import heis.berg.pega_paga.dto.CreateInvoiceRequest;
import heis.berg.pega_paga.dto.InvoiceResponse;
import heis.berg.pega_paga.mapper.ApiMapper;
import heis.berg.pega_paga.service.PointOfSaleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pos")
public class PointOfSaleController {

    private final PointOfSaleService pointOfSaleService;

    public PointOfSaleController(PointOfSaleService pointOfSaleService) {
        this.pointOfSaleService = pointOfSaleService;
    }

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('POS_OPERATOR','SUPER_ADMIN')")
    public InvoiceResponse createInvoice(
            @AuthenticationPrincipal AppUser currentUser,
            @Valid @RequestBody CreateInvoiceRequest request
    ) {
        Invoice invoice = pointOfSaleService.createInvoice(currentUser, request);
        return ApiMapper.toInvoiceResponse(invoice);
    }

    @GetMapping("/invoices/{invoiceNumber}")
    @PreAuthorize("hasAnyRole('POS_OPERATOR','SUPER_ADMIN')")
    public InvoiceResponse getInvoice(@PathVariable String invoiceNumber) {
        return ApiMapper.toInvoiceResponse(pointOfSaleService.findInvoiceByNumber(invoiceNumber));
    }
}
