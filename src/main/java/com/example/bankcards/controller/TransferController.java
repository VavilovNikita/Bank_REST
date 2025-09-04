package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "Transfers", description = "API for fund transfers between cards")
@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }
    @Operation(
        summary = "Transfer funds",
        description = "Transfers funds between user's own cards. Both cards must be active and belong to the same user"
    )
    @ApiResponse(responseCode = "200", description = "Transfer successfully completed")
    @ApiResponse(responseCode = "400", description = "Invalid transfer data or insufficient funds")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Card not found")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> transfer(@RequestBody TransferDto transferDto) {
        transferService.transfer(transferDto);
        return ResponseEntity.ok().build();
    }
}