package com.recipeforcode.inventory.controller;

import com.recipeforcode.inventory.config.GlobalExceptionHandler;
import com.recipeforcode.inventory.domain.ReservationStatus;
import com.recipeforcode.inventory.service.InventoryService;
import com.recipeforcode.inventory.service.dto.ConfirmResponse;
import com.recipeforcode.inventory.service.dto.ReserveResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventoryController.class)
@Import(GlobalExceptionHandler.class)
class InventoryControllerValidationTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    InventoryService service;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldReturnBadRequestByMissingMandatoryFields() throws Exception {
        String body = "{\n  \"items\": [],\n  \"ttlSec\": 0\n}";

        mvc.perform(post("/inventory/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void shouldReturnBadRequestForInvalidQty() throws Exception {
        String body = "{\n  \"reservationId\": \"RES-OK\",\n  \"items\": [{\"productCode\": \"P-1\", \"qty\": 0}],\n  \"ttlSec\": 60\n}";

        mvc.perform(post("/inventory/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void shouldReturnBadRequestOnMalformedJson() throws Exception {
        String body = "{ \"reservationId\": \"RES-OK\", \"items\": ["; // malformed JSON

        mvc.perform(post("/inventory/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReserveSuccessfully() throws Exception {
        var expiresAt = Instant.parse("2030-01-01T00:00:00Z");
        when(service.reserve(any())).thenReturn(new ReserveResponse("RES-OK", expiresAt));

        String body = "{\n  \"reservationId\": \"RES-OK\",\n  \"items\": [{\"productCode\": \"P-1\", \"qty\": 1}],\n  \"ttlSec\": 60\n}";

        mvc.perform(post("/inventory/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value("RES-OK"))
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    void shouldConfirmSuccessfully() throws Exception {
        when(service.confirm("RES-1")).thenReturn(new ConfirmResponse("RES-1", ReservationStatus.CONFIRMED));

        mvc.perform(post("/inventory/reservations/RES-1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value("RES-1"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldReturnNotFoundOnConfirmMissing() throws Exception {
        when(service.confirm("RES-MISSING")).thenThrow(new EntityNotFoundException("Reservation not found"));

        mvc.perform(post("/inventory/reservations/RES-MISSING/confirm"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnConflictOnConfirmAlreadyConfirmed() throws Exception {
        when(service.confirm("RES-2")).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Reservation already confirmed"));

        mvc.perform(post("/inventory/reservations/RES-2/confirm"))
                .andExpect(status().isConflict());
    }
}
