package br.com.charifa.repository;

import br.com.charifa.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findAllByOrderByCreatedAtDesc();
}
