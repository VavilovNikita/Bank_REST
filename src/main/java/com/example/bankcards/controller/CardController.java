package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreationDto;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardUpdateDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@Tag(name = "Cards", description = "API for managing bank cards")
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }
    @Operation(
        summary = "Get paginated cards",
        description = "Retrieves paginated list of cards. Users see only their cards, admins see all cards"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved cards")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<CardDto>> getCards(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) String status) {
        return ResponseEntity.ok(cardService.getCards(page, size, status));
    }
    @Operation(
        summary = "Create new card",
        description = "Creates a new bank card. Accessible only to ADMIN users"
    )
    @ApiResponse(responseCode = "201", description = "Card successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid card data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@RequestBody CardCreationDto creationDto) {
        return ResponseEntity.created(null).body(cardService.createCard(creationDto));
    }
    @Operation(
        summary = "Get card by ID",
        description = "Retrieves specific card by ID. Users can only access their own cards"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved card")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Card not found")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }
    @Operation(
        summary = "Update card",
        description = "Updates card information. Accessible only to ADMIN users"
    )
    @ApiResponse(responseCode = "200", description = "Card successfully updated")
    @ApiResponse(responseCode = "400", description = "Invalid update data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Card not found")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> updateCard(@PathVariable Long id, @RequestBody CardUpdateDto updateDto) {
        return ResponseEntity.ok(cardService.updateCard(id, updateDto));
    }
    @Operation(
        summary = "Delete card",
        description = "Deletes a card. Accessible only to ADMIN users"
    )
    @ApiResponse(responseCode = "204", description = "Card successfully deleted")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Card not found")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(
        summary = "Block card",
        description = "Blocks a card. Users can only block their own cards"
    )
    @ApiResponse(responseCode = "200", description = "Card successfully blocked")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Card not found")
    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> blockCard(@PathVariable Long id) {
        cardService.blockCard(id);
        return ResponseEntity.ok().build();
    }
    @Operation(
        summary = "Activate card",
        description = "Activates a card. Accessible only to ADMIN users"
    )
    @ApiResponse(responseCode = "200", description = "Card successfully activated")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Card not found")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateCard(@PathVariable Long id) {
        cardService.activateCard(id);
        return ResponseEntity.ok().build();
    }
}