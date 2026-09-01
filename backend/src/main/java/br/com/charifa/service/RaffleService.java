package br.com.charifa.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.charifa.api.RaffleNumberResponse;
import br.com.charifa.api.RaffleResponse;
import br.com.charifa.api.ReservationRequest;
import br.com.charifa.api.ReservationResponse;
import br.com.charifa.domain.InvalidReservationRequestEException;
import br.com.charifa.domain.Raffle;
import br.com.charifa.domain.RaffleNumber;
import br.com.charifa.domain.Reservation;
import br.com.charifa.repository.RaffleNumberRepository;
import br.com.charifa.repository.RaffleRepository;
import br.com.charifa.repository.ReservationRepository;

@Service
public class RaffleService {
    private final RaffleRepository raffleRepository;
    private final RaffleNumberRepository numberRepository;
    private final ReservationRepository reservationRepository;
    private final Duration reservationDuration;
    private final String whatsappNumber;

    public RaffleService(RaffleRepository raffleRepository, RaffleNumberRepository numberRepository,
                         ReservationRepository reservationRepository,
                         @Value("${app.reservation-minutes}") long reservationMinutes,
                         @Value("${app.whatsapp-number}") String whatsappNumber) {
        this.raffleRepository = raffleRepository;
        this.numberRepository = numberRepository;
        this.reservationRepository = reservationRepository;
        this.reservationDuration = Duration.ofMinutes(reservationMinutes);
        this.whatsappNumber = whatsappNumber;
    }

    @Transactional(readOnly = true)
    public RaffleResponse getActiveRaffle() {
        return RaffleResponse.from(requireActiveRaffle(), whatsappNumber);
    }

    @Transactional(readOnly = true)
    public List<RaffleNumberResponse> getNumbers(UUID raffleId) {
        Instant now = Instant.now();
        return numberRepository.findAllWithReservation(raffleId).stream()
                .map(number -> RaffleNumberResponse.from(number, now))
                .toList();
    }

    @Transactional
    public ReservationResponse reserve(UUID raffleId, ReservationRequest request) {

        if (request.numbers() == null || request.numbers().isEmpty()) {
            throw new InvalidReservationRequestEException("Numbers list cannot be empty");
        }

        Raffle raffle = raffleRepository.findById(raffleId)
                .filter(Raffle::isActive)
                .orElseThrow(RaffleNotFoundException::new);
        
        List<Integer> numerosSolicitados = request.numbers()
            .stream()
            .distinct()
            .sorted()
            .toList();

        if (numerosSolicitados.size() != request.numbers().size()) {
            throw new IllegalArgumentException("Números duplicados não são permitidos.");
        }

        // O SELECT FOR UPDATE mantém as linhas bloqueadas até o commit da transação.
        List<RaffleNumber> numerosBloqueados = numberRepository.findAllForUpdate(raffleId, numerosSolicitados);
        if (numerosBloqueados.size() != numerosSolicitados.size()) {
            List<Integer> foundNumbers = numerosBloqueados.stream().map(RaffleNumber::getNumber).toList();
            List<Integer> missingNumbers = numerosSolicitados.stream()
                .filter(num -> !foundNumbers.contains(num))
                .toList();
            throw new NumberUnavailableException(missingNumbers);
        }

        Instant now = Instant.now();
        List<Integer> unavailable = numerosBloqueados.stream()
                .filter(number -> !number.isAvailable(now))
                .map(RaffleNumber::getNumber)
                .toList();

        if (!unavailable.isEmpty()) throw new NumberUnavailableException(unavailable);

        numerosBloqueados.forEach(number -> number.liberarNumeroExpirado(now));

        Reservation reservation = reservationRepository.save(new Reservation(
                raffle, 
                request.name().trim(), 
                request.phone().trim(), 
                request.paymentMethod(),
                now.plus(reservationDuration)));

        numerosBloqueados.forEach(number -> {
            number.liberarNumeroExpirado(now);
            number.reserve(reservation);
        });

        numberRepository.saveAll(numerosBloqueados);

        return ReservationResponse.from(reservation, numerosSolicitados);
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void releaseExpiredReservations() {
        Instant now = Instant.now();
        numberRepository.findReservedForUpdate().forEach(number -> number.liberarNumeroExpirado(now));
    }

    private Raffle requireActiveRaffle() {
        return raffleRepository.findFirstByActiveTrue()
                .orElseThrow(RaffleNotFoundException::new);
    }
}
