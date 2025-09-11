package com.example.inventory.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Command object to create an inventory reservation.
 *
 * - reservationId: client-supplied idempotency key for the reservation aggregate.
 * - items: product lines identified by productCode with requested quantity.
 * - ttlSec: time-to-live window (in seconds).
 */
public record ReserveRequest(
        @NotBlank String reservationId,
        @NotEmpty List<Item> items,
        @Min(1) long ttlSec
) {
    /** Single product line within the reservation request. */
    public record Item(@NotBlank String productCode, @Min(1) int qty) {}
}
