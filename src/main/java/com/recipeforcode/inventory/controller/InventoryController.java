package com.recipeforcode.inventory.controller;

import com.recipeforcode.inventory.service.InventoryService;
import com.recipeforcode.inventory.service.dto.ConfirmResponse;
import com.recipeforcode.inventory.service.dto.ReserveRequest;
import com.recipeforcode.inventory.service.dto.ReserveResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @Operation(
            summary = "Create inventory reservation",
            description = "Idempotently creates a reservation with items; duplicate product codes are merged."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation created or already existed"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/reservations")
    public ResponseEntity<ReserveResponse> reserve(
            @Valid @RequestBody ReserveRequest req) {
        return ResponseEntity.ok(service.reserve(req));
    }

    @Operation(
            summary = "Confirm reservation",
            description = "Confirms a reservation. Returns 404 if not found, 409 if already confirmed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Confirmed"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "409", description = "Already confirmed")
    })
    @PostMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<ConfirmResponse> confirm(@PathVariable @Size(max = 50) String reservationId) {
        return ResponseEntity.ok(service.confirm(reservationId));
    }
}
