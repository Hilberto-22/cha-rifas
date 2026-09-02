package br.com.charifa.repository;

import br.com.charifa.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findAllByOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    java.util.Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);
}
