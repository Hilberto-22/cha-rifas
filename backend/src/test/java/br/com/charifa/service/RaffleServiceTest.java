package br.com.charifa.service;

import br.com.charifa.api.ReservationRequest;
import br.com.charifa.domain.*;
import br.com.charifa.repository.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RaffleServiceTest {
    @Test
    void rejectsReservationWhenAnyLockedNumberIsUnavailable() throws Exception {
        RaffleRepository raffles = mock(RaffleRepository.class);
        RaffleNumberRepository numbers = mock(RaffleNumberRepository.class);
        ReservationRepository reservations = mock(ReservationRepository.class);
        Raffle raffle = entity(Raffle.class);
        set(raffle, "id", UUID.randomUUID()); set(raffle, "active", true);
        RaffleNumber unavailable = entity(RaffleNumber.class);
        set(unavailable, "number", 12); set(unavailable, "status", RaffleNumberStatus.CONFIRMED);
        when(raffles.findById(raffle.getId())).thenReturn(Optional.of(raffle));
        when(numbers.findAllForUpdate(raffle.getId(), List.of(12))).thenReturn(List.of(unavailable));
        RaffleService service = new RaffleService(raffles, numbers, reservations, 15, "5599999999999");

        assertThrows(NumberUnavailableException.class, () -> service.reserve(raffle.getId(),
                new ReservationRequest("Pessoa Teste", "00000000000", PaymentMethod.PIX, List.of(12))));
        verify(reservations, never()).save(any());
    }

    @Test
    void effectiveStatusMakesExpiredReservationAvailable() throws Exception {
        RaffleNumber number = entity(RaffleNumber.class);
        Reservation reservation = entity(Reservation.class);
        set(number, "status", RaffleNumberStatus.RESERVED); set(number, "reservation", reservation);
        set(reservation, "status", ReservationStatus.PENDING);
        set(reservation, "expiresAt", Instant.now().minusSeconds(1));
        assertTrue(number.isAvailable(Instant.now()));
    }

    private static <T> T entity(Class<T> type) throws Exception {
        var constructor = type.getDeclaredConstructor(); constructor.setAccessible(true); return constructor.newInstance();
    }
    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }
}
