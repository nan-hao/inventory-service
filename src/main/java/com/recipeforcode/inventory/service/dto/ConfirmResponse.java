package com.recipeforcode.inventory.service.dto;

import com.recipeforcode.inventory.domain.ReservationStatus;

public record ConfirmResponse(String reservationId, ReservationStatus status) {}
