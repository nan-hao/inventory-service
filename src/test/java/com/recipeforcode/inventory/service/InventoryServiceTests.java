package com.recipeforcode.inventory.service;

import com.recipeforcode.inventory.InventoryServiceApplication;
import com.recipeforcode.inventory.domain.Reservation;
import com.recipeforcode.inventory.domain.ReservationStatus;
import com.recipeforcode.inventory.repository.ReservationRepository;
import com.recipeforcode.inventory.service.dto.ReserveRequest;
import com.recipeforcode.inventory.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = InventoryServiceApplication.class)
class InventoryServiceTests extends AbstractIntegrationTest {

    @Autowired
    InventoryService service;

    @Autowired
    ReservationRepository reservations;

    @Test
    void shouldCreateReservationAndMergeDuplicateItems() {
        var req = new ReserveRequest(
                "RES-T1",
                List.of(new ReserveRequest.Item("P-1", 1),
                        new ReserveRequest.Item("P-1", 2),
                        new ReserveRequest.Item("P-2", 3)),
                300
        );

        var res = service.reserve(req);
        assertThat(res.reservationId()).isEqualTo("RES-T1");

        Reservation r = reservations.findWithItemsById("RES-T1").orElseThrow();
        assertThat(r.getItems()).hasSize(2);
        assertThat(r.getItems()).anySatisfy(it -> {
            if (it.getProductCode().equals("P-1")) {
                assertThat(it.getQty()).isEqualTo(3);
            }
        });
    }

    @Test
    void shouldNotExtendTtlOnIdempotentReserve() {
        var first = service.reserve(new ReserveRequest(
                "RES-T2",
                List.of(new ReserveRequest.Item("P-1", 1)),
                100
        ));
        var second = service.reserve(new ReserveRequest(
                "RES-T2",
                List.of(new ReserveRequest.Item("P-1", 1)),
                1000
        ));

        assertThat(second.expiresAt()).isEqualTo(first.expiresAt());
    }

    @Test
    void shouldConfirmReservationAndReturnConflictOnSecondConfirm() {
        var resId = "RES-T3";
        service.reserve(new ReserveRequest(resId, List.of(new ReserveRequest.Item("P-1", 1)), 100));

        var confirmed = service.confirm(resId);
        assertThat(confirmed.status()).isEqualTo(ReservationStatus.CONFIRMED);

        assertThatThrownBy(() -> service.confirm(resId))
                .isInstanceOf(ResponseStatusException.class);
    }
}
