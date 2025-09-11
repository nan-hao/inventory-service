package com.example.inventory.controller;

import com.example.inventory.service.InventoryService;
import com.example.inventory.service.dto.ConfirmResponse;
import com.example.inventory.service.dto.ReserveRequest;
import com.example.inventory.service.dto.ReserveResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @PostMapping("/reservations")
    public ResponseEntity<ReserveResponse> reserve(@Valid @RequestBody ReserveRequest req) {
        return ResponseEntity.ok(service.reserve(req));
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<ConfirmResponse> confirm(@PathVariable String reservationId) {
        return ResponseEntity.ok(service.confirm(reservationId));
    }
}