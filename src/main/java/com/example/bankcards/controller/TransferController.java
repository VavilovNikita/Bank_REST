package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> transfer(@RequestBody TransferDto transferDto) {
        transferService.transfer(transferDto);
        return ResponseEntity.ok().build();
    }
}