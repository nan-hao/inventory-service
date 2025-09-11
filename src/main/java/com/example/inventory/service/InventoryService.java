package com.example.inventory.service;

import com.example.inventory.domain.Reservation;
import com.example.inventory.domain.ReservationStatus;
import com.example.inventory.repository.ReservationRepository;
import com.example.inventory.service.dto.ConfirmResponse;
import com.example.inventory.service.dto.ReserveRequest;
import com.example.inventory.service.dto.ReserveResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public ReserveResponse reserve(ReserveRequest req) {
        return reservationRepository.findById(req.reservationId())
                .map(r -> new ReserveResponse(r.getReservationId(), r.getExpiresAt()))
                .orElseGet(() -> {
                    Reservation r = Reservation.createPending(
                            req.reservationId(),
                            Instant.now().plusSeconds(req.ttlSec())
                    );
                    req.items().forEach(it -> r.addItem(it.productCode(), it.qty()));
                    try {
                        reservationRepository.save(r);
                        return new ReserveResponse(r.getReservationId(), r.getExpiresAt());
                    } catch (DataIntegrityViolationException e) {
                        // Insert race: another thread created the same reservation concurrently.
                        return reservationRepository.findById(req.reservationId())
                                .map(existing -> new ReserveResponse(existing.getReservationId(), existing.getExpiresAt()))
                                .orElseThrow(() -> e);
                    }
                });
    }

    @Transactional
    public ConfirmResponse confirm(String reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found: " + reservationId));
        if (r.getStatus() == ReservationStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reservation already confirmed");
        }
        r.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(r);
        return new ConfirmResponse(reservationId, ReservationStatus.CONFIRMED);
    }
}
