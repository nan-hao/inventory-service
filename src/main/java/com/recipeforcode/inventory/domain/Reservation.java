package com.recipeforcode.inventory.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/*
 * Rich domain model aggregate root of reservation, encapsulates item operations and invariants.
 */
@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
public class Reservation {
    @Id
    @Column(name = "reservation_id", length = 50, nullable = false)
    private String reservationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Version
    private Long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private List<ReservationItem> items = new ArrayList<>();

    /**
     * Factory for creating a new pending reservation with an expiry.
     */
    public static Reservation createPending(String reservationId, Instant expiresAt) {
        Reservation r = new Reservation();
        r.setReservationId(reservationId);
        r.setStatus(ReservationStatus.PENDING);

        long nanos = expiresAt.getNano();
        long normalizedNanos = nanos - (nanos % 1_000); // drop sub-microsecond precision
        r.setExpiresAt(Instant.ofEpochSecond(expiresAt.getEpochSecond(), normalizedNanos));
        return r;
    }

    /**
     * Adds an item. In case the product already exists in the reservation, merge by summing quantities.
     */
    public void addItem(String productCode, int qty) {
        for (ReservationItem existing : items) {
            if (existing.getProductCode().equals(productCode)) {
                existing.setQty(existing.getQty() + qty);
                return;
            }
        }
        ReservationItem it = new ReservationItem();
        it.setProductCode(productCode);
        it.setQty(qty);
        items.add(it);
    }
}
