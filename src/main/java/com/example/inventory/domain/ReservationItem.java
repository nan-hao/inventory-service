package com.example.inventory.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "reservation_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_reservation_product", columnNames = {"reservation_id", "product_code"})
)
@Data
@NoArgsConstructor
public class ReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "product_code", length = 50, nullable = false)
    private String productCode;

    @Column(nullable = false)
    private int qty;

    // Optimistic locking to guard concurrent modifications of a line item
    @Version
    private Long version;
}
