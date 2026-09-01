package br.com.charifa.api.adminstracao;

import br.com.charifa.domain.AdminReservationResponse;
import br.com.charifa.service.AdminService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service) { this.service = service; }
    @GetMapping("/reservations")
    public List<AdminReservationResponse> reservations() { return service.listReservations(); }

    @PatchMapping("/reservations/{reservationId}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirm(@PathVariable UUID reservationId) { service.confirm(reservationId); }

    @PatchMapping("/reservations/{reservationId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID reservationId) { service.cancel(reservationId); }
}
