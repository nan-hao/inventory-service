package com.example.inventory.service.dto;

import com.example.inventory.domain.ReservationStatus;

public record ConfirmResponse(String reservationId, ReservationStatus status) {}
