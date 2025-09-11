package com.recipeforcode.inventory.service.dto;

import java.time.Instant;

public record ReserveResponse(String reservationId, Instant expiresAt) {}
