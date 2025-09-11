package com.example.inventory.repository;

import com.example.inventory.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, String> {

    @Query("select r from Reservation r left join fetch r.items where r.reservationId = :id")
    Optional<Reservation> findWithItemsById(@Param("id") String id);
}
