package br.com.charifa.api;

import br.com.charifa.service.RaffleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/raffles")
public class RaffleController {
    private final RaffleService service;
    public RaffleController(RaffleService service) { this.service = service; }

    @GetMapping("/active")
    public RaffleResponse active() { return service.getActiveRaffle(); }

    @GetMapping("/{raffleId}/numbers")
    public List<RaffleNumberResponse> numbers(@PathVariable UUID raffleId) {
        return service.getNumbers(raffleId);
    }

    @PostMapping("/{raffleId}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(@PathVariable UUID raffleId, @Valid @RequestBody ReservationRequest request) {
        return service.reserve(raffleId, request);
    }

    @PatchMapping("/{raffleId}/reservations/{reservationId}/payment-reported")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reportPayment(@PathVariable UUID raffleId, @PathVariable UUID reservationId) {
        service.reportPayment(raffleId, reservationId);
    }
}
