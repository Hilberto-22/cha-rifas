package br.com.charifa.service;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.charifa.api.ReservationRequest;
import br.com.charifa.domain.PaymentMethod;
import br.com.charifa.domain.Raffle;
import br.com.charifa.domain.RaffleNumber;
import br.com.charifa.domain.RaffleNumberStatus;
import br.com.charifa.domain.Reservation;
import br.com.charifa.domain.ReservationStatus;
import br.com.charifa.repository.RaffleNumberRepository;
import br.com.charifa.repository.RaffleRepository;
import br.com.charifa.repository.ReservationRepository;

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
        RaffleService service = new RaffleService(raffles, numbers, reservations, 15, "5599999999999",
                mock(PixPayloadService.class));

        var exception = assertThrows(NumberUnavailableException.class, () -> service.reserve(raffle.getId(),
                new ReservationRequest("Pessoa Teste", "00000000000", PaymentMethod.PIX, List.of(12))));
        assertNotNull(exception);
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
