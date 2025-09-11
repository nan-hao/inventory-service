package com.recipeforcode.inventory.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.recipeforcode.inventory.repository.ReservationRepository;
import com.recipeforcode.inventory.service.InventoryService;
import com.recipeforcode.inventory.service.dto.ReserveRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = TimingAspectTest.Config.class)
class TimingAspectTest {
    @Autowired
    InventoryService service;

    @Autowired
    ReservationRepository reservations;

    @Test
    void shouldLogTimingAroundServiceMethod_andServiceIsProxied() {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        Logger aspectLogger = (Logger) LoggerFactory.getLogger(TimingAspect.class);
        aspectLogger.addAppender(appender);

        // Given
        Mockito.when(reservations.findById("RES-LOG")).thenReturn(java.util.Optional.empty());
        Mockito.when(reservations.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        // When: call a method matched by pointcut (within service package)
        service.reserve(new ReserveRequest("RES-LOG",
                List.of(new ReserveRequest.Item("P-1", 1)), 60));

        // Then: bean is proxied (AOP applied)
        assertThat(AopUtils.isAopProxy(service)).isTrue();

        // Then: aspect logged timing for the method
        boolean hasTimingLog = appender.list.stream()
                .anyMatch(e -> e.getLevel().equals(Level.INFO)
                        && e.getFormattedMessage().contains("took")
                        && e.getFormattedMessage().contains("InventoryService.reserve"));
        assertThat(hasTimingLog).isTrue();

        aspectLogger.detachAppender(appender);
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class Config {
        @Bean
        TimingAspect timingAspect() { return new TimingAspect(); }

        @Bean
        ReservationRepository reservationRepository() { return Mockito.mock(ReservationRepository.class); }

        @Bean
        InventoryService inventoryService(ReservationRepository repo) { return new InventoryService(repo); }
    }
}
